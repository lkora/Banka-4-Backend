package rs.banka4.stock_service.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.banka4.rafeisen.common.test.BadUUIDGenerator;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.listing.db.Listing;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.ListingDailyPriceInfoRepository;
import rs.banka4.stock_service.repositories.ListingRepository;

/**
 * Test-writing utility class for generating various listings.
 */
public class ListingGenerator {
    public static final UUID LISTING_1_UUID =
        UUID.fromString("8d795b5d-506b-4256-a420-4246f64f5e12");
    private static final Logger LOGGER = LoggerFactory.getLogger(ListingGenerator.class);

    private static UUID makeBadUUID(Random generator) {
        return BadUUIDGenerator.generateBadUUIDv4(generator.nextLong(), generator.nextLong());
    }

    private static int getContractSize(Security security, Random generator) {
        return switch (security) {
        case Stock ignored -> 1;
        case ForexPair ignored -> 1000;
        case Future ignored -> generator.nextInt(1, 100);
        default -> throw new IllegalStateException("Please add contract size for your new class");
        };
    }

    private static BigDecimal makeRoundDecimalFrom1To100(Random generator, int scale) {
        return new BigDecimal(generator.nextDouble(1, 100)).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Given a {@link Security} {@code security} and {@link Exchange} {@code
     * exchange}, populate {@code listingRepo} with some listings for it in the last five days, and
     * {@code listingHistoryRepo} with matching daily summaries.
     *
     * The generated data is pseudo-random with a seed based on {@link Security#getId()}.
     */
    public static void makeExampleListings(
        Security security,
        Exchange exchange,
        ListingRepository listingRepo,
        ListingDailyPriceInfoRepository listingHistoryRepo
    ) {
        Objects.requireNonNull(
            security.getId(),
            "Securities passed to makeExampleListings must have IDs as IDs are used as seeds"
        );

        final var generator =
            new Random(
                security.getId()
                    .getMostSignificantBits()
                    ^ security.getId()
                        .getLeastSignificantBits()
            );

        final var now = OffsetDateTime.now();
        var yesterdayLastPrice = (BigDecimal) null;
        for (int dayOffset = -5; dayOffset <= 0; dayOffset++) {
            final var dayStart =
                now.plusDays(/* Negative. */ dayOffset)
                    .truncatedTo(ChronoUnit.DAYS);
            var askHigh = (BigDecimal) null;
            var bidLow = (BigDecimal) null;
            var lastPrice = (BigDecimal) null;
            for (int i = 0; i < 3; i++) {
                final var listingRefreshTime = dayStart.withHour(3 * (i + 1));

                /* Most elegant Java code: */
                final var bidAsk =
                    List.of(
                        makeRoundDecimalFrom1To100(generator, 2),
                        makeRoundDecimalFrom1To100(generator, 2)
                    );
                final var ask = Collections.min(bidAsk);
                final var bid = Collections.max(bidAsk);

                final var listing =
                    Listing.builder()
                        .id(makeBadUUID(generator))
                        .security(security)
                        .exchange(exchange)
                        .lastRefresh(listingRefreshTime)
                        .ask(ask)
                        .bid(bid)
                        .contractSize(getContractSize(security, generator))
                        .active(dayOffset == 0)
                        .build();
                askHigh = askHigh == null ? ask : ask.max(askHigh);
                bidLow = bidLow == null ? bid : bid.min(bidLow);

                /* TODO(arsen): lol, there's no way that's right */
                lastPrice = ask;
                LOGGER.debug("Saving listing {}", listing);
                listingRepo.save(listing);
            }

            if (dayOffset == 0) continue;

            final var summary =
                ListingDailyPriceInfo.builder()
                    .id(makeBadUUID(generator))
                    .security(security)
                    .exchange(exchange)
                    .date(dayStart)
                    .lastPrice(lastPrice)
                    .askHigh(askHigh)
                    .bigLow(bidLow)
                    /* TODO(arsen) */
                    .change(
                        yesterdayLastPrice != null
                            ? lastPrice.subtract(yesterdayLastPrice)
                            : lastPrice
                    )
                    .volume(generator.nextInt(1, 100))
                    .build();
            LOGGER.debug("Saving daily summary {}", summary);
            yesterdayLastPrice = lastPrice;

            listingHistoryRepo.save(summary);
        }
    }
}
