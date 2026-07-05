package vn.thucvu.service;

import vn.thucvu.controller.request.SignInRequest;
import vn.thucvu.controller.response.TokenResponse;
import vn.thucvu.controller.response.VerifyTokenResponse;

public interface AuthenticationService {

    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(String request);

    VerifyTokenResponse getVerifyToken(String request);
}
