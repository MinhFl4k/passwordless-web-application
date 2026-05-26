package com.app.demo.service.Impl;

import com.app.demo.dto.request.DepartmentReqDto;
import com.app.demo.dto.request.DepartmentUpdateDto;
import com.app.demo.dto.response.DepartmentResDto;
import com.app.demo.enums.ErrorMessage;
import com.app.demo.model.Department;
import com.app.demo.repository.DepartmentRepository;
import com.app.demo.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentResDto> findAll() {

        return departmentRepository.findAll()
                .stream()
                .map(department -> new DepartmentResDto(
                        department.getId(),
                        department.getName(),
                        department.getDescription()
                ))
                .toList();
    }

    @Override
    public DepartmentResDto findById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        ErrorMessage.DEPARTMENT_NOT_FOUND.getMessage()
                ));

        return mapToDepartmentResDto(department);
    }

    @Override
    public DepartmentResDto create(DepartmentReqDto departmentReqDto) {
        Department department = new Department();

        department.setName(departmentReqDto.getName());
        department.setDescription(departmentReqDto.getDescription());

        Department savedDepartment = departmentRepository.save(department);

        return mapToDepartmentResDto(savedDepartment);
    }

    @Override
    public DepartmentResDto update(UUID id, DepartmentUpdateDto departmentUpdateDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        ErrorMessage.DEPARTMENT_NOT_FOUND.getMessage()
                ));

        department.setName(departmentUpdateDto.getName());
        department.setDescription(departmentUpdateDto.getDescription());

        Department updatedDepartment = departmentRepository.save(department);

        return mapToDepartmentResDto(updatedDepartment);
    }

    @Override
    public void delete(UUID id) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.DEPARTMENT_NOT_FOUND.getMessage()));
        departmentRepository.delete(existingDepartment);
    }

    private DepartmentResDto mapToDepartmentResDto(Department department) {
        return new DepartmentResDto(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }
}
