package com.vei.ems.service;

import com.vei.ems.domain.Department;
import com.vei.ems.domain.Employee;
import com.vei.ems.domain.EmployeeType;
import com.vei.ems.dto.EmployeeRequest;
import com.vei.ems.dto.EmployeeResponse;
import com.vei.ems.dto.PayslipResponse;
import com.vei.ems.exception.BadRequestException;
import com.vei.ems.exception.ConflictException;
import com.vei.ems.exception.NotFoundException;
import com.vei.ems.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Business rules for employees. Controllers call this; it calls the repository. */
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    // ------------------------------------------------------------ reads

    @Transactional(readOnly = true)
    public List<EmployeeResponse> search(String search, Department department) {
        String text = search == null ? "" : search.trim().toLowerCase();
        return repository.findAll(Sort.by("id")).stream()
                .filter(e -> text.isEmpty()
                        || e.getName().toLowerCase().contains(text)
                        || (e.getEmployeeCode() != null && e.getEmployeeCode().toLowerCase().contains(text)))
                .filter(e -> department == null || e.getDepartment() == department)
                .map(EmployeeService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public PayslipResponse payslip(Long id) {
        Employee e = find(id);
        List<PayslipResponse.Line> lines = new ArrayList<>();
        if (e.getType() == EmployeeType.FULL_TIME) {
            double basic = e.getBasicSalary();
            lines.add(new PayslipResponse.Line("Basic salary", basic));
            lines.add(new PayslipResponse.Line("HRA (20%)", PayCalculator.hra(basic)));
            lines.add(new PayslipResponse.Line("DA (10%)", PayCalculator.da(basic)));
            lines.add(new PayslipResponse.Line("Gross salary", PayCalculator.gross(basic)));
            lines.add(new PayslipResponse.Line("PF deduction (12%)", -PayCalculator.pf(basic)));
        } else {
            lines.add(new PayslipResponse.Line("Hourly rate", e.getHourlyRate()));
            lines.add(new PayslipResponse.Line("Hours worked", e.getHoursWorked()));
        }
        return new PayslipResponse(e.getEmployeeCode(), e.getName(), e.getDepartment().getDisplayName(),
                e.getDesignation(), e.getType().name(), lines, monthlyPay(e));
    }

    // ------------------------------------------------------------ writes

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        Employee e = new Employee();
        apply(e, request);
        Employee saved = repository.save(e);
        saved.setEmployeeCode(String.format("EMP%03d", saved.getId()));
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee e = find(id);
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("Email already registered: " + email);
        }
        apply(e, request);
        return toResponse(e);
    }

    @Transactional
    public EmployeeResponse delete(Long id) {
        Employee e = find(id);
        EmployeeResponse snapshot = toResponse(e);
        repository.delete(e);
        return snapshot;
    }

    // ------------------------------------------------------------ helpers

    private Employee find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No employee found with id " + id));
    }

    /** Copies the request into the entity and checks the pay fields that match the employee type. */
    private void apply(Employee e, EmployeeRequest r) {
        e.setName(r.name().trim());
        e.setEmail(r.email().trim().toLowerCase());
        e.setPhone(r.phone().trim());
        e.setDepartment(r.department());
        e.setDesignation(r.designation().trim());
        e.setJoiningDate(r.joiningDate());
        e.setType(r.type());

        if (r.type() == EmployeeType.FULL_TIME) {
            if (r.basicSalary() == null || r.basicSalary() <= 0) {
                throw new BadRequestException("Basic salary must be greater than 0 for a full-time employee.");
            }
            e.setBasicSalary(r.basicSalary());
            e.setHourlyRate(null);
            e.setHoursWorked(null);
        } else {
            if (r.hourlyRate() == null || r.hourlyRate() <= 0) {
                throw new BadRequestException("Hourly rate must be greater than 0 for a part-time employee.");
            }
            if (r.hoursWorked() == null) {
                throw new BadRequestException("Hours worked is required for a part-time employee.");
            }
            e.setHourlyRate(r.hourlyRate());
            e.setHoursWorked(r.hoursWorked());
            e.setBasicSalary(null);
        }
    }

    static double monthlyPay(Employee e) {
        if (e.getType() == EmployeeType.FULL_TIME) {
            return PayCalculator.netFullTime(e.getBasicSalary());
        }
        return PayCalculator.netPartTime(e.getHourlyRate(), e.getHoursWorked());
    }

    static EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getEmployeeCode(), e.getName(), e.getEmail(), e.getPhone(),
                e.getDepartment(), e.getDepartment().getDisplayName(), e.getDesignation(), e.getJoiningDate(),
                e.getType(), e.getBasicSalary(), e.getHourlyRate(), e.getHoursWorked(), monthlyPay(e));
    }
}
