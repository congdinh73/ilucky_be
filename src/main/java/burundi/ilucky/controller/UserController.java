package burundi.ilucky.controller;

import burundi.ilucky.model.User;
import burundi.ilucky.model.dto.UserDTO;
import burundi.ilucky.payload.AuthRequest;
import burundi.ilucky.payload.AuthResponse;
import burundi.ilucky.payload.Response;
import burundi.ilucky.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/info")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserName(userDetails.getUsername());
            UserDTO userDTO = new UserDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> auth(@Valid @RequestBody AuthRequest authRequest) {
        Response response = userService.login(authRequest.getUsername(), authRequest.getPassword());

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(new AuthResponse(response.getMessage()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody User user) {
        Response response = userService.register(user);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


}
