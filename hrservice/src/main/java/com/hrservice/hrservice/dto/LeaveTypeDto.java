package com.hrservice.hrservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveTypeDto {
    @NotBlank(message = "Leave type name is required")
    private String name;
    private String description;
    private Integer maxDaysPerYear;
    private Boolean requiresDocumentation = false;
}

