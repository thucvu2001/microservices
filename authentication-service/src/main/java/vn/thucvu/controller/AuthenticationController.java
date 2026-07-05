package vn.thucvu.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.controller.request.SignInRequest;
import vn.thucvu.controller.response.TokenResponse;
import vn.thucvu.controller.response.VerifyTokenResponse;
import vn.thucvu.service.AuthenticationService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Access token", description = "Get access token and refresh token by username and password")
    @PostMapping("/access-token")
    public TokenResponse accessToken(@Valid @RequestBody SignInRequest request) {
        log.info("Access token request");

        // Validate for mobile and mini app
        request.validate();

        return authenticationService.getAccessToken(request);
    }

    @Operation(summary = "Refresh token", description = "Get access token by refresh token")
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(@Valid @RequestBody String refreshToken) {
        log.info("Refresh token request");

        return authenticationService.getRefreshToken(refreshToken);
    }

    @Operation(summary = "Verify access token", description = "Verify JWT for access token")
    @PostMapping("/verify-token")
    public VerifyTokenResponse verifyAccessToken(@RequestBody String token) {
        log.info("Verify access token request");

        return authenticationService.getVerifyToken(token);
    }
}