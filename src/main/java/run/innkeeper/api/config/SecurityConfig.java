package run.innkeeper.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    if(System.getenv("NO_AUTH")==null) {
      http.authorizeRequests().requestMatchers("/oauth/**").authenticated()
          .and()
          .oauth2Login()
          .and()
          .csrf().disable();
    }else{
      http.authorizeRequests().anyRequest().permitAll()
          .and()
          .csrf().disable();
    }
    return http.build();
  }
}

