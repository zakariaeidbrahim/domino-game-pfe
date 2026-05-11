package com.pfe.dominogame.auth;

import com.pfe.dominogame.auth.AuthDtos.AuthResponse;
import com.pfe.dominogame.auth.AuthDtos.LoginRequest;
import com.pfe.dominogame.auth.AuthDtos.RegisterRequest;
import com.pfe.dominogame.user.User;
import com.pfe.dominogame.user.UserRepository;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
    String username = req.username.trim().toLowerCase();
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("Username already exists");
    }
    User user = new User(username, passwordEncoder.encode(req.password));
    userRepository.save(user);
    String token = jwtService.createToken(username);
    return ResponseEntity.ok(new AuthResponse(token, username));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
    String username = req.username.trim().toLowerCase();
    User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Bad credentials"));
    if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Bad credentials");
    }
    String token = jwtService.createToken(username);
    return ResponseEntity.ok(new AuthResponse(token, username));
  }
}

