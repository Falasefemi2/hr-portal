package com.hrservice.hrservice.service;

import com.hrservice.hrservice.dto.LeaveTypeDto;
import com.hrservice.hrservice.entity.LeaveType;
import com.hrservice.hrservice.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public List<LeaveType> findAll() {
        return leaveTypeRepository.findAll();
    }

    public Optional<LeaveType> findById(Long id) {
        return leaveTypeRepository.findById(id);
    }

    @Transactional
    public LeaveType create(LeaveTypeDto leaveTypeDto) {
        if (leaveTypeRepository.findByName(leaveTypeDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Leave type with name " + leaveTypeDto.getName() + " already exists");
        }
        
        LeaveType leaveType = new LeaveType();
        leaveType.setName(leaveTypeDto.getName());
        leaveType.setDescription(leaveTypeDto.getDescription());
        leaveType.setMaxDaysPerYear(leaveTypeDto.getMaxDaysPerYear());
        leaveType.setRequiresDocumentation(leaveTypeDto.getRequiresDocumentation() != null ? leaveTypeDto.getRequiresDocumentation() : false);
        
        return leaveTypeRepository.save(leaveType);
    }

    @Transactional
    public LeaveType update(Long id, LeaveTypeDto leaveTypeDto) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));
        
        if (leaveTypeDto.getName() != null && !leaveTypeDto.getName().equals(leaveType.getName())) {
            if (leaveTypeRepository.findByName(leaveTypeDto.getName()).isPresent()) {
                throw new IllegalArgumentException("Leave type with name " + leaveTypeDto.getName() + " already exists");
            }
            leaveType.setName(leaveTypeDto.getName());
        }
        
        if (leaveTypeDto.getDescription() != null) {
            leaveType.setDescription(leaveTypeDto.getDescription());
        }
        
        if (leaveTypeDto.getMaxDaysPerYear() != null) {
            leaveType.setMaxDaysPerYear(leaveTypeDto.getMaxDaysPerYear());
        }
        
        if (leaveTypeDto.getRequiresDocumentation() != null) {
            leaveType.setRequiresDocumentation(leaveTypeDto.getRequiresDocumentation());
        }
        
        return leaveTypeRepository.save(leaveType);
    }

    @Transactional
    public void delete(Long id) {
        if (!leaveTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Leave type not found with id: " + id);
        }
        leaveTypeRepository.deleteById(id);
    }
}

