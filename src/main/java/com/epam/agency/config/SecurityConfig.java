package com.epam.agency.config;

import com.epam.agency.model.enums.Role;
import com.epam.agency.oauth2.OAuth2SuccessHandler;
import com.epam.agency.oauth2.OAuth2UserService;
import com.epam.agency.security.JwtFilter;
import com.epam.agency.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/management",
                                "/users")
                        .hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name())

                        .requestMatchers("/management/create",
                                "/management/edit/{voucherId}",
                                "/management/archive",
                                "/management/delete/{voucherId}",
                                "/management/restore/{voucherId}",
                                "/users/status/{userId}")
                        .hasRole(Role.ADMIN.name())

                        .requestMatchers("/management/requests",
                                "/management/order/{orderId}/approve",
                                "/management/order/{orderId}/cancel")
                        .hasRole(Role.MANAGER.name())

                        .requestMatchers("/users/info")
                        .hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name(), Role.USER.name())

                        .requestMatchers("/users/info/cancel/{orderId}",
                                "/users/top-up",
                                "/users/info/settings",
                                "/users/info/settings/avatar",
                                "/order/create/{voucherId}",
                                "/order/{orderId}/pdf",
                                "/order/pay/{orderId}",
                                "/order/delete/{orderId}")
                        .hasRole(Role.USER.name())

                        .requestMatchers("/**",
                                "/login",
                                "/register",
                                "/error",
                                "/vouchers",
                                "/contacts",
                                "/about-us",
                                "/vouchers/info/{voucherId}",
                                "/auth/**",
                                "/forgot-password",
                                "/reset-password")
                        .permitAll().anyRequest().authenticated())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirect -> redirect
                                .baseUri("/auth/{provider}/callback"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .invalidateHttpSession(false)
                        .clearAuthentication(true)
                        .deleteCookies("TOKEN", "JSESSIONID")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(manager -> manager
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
