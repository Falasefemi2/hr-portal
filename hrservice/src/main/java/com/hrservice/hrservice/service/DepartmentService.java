package com.hrservice.hrservice.service;

import com.hrservice.hrservice.dto.DepartmentDto;
import com.hrservice.hrservice.entity.Department;
import com.hrservice.hrservice.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuthServiceClient authServiceClient;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department create(DepartmentDto departmentDto) {
        if (departmentRepository.findByName(departmentDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Department with name " + departmentDto.getName() + " already exists");
        }
        
        Department department = new Department();
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        
        if (departmentDto.getHodEmployeeId() != null && !departmentDto.getHodEmployeeId().isEmpty()) {
            // Validate HOD exists (would need to call auth-service)
            department.setHodEmployeeId(departmentDto.getHodEmployeeId());
        }
        
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        
        if (departmentDto.getName() != null && !departmentDto.getName().equals(department.getName())) {
            if (departmentRepository.findByName(departmentDto.getName()).isPresent()) {
                throw new IllegalArgumentException("Department with name " + departmentDto.getName() + " already exists");
            }
            department.setName(departmentDto.getName());
        }
        
        if (departmentDto.getDescription() != null) {
            department.setDescription(departmentDto.getDescription());
        }
        
        if (departmentDto.getHodEmployeeId() != null) {
            department.setHodEmployeeId(departmentDto.getHodEmployeeId());
        }
        
        return departmentRepository.save(department);
    }

    @Transactional
    public void assignHod(Long departmentId, String hodEmployeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
        
        department.setHodEmployeeId(hodEmployeeId);
        departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }
}

