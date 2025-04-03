package rs.banka4.stock_service.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.dtos.SecurityResponse;
import rs.banka4.stock_service.domain.security.dtos.TotalProfitResponse;
import rs.banka4.stock_service.domain.security.forex.dtos.ForexPairDto;
import rs.banka4.stock_service.domain.security.future.dtos.FutureDto;
import rs.banka4.stock_service.domain.security.stock.dtos.StockDto;

import java.util.List;

public interface SecuritiesApiDocumentation {

    @Operation(
        summary = "Search Securities",
        description = "Retrieves securities based on the security type and name filter. User can see securities "
            + "that are allowed for his role.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved securities",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(anyOf = {StockDto.class, FutureDto.class, ForexPairDto.class})
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    ResponseEntity<Page<SecurityDto>> getSecurities(
        @Parameter(description = "Type of security to filter by") String securityType,
        @Parameter(description = "Name of the security") String name,
        @Parameter(description = "Page number") int page,
        @Parameter(description = "Number of securities per page") int size
    );

    @Operation(
        summary = "Get User Securities",
        description = """
            Retrieves all securities currently owned by the authenticated user with financial metrics.

            **Required Roles:**
            - ACTUARY

            Returns empty array if no holdings exist.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user securities",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = SecurityResponse.class))
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - User lacks required role",
                content = @Content(schema = @Schema(hidden = true))
            )
        }
    )
    ResponseEntity<List<SecurityResponse>> getMySecurities(Authentication auth);

    @Operation(
        summary = "Get Portfolio Profit",
        description = """
        Calculates total unrealized profit for stock holdings converted to account currency

        **Business Rules:**
        - Only considers stock positions
        - Converts all profits to account's primary currency
        - Uses latest available exchange rates
        - Requires FOREX rates for all held currencies

        **Required Roles:**
        - CLIENT
        - ACTUARY
        """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully calculated total profit",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TotalProfitResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "424",
                description = "Failed dependency - Missing FOREX rates",
                content = @Content(schema = @Schema(hidden = true))
            )
        }
    )
    ResponseEntity<TotalProfitResponse> getTotalProfit(Authentication auth);
}
