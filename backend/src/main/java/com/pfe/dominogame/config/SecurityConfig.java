package com.pfe.dominogame.config;

import com.pfe.dominogame.auth.JwtService;
import com.pfe.dominogame.user.UserRepository;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         JwtService jwtService,
                                         UserRepository userRepository,
                                         CorsConfigurationSource corsSource) throws Exception {

    http.csrf().disable();
    http.cors().configurationSource(corsSource);
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    http.authorizeRequests()
        .antMatchers("/api/auth/**").permitAll()
        .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .antMatchers("/ws/**").permitAll()
        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .anyRequest().authenticated();

    http.addFilterBefore(new JwtFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(AppProperties props) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(props.getCorsAllowedOrigins());
    config.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(java.util.Arrays.asList("Authorization", "Content-Type"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  static class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    JwtFilter(JwtService jwtService, UserRepository userRepository) {
      this.jwtService = jwtService;
      this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
      String auth = request.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring("Bearer ".length());
        if (jwtService.isValid(token)) {
          String username = jwtService.getUsername(token);
          if (userRepository.findByUsername(username).isPresent()) {
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
      }
      filterChain.doFilter(request, response);
    }
  }
}

