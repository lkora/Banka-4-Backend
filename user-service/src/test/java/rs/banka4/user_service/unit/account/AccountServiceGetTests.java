package rs.banka4.user_service.unit.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.user.IncorrectCredentials;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.generator.AccountObjectMother;
import rs.banka4.user_service.generator.ClientObjectMother;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.AccountServiceImpl;

public class AccountServiceGetTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private JwtService jwtService;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAccountsForClientSuccess() {
        // Arrange
        String token = "authToken";
        String email = "client@example.com";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), email);
        Account account = AccountObjectMother.generateBasicFromAccount();
        account.setClient(client);
        Set<Account> accounts = Set.of(account);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientService.findClientById(client.getId())).thenReturn(Optional.of(client));
        when(accountRepository.findAllByClient(client)).thenReturn(accounts);

        // Act
        Set<AccountDto> result = accountService.getAccountsForClient(token);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        AccountDto accountDto =
            result.iterator()
                .next();
        assertEquals(account.getAccountNumber(), accountDto.accountNumber());
        assertEquals(account.getBalance(), accountDto.balance());
        assertEquals(account.getAvailableBalance(), accountDto.availableBalance());
        assertTrue(accountDto.active());
    }

    @Test
    void testGetAccountsForClientClientNotFound() {
        // Arrange
        String token = "authToken";
        UUID clientId = UUID.randomUUID();

        when(jwtService.extractUserId(token)).thenReturn(clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        when(clientService.findClientById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClientNotFound.class, () -> accountService.getAccountsForClient(token));
    }

    @Test
    void testGetAccountSuccess() {
        // Arrange
        String token = "authToken";
        String email = "client@example.com";
        String accountNumber = "123456789";
        Client client = ClientObjectMother.generateClient(UUID.randomUUID(), email);
        Account account = AccountObjectMother.generateBasicFromAccount();
        account.setClient(client);
        account.setAccountNumber(accountNumber);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientService.findClientById(client.getId())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(
            Optional.of(account)
        );

        // Act
        AccountDto result = accountService.getAccount(token, accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.accountNumber());
        assertEquals(account.getBalance(), result.balance());
        assertEquals(account.getAvailableBalance(), result.availableBalance());
        assertTrue(result.active());
    }

    @Test
    void testGetAccountNotFound() {
        // Arrange
        String token = "authToken";
        String accountNumber = "123456789";

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(AccountNotFound.class, () -> accountService.getAccount(token, accountNumber));
    }

    @Test
    void testGetAccountIncorrectCredentials() {
        // Arrange
        String token = "authToken";
        String accountNumber = "123456789";
        Client client = new Client();
        Client client2 = new Client();
        client.setEmail("other@example.com");
        client2.setEmail("other2@example.com");
        Account account = new Account();
        account.setClient(client2);

        when(jwtService.extractUserId(token)).thenReturn(client.getId());
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(clientService.findClientById(client.getId())).thenReturn(Optional.of(client));
        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(
            Optional.of(account)
        );

        // Act & Assert
        assertThrows(
            IncorrectCredentials.class,
            () -> accountService.getAccount(token, accountNumber)
        );
    }

    @Test
    void testGetAccountByAccountNumberSuccess() {
        // Arrange
        String accountNumber = "123456789";
        Account account = new Account();
        account.setAccountNumber(accountNumber);

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(
            Optional.of(account)
        );

        // Act
        Account result = accountService.getAccountByAccountNumber(accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
    }

    @Test
    void testGetAccountByAccountNumberNotFound() {
        // Arrange
        String accountNumber = "123456789";

        when(accountRepository.findAccountByAccountNumber(accountNumber)).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(
            AccountNotFound.class,
            () -> accountService.getAccountByAccountNumber(accountNumber)
        );
    }

}
