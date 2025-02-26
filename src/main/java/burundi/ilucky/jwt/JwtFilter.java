package burundi.ilucky.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import burundi.ilucky.service.UserDetailsServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private UserDetailsServiceImpl customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = getJwtFromRequest(request);

			// Kiểm tra token có trong danh sách đen không
			if (StringUtils.hasText(jwt) && tokenProvider.isTokenBlacklisted(jwt)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
				return; // Dừng xử lý nếu token bị vô hiệu
			}

			if (StringUtils.hasText(jwt)){
				// Validate token và xác thực người dùng
				if (tokenProvider.validateToken(jwt)) {
					Long userId = tokenProvider.getUserIdFromJWT(jwt);
					UserDetails userDetails = customUserDetailsService.loadUserById(userId);

					if (userDetails != null) {
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
		} catch (Exception ex) {
			log.error("Authentication failed: ", ex);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
