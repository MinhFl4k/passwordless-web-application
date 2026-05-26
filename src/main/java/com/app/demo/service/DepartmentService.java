package com.app.demo.service;

import com.app.demo.dto.request.DepartmentReqDto;
import com.app.demo.dto.request.DepartmentUpdateDto;
import com.app.demo.dto.response.DepartmentResDto;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    List<DepartmentResDto> findAll();

    DepartmentResDto findById(UUID id);

    DepartmentResDto create(DepartmentReqDto departmentReqDto);

    DepartmentResDto update(UUID id, DepartmentUpdateDto departmentUpdateDto);

    void delete(UUID id);
}
