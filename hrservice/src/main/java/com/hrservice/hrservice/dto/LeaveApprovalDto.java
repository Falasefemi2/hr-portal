package com.hrservice.hrservice.dto;

import lombok.Data;

@Data
public class LeaveApprovalDto {
    private String action; // "approve" or "reject"
    private String rejectionReason;
}

