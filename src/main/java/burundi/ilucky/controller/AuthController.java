package burundi.ilucky.controller;

import burundi.ilucky.jwt.JwtTokenProvider;
import burundi.ilucky.model.User;
import burundi.ilucky.payload.*;
import burundi.ilucky.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserService userService;


	@PostMapping
	public ResponseEntity<?> auth(@Valid @RequestBody AuthRequest authRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = tokenProvider.generateToken((User) authentication.getPrincipal());

		AuthResponse authResponse = new AuthResponse(jwt);
		return ResponseEntity.ok().body(authResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<Response> logout(HttpServletRequest request) {
		String token = jwtTokenProvider.resolveToken(request);
		if (token != null) {
			userService.logout(token);
		}
		return ResponseEntity.ok(new Response("SUCCESS", "Logged out successfully"));
	}
}



