// JwtTokenProvider.java
package com.mydea.mydea_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JwtTokenProvider {
    private final byte[] key;
    private final SecretKey secretKey;
    private final long accessTtlSeconds; // 예: 1800(30m)

    public JwtTokenProvider(String secret, long accessTtlSeconds) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(this.key);
        this.accessTtlSeconds = accessTtlSeconds;
    }

    /**
     *  - sub: accountId
     *  - lid: 로그인아이디
     *  - role: 단일 문자열(예: ROLE_USER)
     */
    public String createAccessToken(Long accountId, String loginId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(accountId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .addClaims(Map.of("lid", loginId, "role", role))
                .signWith(secretKey)
                .compact();
    }

    public long getAccessTtlSeconds() { return accessTtlSeconds; }


    /** 토큰 유효성(서명/만료) 검증 */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** sub(= accountId) 그대로 반환 */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /** sub를 Long으로 파싱 (숫자가 아니면 null) */
    public Long getUserIdAsLong(String token) {
        try {
            return Long.valueOf(getUserId(token));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 커스텀 클레임: 로그인 아이디(lid) */
    public String getLoginId(String token) {
        return parseClaims(token).get("lid", String.class);
    }

    /** 단일 역할 문자열(role) — 기존 토큰 포맷 호환 */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * roles 목록 반환
     * - roles(리스트형) 클레임이 있으면 우선 사용
     * - 없으면 role(문자열) 1개로 대체
     */
    public List<String> getRoles(String token) {
        Claims c = parseClaims(token);

        Object raw = c.get("roles");
        if (raw instanceof List<?>) {
            return ((List<?>) raw).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        String single = c.get("role", String.class);
        return single != null ? List.of(single) : List.of();
    }

    /** 만료 시각 */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /** 내부용: 서명 검증 포함한 Claims 파싱 */
    private Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
