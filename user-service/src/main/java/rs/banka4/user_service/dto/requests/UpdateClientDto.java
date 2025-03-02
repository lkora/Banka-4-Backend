package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "DTO for updating client details")
public record UpdateClientDto(
        @Schema(description = "First name", example = "Mehmedalija")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-01-01")
        LocalDate dateOfBirth,

        @Schema(description = "Gender", example = "Male")
        String gender,

        @Email
        @Schema(description = "Email address", example = "danny.jo@example.com")
        String email,

        @Schema(description = "Phone number", example = "123-456-7890")
        String phone,

        @Schema(description = "Address", example = "123 Main St")
        String address,

        @Schema(description = "Set of accounts linked to the client")
        @Valid List<UpdateAccountDto> accounts
) { }
