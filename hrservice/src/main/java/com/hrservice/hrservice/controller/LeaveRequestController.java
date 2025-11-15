package com.hrservice.hrservice.controller;

import com.hrservice.hrservice.annotation.RequiresRole;
import com.hrservice.hrservice.dto.LeaveApprovalDto;
import com.hrservice.hrservice.dto.LeaveRequestDto;
import com.hrservice.hrservice.entity.LeaveRequest;
import com.hrservice.hrservice.service.LeaveRequestService;
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
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Leave request management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    @RequiresRole({"hr", "hod"})
    public ResponseEntity<List<LeaveRequest>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.findAll());
    }

    @GetMapping("/my-leaves")
    @RequiresRole({"hr", "hod", "employee"})
    public ResponseEntity<List<LeaveRequest>> getMyLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.findMyLeaveRequests());
    }

    @GetMapping("/pending")
    @RequiresRole({"hod"})
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.findPendingForHod());
    }

    @GetMapping("/{id}")
    @RequiresRole({"hr", "hod", "employee"})
    public ResponseEntity<LeaveRequest> getLeaveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.findById(id));
    }

    @PostMapping
    @RequiresRole({"employee"})
    @Operation(summary = "Create leave request", description = "Create a new leave request (Employee only). Prevents overlapping leaves in the same department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leave request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or overlapping leave exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only employees can create leave requests")
    })
    public ResponseEntity<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveRequestDto leaveRequestDto) {
        LeaveRequest leaveRequest = leaveRequestService.create(leaveRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequest);
    }

    @PutMapping("/{id}/approve-reject")
    @RequiresRole({"hod"})
    @Operation(summary = "Approve or reject leave request", description = "Approve or reject a leave request (HOD only). Prevents overlapping approved leaves in the same department.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave request processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or overlapping leave exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only HOD can approve/reject leave requests"),
            @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<LeaveRequest> approveOrRejectLeaveRequest(
            @Parameter(description = "Leave request ID") @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalDto approvalDto) {
        LeaveRequest leaveRequest = leaveRequestService.approveOrReject(id, approvalDto);
        return ResponseEntity.ok(leaveRequest);
    }
}

