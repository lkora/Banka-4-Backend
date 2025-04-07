package rs.banka4.rafeisen.common.utils.jwt;

/**
 * Represents a single JWT that's yet to be parsed.
 *
 * @param rawJwt Raw JWT string (<strong>NOT</strong> a bearer string)
 */
public record UnverifiedToken(String rawJwt) {
}
