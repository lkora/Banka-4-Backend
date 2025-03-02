package rs.banka4.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.models.AccountType;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Override
    public ResponseEntity<List<AccountDto>> getAccountsForClient(UUID clientId) {
        // Create some mocked account data
        AccountDto account1 = new AccountDto(
                UUID.fromString("11111111-2222-3333-4444-555555555555"),
                "1234567890",
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                new BigDecimal("100.00"),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2028, 1, 1),
                true,
                AccountType.SAVINGS,
                new BigDecimal("100.00"),
                new BigDecimal("1000.00")
        );

        AccountDto account2 = new AccountDto(
                UUID.fromString("22222222-3333-4444-5555-666666666666"),
                "0987654321",
                new BigDecimal("5000.00"),
                new BigDecimal("4500.00"),
                BigDecimal.ZERO, // Assuming maintenance is not applied here
                LocalDate.of(2022, 6, 15),
                LocalDate.of(2027, 6, 15),
                true,
                AccountType.AD,
                new BigDecimal("200.00"),
                new BigDecimal("5000.00")
        );

        List<AccountDto> accounts = List.of(account1, account2);
        return ResponseEntity.ok(accounts);
    }
}
