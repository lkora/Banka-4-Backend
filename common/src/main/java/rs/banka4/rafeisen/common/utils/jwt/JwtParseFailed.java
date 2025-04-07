package rs.banka4.rafeisen.common.utils.jwt;

/**
 * Indicates that a {@link VerifiedToken} instance (specifically, an instance of one of its
 * subclasses) could not be created due to a parsing error or similar.
 */
public class JwtParseFailed extends Exception {
    public JwtParseFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
