package com.hrservice.hrservice.controller;

import com.hrservice.hrservice.annotation.RequiresRole;
import com.hrservice.hrservice.dto.LeaveTypeDto;
import com.hrservice.hrservice.entity.LeaveType;
import com.hrservice.hrservice.service.LeaveTypeService;
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
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
@Tag(name = "Leave Types", description = "Leave type management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    @RequiresRole({"hr", "hod", "employee"})
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeService.findAll());
    }

    @GetMapping("/{id}")
    @RequiresRole({"hr", "hod", "employee"})
    public ResponseEntity<LeaveType> getLeaveType(@PathVariable Long id) {
        return leaveTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequiresRole({"hr"})
    @Operation(summary = "Create leave type", description = "Create a new leave type (HR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leave type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only HR can create leave types")
    })
    public ResponseEntity<LeaveType> createLeaveType(@Valid @RequestBody LeaveTypeDto leaveTypeDto) {
        LeaveType leaveType = leaveTypeService.create(leaveTypeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveType);
    }

    @PutMapping("/{id}")
    @RequiresRole({"hr"})
    public ResponseEntity<LeaveType> updateLeaveType(@PathVariable Long id, @Valid @RequestBody LeaveTypeDto leaveTypeDto) {
        LeaveType leaveType = leaveTypeService.update(id, leaveTypeDto);
        return ResponseEntity.ok(leaveType);
    }

    @DeleteMapping("/{id}")
    @RequiresRole({"hr"})
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

