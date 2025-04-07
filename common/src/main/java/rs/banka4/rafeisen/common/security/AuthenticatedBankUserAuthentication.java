package rs.banka4.rafeisen.common.security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * An authentication instance representing a user previously authenticated via a JWT token (if
 * {@link #getCredentials} returns non-{@code null}), or freshly authenticated (otherwise).
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class AuthenticatedBankUserAuthentication extends AbstractAuthenticationToken {
    private final AuthenticatedBankUserPrincipal principal;
    private final String token;

    /**
     * @param principal Principal of the authenticated user.
     * @param token If derived from a token, the raw token this was derived from. Otherwise,
     *        {@code null}.
     * @param privileges Privileges granted to this token.
     */
    public AuthenticatedBankUserAuthentication(
        AuthenticatedBankUserPrincipal principal,
        String token,
        EnumSet<Privilege> privileges
    ) {
        super(calculatePrivilegesAndRole(principal, privileges));
        super.setAuthenticated(true);
        this.principal = principal;
        this.token = token;
    }

    private static Collection<? extends GrantedAuthority> calculatePrivilegesAndRole(
        AuthenticatedBankUserPrincipal principal,
        EnumSet<Privilege> privileges
    ) {
        return Stream.concat(
            Stream.of(
                principal.userType()
                    .asAuthority()
            ),
            privileges.stream()
                .map(SecurityUtils::asGrantedAuthority)
        )
            .toList();
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public AuthenticatedBankUserPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated)
            throw new IllegalArgumentException(
                /* TODO(arsen): remove? */
                "Changing the authenticated status to true seems suspect"
            );
        super.setAuthenticated(false);
    }
}
