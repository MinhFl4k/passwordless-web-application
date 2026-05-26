package com.app.demo.controller;

import com.app.demo.dto.request.DepartmentReqDto;
import com.app.demo.dto.request.DepartmentUpdateDto;
import com.app.demo.dto.response.DepartmentResDto;
import com.app.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/list")
    public String listDepartments(Model model) {
        List<DepartmentResDto> departments = departmentService.findAll();
        model.addAttribute("departments", departments);
        return "departments/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new DepartmentReqDto());
        return "departments/create";
    }

    @PostMapping("/create")
    public String createDepartment(
            @Valid @ModelAttribute("department") DepartmentReqDto departmentReqDto,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "departments/create";
        }

        departmentService.create(departmentReqDto);
        return "redirect:/departments/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable UUID id,
            Model model
    ) {
        DepartmentResDto department = departmentService.findById(id);

        DepartmentUpdateDto departmentUpdateDto = new DepartmentUpdateDto();
        departmentUpdateDto.setName(department.getName());
        departmentUpdateDto.setDescription(department.getDescription());

        model.addAttribute("department", departmentUpdateDto);
        model.addAttribute("departmentId", id);

        return "departments/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateDepartment(
            @PathVariable UUID id,
            @Valid @ModelAttribute("department") DepartmentUpdateDto departmentUpdateDto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("departmentId", id);
            return "departments/edit";
        }

        departmentService.update(id, departmentUpdateDto);
        return "redirect:/departments/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable UUID id) {
        departmentService.delete(id);
        return "redirect:/departments/list";
    }
}
