package com.hoang.monitoring.entity;

public enum CheckStatus {
    UP,       // Website hoạt động bình thường
    DOWN,     // Website không phản hồi hoặc lỗi
    UNKNOWN   // Chưa check lần nào
}
