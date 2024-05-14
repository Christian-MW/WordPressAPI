package com.wordpress.api.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordpress.api.dto.model.UserDetails;
import com.wordpress.api.dto.model.UserToken;
import com.wordpress.api.exceptions.BadRequestException;
import com.wordpress.api.exceptions.ForbiddenException;
import com.wordpress.api.exceptions.UnauthorizetException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/authenticate")
public class AuthenticationJWTController {
	private static Logger log = Logger.getLogger(AuthenticationJWTController.class);
    @Value("${jwt.secret}")
    private String JWT_SECRET; 
    @Value("${jwt.expiration.time}")
    private Long EXPIRATION_TIME;
    
    @GetMapping(value = "/token")
    public ResponseEntity<UserDetails> authenticate() {
    	log.debug("token services");
        UserDetails userDetails = new UserDetails();
        try {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            
            Claims principal = (Claims) auth.getPrincipal();            
            userDetails.setUserId(Long.parseLong(principal.get("userId").toString()));            
            userDetails.setUser(principal.getSubject());
            userDetails.setIat(Long.parseLong(principal.get("iat").toString()));
            userDetails.setExp(Long.parseLong(principal.get("exp").toString()));
        } catch (Exception e) {
            throw new UnauthorizetException("Missing request header 'Authorization' for method parameter of type String");
        }        
        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }
    
	@CrossOrigin(origins = "*")
	@PostMapping(value = "/user")
	public ResponseEntity<UserToken> token(@RequestHeader(value = "Authorization", defaultValue = "") String secret,
			@RequestBody UserDetails userDetails) {
		LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(EXPIRATION_TIME);

		if (StringUtils.isEmpty(secret)) {
			throw new ForbiddenException();
		}
		if (!JWT_SECRET.equals(secret)) {
			throw new UnauthorizetException("Unauthorized");
		}
		if (userDetails.getPassword() == null || userDetails.getPassword().toString().trim().isEmpty()) {
			throw new BadRequestException("Required parameter 'pwd' is not present or is not valid");
		}
		if (userDetails.getUser() == null || userDetails.getUser().toString().trim().isEmpty()) {
			throw new BadRequestException("Required parameter 'username' is not present or is not valid");
		}
		//####_BASE DE DATOS
		/*boolean isUserLambda = false;
		UserEntity usr = new UserEntity();
		if (LAMBDA_USER.equals(userDetails.getUser()) && LAMBDA_PWD.equals(userDetails.getPassword())) {
			isUserLambda = true;
			usr.setId(new Date().getTime());
			usr.setUser(LAMBDA_USER);
		} else {
			usr = userRepository
					.findByUserAndPasswordAndIsActive(userDetails.getUser(), userDetails.getPassword(), true)
					.orElse(null);
			if (usr == null && !isUserLambda) {

				throw new BadRequestException("Invalid user");
			}
		}*/
		String userDEMO = "christian.garcia@mwgroup.com.mx";
		Long useridDEMO = 12793842L;

		UserToken userToken = new UserToken();
		userToken.setUserId(useridDEMO);
		userToken.setUsername(userDEMO);
		String token = Jwts.builder()
				.setId("")
				.setSubject(userDetails.getUser())
				.claim("userId", useridDEMO)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
				.signWith(SignatureAlgorithm.HS512, JWT_SECRET.getBytes()).compact();
		userToken.setToken("Bearer " + token);

		return new ResponseEntity<UserToken>(userToken, HttpStatus.OK);
	}
}
