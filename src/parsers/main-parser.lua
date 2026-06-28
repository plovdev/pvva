-- UTILS METHODS --
function parseViews(viewsStr)
    if not viewsStr or viewsStr == "" then
        return 0
    end
    local cleaned = string.gsub(viewsStr, ",", "")
    return tonumber(cleaned) or 0
end

function parseRating(ratingStr)
    if not ratingStr or ratingStr == "" then
        return 0
    end
    local num = string.gsub(ratingStr, "%%", "")
    return tonumber(num) or 0
end

-- CONTRACT METHODS --
function parseVideos(htmlstr)
    local videos = {}
    local doc = html.parseHtml(htmlstr)
    local elements = doc.select("li.video_block")

    for i = 1, #elements do
        local block = elements[i]

        local link = block.selectFirst("a.image")
        if not link then goto continue end

        local url = link.attr("abs:href")
        if not url then goto continue end

        local img = link.selectFirst("img")
        local picture = img and img.attr("src") or ""

        local title = block.selectFirst("p")
        local titleText = title and title.text() or ""
        if titleText == "" then goto continue end
        local videoId = block.id() or ""

        local durationSpan = block.selectFirst("span.duration")
        local duration = pvlib.parseDuration(durationSpan and durationSpan.text() or "")

        local viewsSpan = block.selectFirst("span.video_views")
        local views = parseViews(viewsSpan and viewsSpan.text() or "")

        local ratingSpan = block.selectFirst("span.mini-rating")
        local ratingValue = parseRating(ratingSpan and ratingSpan.text() or "")

        table.insert(videos, {
            id = videoId,
            title = titleText,
            url = url,
            picture = picture,
            duration = duration,
            views = views,
            rating = {
            rating = ratingValue,
            ratingType = "PERCENTAGE"
        }})

        ::continue::
    end

    return json.toJson(videos)
end

function parseCategories(htmlstr)
    local categories = {}
    local doc = html.parseHtml(htmlstr)

    local topMenu = doc.select("ul.top-menu li a")
    for i = 1, #topMenu do
        local link = topMenu[i]
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
    local items = doc.select("div.item_model")

    for i = 1, #items do
        local block = items[i]

        local link = block.selectFirst("a[href]")
        if not link then goto continue end

        local url = link.attr("abs:href")
        if not url then goto continue end

        local img = block.selectFirst("img")
        local avatar = img and img.attr("src") or ""

        local country = ""
        local flagSpan = block.selectFirst("span.flag")
        if flagSpan then
            local classAttr = flagSpan.attr("class")
            country = string.match(classAttr, "%-([^%-]+)$") or ""
        end

        local videoCountSpan = block.selectFirst("span.cnt_span")
        local videoCount = videoCountSpan and tonumber(videoCountSpan.text()) or 0

        local nameSpan = block.selectFirst("span.model_eng_name")
        if not nameSpan then
            nameSpan = block.selectFirst("span.model_rus_name")
        end
        local name = nameSpan and nameSpan.text() or ""

        if name ~= "" and url ~= "" then
            table.insert(models, {
                name = name,
                url = url,
                avatar = avatar,
                country = country,
                videoCount = videoCount
            })
        end

        ::continue::
    end
    return json.toJson(models)
end

function parseVideoPage(htmlstr)
    local doc = html.parseHtml(htmlstr)
    local video = {
        comments = {},
        qualityMap = {},
        timecodes = {},
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
    local lastSlash = string.match(content, "/([^/]+)$")
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

    local ratingScore = doc.selectFirst(".rating_score")
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

    local viewsSpan = doc.selectFirst("span[itemprop=interactionCount]")
    video.views = viewsSpan and tonumber(viewsSpan.text()) or 0

    local categoriesDiv = doc.selectFirst("div.video-categories")
    if categoriesDiv then
        local catLinks = categoriesDiv.select("a[href]")
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

    local modelsDiv = doc.selectFirst("div.video-models")
    if modelsDiv then
        local modelLinks = modelsDiv.select("a[href]")
        for i = 1, #modelLinks do
            local model = modelLinks[i]
            local modelName = model.text()
            local modelUrl = model.attr("abs:href")
            if modelName ~= "" then
                video.modelLinks[modelName] = modelUrl
            end
        end
    end

    local tagsDiv = doc.selectFirst("div.video-tags")
    if tagsDiv then
        local tagLinks = tagsDiv.select("a[href]")
        for i = 1, #tagLinks do
            local tag = tagLinks[i]
            local tagName = tag.text()
            local tagUrl = tag.attr("abs:href")
            if tagName ~= "" then
                video.tagLinks[tagName] = tagUrl
            end
        end
    end

    local timecodeDiv = doc.selectFirst("div.video-tags[data-nosnippet=true]")
    if timecodeDiv then
        local text = timecodeDiv.text()
        local pattern = "([^:]+)%s*%-%s*(%d+:%d+)"
        local pos = 1
        while true do
            local desc, timeStr = string.match(text, pattern, pos)
            if not desc then break end
                local parts = {}
                for part in string.gmatch(timeStr, "(%d+)") do
                    table.insert(parts, part)
                end
                local seconds = 0
                if #parts == 2 then
                    seconds = tonumber(parts[1]) * 60 + tonumber(parts[2])
                elseif #parts == 1 then
                    seconds = tonumber(parts[1])
                end
                if desc and seconds > 0 then
                    table.insert(video.timecodes, {
                    time = string.format("PT%sS", seconds),
                    text = string.gsub(desc, "^[%s;]+", "") .. " - " .. timeStr
                })
            end
            pos = string.find(text, timeStr, pos) + #timeStr
        end
    end

    local qualityDiv = doc.selectFirst("div.quality_chooser")
    if qualityDiv then
        local qualityLinks = qualityDiv.select("a.choose")
        for i = 1, #qualityLinks do
            local q = qualityLinks[i]
            local qName = q.text()
            local qUrl = q.attr("href")
            if qName ~= "" and qUrl ~= "" then
                video.qualityMap[qName] = qUrl
            end
        end
    end
    return json.toJson(video)
end