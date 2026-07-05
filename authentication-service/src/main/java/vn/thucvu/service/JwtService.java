package vn.thucvu.service;

import vn.thucvu.common.TokenType;

import java.util.List;

public interface JwtService {

    String generateAccessToken(String username, List<String> authorities);

    String generateRefreshToken(String username, List<String> authorities);

    String extractUsername(String token, TokenType type);
}
