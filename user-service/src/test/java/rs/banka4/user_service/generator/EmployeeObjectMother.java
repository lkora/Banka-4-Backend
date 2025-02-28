package rs.banka4.user_service.generator;

import rs.banka4.user_service.dto.CreateEmployeeDto;
import rs.banka4.user_service.models.Privilege;

import java.time.LocalDate;
import java.util.Set;

public class EmployeeObjectMother {
    public static CreateEmployeeDto generateBasicCreateEmployeeDto() {
        return new CreateEmployeeDto(
                "John", "Doe", "johndoe", LocalDate.of(1990, 1, 1),
                "Male", "john.doe@example.com", "+1234567890", "123 Main St",
                "password123", Set.of(Privilege.SEARCH), "Developer", "IT");
    }
}
