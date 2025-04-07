package rs.banka4.stock_service.domain.listing.dtos.specific;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dto for additional fields for Stock security")
public class StockDto extends ListingDetailsDto {
    private long outstandingShares;
    private BigDecimal dividendYield;
    private OffsetDateTime createdAt;
}
