package rs.banka4.stock_service.domain.security.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Represents a security holding response with financial metrics.
 *
 * @param type Type of security (e.g., Stock, Option, Forex)
 * @param ticker Symbol identifier for the security
 * @param amount Quantity currently owned
 * @param price Current market price per unit
 * @param profit Unrealized profit/loss for the holding
 * @param lastModified Timestamp of last modification from relevant orders
 * @param currency Currency code of the related amount
 */
@Schema(
    name = "SecurityResponse",
    description = "Represents a security holding with financial metrics"
)
public record SecurityResponse(
    @Schema(
        description = "Type of security",
        example = "Stock",
        allowableValues = {"Stock", "Option", "Forex", "Future"}
    )
    String type,

    @Schema(
        description = "Ticker symbol of the security",
        example = "AAPL"
    )
    String ticker,

    @Schema(
        description = "Quantity currently owned",
        example = "30"
    )
    int amount,

    @Schema(
        description = "Current market price per unit",
        example = "172.50"
    )
    BigDecimal price,

    @Schema(
        description = "Unrealized profit/loss for the position",
        example = "495.00"
    )
    BigDecimal profit,

    @Schema(
        description = "Timestamp of last buy/sell transaction",
        example = "2025-03-28T14:12:00+00:00"
    )
    OffsetDateTime lastModified,

    @Schema(
        description = "Currency code for profit calculations",
        example = "USD",
        allowableValues = {"RSD", "EUR", "USD", "CHF", "JPY", "AUD", "CAD", "GBP"}
    )
    CurrencyCode currency

) {}
