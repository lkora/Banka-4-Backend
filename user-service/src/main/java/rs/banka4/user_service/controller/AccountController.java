package rs.banka4.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.user_service.dto.AccountDto;
import rs.banka4.user_service.service.abstraction.AccountService;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Tag(name = "AccountController", description = "Endpoints for accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Get Client Accounts by ID",
            description = "Retrieves the list of accounts for the authenticated client using a provided client ID. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id, Authentication auth) {
        return this.accountService.getAccount(auth.getCredentials().toString(), id);
    }

    @Operation(
            summary = "Get Client Accounts",
            description = "Retrieves the list of accounts for the authenticated client using token. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )
    @GetMapping("/")
    public ResponseEntity<List<AccountDto>> getAccountsForClient(Authentication auth) {
        return this.accountService.getAccountsForClient(auth.getCredentials().toString());
    }


    @Operation(
            summary = "Get Client Accounts",
            description = "Retrieves the list of accounts for the authenticated client using token. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                            content = @Content(schema = @Schema(implementation = AccountDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Token errors"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
            }
    )


    @GetMapping("/recipients")
    public ResponseEntity<List<AccountDto>> getRecentRecipients(Authentication auth) {
        return this.accountService.getRecentRecipientsFor(auth.getCredentials().toString());
    }
}