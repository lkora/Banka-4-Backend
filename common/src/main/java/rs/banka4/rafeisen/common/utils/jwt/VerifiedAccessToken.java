package rs.banka4.rafeisen.common.utils.jwt;

import io.jsonwebtoken.Claims;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(
    level = AccessLevel.PRIVATE,
    makeFinal = true
)
public class VerifiedAccessToken extends VerifiedToken {
    /** Permissions the user has. */
    private final EnumSet<Privilege> privileges;

    /**
     * Construct a fresh access token for a given user with some privileges and roles.
     */
    public VerifiedAccessToken(UserType role, UUID userId, EnumSet<Privilege> privileges) {
        super(role, userId, JwtTokenType.ACCESS);
        this.privileges = privileges;
    }

    /**
     * Given {@code claims}, construct an access token populated with information therein.
     */
    public VerifiedAccessToken(Claims claims) throws JwtParseFailed {
        super(claims, JwtTokenType.ACCESS);
        try {
            final var privClaim = claims.get("privileges");
            if (
                privClaim instanceof List<?> privileges
                    && privileges.stream()
                        .allMatch(x -> x instanceof String)
            ) {

                this.privileges =
                    privileges.isEmpty()
                        ? EnumSet.noneOf(Privilege.class)
                        : EnumSet.copyOf(
                            privileges.stream()
                                .map(x -> (String) x)
                                .map(Privilege::valueOf)
                                .collect(Collectors.toSet())
                        );
            } else {
                throw new IllegalArgumentException("Privilege not a list of strings");
            }
        } catch (Exception e) {
            throw new JwtParseFailed("Invalid token privileges", e);
        }
    }

    @Override
    public Map<String, ?> getClaims() {
        final var result = new HashMap<String, Object>();
        result.putAll(super.populateBaseClaims());
        if (result.put("privileges", privileges) != null)
            throw new AssertionError("privileges in base token??");
        return Collections.unmodifiableMap(result);
    }
}
