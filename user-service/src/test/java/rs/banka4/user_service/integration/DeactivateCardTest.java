package rs.banka4.user_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import rs.banka4.user_service.domain.card.db.Card;
import rs.banka4.user_service.domain.card.db.CardStatus;
import rs.banka4.user_service.integration.generator.UserGenerator;
import rs.banka4.user_service.integration.seeder.TestDataSeeder;
import rs.banka4.user_service.repositories.CardRepository;
import rs.banka4.user_service.service.abstraction.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@DbEnabledTest
public class DeactivateCardTest {

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
    private CardRepository cardRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    private Account account;
    private Card card;
    private String accessToken;

    @BeforeEach
    void setUp() {
        account = testDataSeeder.seedAccount();
        userGen.createEmployee(x -> x);
        var toks = userGen.doEmployeeLogin("john.doe@example.com", "test");
        accessToken = toks.accessToken();

        card = testDataSeeder.seedActiveCard(account);
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
    }

    @Test
    void deactivateCardSuccessfully() throws Exception {
        m.put()
            .uri("/cards/deactivate/" + card.getCardNumber())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.OK);

        Card deactivatedCard =
            cardRepository.findById(card.getId())
                .orElse(null);
        assertThat(deactivatedCard).isNotNull();
        assertThat(deactivatedCard.getCardStatus()).isEqualTo(CardStatus.DEACTIVATED);
    }


    @Test
    void deactivateCardUnsuccessfullyBecauseOfNonExistentCard() throws Exception {
        String nonExistentCardNumber = "0000000000000000";
        m.put()
            .uri("/cards/deactivate/" + nonExistentCardNumber)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deactivateCardUnsuccessfullyBecauseItIsAlreadyDeactivated() throws Exception {
        card.setCardStatus(CardStatus.DEACTIVATED);
        cardRepository.save(card);
        m.put()
            .uri("/cards/deactivate/" + card.getCardNumber())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
