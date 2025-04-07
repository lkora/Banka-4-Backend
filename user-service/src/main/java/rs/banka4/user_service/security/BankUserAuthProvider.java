package rs.banka4.user_service.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;

@RequiredArgsConstructor
@Component
public class BankUserAuthProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankUserAuthProvider.class);
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication maybeAuth) throws AuthenticationException {
        /* Should be guaranteed by #supports. */
        if (!(maybeAuth instanceof PreAuthBankUserAuthentication auth))
            throw new IllegalArgumentException(
                "BankUserAuthProvider only takes PreAuthBankUserAuthentication tokens"
            );

        final var principal = auth.getPrincipal();
        final var maybeUser = switch (principal.userType()) {
        /* DO NOT ADD A DEFAULT CASE. */
        case CLIENT -> clientRepository.findByEmail(principal.email());
        case EMPLOYEE -> employeeRepository.findByEmail(principal.email());
        };
        if (!maybeUser.isPresent()) {
            LOGGER.debug("Failed to find user {} by email", principal);
            throw new UsernameNotFoundException("User %s not found".formatted(principal));
        }
        final var user = maybeUser.get();

        /*
         * TODO(arsen): Need to deal with "timing attacks", and to allow password upgrading, if
         * anyone cares. I doubt anyone does, to be frank.
         */
        if (!passwordEncoder.matches(auth.getCredentials(), user.getPassword())) {
            LOGGER.debug("User {} provided incorrect password", principal);
            throw new BadCredentialsException("Incorrect password");
        }

        return new AuthenticatedBankUserAuthentication(
            new AuthenticatedBankUserPrincipal(principal.userType(), user.getId()),
            null,
            user.getPrivileges()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthBankUserAuthentication.class.equals(authentication);
    }

}
