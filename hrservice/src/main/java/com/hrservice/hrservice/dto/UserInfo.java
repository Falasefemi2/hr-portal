package com.hrservice.hrservice.dto;

import lombok.Data;

@Data
public class UserInfo {
    private String id;
    private String employeeId;
    private String email;
    private String role;
    private Integer departmentId;
    private Boolean isActive;
}

