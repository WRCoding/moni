package com.longjunwang.moni.entity;

import lombok.Data;

@Data
public class RequestLog {
    private Integer id;
    private String method;
    private String path;
    private String query;
    private String headers;
    private String body;
    private String clientIp;
    private Integer status;
    private String createdAt;
}
