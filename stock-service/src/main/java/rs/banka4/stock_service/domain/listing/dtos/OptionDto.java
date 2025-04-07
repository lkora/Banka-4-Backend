package rs.banka4.stock_service.domain.listing.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;

@Schema(
    description = "Dto in exact format needed for options table in specification example 2 with needed extension.\n"
        + "Dto includes two options, one call and one put for one strike price for one settlement date. \n"
        + "As we dont have our mechanism for options listings, last price and change will be 0 always..."
)
public record OptionDto(
    UUID callsId,
    BigDecimal callsLastPrice,
    BigDecimal callsChange,
    double callsImpliedVolatility,
    int callsVolume,
    int callsOpenInterest,
    @Schema(description = "price of call option") MonetaryAmount callsPremium,
    BigDecimal strike,
    UUID putsId,
    BigDecimal putsLastPrice,
    BigDecimal putsChange,
    double putsImpliedVolatility,
    int putsVolume,
    int putsOpenInterest,
    @Schema(description = "price of put option") MonetaryAmount putsPremium
) {

}
