package com.thehecklers.ssecressvr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@Component
class TokenManager {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
  
    public TokenManager(OAuth2AuthorizedClientManager authorizedClientManager) {
      this.authorizedClientManager = authorizedClientManager;
    }
  
    private Authentication createAuthentication(final String principalName) {
		return new AbstractAuthenticationToken(null) {
			private static final long serialVersionUID = -2038812908189509872L;

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getPrincipal() {
				return principalName;
			}
		};
	}

    public String getAccessToken() {
      OAuth2AuthorizeRequest authorizeRequest =
              OAuth2AuthorizeRequest
                    .withClientRegistrationId("casum")
                    .principal(createAuthentication("casum"))
                    .build();
  
      OAuth2AuthorizedClient authorizedClient =
              this.authorizedClientManager.authorize(authorizeRequest);
  
      OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
  
      return accessToken.getTokenValue();
    }
  }

@Configuration
class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
        
  
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
  
      OAuth2AuthorizedClientProvider authorizedClientProvider =
              OAuth2AuthorizedClientProviderBuilder.builder()
                      .clientCredentials()
                      .build();
  
      var authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                                            clientRegistrationRepository,
                                            authorizedClientRepository);
  
      authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
  
      return authorizedClientManager;
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

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    TokenManager tokenManager;

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

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + tokenManager.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(
                "http://"+finalServer+":8080/"+rootPath+"/"+method, 
                HttpMethod.GET, request, String.class)
                .getBody();
	}
    
}