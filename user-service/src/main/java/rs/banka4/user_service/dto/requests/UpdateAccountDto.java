package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO for updating account details")
public record UpdateAccountDto(
        @NotBlank
        @Schema(description = "Account number", example = "1234567890")
        String accountNumber,

        @NotBlank
        @Schema(description = "Balance", example = "1000.00")
        BigDecimal balance,

        @NotNull
        @Schema(description = "Expiration date", example = "2024-01-01")
        LocalDate expirationDate,

        @NotBlank
        @Schema(description = "Active status", example = "true")
        boolean active,

        @NotNull
        @Schema(description = "Type of account", example = "SAVINGS")
        AccountType accountType,

        @NotBlank
        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @NotBlank
        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @NotBlank
        @Schema(description = "Daily spending", example = "50.00")
        BigDecimal dailySpending,

        @NotBlank
        @Schema(description = "Monthly spending", example = "200.00")
        BigDecimal monthlySpending,

        @NotNull
        @Schema(description = "Currency ID to be associated with this account", example = "11111111-2222-3333-4444-555555555555")
        UUID currencyId

) { }
