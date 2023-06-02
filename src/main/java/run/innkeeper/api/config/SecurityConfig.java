package run.innkeeper.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    if(System.getenv("NO_AUTH")==null) {
      http.authorizeRequests()
          .anyRequest()
          .authenticated()
          .and()
          .oauth2Login().and().csrf().disable();
    }
    return http.build();
  }
}

