package com.thehecklers.ssecressvr;

import java.util.Base64;

// import java.util.Map;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
// import org.springframework.security.oauth2.client.registration.ClientRegistration;
// import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
// import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
// import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
// import org.springframework.security.oauth2.core.AuthorizationGrantType;
// import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.security.oauth2.client.*;
// import org.springframework.security.oauth2.client.registration.*;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

/*@Configuration
public class OAuthClientConfiguration {

    @Bean
    ReactiveClientRegistrationRepository clientRegistrations(
            @Value("${spring.security.oauth2.client.provider.casum.token-uri}") String token_uri,
            @Value("${spring.security.oauth2.client.registration.casum.client-id}") String client_id,
            @Value("${spring.security.oauth2.client.registration.casum.client-secret}") String client_secret,
            @Value("${spring.security.oauth2.client.registration.casum.scope}") String scope,
            @Value("${spring.security.oauth2.client.registration.casum.authorization-grant-type}") String authorizationGrantType

    ) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("casum")
                .tokenUri(token_uri)
                .clientId(client_id)
                .clientSecret(client_secret)
                .scope(scope)
                .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType))
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean
    WebClient webClientCredentials(ReactiveClientRegistrationRepository clientRegistrations) {
        InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId("okta");
        return WebClient.builder()
                .filter(oauth)
                .build();

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
}*/

@RestController
@RequestMapping("/resources")
class ResourceController {

    @Value("${SNAME}")
    private String sName;

    // @Value("${remote.resource.url}")
    // private String remoteUrl;
    // private final WebClient webClient = WebClient.builder().build();
    // @Autowired
	// private WebClient webClientCredentials;

    @GetMapping("/hello")
    String getHello() {
        return "HELLO WORLD ["+sName+"]";
    }

    @GetMapping("/bearer")
    String getBearer(@RequestHeader (name="Authorization") String token) {
        String[] chunks = token.replaceFirst("Bearer ","").split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        return "HELLO WORLD ["+header+"]["+payload+"]";
    }

    // @GetMapping("/myname")
    // String showName(@AuthenticationPrincipal Jwt jwt) {
    //     return jwt.getSubject();
    // }

    // @GetMapping("/claims")
    // Map<String, Object> getClaims(@AuthenticationPrincipal Jwt jwt) {
    //     return jwt.getClaims();
    // }

    // @GetMapping("/email")
    // String getSubject(@AuthenticationPrincipal Jwt jwt) {
    //     return jwt.getSubject();
    // }

    // @GetMapping("/remote")
    // public String helloWebClient(@AuthenticationPrincipal Jwt jwt) {
    //     //Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //     return webClient.get()
    //             .uri(remoteUrl)
    //             .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
    //             .retrieve()
    //             .bodyToMono(String.class)
    //             .block();
    // }    
}