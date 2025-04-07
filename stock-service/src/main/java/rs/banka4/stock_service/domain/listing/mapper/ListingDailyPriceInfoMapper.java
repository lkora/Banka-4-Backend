package rs.banka4.stock_service.domain.listing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.stock_service.domain.listing.dtos.PriceChangeDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListingDailyPriceInfoMapper {
    ListingDailyPriceInfoMapper INSTANCE = Mappers.getMapper(ListingDailyPriceInfoMapper.class);

    @Mapping(
        target = "price",
        source = "lastPrice"
    )
    PriceChangeDto toPriceChangeDto(ListingDailyPriceInfo listingDailyPriceInfo);
}
