package rs.banka4.user_service.service.impl;

import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.CreateEmployee;
import rs.banka4.user_service.models.Employee;
import rs.banka4.user_service.service.abstraction.EmployeeAbs;

@Service
public class EmployeeService implements EmployeeAbs {
    @Override
    public Employee createEmployee(CreateEmployee dto) {
        return null;
    }
}
