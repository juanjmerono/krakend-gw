package com.thehecklers.ssecressvr;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@OpenAPIDefinition (
    info = @Info(title = "Composed API (krakend)", version = "0.0", description = "This is a composed API for krakend endpoints."),
    servers = { @Server(url="${endpoints.gateway-url}") }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "OAuthUser",
        type = SecuritySchemeType.OPENIDCONNECT,
        flows = @OAuthFlows( authorizationCode = @OAuthFlow(tokenUrl = "${oauth.issuer-uri}/accessToken") ),
        openIdConnectUrl = "${oauth.issuer-uri}/.well-known/openid-configuration"
    ),
    @SecurityScheme(
        name = "BasicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
    )
})
@SpringBootApplication
public class SsecApiDocSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecApiDocSvrApplication.class, args);
    }

}

@Controller
class RootController {
    // SwaggerUI is not adding context in redirect-uri this is a hack
	@GetMapping("/oauth2-redirect.html")
	String index() {
		return "redirect.html";
	}
}

@Data
@Component
@ConfigurationProperties(prefix = "endpoints")
class SwaggerUrlsConfig {
    private List<SwaggerUrl> urls;

    @Data
    public static class SwaggerUrl {
        private String name;
        private String url;
        private String privateurl;
    }

}

@RestController
@RequestMapping("/swagger-ui")
class ApiDocResourceController {

    @Autowired
    private SwaggerUrlsConfig swaggerUrlsConfig;

    @Operation(hidden = true, description = "Swagger Configuration Files")
    @GetMapping("/swagger-config.json")
    SwaggerUrlsConfig getSwaggerConfig() {
        return swaggerUrlsConfig;
    }

    @Operation(hidden = true, description = "Proxy to backend apidocs")
    @GetMapping("/api-docs/{service}")
    String getServerApiDocs(@PathVariable("service") String service) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> request = new HttpEntity<>(null);
        return restTemplate.exchange(
                swaggerUrlsConfig.getUrls().stream().filter(u->service.equals(u.getName())).findFirst().get().getPrivateurl(), 
                HttpMethod.GET, request, String.class)
                .getBody();
    }

}

@RestController
@RequestMapping("/public")
class ApiDocComposedPublicResourceController {

    @Operation(summary = "Get some public message.", 
            description = "Get a public message depending on method from multiple endpoints.",
            tags = {"Public"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Response message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/composed/{method}")
    Map<String,String> fakeApiEndpoint1(@PathVariable("method") String method) {
        return null;
    }
}

@RestController
@SecurityRequirement(name = "OAuthUser")
@RequestMapping("/oauth")
class ApiDocComposedOAuthResourceController {

    @Operation(summary = "Get some private message.", 
            description = "Get a private message depending on method from multiple endpoints.",
            tags = {"OAuth"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Response message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/composed/{method}")
    Map<String,String> fakeApiEndpoint2(@PathVariable("method") String method) {
        return null;
    }

}

@RestController
@RequestMapping("/legacy")
class ApiDocComposedResourceController {

    @Operation(summary = "Get some legacy message.", 
            description = "Get a legacy message from outside.",
            tags = {"Legacy"},
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Response message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/public/github/{userId}")
    String fakeApiEndpoint3(@PathVariable("userId") String userId) {
        return null;
    }

    @Operation(summary = "Get some private legacy message.", 
            description = "Get a private legacy message from outside.",
            tags = {"Legacy"},
            security = { @SecurityRequirement(name = "BasicAuth") },
            responses = { 
                @ApiResponse(responseCode = "200",
                            description="Response message",
                            content= @Content(schema=@Schema(implementation=Map.class)))
            })
    @GetMapping("/private/rrhh/hello")
    String fakeApiEndpoint4() {
        return null;
    }
}


