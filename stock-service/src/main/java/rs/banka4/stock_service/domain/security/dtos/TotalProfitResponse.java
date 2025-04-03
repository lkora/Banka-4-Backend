package rs.banka4.stock_service.domain.security.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;

import java.math.BigDecimal;

/**
 * Represents aggregated profit information for stock holdings.
 *
 * @param totalProfit Sum of unrealized profits across all stock positions
 * @param currency Currency code for profit calculations (always USD)
 */
@Schema(
    name = "TotalProfitResponse",
    description = "Aggregated profit information for stock portfolio"
)
public record TotalProfitResponse(
    @Schema(
        description = "Total unrealized profit/loss for stock holdings",
        example = "1095.00"
    )
    BigDecimal totalProfit,

    @Schema(
        description = "Currency code for profit calculations",
        example = "USD",
        allowableValues = {"RSD", "EUR", "USD", "CHF", "JPY", "AUD", "CAD", "GBP"}
    )
    CurrencyCode currency
) {}
