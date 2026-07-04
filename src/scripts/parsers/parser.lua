function parseVideos(htmlstr)
    local videos = {}
    local doc = html.parseHtml(htmlstr)
    local elements = doc.select(".dle-content .media-obj.widethumb")

    print("=== PARSER DEBUG ===")
    print("Elements found: " .. #elements)

    for i = 1, #elements do
        local block = elements[i]
        local link = block.selectFirst("a[href]")
        if not link then goto continue end

        local url = link.attr("abs:href")
        if not url or url == "" then goto continue end

        local titleTag = block.selectFirst("p.video-title")
        local title = titleTag and titleTag.text() or ""
        if title == "" then goto continue end

        local img = block.selectFirst("img.tumb-img")
        local picture = ""
        if img then
            picture = img.attr("src") or ""
            if picture == "" or picture:find("data:image") then
                picture = img.attr("data-original") or ""
            end
        end

        local durationSpan = block.selectFirst("span.vid-info.duration")
        local duration = durationSpan and durationSpan.text() or ""
        duration = duration:gsub("^%s+", ""):gsub("%s+$", "")

        local videoId = ""
        if url then
            videoId = string.match(url, "/video/([^/]+)%.html$") or ""
            if videoId == "" then
                videoId = string.match(url, "/([^/]+)$") or ""
            end
        end

        local views = 0
        local viewsSpan = block.selectFirst("span[itemprop=userInteractionCount]")
        if viewsSpan then
            local viewsText = viewsSpan.attr("content") or viewsSpan.text() or ""
            viewsText = viewsText:gsub("[^%d]", "")
            views = tonumber(viewsText) or 0
        end

        local rating = 0

        table.insert(videos, {
            id = videoId,
            title = title,
            url = url,
            picture = picture,
            duration = duration,
            views = views,
            rating = {
                rating = rating,
                ratingType = "PERCENTAGE"
            }
        })

        ::continue::
    end

    print("Videos count: " .. #videos)
    print("=== END DEBUG ===")
    return json.toJson(videos)
end

function parseCategories(htmlstr)
    local categories = {}
    local doc = html.parseHtml(htmlstr)
    local menu = doc.select("div.dropdown-container .dropdown-cat a.dropdown-cat-link")

    for i = 1, #menu do
        local link = menu[i]
        local name = link.text()
        local url = link.attr("abs:href")

        if name ~= "Все категории" and name ~= "" then
            table.insert(categories, {
                name = name,
                url = url
            })
        end
    end

    return json.toJson(categories)
end

function parseModels(htmlstr)
    local models = {}
    local doc = html.parseHtml(htmlstr)

    local content = doc.selectFirst("div.dle-content")
    if not content then
        return json.toJson(models)
    end
    local items = content.select("div.media-obj.cat")

    for i = 1, #items do
        local block = items[i]

        local link = block.selectFirst("a[href]")
        if not link then goto continue end

        local url = link.attr("abs:href")
        if not url or url == "" then goto continue end

        if not url:find("/models/") then goto continue end

        local titleSpan = block.selectFirst("span.cat-title")
        local name = titleSpan and titleSpan.text() or ""
        if name == "" then goto continue end

        local img = block.selectFirst("img")
        local avatar = ""
        if img then
            avatar = img.attr("src") or ""
            if avatar == "" or avatar:find("data:image") then
                avatar = img.attr("data-original") or ""
            end
        end

        local videoCount = 0
        local countSpan = block.selectFirst("span.count-videos")
        if countSpan then
            local countText = countSpan.text():gsub("[^%d]", "")
            videoCount = tonumber(countText) or 0
        end

        table.insert(models, {
            name = name,
            url = url,
            avatar = avatar,
            country = nil,
            videoCount = videoCount
        })

        ::continue::
    end

    return json.toJson(models)
end

function parseVideoPage(htmlstr)
    local doc = html.parseHtml(htmlstr)
    local video = {
        qualityMap = {},
        tagLinks = {},
        modelsLinks = {},
        categories = {}
    }

    local metaTags = doc.select("head meta[property]")
    for i = 1, #metaTags do
        local meta = metaTags[i]
        local prop = meta.attr("property")
        local content = meta.attr("content")

        if prop == "og:title" then
            video.title = content
        elseif prop == "og:url" then
            video.videoUri = content
            local lastSlash = string.match(content, "/([^/]+)%.html$")
            video.videoId = lastSlash or string.sub(content, 30)
        elseif prop == "og:duration" then
            local seconds = tonumber(content)
            if seconds then
                video.videoDuration = string.format("PT%sS", seconds)
            end
        elseif prop == "og:description" then
            video.description = content
        elseif prop == "og:image" then
            video.previewUrl = content
        end
    end

    local ratingScore = doc.selectFirst(".rate-count")
    if ratingScore then
        local ratingValue = string.match(ratingScore.text(), "(%d+)%%")
        if ratingValue then
            video.rating = {
                rating = tonumber(ratingValue),
                ratingType = "PERCENTAGE"
            }
        else
            video.rating = { rating = 0, ratingType = "PERCENTAGE" }
        end
    else
        video.rating = { rating = 0, ratingType = "PERCENTAGE" }
    end

    local viewsSpan = doc.selectFirst(".before-quater div[itemprop=userInteractionCount]")
    if viewsSpan then
        local viewsText = viewsSpan.text():gsub("[^%d]", "")
        if viewsText == "" or viewsText == "1" then
            video.views = 0
        else
            video.views = tonumber(viewsText) or 0
        end
    else
        video.views = 0
    end

    local categoriesDiv = doc.selectFirst(".additional-info__list .all_cats[href='/categories']")
    if categoriesDiv then
        local parentDiv = categoriesDiv:parent()
        if parentDiv then
            local catLinks = parentDiv.select("a.cat-link")
            for i = 1, #catLinks do
                local cat = catLinks[i]
                local catName = cat.text()
                local catUrl = cat.attr("abs:href")
                if catName ~= "" then
                    table.insert(video.categories, {
                        name = catName,
                        url = catUrl
                    })
                end
            end
        end
    end

    local modelsDiv = doc.selectFirst(".additional-info__list .all_cats[href='/aktrisy']")
    if modelsDiv then
        local parentDiv = modelsDiv:parent()
        if parentDiv then
            local modelLinks = parentDiv.select("a.cat-link[href^='/models/']")
            for i = 1, #modelLinks do
                local model = modelLinks[i]
                local modelName = model.text()
                local modelUrl = model.attr("abs:href")
                if modelName ~= "" then
                    video.modelsLinks[modelName] = modelUrl
                end
            end
        end
    end

    local tagsDiv = doc.selectFirst(".additional-info__list .all_cats[href='/tags']")
    if tagsDiv then
        local parentDiv = tagsDiv:parent()
        if parentDiv then
            local tagLinks = parentDiv.select("a.cat-link")
            for i = 1, #tagLinks do
                local tag = tagLinks[i]
                local tagName = tag.text():gsub("^#", "")
                local tagUrl = tag.attr("abs:href")
                if tagName ~= "" then
                    video.tagLinks[tagName] = tagUrl
                end
            end
        end
    end
    local qualityMeta = doc.selectFirst("meta[property='ya:ovs:quality']")
    if qualityMeta then
        local quality = qualityMeta.attr("content")
        if quality and quality ~= "" then
            video.qualityMap[quality] = ""
        end
    end
    local videoFile = doc.selectFirst("meta[property='og:video']")
    if videoFile then
        video.videoUri = videoFile.attr("content")
    end

    if not video.videoUri or video.videoUri == "" then
        local script = doc.selectFirst("script:contains(Playerjs)")
        if script then
            local scriptText = script.text()
            local file = string.match(scriptText, 'file:%s*"([^"]+)"')
            if file then
                video.videoUri = "https://sex.pornobolt.in" .. file
            end
        end
    end

    return json.toJson(video)
end