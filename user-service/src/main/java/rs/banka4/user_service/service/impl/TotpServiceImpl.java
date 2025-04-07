package rs.banka4.user_service.service.impl;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.domain.user.User;
import rs.banka4.user_service.exceptions.authenticator.NoTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotActiveTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.abstraction.TotpService;

@Service
@RequiredArgsConstructor
public class TotpServiceImpl implements TotpService {

    private static final int SECRET_LENGTH = 32;
    private static final int TIME_PERIOD = 30;
    private static final int CODE_LENGTH = 6;
    private static final HashingAlgorithm ALGORITHM = HashingAlgorithm.SHA1;

    private final JwtService jwtService;
    private final UserTotpSecretRepository repository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void verifyNewAuthenticator(Authentication auth, String code) {
        /* TODO(arsen): move cast outside */
        final var userId =
            ((AuthenticatedBankUserAuthentication) auth).getPrincipal()
                .userId();
        UserTotpSecret totp = getTotpSecretByUserId(userId);
        validateTotpCode(totp, code);
        totp.setIsActive(true);
        repository.save(totp);
    }

    @Override
    public boolean validate(String authorization, String code) {
        final var userId = jwtService.extractUserId(authorization);
        UserTotpSecret totp = getTotpSecretByUserId(userId);
        if (!totp.getIsActive()) {
            throw new NotActiveTotpException();
        }
        return isCodeValid(totp.getSecret(), code);
    }

    @Override
    public RegenerateAuthenticatorResponseDto regenerateSecret(Authentication auth) {
        /* TODO(arsen): move cast outside */
        final var userId =
            ((AuthenticatedBankUserAuthentication) auth).getPrincipal()
                .userId();

        SecretGenerator secretGenerator = new DefaultSecretGenerator(SECRET_LENGTH);
        String newSecret = secretGenerator.generate();

        final var client = clientRepository.findById(userId);
        final var employee = employeeRepository.findById(userId);
        final var email =
            client.map(x -> (User) /* Java moment. */ x)
                .or(() -> employee)
                .map(User::getEmail)
                .orElseThrow(NotFound::new);

        final var secret =
            repository.findByClient_Id(userId)
                .or(() -> repository.findByEmployee_Id(userId))
                .orElseGet(
                    () -> new UserTotpSecret(
                        null,
                        newSecret,
                        client.orElse(null),
                        employee.orElse(null),
                        false
                    )
                );
        secret.setSecret(newSecret);
        secret.setIsActive(false);
        repository.save(secret);

        return new RegenerateAuthenticatorResponseDto(
            createTotpUrl("RAFeisen", email, newSecret),
            newSecret
        );
    }

    @Override
    public boolean verifyClient(Authentication authentication, String otpCode) {
        return validate(
            authentication.getCredentials()
                .toString(),
            otpCode
        );
    }

    @Override
    public String generateCode(String authorization) {
        final var userId = jwtService.extractUserId(authorization);
        UserTotpSecret totp = getTotpSecretByUserId(userId);
        return generateCodeFromSecret(totp.getSecret());
    }


    // --- Private helper methods ---

    private String generateCodeFromSecret(String secret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        long currentBucket = timeProvider.getTime() / TIME_PERIOD;
        try {
            return codeGenerator.generate(secret, currentBucket);
        } catch (CodeGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCodeValid(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(ALGORITHM);
        DefaultCodeVerifier validator = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return validator.isValidCode(secret, code);
    }

    private void validateTotpCode(UserTotpSecret totp, String code) {
        if (!isCodeValid(totp.getSecret(), code)) {
            throw new NotValidTotpException();
        }
    }

    private UserTotpSecret getTotpSecretByUserId(UUID userId) {
        return repository.findByClient_Id(userId)
            .or(() -> repository.findByEmployee_Id(userId))
            .orElseThrow(NoTotpException::new);
    }

    private String createTotpUrl(String issuer, String email, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
            issuer,
            email,
            secret,
            issuer,
            ALGORITHM,
            CODE_LENGTH,
            TIME_PERIOD
        );
    }
}
