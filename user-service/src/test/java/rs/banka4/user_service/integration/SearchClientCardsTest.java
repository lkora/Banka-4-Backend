package rs.banka4.user_service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import rs.banka4.testlib.integration.DbEnabledTest;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
import rs.banka4.user_service.repositories.AccountRepository;
import rs.banka4.user_service.repositories.ClientRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class SearchClientCardsTest {

    @Autowired
    private MockMvcTester m;

    @Autowired
    private UserGenerator userGen;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private AccountRepository accountRepo;

    private String accessToken;

    @BeforeEach
    void setUp() {
        Account account = testDataSeeder.seedAccount();
        testDataSeeder.seedActiveCard(account);

        userGen.createClient(x -> x);
        account.setClient(
            clientRepo.findByEmail("john.doe@example.com")
                .orElseThrow()
        );
        accountRepo.save(account);
        var toks = userGen.doClientLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();
    }

    @Test
    void clientSearchCardsSuccessfully() throws Exception {
        m.get()
            .uri("/cards/client/search")
            .header("Authorization", "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            /*
             * Be lenient. We're testing that the array is size one, and that we're looking at page
             * three.
             */
            .isLenientlyEqualTo(
                "testdata/SearchClientCardsTest_clientSearchCardsSuccessfully.json"
            );
    }

    @Test
    void clientSearchCardsAccountNumberFilterSuccessfully() throws Exception {
        m.get()
            .uri("/cards/client/search?accountNumber=123456789")
            .header("Authorization", "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatusOk()
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .bodyJson()
            /*
             * Be lenient. We're testing that the array is size one, and that we're looking at page
             * three.
             */
            .isLenientlyEqualTo(
                "testdata/SearchClientCardsTest_clientSearchCardsSuccessfully.json"
            );
    }

    @Test
    void clientSearchCards_failsWhenNotOwnerOfAccount() {
        String fakeAccountNumber = "9999999999";

        m.get()
            .uri("/cards/client/search?accountNumber=" + fakeAccountNumber)
            .header("Authorization", "Bearer " + accessToken)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN);
    }

}
