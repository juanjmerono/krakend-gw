package com.thehecklers.ssecressvr;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .mvcMatchers("/resources/myname/**").hasAuthority("SCOPE_openid")
                .mvcMatchers("/resources/claims/**").hasAuthority("SCOPE_openid")
                .mvcMatchers("/resources/email/**").hasAuthority("SCOPE_email")
                .and().oauth2ResourceServer().jwt();
    }
}

@RestController
@RequestMapping("/resources")
class ResourceController {

    @Value("${remote.resource.url}")
    private String remoteUrl;
    @Value("${SNAME}")
    private String sName;
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/something")
    String getSomething() {
        return "HELLO WORLD ["+sName+"]";
    }

    @GetMapping("/myname")
    String showName(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject();
    }

    @GetMapping("/claims")
    Map<String, Object> getClaims(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }

    @GetMapping("/email")
    String getSubject(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject();
    }

    @GetMapping("/remote")
    public String helloWebClient(@AuthenticationPrincipal Jwt jwt) {
        //Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return webClient.get()
                .uri(remoteUrl)
                .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }    
}