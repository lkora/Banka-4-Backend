package rs.banka4.stock_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.banka4.stock_service.controller.docs.SecuritiesApiDocumentation;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.dtos.SecurityResponse;
import rs.banka4.stock_service.domain.security.dtos.TotalProfitResponse;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/securities")
@RequiredArgsConstructor
public class SecuritiesController implements SecuritiesApiDocumentation {

    private final SecuritiesService securityService;

    @Override
    @GetMapping
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        @RequestParam(required = false) String securityType,
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return securityService.getSecurities(securityType, name, PageRequest.of(page, size));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<SecurityResponse>> getMySecurities(
        Authentication auth
    ) {
        List<SecurityResponse> response = securityService.getMySecurities(auth);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/profit")
    public ResponseEntity<TotalProfitResponse> getTotalProfit(Authentication auth) {
        return ResponseEntity.ok(securityService.calculateTotalProfit(auth));
    }


}
