package com.example.healthydiet.entity;

public class ChatMessage {
    private String content;
    private boolean isUser;

    public ChatMessage(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }
    public void appendContent(String additionalContent) {
        this.content += additionalContent;
    }
    // 添加getter方法
    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}
