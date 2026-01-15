package com.example.projectbinar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtUtils jwtUtils;
  private final CustomUserDetailsService userDetailsService;
  private final RedisTokenService redisTokenService;

  public JwtAuthenticationFilter(
      JwtUtils jwtUtils,
      CustomUserDetailsService userDetailsService,
      RedisTokenService redisTokenService) {
    this.jwtUtils = jwtUtils;
    this.userDetailsService = userDetailsService;
    this.redisTokenService = redisTokenService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
        // Check if token is blacklisted
        if (redisTokenService.isTokenBlacklisted(jwt)) {
          logger.warn("Token is blacklisted");
          filterChain.doFilter(request, response);
          return;
        }

        String username = jwtUtils.getUsernameFromToken(jwt);
        String authoritiesStr = jwtUtils.getAuthoritiesFromToken(jwt);

        // Parse authorities from token, ensuring they have passed through correctly
        // However, we rely on UserDetailsService to get the freshest roles/permissions from DB
        // to avoid stale permission issues if the token is old but valid.

        // Load user details for additional validation and fresh authorities
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails.isEnabled()) {
          // Use authorities from the loaded userDetails (fresh from DB)
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception ex) {
      logger.error("Could not set user authentication in security context", ex);
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
