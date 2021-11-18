package com.thehecklers.ssecressvr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@OpenAPIDefinition (
    info = @Info(title = "Sample API (${SNAME})", version = "0.0", description = "This is a sample API for testing public endpoints."),
    servers = { @Server(url="${endpoints.gateway-url}") }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "OAuthUser",
        type = SecuritySchemeType.OPENIDCONNECT,
        flows = @OAuthFlows( authorizationCode = @OAuthFlow(tokenUrl = "${spring.security.oauth2.client.provider.api1.issuer-uri}/accessToken")),
        openIdConnectUrl = "${spring.security.oauth2.client.provider.api1.issuer-uri}/.well-known/openid-configuration"
    )
})
@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@Component
class TokenManager {

    @Value("${CASCLIENT}")
    private String casClient;

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
                    .withClientRegistrationId(casClient)
                    .principal(createAuthentication(casClient))
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
            .mvcMatchers("/api-docs/**").permitAll()
			.mvcMatchers("/public/**").permitAll()
            .mvcMatchers(HttpMethod.GET, "/oauth/**").hasAuthority("SCOPE_openid")
            .anyRequest()
            .authenticated()
            .and()
            .oauth2ResourceServer().jwt();
	}
}

@RestController
@RequestMapping("/public/${SNAME}")
class PublicResourceController {

    @Value("${SNAME}")
    private String sName;

    @Operation(summary = "Get hello world public message.", 
            description = "Get a public message with serverId.",
            tags = {"Public"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Hi message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/hello")
    Map<String,String> getHello() {
        return (Map<String, String>) Collections.singletonMap("response", "PUBLIC HELLO WORLD ["+sName+"]");
    }

}

@RestController
@SecurityRequirement(name = "OAuthUser")
@RequestMapping("/oauth/${SNAME}")
class ResourceController {

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    TokenManager tokenManager;

    @Value("${SNAME}")
    private String sName;

    @Operation(summary = "Get hello world private message.", 
            description = "Get a private hello world message with serverId.",
            tags = {"OAuth"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Hi message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/hello")
    Map<String,String> getHello() {
        return (Map<String, String>) Collections.singletonMap("response", "HELLO WORLD ["+sName+"]");
    }

    @Operation(summary = "Get username from access token and serverId.", 
            description = "Get username from access token and serverId.",
            tags = {"OAuth"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Server and username.",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/username")
    Map<String,String> getUserName(@AuthenticationPrincipal Jwt principal) {
        HashMap<String,String> hmap = new HashMap<String,String>();
        hmap.put("server",sName);
        hmap.put("sub",principal.getSubject());
        return hmap;
    }

    @Operation(summary = "Get response from a remote API via Clien Credentials.", 
            description = "Get response from a remote API via Clien Credentials.",
            tags = {"OAuth"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Server and username.",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
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
                "http://"+finalServer+":8080/"+rootPath+"/"+server+"/"+method, 
                HttpMethod.GET, request, String.class)
                .getBody();
	}
    
}