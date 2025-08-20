//package bitc.full502.project2back.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/join", "/api/login").permitAll()
//                        // GET 방식으로 /api/reviews/ 로 시작하는 주소는 모두 허용
//                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
//                        // 위에서 지정한 주소 외의 모든 요청은 인증(로그인)이 필요함
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}