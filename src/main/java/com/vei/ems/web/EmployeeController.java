package com.vei.ems.web;

import com.vei.ems.domain.Department;
import com.vei.ems.dto.EmployeeRequest;
import com.vei.ems.dto.EmployeeResponse;
import com.vei.ems.dto.PayslipResponse;
import com.vei.ems.service.AuditService;
import com.vei.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuditService auditService;

    public EmployeeController(EmployeeService employeeService, AuditService auditService) {
        this.employeeService = employeeService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<EmployeeResponse> list(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) Department department) {
        return employeeService.search(search, department);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return employeeService.get(id);
    }

    @GetMapping("/{id}/payslip")
    public PayslipResponse payslip(@PathVariable Long id) {
        return employeeService.payslip(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request, Authentication authentication) {
        EmployeeResponse created = employeeService.create(request);
        auditService.log(authentication.getName(), "ADD_EMPLOYEE", created.employeeCode() + " " + created.name());
        return created;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request,
                                   Authentication authentication) {
        EmployeeResponse updated = employeeService.update(id, request);
        auditService.log(authentication.getName(), "UPDATE_EMPLOYEE", updated.employeeCode() + " " + updated.name());
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void delete(@PathVariable Long id, Authentication authentication) {
        EmployeeResponse deleted = employeeService.delete(id);
        auditService.log(authentication.getName(), "DELETE_EMPLOYEE", deleted.employeeCode() + " " + deleted.name());
    }
}
