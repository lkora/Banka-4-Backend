package rs.banka4.stock_service.domain.listing.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Dto for data for price change chart")
public record PriceChangeDto(
    @Schema(description = "dates for chart") OffsetDateTime date,
    @Schema(description = "last price of the day for that date on chart") BigDecimal price
) {
}
