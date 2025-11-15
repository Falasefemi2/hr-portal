package com.hrservice.hrservice.controller;

import com.hrservice.hrservice.annotation.RequiresRole;
import com.hrservice.hrservice.dto.DepartmentDto;
import com.hrservice.hrservice.entity.Department;
import com.hrservice.hrservice.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @RequiresRole({"hr", "hod", "employee"})
    @Operation(summary = "Get all departments", description = "Retrieve a list of all departments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @GetMapping("/{id}")
    @RequiresRole({"hr", "hod", "employee"})
    @Operation(summary = "Get department by ID", description = "Retrieve a specific department by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department found"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Department> getDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequiresRole({"hr"})
    @Operation(summary = "Create department", description = "Create a new department (HR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only HR can create departments")
    })
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        Department department = departmentService.create(departmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @PutMapping("/{id}")
    @RequiresRole({"hr"})
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto) {
        Department department = departmentService.update(id, departmentDto);
        return ResponseEntity.ok(department);
    }

    @PutMapping("/{id}/assign-hod")
    @RequiresRole({"hr"})
    @Operation(summary = "Assign HOD to department", description = "Assign a Head of Department to a department (HR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "HOD assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only HR can assign HOD")
    })
    public ResponseEntity<Department> assignHod(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @RequestBody AssignHodRequest request) {
        departmentService.assignHod(id, request.getHodEmployeeId());
        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"hr"})
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    public static class AssignHodRequest {
        private String hodEmployeeId;
    }
}

