package rs.banka4.user_service.unit.loan;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.InterestRate;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.dtos.LoanApplicationDto;
import rs.banka4.user_service.domain.user.client.db.Client;
import rs.banka4.user_service.exceptions.account.AccountNotActive;
import rs.banka4.user_service.exceptions.account.AccountNotFound;
import rs.banka4.user_service.exceptions.account.NotAccountOwner;
import rs.banka4.user_service.exceptions.user.client.ClientNotFound;
import rs.banka4.user_service.generator.LoanObjectMother;
import rs.banka4.user_service.repositories.InterestRateRepository;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.repositories.LoanRequestRepository;
import rs.banka4.user_service.service.abstraction.AccountService;
import rs.banka4.user_service.service.abstraction.ClientService;
import rs.banka4.user_service.service.abstraction.JwtService;
import rs.banka4.user_service.service.impl.LoanServiceImpl;
import rs.banka4.user_service.service.impl.UserService;
import rs.banka4.user_service.utils.loans.LoanRateUtil;

@ExtendWith(MockitoExtension.class)
public class CreateLoanTests {
    @Mock
    private JwtService jwtService;

    @Mock
    private LoanRateUtil loanRateUtil;

    @Mock
    private InterestRateRepository interestRateRepository;

    @Mock
    private LoanRequestRepository loanRequestRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private AccountService accountService;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Mock
    private UserService userService;

    private LoanApplicationDto loanApplicationDto;
    private Client client;
    private Account account;

    @BeforeEach
    void setUp() {
        loanApplicationDto = LoanObjectMother.generateLoanApplicationDto();
        client = new Client();
        UUID clientId = UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924");
        client.setId(clientId);
        client.setEmail("test@example.com");
        client.setFirstName("OldFirstName");
        client.setLastName("OldLastName");

        UUID accountId = UUID.randomUUID();

        account = new Account();
        account.setId(accountId);
        account.setClient(client);
        account.setAccountNumber("444394438340549");
        account.setActive(true);
    }

    @Test
    void createLoanApplication_Success() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));
        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(loanRepository.save(any(Loan.class))).thenReturn(new Loan());
        when(
            interestRateRepository.findByAmountAndDate(BigDecimal.valueOf(1000.0), LocalDate.now())
        ).thenReturn(Optional.of(new InterestRate()));
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);


        assertDoesNotThrow(
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );
    }

    @Test
    void createLoanApplication_ClientNotFound() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.empty());

        assertThrows(
            ClientNotFound.class,
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );
    }

    @Test
    void connectAccountToLoan_AccountNotFound() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));
        when(accountService.getAccountByAccountNumber("444394438340549")).thenThrow(
            new AccountNotFound()
        );
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);


        assertThrows(
            AccountNotFound.class,
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );
    }

    @Test
    void connectAccountToLoan_AccountNotActive() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));

        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);


        account.setActive(false);

        assertThrows(
            AccountNotActive.class,
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );
    }

    @Test
    void connectAccountToLoan_Success() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));

        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);


        when(
            interestRateRepository.findByAmountAndDate(BigDecimal.valueOf(1000.0), LocalDate.now())
        ).thenReturn(Optional.of(new InterestRate()));

        assertDoesNotThrow(
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setAmount(loanApplicationDto.amount());
        loan.setInterestType(loanApplicationDto.interestType());

        assertEquals(loanApplicationDto.amount(), loan.getAmount());
        assertEquals(loanApplicationDto.interestType(), loan.getInterestType());
        assertEquals(
            loanApplicationDto.accountNumber(),
            loan.getAccount()
                .getAccountNumber()
        );
    }

    @Test
    void connectAccountToLoan_notCorrectClient() {
        Client client = new Client();
        client.setEmail("test@test.ru");
        UUID clientId = UUID.randomUUID();
        client.setId(clientId);
        when(jwtService.extractUserId(any())).thenReturn(clientId);
        when(clientService.findClientById(clientId)).thenReturn(Optional.of(client));

        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);

        account.getClient()
            .setEmail("aaaa@example.com");

        assertThrows(
            NotAccountOwner.class,
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );
    }

    @Test
    void generateLoanNumber_Success() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));

        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);

        when(
            interestRateRepository.findByAmountAndDate(BigDecimal.valueOf(1000.0), LocalDate.now())
        ).thenReturn(Optional.of(new InterestRate()));

        Loan loan = new Loan();
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        loan.setLoanNumber(321432L);

        assertDoesNotThrow(
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );

        assertNotNull(loan.getLoanNumber());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void generateLoanNumber_DuplicateHandling() {
        when(jwtService.extractUserId(anyString())).thenReturn(
            UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")
        );
        when(clientService.findClientById(UUID.fromString("4ba6cdd2-33c1-4573-ad1e-1a783d3b3924")))
            .thenReturn(Optional.of(client));

        when(accountService.getAccountByAccountNumber("444394438340549")).thenReturn(account);
        when(userService.isPhoneNumberValid(anyString())).thenReturn(true);

        Loan loan = new Loan();
        when(loanRepository.save(any(Loan.class))).thenThrow(
            new DataIntegrityViolationException("Duplicate")
        )
            .thenReturn(loan);

        when(
            interestRateRepository.findByAmountAndDate(BigDecimal.valueOf(1000.0), LocalDate.now())
        ).thenReturn(Optional.of(new InterestRate()));

        loan.setLoanNumber(321432L);

        assertDoesNotThrow(
            () -> loanService.createLoanApplication(loanApplicationDto, anyString())
        );

        assertNotNull(loan.getLoanNumber());

        verify(loanRepository, atLeast(2)).save(any(Loan.class));
    }

}
