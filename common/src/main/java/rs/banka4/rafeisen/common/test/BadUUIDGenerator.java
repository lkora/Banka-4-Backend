package rs.banka4.rafeisen.common.test;

import java.util.UUID;

/* A tool for bad UUID generation. Never use outside of tests. */
public class BadUUIDGenerator {
    /**
     * Given a pair of {@code long}s—one for most significant bits and one for least significant
     * bits—generate a UUIDv4 with said bits, except for the variant and version bits set to v4,
     * IETF. NEVER USE OUTSIDE OF TESTS!
     */
    public static UUID generateBadUUIDv4(long msb, long lsb) {
        /* Set version. */
        msb &= 0xffffffffffff0fffL;
        msb |= 0x0000000000004000L;
        /* Set variant. */
        lsb &= 0x3fffffffffffffffL;
        lsb |= 0x8000000000000000L;
        return new UUID(msb, lsb);
    }
}
