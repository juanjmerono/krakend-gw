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
@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
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
