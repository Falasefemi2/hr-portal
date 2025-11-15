package com.hrservice.hrservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDto {
    @NotBlank(message = "Department name is required")
    private String name;
    private String description;
    private String hodEmployeeId;
}

