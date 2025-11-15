package com.hrservice.hrservice.service;

import com.hrservice.hrservice.dto.LeaveApprovalDto;
import com.hrservice.hrservice.dto.LeaveRequestDto;
import com.hrservice.hrservice.dto.UserInfo;
import com.hrservice.hrservice.entity.Department;
import com.hrservice.hrservice.entity.LeaveRequest;
import com.hrservice.hrservice.entity.LeaveType;
import com.hrservice.hrservice.repository.DepartmentRepository;
import com.hrservice.hrservice.repository.LeaveRequestRepository;
import com.hrservice.hrservice.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthServiceClient authServiceClient;

    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    public LeaveRequest findById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));
    }

    public List<LeaveRequest> findByEmployeeId(String employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> findMyLeaveRequests() {
        UserInfo userInfo = getCurrentUser();
        return leaveRequestRepository.findByEmployeeId(userInfo.getEmployeeId());
    }

    public List<LeaveRequest> findPendingForHod() {
        UserInfo userInfo = getCurrentUser();
        if (!"hod".equalsIgnoreCase(userInfo.getRole())) {
            throw new SecurityException("Only HOD can view pending leave requests");
        }

        // Find department where this user is HOD
        Department department = departmentRepository.findAll().stream()
                .filter(dept -> userInfo.getEmployeeId().equals(dept.getHodEmployeeId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No department found where you are HOD"));

        // Get all employees in this department from auth-service
        String token = getCurrentToken();
        List<UserInfo> departmentUsers = authServiceClient.getUsersByDepartment(department.getId(), token);
        List<String> departmentEmployeeIds = departmentUsers.stream()
                .map(UserInfo::getEmployeeId)
                .collect(Collectors.toList());

        // Get all pending leave requests for employees in this department
        return leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveRequest.Status.PENDING)
                .filter(lr -> departmentEmployeeIds.contains(lr.getEmployeeId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequest create(LeaveRequestDto leaveRequestDto) {
        UserInfo userInfo = getCurrentUser();
        
        // Validate dates
        if (leaveRequestDto.getEndDate().isBefore(leaveRequestDto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        // Get leave type
        LeaveType leaveType = leaveTypeRepository.findById(leaveRequestDto.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        // Get user's department
        if (userInfo.getDepartmentId() == null) {
            throw new IllegalArgumentException("Employee must be assigned to a department");
        }

        Department department = departmentRepository.findById(userInfo.getDepartmentId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        // Get all employees in the same department
        // In a real scenario, you'd call auth-service to get employees by departmentId
        // For now, we'll check all leave requests and filter by department
        // This requires getting employee IDs from auth-service
        List<String> departmentEmployeeIds = getDepartmentEmployeeIds(department.getId());

        // Check for overlapping approved or pending leaves in the same department
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                departmentEmployeeIds,
                leaveRequestDto.getStartDate(),
                leaveRequestDto.getEndDate(),
                0L // excludeId = 0 for new requests
        );

        if (!overlappingLeaves.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot apply for leave: Another employee in your department already has an approved or pending leave during this period");
        }

        // Create leave request
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(userInfo.getEmployeeId());
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(leaveRequestDto.getStartDate());
        leaveRequest.setEndDate(leaveRequestDto.getEndDate());
        leaveRequest.setReason(leaveRequestDto.getReason());
        leaveRequest.setStatus(LeaveRequest.Status.PENDING);

        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approveOrReject(Long id, LeaveApprovalDto approvalDto) {
        UserInfo userInfo = getCurrentUser();
        
        if (!"hod".equalsIgnoreCase(userInfo.getRole())) {
            throw new SecurityException("Only HOD can approve or reject leave requests");
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalArgumentException("Leave request is not pending");
        }

        // Get the employee's department from auth-service
        String token = getCurrentToken();
        UserInfo requestEmployee = authServiceClient.getUserByEmployeeId(leaveRequest.getEmployeeId(), token);
        if (requestEmployee == null || requestEmployee.getDepartmentId() == null) {
            throw new IllegalArgumentException("Employee must be assigned to a department");
        }
        
        Department department = departmentRepository.findById(requestEmployee.getDepartmentId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        if (!userInfo.getEmployeeId().equals(department.getHodEmployeeId())) {
            throw new SecurityException("You are not the HOD of this employee's department");
        }

        if ("approve".equalsIgnoreCase(approvalDto.getAction())) {
            // Check for overlapping leaves before approving
            List<String> departmentEmployeeIds = getDepartmentEmployeeIds(department.getId());
            List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                    departmentEmployeeIds,
                    leaveRequest.getStartDate(),
                    leaveRequest.getEndDate(),
                    leaveRequest.getId()
            );

            if (!overlappingLeaves.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cannot approve: Another employee in this department already has an approved or pending leave during this period");
            }

            leaveRequest.setStatus(LeaveRequest.Status.APPROVED);
            leaveRequest.setApprovedBy(userInfo.getEmployeeId());
        } else if ("reject".equalsIgnoreCase(approvalDto.getAction())) {
            leaveRequest.setStatus(LeaveRequest.Status.REJECTED);
            leaveRequest.setApprovedBy(userInfo.getEmployeeId());
            leaveRequest.setRejectionReason(approvalDto.getRejectionReason());
        } else {
            throw new IllegalArgumentException("Action must be 'approve' or 'reject'");
        }

        return leaveRequestRepository.save(leaveRequest);
    }

    private UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserInfo)) {
            throw new SecurityException("User not authenticated");
        }
        return (UserInfo) authentication.getPrincipal();
    }

    private List<String> getDepartmentEmployeeIds(Long departmentId) {
        UserInfo currentUser = getCurrentUser();
        String token = getCurrentToken(); // We'll need to extract token from SecurityContext
        
        List<UserInfo> users = authServiceClient.getUsersByDepartment(departmentId, token);
        return users.stream()
                .map(UserInfo::getEmployeeId)
                .collect(Collectors.toList());
    }

    private Integer getUserDepartmentId(String employeeId) {
        // For now, we'll get it from the current user context
        // In production, you could add an endpoint to get user by employeeId
        UserInfo userInfo = getCurrentUser();
        if (userInfo.getEmployeeId().equals(employeeId)) {
            return userInfo.getDepartmentId();
        }
        // If different employee, we'd need to call auth-service
        // For now, return null and handle in calling code
        return null;
    }

    private String getCurrentToken() {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return com.hrservice.hrservice.security.JwtAuthenticationFilter.getCurrentToken();
    }
}

