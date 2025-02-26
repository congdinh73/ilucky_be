package burundi.ilucky.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import burundi.ilucky.jwt.JwtFilter;
import burundi.ilucky.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import burundi.ilucky.model.User;
import burundi.ilucky.payload.Response;
import burundi.ilucky.repository.UserRepository;

@Service
public class UserService {
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtFilter jwtFilter;

	@Autowired
	PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;



	public User findByUserName(String username) {
		try {
			return userRepository.findByUsername(username);
		} catch (Exception e) {
			return null;
		}
	}

	public User saveUser(User user) {
		return userRepository.save(user);
	}

	public Response login(String username, String password) {
		User user = findByUserName(username);
		if (user == null) {
			return new Response("FAILED", "Invalid username or password");
		}

		if (!passwordEncoder.matches(password, user.getPassword())) {
			return new Response("FAILED", "Invalid username or password");
		}
		String jwt = jwtTokenProvider.generateToken(user);
		return new Response("SUCCESS", jwt);
	}

	public Response register(User user) {
		if (userRepository.findByUsername(user.getUsername()) != null) {
			return new Response("FAILED", "Username already exists");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setAddTime(new Date());
		userRepository.save(user);
		return new Response("SUCCESS", "User registered successfully");
	}

	public Response logout(String token) {
		jwtTokenProvider.invalidateToken(token); // Add token to blacklist
		return new Response("SUCCESS", "Logged out successfully");
	}
}
