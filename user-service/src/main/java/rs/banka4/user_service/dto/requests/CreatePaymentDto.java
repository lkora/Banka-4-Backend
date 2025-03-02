package rs.banka4.user_service.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO for creating payment order")
public record CreatePaymentDto(
        @NotBlank
        @Schema(description = "From account number", example = "102-39443942389")
        String fromAccount,

        @NotBlank
        @Schema(description = "To account number", example = "102-394438340549")
        String toAccount,

        @NotBlank
        @Schema(description = "Payment amount", example = "1.00 EUR (fromAccount currency is used)")
        BigDecimal amount,

        @NotBlank
        @Schema(description = "Recipient name", example = "Pera Perić")
        String recipient,

        @NotBlank
        @Schema(description = "Payment code (3-digit, e.g., 2xx)", example = "289")
        String paymentCode,

        @NotBlank
        @Schema(description = "Reference number", example = "1176926")
        String referenceNumber,
        
        @NotBlank
        @Schema(description = "Payment purpose (optional)", example = "za privatni čas")
        String paymentPurpose
) { }
