package net.kdt.pojavlaunch.vartox;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GravitWebSocketClient extends WebSocketClient {
    private static final String TAG = "GravitWS";
    private static GravitWebSocketClient instance;
    private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> pendingRequests = new ConcurrentHashMap<>();

    public GravitWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public static synchronized GravitWebSocketClient getInstance() {
        if (instance == null || instance.isClosed()) {
            try {
                instance = new GravitWebSocketClient(new URI("ws://haxzer.online:9274/api"));
                instance.connectBlocking(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "Connection failed", e);
            }
        }
        return instance;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "Connected to LaunchServer WebSocket");
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String requestUUID = json.optString("requestUUID", null);
            if (requestUUID != null && pendingRequests.containsKey(requestUUID)) {
                CompletableFuture<JSONObject> future = pendingRequests.remove(requestUUID);
                if (future != null) {
                    future.complete(json);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + message, e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.w(TAG, "WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "WebSocket error", ex);
    }

    public CompletableFuture<JSONObject> sendRequest(String type, JSONObject params) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        try {
            String uuid = UUID.randomUUID().toString();
            JSONObject request = new JSONObject();
            request.put("type", type);
            request.put("requestUUID", uuid);
            if (params != null) {
                for (java.util.Iterator<String> it = params.keys(); it.hasNext(); ) {
                    String key = it.next();
                    request.put(key, params.get(key));
                }
            }
            pendingRequests.put(uuid, future);
            send(request.toString());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
