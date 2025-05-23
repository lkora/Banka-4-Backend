package rs.banka4.user_service.service.mock.generators;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;
import rs.banka4.rafeisen.common.currency.CurrencyCode;
import rs.banka4.rafeisen.common.dto.Gender;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.user_service.domain.account.db.Account;
import rs.banka4.user_service.domain.account.db.AccountType;
import rs.banka4.user_service.domain.account.dtos.AccountClientIdDto;
import rs.banka4.user_service.domain.account.dtos.AccountDto;
import rs.banka4.user_service.domain.account.dtos.AccountTypeDto;
import rs.banka4.user_service.domain.account.dtos.CreateAccountDto;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.domain.currency.mapper.CurrencyMapper;
import rs.banka4.user_service.domain.user.client.dtos.ClientDto;
import rs.banka4.user_service.domain.user.employee.dtos.EmployeeDto;

public class AccountObjectMother {

    public static CreateAccountDto generateBasicCreateAccountDto() {
        return new CreateAccountDto(
            new AccountClientIdDto(
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "john.doe@example.com",
                "+381667891125",
                "123 Grove Street, City, Country",
                EnumSet.noneOf(Privilege.class)
            ),
            null,
            BigDecimal.valueOf(1000.00),
            CurrencyCode.Code.RSD,
            false
        );
    }

    public static AccountDto generateBasicAccountDto() {
        return new AccountDto(
            UUID.randomUUID()
                .toString(),
            "444394438340549",
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(800.00),
            BigDecimal.valueOf(100.00),
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2028, 1, 1),
            true,
            AccountTypeDto.CheckingPersonal,
            BigDecimal.valueOf(100.00),
            BigDecimal.valueOf(1000.00),
            CurrencyMapper.INSTANCE.toDto(
                Currency.builder()
                    .code(CurrencyCode.Code.RSD)
                    .build()
            ),
            new EmployeeDto(
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "mehmedalija.doe@example.com",
                "+381656789012",
                "123 Main St",
                "Mahd",
                "Developer",
                "IT",
                true
            ),
            new ClientDto(
                UUID.randomUUID(),
                "Jane",
                "Doe",
                LocalDate.of(1990, 1, 1),
                Gender.FEMALE,
                "jane.doe@example.com",
                "+381645678901",
                "123 Main St",
                EnumSet.noneOf(Privilege.class),
                false
            ),
            null
        );
    }

    public static Account generateBasicFromAccount() {
        Account account = new Account();
        account.setAccountNumber("444394438340549");
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setAvailableBalance(BigDecimal.valueOf(8000.00));
        account.setActive(true);
        account.setAccountType(AccountType.STANDARD);
        account.setDailyLimit(BigDecimal.valueOf(1000.00));
        account.setMonthlyLimit(BigDecimal.valueOf(10000.00));
        account.setCurrency(
            new Currency(
                CurrencyCode.Code.RSD,
                "Serbian Dinar",
                "RSD",
                "Serbian Dinar currency",
                true
            )
        );
        account.setEmployee(EmployeeObjectMother.generateBasicEmployee());
        account.setClient(
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec97"),
                "markezaa@example.com"
            )
        );
        return account;
    }

    public static Account generateBasicToAccount() {
        Account account = new Account();
        account.setAccountNumber("444394438340523");
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setAvailableBalance(BigDecimal.valueOf(8000.00));
        account.setActive(true);
        account.setAccountType(AccountType.STANDARD);
        account.setDailyLimit(BigDecimal.valueOf(1000.00));
        account.setMonthlyLimit(BigDecimal.valueOf(10000.00));
        account.setCurrency(
            new Currency(
                CurrencyCode.Code.RSD,
                "Serbian Dinar",
                "RSD",
                "Serbian Dinar currency",
                true
            )
        );
        account.setEmployee(EmployeeObjectMother.generateBasicEmployee());
        account.setClient(
            ClientObjectMother.generateClient(
                UUID.fromString("9df5e618-f21d-48a7-a7a4-ac55ea8bec93"),
                "zorz@example.com"
            )
        );
        return account;
    }

}
