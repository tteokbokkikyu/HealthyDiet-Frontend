package com.example.healthydiet.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebSocketManager {
    //private static final String WS_URL = "ws://47.110.58.252:8080/hd/websocket";
    private static final String WS_URL = "ws://10.0.2.2:8080/hd/websocket";
    private static WebSocketManager instance;
    private WebSocketClient webSocketClient;
    private Handler handler;
    private boolean isConnecting = false;
    private static final int RECONNECT_DELAY = 3000;
    
    // 用于存储不同类型消息的回调
    private Map<String, WebSocketCallback> callbackMap = new HashMap<>();
    
    // 私有构造函数
    private WebSocketManager() {
        handler = new Handler(Looper.getMainLooper());
        initWebSocket();
    }
    
    // 获取单例实例
    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }
    
    private void initWebSocket() {
        if (isConnecting) {
            Log.d("WebSocket", "Connection already in progress");
            return;
        }
        
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.d("WebSocket", "WebSocket already connected");
            return;
        }
        
        try {
            isConnecting = true;
            Log.d("WebSocket", "Initializing new WebSocket connection");
            URI uri = URI.create(WS_URL);
            
            // 如果存在旧的连接，先关闭
            if (webSocketClient != null) {
                try {
                    webSocketClient.close();
                } catch (Exception e) {
                    Log.e("WebSocket", "Error closing existing connection: " + e.getMessage());
                }
            }
            
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "Connected successfully");
                    isConnecting = false;
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", "Raw message received: " + message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WebSocket", "Connection closed: " + reason + " (code: " + code + ")");
                    isConnecting = false;
                }

                @Override
                public void onError(Exception ex) {
                    Log.e("WebSocket", "WebSocket error: " + ex.getMessage());
                    isConnecting = false;
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Error initializing WebSocket: " + e.getMessage());
            isConnecting = false;
            e.printStackTrace();
        }
    }

    // 处理接收到的消息
    private void handleMessage(String message) {
        Log.d("WebSocket", "Received message: " + message);
        try {

            JSONObject get=new JSONObject(message);
            int code=get.getInt("code");
            WebSocketCode wscode= WebSocketCode.class.getEnumConstants()[code];
            switch (wscode) {
                case REGISTER_SUCCESS:
                case REGISTER_FAIL: {
                    String type = WebSocketMessageType.REGISTER;
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for register response");
                        handler.post(() -> callback.onMessage(message));
                    }
                    return;
                }
                case LOGIN_SUCCESS:
                 {
                     String msg=get.getString("data");
                    String type = WebSocketMessageType.LOGIN;
                    //  Log.d("WebSocket", "Determined message type: " + type);
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for type: " + type);
                        handler.post(() -> callback.onMessage(msg));
                    } else {
                        Log.d("WebSocket", "No callback found for type: " + type);
                    }
                    return;
                }

                case FOOD_LIST_SUCCESS: {
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as food list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.FOOD_LIST);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case FOOD_RECORD_ADD_SUCCESS:
                case FOOD_RECORD_ADD_FAIL: {
                    String type = WebSocketMessageType.FOOD_RECORD_ADD;
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for add food record response");
                        Log.d("WebSocket", message);
                        handler.post(() -> callback.onMessage(message));
                    }
                    return;
                }
                case FOOD_RECORD_GET_SUCCESS: {
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as food record list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.FOOD_RECORD_GET);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case FOOD_ITEM_GET_SUCCESS: {
                    String type = WebSocketMessageType.FOOD_ITEM_GET;
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for add food record response");
                        Log.d("WebSocket", message);
                        handler.post(() -> callback.onMessage(message));
                    }
                    return;
                }

                case EXERCISE_RECORD_GET_SUCCESS: {
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as exercise record list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.EXERCISE_RECORD_GET);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case EXERCISE_LIST_SUCCESS: {
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as exercise list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.EXERCISE_LIST);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case UPDATE_USER_SUCCESS:
                case UPDATE_USER_FAIL: {
                    String type = WebSocketMessageType.UPDATE_USER;
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for add food record response");
                        Log.d("WebSocket", message);
                        handler.post(() -> callback.onMessage(message));
                    }
                    return;
                }
                case FOOD_IDENTIFY_SUCCESS:{
                    String type = WebSocketMessageType.FOOD_IDENTIFY;
                    WebSocketCallback callback = callbackMap.get(type);
                    if (callback != null) {
                        Log.d("WebSocket", "Found callback for add food record response");
                        Log.d("WebSocket", message);
                        handler.post(() -> callback.onMessage(message));
                    }
                    return;
                }

                case POST_GET_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as post list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_POST);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case POST_GET_FAIL:{
                    Log.d("WebSocket", "获取帖子失败");
                }
                case POST_CREATE_SUCCESS:{
                    String msg = get.getString("message");
                    Log.d("WebSocket", "Received create post message:"+msg);
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.ADD_POST);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case COMMENT_CREATE_SUCCESS:{
                    String msg = get.getString("message");
                    Log.d("WebSocket", "Received create comment message:"+msg);
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.ADD_COMMENT);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case COMMENT_GET_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as comment list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_POSTCOMMENTS);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                //管理员...
                case GET_ALL_USERS_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as user list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_ALL_USERS);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case BLOCK_USER_SUCCESS:{
                    String msg = get.getString("message");
                    Log.d("WebSocket", "Received block user message:"+msg);
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.BLOCK_USER);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case UNBLOCK_USER_SUCCESS:{
                    String msg = get.getString("message");
                    Log.d("WebSocket", "Received unblock user message:"+msg);
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.UNBLOCK_USER);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case POST_GET_ALL_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as post list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_ALL_POSTS);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case COMMENT_GET_ALL_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as all comment list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_ALL_COMMENTS);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }

                case NOTIFICATION_GET_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as notification list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.GET_USER_NOTIFICATION);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case WEIGHT_GET_SUCCESS:{
                    String msg = get.getString("data");
                    Log.d("WebSocket", "Received array message, treating as comment list");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.WEIGHT_RECORD_GET);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
//                case LLM_QUERY_SUCCESS:{
//                    String msg = get.getString("data");
//                    Log.d("WebSocket", "Received llm message, treating as llm response");
//                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.ASK_LLM);
//                    if (callback != null) {
//                        handler.post(() -> callback.onMessage(msg));
//                    }
//                    return;
//                }
                case LLM_STREAM_CHUNK:{
                    String msg = get.getString("stream_chunk");
                    Log.d("WebSocket", "Received llm chunk, treating as llm response");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.ASK_LLM);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                case LLM_STREAM_COMPLETE: {
                    Log.d("WebSocket", "Stream ended");
                    // 可以在这里添加"思考中..."消失的逻辑
                    break;
                }
                case LLM_HISTORY_CLEARED:{
                    String msg = get.getString("message");
                    Log.d("WebSocket", "Received llm message, treating as llm response");
                    WebSocketCallback callback = callbackMap.get(WebSocketMessageType.CLEAR_LLM);
                    if (callback != null) {
                        handler.post(() -> callback.onMessage(msg));
                    }
                    return;
                }
                    default:
                        break;




            }
        } catch (Exception e) {
            Log.e("WebSocket", "Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // 发送消息的方法
    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.d("WebSocket", "Sending message: " + message);
            webSocketClient.send(message);
        } else {
            Log.e("WebSocket", "WebSocket is not connected");
        }
    }

    // 注册回调
    public void registerCallback(String type, WebSocketCallback callback) {
        Log.d("WebSocket", "Registering callback for type: " + type);
        callbackMap.put(type, callback);
    }

    // 移除回调
    public void unregisterCallback(String type) {
        callbackMap.remove(type);
    }

    // 关闭连接
    public void closeConnection() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public void reconnect() {
        Log.d("WebSocket", "Forcing reconnection...");
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        initWebSocket();
    }

    // 添加连接状态的日志方法
    public void logConnectionStatus() {
        if (webSocketClient == null) {
            Log.d("WebSocket", "Connection status: No WebSocket instance");
        } else {
            Log.d("WebSocket", "Connection status: " + 
                (webSocketClient.isOpen() ? "Connected" : "Disconnected") +
                ", isConnecting: " + isConnecting);
        }
    }
} 