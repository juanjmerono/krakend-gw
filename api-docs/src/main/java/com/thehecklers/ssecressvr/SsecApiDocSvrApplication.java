package com.thehecklers.ssecressvr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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



@OpenAPIDefinition (
    info = @Info(title = "Composed API (krakend)", version = "0.0", description = "This is a composed API for krakend endpoints."),
    servers = { @Server(url="${endpoints.gateway-url}") }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "OAuthUser",
        type = SecuritySchemeType.OPENIDCONNECT,
        flows = @OAuthFlows( authorizationCode = @OAuthFlow(tokenUrl = "${oauth.issuer-uri}/accessToken")),
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

class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) 
      throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}

class SwaggerUrlsConfig {

    private List<SwaggerUrl> urls;

    public static class SwaggerUrl {
        private String url;
        private String name;
        public SwaggerUrl(String name, String url) {
            this.url = url; this.name = name;
        }
        public String getUrl() { return this.url; }
        public String getName() { return this.name; }
    }

    public SwaggerUrlsConfig(List<SwaggerUrl> urls) {
        this.urls = urls;
    }

    public List<SwaggerUrl> getUrls() { return urls; }
}

@RestController
@RequestMapping("/swagger-ui")
class ApiDocResourceController {

    @Operation(hidden = true, description = "Swagger Configuration Files")
    @GetMapping("/swagger-config.json")
    SwaggerUrlsConfig swaggerConfig() {
        List<SwaggerUrlsConfig.SwaggerUrl> urls = new ArrayList<SwaggerUrlsConfig.SwaggerUrl>();
        urls.add(new SwaggerUrlsConfig.SwaggerUrl("COMPOSED","/api-docs"));
        urls.add(new SwaggerUrlsConfig.SwaggerUrl("API1","/swagger-ui/api-docs/api1"));
        urls.add(new SwaggerUrlsConfig.SwaggerUrl("API2","/swagger-ui/api-docs/api2"));
        return new SwaggerUrlsConfig(urls);
    }

    @Operation(hidden = true, description = "Proxy to backend apidocs")
    @GetMapping("/api-docs/{server}")
    String getServerApiDocs(@PathVariable("server") String server) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> request = new HttpEntity<>(null);
        return restTemplate.exchange(
                "http://"+server+":8080/api-docs/", 
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


