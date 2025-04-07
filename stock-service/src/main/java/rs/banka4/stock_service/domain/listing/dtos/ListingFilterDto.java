package rs.banka4.stock_service.domain.listing.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingFilterDto {
    private String searchName;
    private String searchTicker;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private BigDecimal askMin;
    private BigDecimal askMax;
    private BigDecimal bidMin;
    private BigDecimal bidMax;
    private String exchangePrefix;
    private SecurityType securityType;
    private OffsetDateTime settlementDateFrom;
    private OffsetDateTime settlementDateTo;
    private Long volumeMin;
    private Long volumeMax;
    private SortBy sortBy;
    private SortDirection sortDirection;

    public static enum SortBy {
        PRICE,
        VOLUME
    }

    public static enum SortDirection {
        ASC,
        DESC
    }
}
