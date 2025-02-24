package rs.banka4.user_service.service.abstraction;

import rs.banka4.user_service.dto.CreateEmployee;
import rs.banka4.user_service.models.Employee;

public interface EmployeeAbs {
    Employee createEmployee(CreateEmployee dto);
}
