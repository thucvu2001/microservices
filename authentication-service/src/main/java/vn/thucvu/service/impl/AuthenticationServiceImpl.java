package vn.thucvu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.thucvu.controller.request.SignInRequest;
import vn.thucvu.controller.response.TokenResponse;
import vn.thucvu.controller.response.VerifyTokenResponse;
import vn.thucvu.exception.ValidationException;
import vn.thucvu.model.PermissionHash;
import vn.thucvu.model.User;
import vn.thucvu.repository.PermissionRepository;
import vn.thucvu.repository.UserRepository;
import vn.thucvu.service.AuthenticationService;
import vn.thucvu.service.JwtService;

import java.util.ArrayList;
import java.util.List;

import static vn.thucvu.common.TokenType.ACCESS_TOKEN;
import static vn.thucvu.common.TokenType.REFRESH_TOKEN;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        log.info("Get access token");

        List<String> authorities = new ArrayList<>();
        try {
            // Thực hiện xác thực với username và password
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            log.info("isAuthenticated = {}", authenticate.isAuthenticated());
            log.info("Authorities: {}", authenticate.getAuthorities().toString());
            authorities.add(authenticate.getAuthorities().toString());

            // Nếu xác thực thành công, lưu thông tin vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (BadCredentialsException | DisabledException e) {
            log.error("errorMessage: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }

        String accessToken = jwtService.generateAccessToken(request.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(request.getUsername(), authorities);

        // Cache permission to Redis
        savePermissions(request.getUsername());

        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        log.info("Get refresh token");

        if (!StringUtils.hasLength(refreshToken)) {
            throw new ValidationException("Token must be not blank");
        }

        try {
            // Verify token
            String userName = jwtService.extractUsername(refreshToken, REFRESH_TOKEN);

            // check user is active or inactivated
            User user = userRepository.findByUsername(userName);

            List<String> authorities = new ArrayList<>();
            user.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));

            // generate new access token
            String accessToken = jwtService.generateAccessToken(user.getUsername(), authorities);

            return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
        } catch (Exception e) {
            log.error("Access denied! errorMessage: {}", e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    public VerifyTokenResponse getVerifyToken(String request) {
        log.info("Get verify token");

        try {
            String userName = jwtService.extractUsername(request, ACCESS_TOKEN);
            log.info("extractUsername = {}", userName);
            return VerifyTokenResponse.builder()
                    .status(200)
                    .isValid(true)
                    .message("Token authenticated")
                    .username(userName)
                    .build();
        } catch (Exception e) {
            log.error("Invalid access token: {}", e.getMessage());
            return VerifyTokenResponse.builder()
                    .status(401)
                    .isValid(false)
                    .message("Token not authenticated")
                    .build();
        }
    }

    /**
     * Save permission to cache
     *
     * @param username
     */
    private void savePermissions(String username) {
        log.info("savePermissions called");

        // Caching is best-effort: it must never fail an already-successful login.
        try {
            List<String> permissions = userRepository.findRolesByUsername(username);
            if (permissions.isEmpty()) {
                log.warn("User {} has no permissions; skip caching", username);
                return;
            }

            PermissionHash data = new PermissionHash();
            data.setId(username);
            data.setRoles(permissions);

            permissionRepository.save(data);
            log.info("Permissions cached for {}", username);
        } catch (Exception e) {
            log.error("Failed to cache permissions for {}: {}", username, e.getMessage());
        }
    }
}
