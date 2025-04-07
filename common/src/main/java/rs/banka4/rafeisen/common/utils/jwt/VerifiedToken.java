package rs.banka4.rafeisen.common.utils.jwt;

import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import rs.banka4.rafeisen.common.security.UserType;

@Getter
@EqualsAndHashCode
@ToString
public abstract class VerifiedToken {
    /**
     * Current version of this class. Alter when updating JWT contents in a backwards-incompatible
     * way, such as changing the meaning of a field.
     */
    public static final int CURRENT_VERSION = 3;

    /** User UUID. */
    private final UUID sub;
    /** User type. Redundant with {@link sub}, but kept for faster lookup. */
    private final JwtTokenType typ;
    /** User role. */
    private final UserType role;

    VerifiedToken(UserType role, UUID userId, JwtTokenType type) {
        this.sub = userId;
        this.typ = type;
        this.role = role;
    }

    VerifiedToken(Claims claims, JwtTokenType expectedType) throws JwtParseFailed {
        try {
            this.sub = UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new JwtParseFailed("Invalid user ID in sub", e);
        }
        try {
            this.typ = JwtTokenType.valueOf(claims.get("typ", String.class));
        } catch (Exception e) {
            throw new JwtParseFailed("Invalid token type", e);
        }
        try {
            final var ver = claims.get("ver", Integer.class);
            if (ver != CURRENT_VERSION) {
                throw new IllegalArgumentException(
                    "Token version unsupported (%d != %d)".formatted(ver, CURRENT_VERSION)
                );
            }
        } catch (Exception e) {
            throw new JwtParseFailed("Token version not acceptable", e);
        }

        try {
            this.role = UserType.valueOf(claims.get("role", String.class));
        } catch (Exception e) {
            throw new JwtParseFailed("Invalid token role", e);
        }

        validateType(expectedType);
    }

    protected void validateType(JwtTokenType expectedType) {
        if (typ == expectedType) return;
        throw new IllegalArgumentException(
            "This token must be a %s but it was %s".formatted(expectedType, typ)
        );
    }

    protected Map<String, ?> populateBaseClaims() {
        return Map.ofEntries(
            Map.entry("sub", sub.toString()),
            Map.entry("typ", typ),
            Map.entry("ver", CURRENT_VERSION),
            Map.entry("role", role)
        );
    }

    public abstract Map<String, ?> getClaims();
}
