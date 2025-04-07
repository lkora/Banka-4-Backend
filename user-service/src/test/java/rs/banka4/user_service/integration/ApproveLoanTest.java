package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.loan.db.Loan;
import rs.banka4.user_service.domain.loan.db.LoanStatus;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
import rs.banka4.user_service.repositories.LoanRepository;
import rs.banka4.user_service.service.abstraction.JwtService;


@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class ApproveLoanTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Qualifier("jwtServiceImpl")
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    private Loan loan;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Account account = testDataSeeder.seedAccount();
        loan = testDataSeeder.seedLoan(account);

        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @Disabled("Disabled due to unexpected behaviour with the main branch compared to old forks")
    @Test
    void approveLoanSuccessfully() throws Exception {
        m.put()
            .uri("/loans/approve/" + loan.getLoanNumber())
            .header("Authorization", "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.OK);

        Loan approvedLoan =
            loanRepository.findByLoanNumber(loan.getLoanNumber())
                .orElseThrow();
        assertThat(approvedLoan.getStatus()).isEqualTo(LoanStatus.APPROVED);
    }
}
