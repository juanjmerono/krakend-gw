package com.thehecklers.ssecoidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@SpringBootApplication
public class SsecOidcApplication {

	@Value("${remote.resource.baseurl}")
	private String resourceUrl;

	@Bean
	WebClient client(ClientRegistrationRepository regRepo,
					 OAuth2AuthorizedClientRepository cliRepo) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction fFunc =
				new ServletOAuth2AuthorizedClientExchangeFilterFunction(
						regRepo,
						cliRepo
				);

		fFunc.setDefaultOAuth2AuthorizedClient(true);

		return WebClient.builder()
				.baseUrl(resourceUrl)
				.apply(fFunc.oauth2Configuration())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SsecOidcApplication.class, args);
	}

}

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
    private ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
			.mvcMatchers("/")
			.permitAll()
			.anyRequest()
			.fullyAuthenticated();
		http
			.oauth2Login()
			.authorizationEndpoint()
			.authorizationRequestResolver(new SsecOidcCustomAuthorizationRequestResolver(
				clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
			));
    }
}

@Controller
class OidcWController {

	@GetMapping("/")
	String index() {
		return "index.html";
	}

}

@RestController
class OidcController {
	private final WebClient client;

	public OidcController(WebClient client) {
		this.client = client;
	}

	@GetMapping("/myname")
	String showName(@AuthenticationPrincipal OidcUser principal) {
		return "Hello ["+principal.getName()+"]";
	}

	@GetMapping("/myclaims")
	Map showClaims(@AuthenticationPrincipal OidcUser principal) {
		return principal.getClaims();
	}

	@GetMapping("/remote/hello")
	String getHelloFromRServer() {
		return client.get()
				.uri("/hello")
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

	@GetMapping("/remote/bearer")
	String getSubjectFromRServer() {
		return client.get()
				.uri("/bearer")
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

	@GetMapping("/remote/composed")
	String getComposedFromRServer() {
		return client.get()
				.uri("/composed")
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

}