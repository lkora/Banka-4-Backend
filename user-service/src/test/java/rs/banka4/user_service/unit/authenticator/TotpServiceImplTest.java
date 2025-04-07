package rs.banka4.user_service.unit.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserAuthentication;
import rs.banka4.rafeisen.common.security.AuthenticatedBankUserPrincipal;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;
import rs.banka4.user_service.domain.authenticator.dtos.RegenerateAuthenticatorResponseDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.authenticator.NotActiveTotpException;
import rs.banka4.user_service.exceptions.authenticator.NotValidTotpException;
import rs.banka4.user_service.exceptions.user.NotFound;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.repositories.EmployeeRepository;
import rs.banka4.user_service.repositories.UserTotpSecretRepository;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.TotpServiceImpl;

public class TotpServiceImplTest {
    private JwtService jwtService;
    private UserTotpSecretRepository repository;
    private ClientRepository clientRepository;
    private EmployeeRepository employeeRepository;
    private TotpServiceImpl totpService;

    private final String testSecret = "TEST_SECRET_1234567890";

    @BeforeEach
    void setUp() throws Exception {
        totpService =
            new TotpServiceImpl(
                jwtService = mock(JwtService.class),
                repository = mock(UserTotpSecretRepository.class),
                clientRepository = mock(ClientRepository.class),
                employeeRepository = mock(EmployeeRepository.class)
            );
    }

    private AuthenticatedBankUserAuthentication makeAuthentication(UserType userType) {
        return new AuthenticatedBankUserAuthentication(
            new AuthenticatedBankUserPrincipal(
                userType,
                UUID.fromString("2366c6cb-5598-4872-8271-c968af8e3326")
            ),
            "token",
            EnumSet.noneOf(Privilege.class)
        );
    }

    // ------------------------- verifyNewAuthenticator Tests -------------------------
    @Test
    @Ignore
    void verifyNewAuthenticator_validCode_activatesAndSaves() throws Exception {
        // Arrange
        final var auth = makeAuthentication(UserType.CLIENT);
        Client client =
            ClientObjectMother.generateClient(UUID.randomUUID(), "5ujtruje@example.com");
        when(jwtService.extractRole(any())).thenReturn("client");
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(jwtService.extractUserId(any())).thenReturn(UUID.randomUUID());
        UserTotpSecret totpSecret =
            new UserTotpSecret(UUID.randomUUID(), testSecret, client, null, false);
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(totpSecret));

        String validCode = generateCodeForSecret(testSecret);

        // Act
        totpService.verifyNewAuthenticator(auth, validCode);

        // Assert
        assertThat(totpSecret.getIsActive()).isTrue();
        verify(repository).save(totpSecret);
    }

    @Test
    @Ignore
    void verifyNewAuthenticator_invalidCode_throwsException() {
        // Arrange
        final var auth = makeAuthentication(UserType.CLIENT);
        UserTotpSecret totpSecret =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, false);
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(totpSecret));

        // Act & Assert
        assertThatThrownBy(() -> totpService.verifyNewAuthenticator(auth, "INVALID_CODE"))
            .isInstanceOf(NotValidTotpException.class);
        verify(repository, never()).save(any());
    }

    // ------------------------- validate Tests -------------------------
    @Test
    void validate_validCodeAndActive_returnsTrue() throws Exception {
        // Arrange
        UserTotpSecret activeTotp =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, true);
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(activeTotp));
        String validCode = generateCodeForSecret(testSecret);

        // Act & Assert
        assertThat(totpService.validate("Bearer validToken", validCode)).isTrue();
    }

    @Test
    void validate_inactive_throwsException() {
        // Arrange
        UserTotpSecret inactiveTotp =
            new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, false);
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(inactiveTotp));

        // Act & Assert
        assertThatThrownBy(() -> totpService.validate("Bearer token", "anyCode")).isInstanceOf(
            NotActiveTotpException.class
        );
    }

    // ------------------------- regenerateSecret Tests -------------------------
    @Test
    void regenerateSecret_clientExistingSecret_updatesSecret() {
        // Arrange
        final var auth = makeAuthentication(UserType.CLIENT);
        Client client =
            ClientObjectMother.generateClient(UUID.randomUUID(), "5ujtruje@example.com");
        UserTotpSecret existingSecret =
            new UserTotpSecret(UUID.randomUUID(), "OLD_SECRET", client, null, true);
        when(clientRepository.findById(any())).thenReturn(Optional.of(client));
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(existingSecret));

        // Act
        RegenerateAuthenticatorResponseDto response = totpService.regenerateSecret(auth);

        // Assert
        assertThat(response.tokenSecret()).hasSize(32); // DefaultSecretGenerator length
        assertThat(existingSecret.getSecret()).isNotEqualTo("OLD_SECRET");
        assertThat(existingSecret.getIsActive()).isFalse();
        verify(repository).save(existingSecret);
    }

    @Test
    void regenerateSecret_noUser_throwsNotFound() {
        // Arrange
        final var auth = makeAuthentication(UserType.CLIENT);
        when(clientRepository.findById(any())).thenReturn(Optional.empty());
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> totpService.regenerateSecret(auth)).isInstanceOf(NotFound.class);
    }

    // ------------------------- generateCode Tests -------------------------
    @Test
    void generateCode_validToken_returnsCode() throws Exception {
        // Arrange
        UserTotpSecret totp = new UserTotpSecret(UUID.randomUUID(), testSecret, null, null, true);
        when(repository.findByClient_Id(any())).thenReturn(Optional.of(totp));

        // Act
        String code = totpService.generateCode("Bearer validToken");

        // Assert
        String expectedCode = generateCodeForSecret(testSecret);
        assertThat(code).isEqualTo(expectedCode);
    }

    // ------------------------- Helper Methods -------------------------
    private String generateCodeForSecret(String secret) throws Exception {
        Method method =
            TotpServiceImpl.class.getDeclaredMethod("generateCodeFromSecret", String.class);
        method.setAccessible(true);
        return (String) method.invoke(totpService, secret);
    }
}
