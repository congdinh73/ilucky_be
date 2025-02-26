package burundi.ilucky.jwt;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import burundi.ilucky.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtTokenProvider {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    private final String JWT_SECRET = "ilucky";

    private final long JWT_EXPIRATION = 604800000L; //7 ngày

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                   .setSubject(Long.toString(user.getId()))
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                   .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                            .setSigningKey(JWT_SECRET)
                            .parseClaimsJws(token)
                            .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }


    // Kiểm tra token có trong blacklist không
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // Thêm token vào blacklist
    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    // Trích xuất token từ request (dùng cho controller)
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}
