package net.kdt.pojavlaunch.vartox;

import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class VartoxConfig {
    public static final String TAG = "VartoxMC";
    public static final String LAUNCH_SERVER_HTTP = "http://haxzer.online:9274";
    public static final String LAUNCH_SERVER_WS = "ws://haxzer.online:9274/api";
    public static final String SKIN_API_URL = "http://vartox.online:3000/api/skin?username=%username%";
    public static final String DEFAULT_SERVER_IP = "vartox.online";
    public static final int DEFAULT_SERVER_PORT = 25565;

    public static boolean downloadServerFile(String remoteRelativePath, File destination) {
        String urlString = LAUNCH_SERVER_HTTP + "/" + remoteRelativePath.replace("\\", "/");
        HttpURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            if (destination.getParentFile() != null) {
                destination.getParentFile().mkdirs();
            }
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + connection.getResponseCode() + " for " + urlString);
                return false;
            }

            in = new BufferedInputStream(connection.getInputStream());
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed downloading " + urlString, e);
            return false;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ignored) {}
            if (connection != null) connection.disconnect();
        }
    }
}
