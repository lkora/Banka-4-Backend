package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.banka4.user_service.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO for creating a new account")
public record CreateAccountDto(
        @NotBlank
        @Schema(description = "Account number", example = "1234567890")
        String accountNumber,

        @NotNull
        @Schema(description = "Initial balance", example = "1000.00")
        BigDecimal balance,

        @NotNull
        @Schema(description = "Expiration date", example = "2024-01-01")
        LocalDate expirationDate,

        @Schema(description = "Active status", example = "true")
        boolean active,

        @NotNull
        @Schema(description = "Type of account", example = "SAVINGS")
        AccountType accountType,

        @NotNull
        @Schema(description = "Daily limit", example = "100.00")
        BigDecimal dailyLimit,

        @NotNull
        @Schema(description = "Monthly limit", example = "1000.00")
        BigDecimal monthlyLimit,

        @NotNull
        @Schema(description = "Daily spending", example = "50.00")
        BigDecimal dailySpending,

        @NotNull
        @Schema(description = "Monthly spending", example = "200.00")
        BigDecimal monthlySpending,

        @NotNull
        @Schema(description = "Currency ID to be associated with this account", example = "11111111-2222-3333-4444-555555555555")
        UUID currencyId

) { }
