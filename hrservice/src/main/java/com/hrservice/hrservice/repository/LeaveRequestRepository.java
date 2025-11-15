package com.hrservice.hrservice.repository;

import com.hrservice.hrservice.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(String employeeId);

    // Find by status
    List<LeaveRequest> findByStatus(LeaveRequest.Status status);
    
    // Find overlapping approved or pending leave requests for employees in the same department
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId IN :employeeIds " +
            "AND lr.status IN ('APPROVED', 'PENDING') " +
            "AND lr.id != :excludeId " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("employeeIds") List<String> employeeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );
}