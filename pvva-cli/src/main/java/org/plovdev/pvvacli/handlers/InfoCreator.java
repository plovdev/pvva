package org.plovdev.pvvacli.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.plovdev.pvva.models.PVVAHeader;
import org.plovdev.pvvacli.PvvaPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class InfoCreator {
    private static final Logger log = LoggerFactory.getLogger(InfoCreator.class);
    private static final HexFormat format = HexFormat.of();
    private static final Gson gson = new Gson();

    private InfoCreator() {
    }

    public static void createPluginInfo(String finalName, String url, PVVAHeader header) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dgst = digest.digest(PvvaPaths.allBytes(PvvaPaths.BUILDS_OUT.resolve(finalName)));

            String sign = format.formatHex(dgst);
            String pluginId = header.getPluginId();
            int buildId = header.getBuildId();

            String jsonString = writeJsonInfo(sign, url, pluginId, buildId);
            try (FileOutputStream stream = new FileOutputStream(PvvaPaths.BUILDS_OUT.resolve(pluginId + ".json").toFile())) {
                stream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("Error to write info to file: ", e);
            }
        } catch (Exception e) {
            log.error("Error to create info: ", e);
        }
    }

    private static String writeJsonInfo(String sign, String url, String pluginId, int buildId) {
        JsonObject info = new JsonObject();
        info.addProperty("pluginId", pluginId);
        info.addProperty("buildId", buildId);
        if (url != null) {
            info.addProperty("url", url);
        }
        info.addProperty("sign", sign);

        return gson.toJson(info);
    }
}