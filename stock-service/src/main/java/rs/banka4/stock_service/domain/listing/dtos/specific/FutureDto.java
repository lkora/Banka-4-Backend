package rs.banka4.stock_service.domain.listing.dtos.specific;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rs.banka4.stock_service.domain.listing.dtos.ListingDetailsDto;
import rs.banka4.stock_service.domain.security.future.db.UnitName;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dto for additional fields for Future security")
public class FutureDto extends ListingDetailsDto {
    private long contractSize;
    private UnitName contractUnit;
    private OffsetDateTime settlementDate;
}
