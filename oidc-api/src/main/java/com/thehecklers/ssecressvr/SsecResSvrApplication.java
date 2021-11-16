package com.thehecklers.ssecressvr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@Configuration
class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(JWTSecurityConfig.class);

    @Bean("rcr")
    ReactiveClientRegistrationRepository getRegistration(
            @Value("${spring.security.oauth2.client.registration.casum.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.casum.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.provider.casum.issuer-uri}") String issuerUri
    ) {
        ClientRegistration registration = ClientRegistrations.fromIssuerLocation(issuerUri)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .registrationId("casum")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                .build();

        return new InMemoryReactiveClientRegistrationRepository(registration);
    }
    @Bean(name = "client")
    WebClient webClient(@Qualifier("rcr") ReactiveClientRegistrationRepository clientRegistrations) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = 
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, 
                                    new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId("casum");
        return WebClient.builder()
                .filter(oauth)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: [{}] {}", clientRequest.method(), clientRequest.url());
            log.debug("Payload: {}", clientRequest.body());

            return Mono.just(clientRequest);
        });
    }
        
    @Override
    protected void configure(HttpSecurity http) throws Exception {// @formatter:off
        http
            .oauth2Client();
        http
            .authorizeRequests()
			.mvcMatchers("/public/**").permitAll()
            .mvcMatchers(HttpMethod.GET, "/oauth/**").hasAuthority("SCOPE_openid")
            .anyRequest()
            .authenticated()
            .and()
            .oauth2ResourceServer().jwt();
	}
}

@RestController
@RequestMapping("/public")
class PublicResourceController {

    @Value("${SNAME}")
    private String sName;

    @GetMapping("/hello")
    Map<String,String> getHello() {
        return (Map<String, String>) Collections.singletonMap("response", "PUBLIC ["+sName+"]");
    }

}

@RestController
@RequestMapping("/oauth")
class ResourceController {
	private final WebClient client;

	public ResourceController(WebClient client) {
		this.client = client;
	}

    @Value("${SNAME}")
    private String sName;

    @GetMapping("/hello")
    Map<String,String> getHello() {
        return (Map<String, String>) Collections.singletonMap("response", "HELLO WORLD ["+sName+"]");
    }

    @GetMapping("/username")
    Map getUserName(@AuthenticationPrincipal Jwt principal) {
        HashMap<String,String> hmap = new HashMap<String,String>();
        hmap.put("server",sName);
        hmap.put("sub",principal.getSubject());
        return hmap;
    }

	@GetMapping("/remote/{server}/{method}")
	String getFromServer(@PathVariable("server") String server,
						 @PathVariable("method") String method) {
        
        String rootPath = "__debug".equals(method)?"__debug":"oauth";
        String finalServer = "__debug".equals(method)?"apigateway":server;
		return client.get()
				.uri("http://"+finalServer+":8080/"+rootPath+"/"+method)
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

}