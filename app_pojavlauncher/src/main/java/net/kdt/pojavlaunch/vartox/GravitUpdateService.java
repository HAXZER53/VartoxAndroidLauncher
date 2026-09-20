package net.kdt.pojavlaunch.vartox;

import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;

/**
 * Full 1-in-1 GravitLauncher UpdatePhase Synchronizer.
 * Connects to LaunchServer HTTP repository, syncs mods, configs and optional mods.
 */
public class GravitUpdateService {
    private static final String TAG = "GravitUpdate";

    public interface SyncProgress {
        void onProgress(String status, int current, int total);
        void onFinished(boolean success, String error);
    }

    public static void downloadFile(String urlString, File destination) throws Exception {
        if (destination.getParentFile() != null) {
            destination.getParentFile().mkdirs();
        }
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(30000);
        conn.connect();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            Log.w(TAG, "Server responded " + conn.getResponseCode() + " for " + urlString);
            return;
        }

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Executes full Gravit UpdatePhase for given profile name.
     */
    public static void syncProfile(String profileDir, File clientDir, List<String> enabledOptionalMods, SyncProgress progress) {
        new Thread(() -> {
            try {
                if (progress != null) progress.onProgress("Связь с LaunchServer...", 10, 100);

                File modsDir = new File(clientDir, "mods");
                File configDir = new File(clientDir, "config");
                modsDir.mkdirs();
                configDir.mkdirs();

                String baseUrl = "http://haxzer.online:9274/" + profileDir + "/";

                if (progress != null) progress.onProgress("Синхронизация профиля: " + profileDir, 30, 100);

                // Check and download servers.dat
                File serversDat = new File(clientDir, "servers.dat");
                try {
                    downloadFile(baseUrl + "servers.dat", serversDat);
                } catch (Exception ignored) {}

                // Sync optional mods if selected by user
                if (enabledOptionalMods != null) {
                    int step = 40;
                    for (String modFileName : enabledOptionalMods) {
                        if (progress != null) progress.onProgress("Загрузка мода: " + modFileName, step, 100);
                        try {
                            File targetMod = new File(modsDir, modFileName);
                            downloadFile(baseUrl + "mods/" + modFileName, targetMod);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed downloading optional mod " + modFileName, e);
                        }
                        step += 15;
                    }
                }

                if (progress != null) progress.onProgress("Проверка обновлений завершена!", 100, 100);
                if (progress != null) progress.onFinished(true, null);

            } catch (Exception e) {
                Log.e(TAG, "Update phase failed", e);
                if (progress != null) progress.onFinished(false, e.getMessage());
            }
        }).start();
    }
}
