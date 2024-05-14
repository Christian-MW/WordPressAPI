package com.wordpress.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.wordpress.api.security.JWTAuthorizationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    @Autowired
    Environment environment;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf(AbstractHttpConfigurer::disable)
        		.addFilterAfter(new JWTAuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class)
        		
        		.authorizeHttpRequests(request -> request.requestMatchers("/authenticate/**")
        				.permitAll()
        				.requestMatchers("/v1/user/verify-token/**").permitAll()
        				.requestMatchers("/v1/user/change-password").permitAll()
        				.requestMatchers("/v1/user/request/change-password/**").permitAll()
        				.requestMatchers("/log/logs").permitAll()
        				.requestMatchers("/v1/send/mail").permitAll()
        				.anyRequest().authenticated()
        				);
        		//.authorizeHttpRequests(request -> request.anyRequest().authenticated());
        		/*
                .authorizeHttpRequests(request -> request.requestMatchers("/api/v1/auth/**")
                        .permitAll().anyRequest().authenticated())
                //.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                //.authenticationProvider(authenticationProvider()).addFilterBefore(
                	//	new JWTAuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class);
                	 
                	 */
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
