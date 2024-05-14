package com.wordpress.api.security;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";
    private String jwtSecret;
    
    public JWTAuthorizationFilter(Environment env) {
        this.jwtSecret = env.getRequiredProperty("jwt.secret");
    }
    
    
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
    		throws ServletException, IOException {       
        try {
            if (checkJWTToken(request, response)) {
                Claims claims = validateToken(request);
                if (claims.get("userId") != null) {
                    setUpSpringAuthentication(claims);                    
                } else {
                    SecurityContextHolder.clearContext();
                    throw new MalformedJwtException("Required parameter 'userId' is not present");
                }
            }else {
                SecurityContextHolder.clearContext();
            }
            /*else {
                throw new SignatureException("JWT Token missing");
            }*/            
            chain.doFilter(request, response);                         
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;

        } catch (SignatureException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }
    }
    
    private Claims validateToken(HttpServletRequest request) throws SignatureException {
        String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");
        return Jwts.parser().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(jwtToken).getBody();
        //return Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(jwtToken).getBody();
    }
    /**
     * Authentication method in Spring flow
     *
     * @param claims
     */
    private void setUpSpringAuthentication(Claims claims) {
        @SuppressWarnings("unchecked")
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims, null, null);        
        SecurityContextHolder.getContext().setAuthentication(auth);       
    }

    private boolean checkJWTToken(HttpServletRequest request, HttpServletResponse res) {
        String authenticationHeader = request.getHeader(HEADER);
        return authenticationHeader == null || !authenticationHeader.startsWith(PREFIX) ? false : true;
    }
}
