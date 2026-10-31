package vn.ktt.music.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
class MusicSecurityConfig {
    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor grpcJwtSecurityFilterChain(GrpcSecurity grpc) throws Exception {
        return grpc
                .authorizeRequests(requests -> requests.allRequests().permitAll())
                .build();
    }

    /** Public music REST endpoints. In a path pattern `**` must be a whole trailing segment, so each prefix is listed on its own. */
    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/intervals/**", "/api/interval-range/**")
                .authorizeHttpRequests(request -> request.anyRequest().permitAll())
                .build();
    }
}
