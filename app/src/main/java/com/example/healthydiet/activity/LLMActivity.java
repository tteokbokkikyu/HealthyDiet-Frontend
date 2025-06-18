package com.example.healthydiet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthydiet.R;
import com.example.healthydiet.adapter.ChatAdapter;
import com.example.healthydiet.entity.ChatMessage;
import com.example.healthydiet.entity.ExerciseItem;
import com.example.healthydiet.websocket.WebSocketCode;
import com.example.healthydiet.websocket.WebSocketManager;
import com.example.healthydiet.websocket.WebSocketMessageType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LLMActivity extends AppCompatActivity {
    private WebSocketManager webSocketManager;
    private EditText messageEditText;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llm);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            // 1. 调用WebSocket接口清除历史
            webSocketManager.sendMessage("clearLLMHistory");

            // 2. 注册回调处理清除结果
            webSocketManager.registerCallback(WebSocketMessageType.CLEAR_LLM, message -> {
                runOnUiThread(() -> {
                    // 3. 收到清除成功的响应后执行返回操作

                    Toast.makeText(this, "对话历史已清除", Toast.LENGTH_SHORT).show();
                    onBackPressed(); // 或者 finish();
                });
            });
        });

        // 初始化视图
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // 设置聊天列表
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String initialAImessage="你好！我是你的智能助手，有什么饮食、运动或健康方面的问题需要我为你解答吗？";
        addMessage(new ChatMessage(initialAImessage, false)); // AI消息

        // 初始化WebSocket
        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.logConnectionStatus();

        // 注册LLM回调
//        webSocketManager.registerCallback(WebSocketMessageType.ASK_LLM, message -> {
//            Log.d("LLM", "Received LLM response: " + message);
//            try {
//                    runOnUiThread(() -> {
//                        addMessage(new ChatMessage(message, false)); // AI消息
//                        // 自动滚动到底部
//                        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
//                    });
//
//            } catch (Exception e) {
//                Log.e("LLM", "Error processing message", e);
//                runOnUiThread(() ->
//                        Toast.makeText(this, "解析响应出错", Toast.LENGTH_SHORT).show()
//                );
//            }
//        });
        // 替换原来的 ASK_LLM 回调，新增 STREAM_CHUNK 回调
        webSocketManager.registerCallback(WebSocketMessageType.ASK_LLM, message -> {
            runOnUiThread(() -> {
                try {
                    // 获取当前最后一条消息（AI的回复）
                    if (!chatMessages.isEmpty()) {
                        ChatMessage lastMessage = chatMessages.get(chatMessages.size() - 1);

                        // 如果是用户消息，需要新建AI消息
                        if (lastMessage.isUser()) {
                            ChatMessage newAiMessage = new ChatMessage(message, false);
                            chatMessages.add(newAiMessage);
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        }
                        // 如果是AI消息，则追加内容
                        else {
                            lastMessage.appendContent(message);
                            chatAdapter.notifyItemChanged(chatMessages.size() - 1);
                        }
                    } else {
                        // 特殊情况：没有消息时直接添加
                        addMessage(new ChatMessage(message, false));
                    }

                    // 滚动到底部
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                } catch (Exception e) {
                    Log.e("LLM", "处理流式消息出错", e);
                }
            });
        });
        // 发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());

        // 输入框回车键发送
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // 连接检查
        if (!webSocketManager.isConnected()) {
            webSocketManager.reconnect();
            Toast.makeText(this, "正在连接服务器...", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 添加到本地聊天记录
        addMessage(new ChatMessage(message, true));
        messageEditText.setText("");

        // 发送到服务器
        try{
            String llmRequest = "askLLM:{" +
                    "\"data\": \"" + message+ "\"," + "}";
            Log.d("LLM", "Sending askLLM message: " + llmRequest);

            if (!webSocketManager.isConnected()) {
                Log.d("LLM", "WebSocket not connected, attempting to reconnect...");
                webSocketManager.reconnect();
            }
            webSocketManager.sendMessage(llmRequest);
            addMessage(new ChatMessage("", false));
        } catch (Exception e) {
            Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
            Log.e("LLM", "Send error", e);
        }
    }

    private void addMessage(ChatMessage message) {
        Log.d("ChatDebug", "添加消息: " + message.getContent() +
                " 类型: " + (message.isUser() ? "用户" : "AI"));
        chatMessages.add(message);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册回调避免内存泄漏
        webSocketManager.unregisterCallback(WebSocketMessageType.ASK_LLM);
    }

}
