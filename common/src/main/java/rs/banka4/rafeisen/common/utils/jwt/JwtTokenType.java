package rs.banka4.rafeisen.common.utils.jwt;

/** Values for the {@code typ} field of JWTs produced by Rafeisen. */
public enum JwtTokenType {
    /** Indicates that this token is an access token. */
    ACCESS,
    /** Indicates that this token is a refresh token. */
    REFRESH;
}
