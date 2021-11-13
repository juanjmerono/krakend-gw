package com.thehecklers.ssecressvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@Configuration
class HttpTraceActuatorConfiguration {

    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
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

    @Autowired
	private HttpServletRequest request;

    @GetMapping("/")
	public ResponseEntity<Map<String, Object>> echoBack(@RequestBody(required = false) byte[] rawBody) throws IOException {

		Map<String, String> headers = new HashMap<String, String>();
		for (String headerName : Collections.list(request.getHeaderNames())) {
			headers.put(headerName, request.getHeader(headerName));
		}

		Map<String, Object> responseMap = new HashMap<String,Object>();
		responseMap.put("protocol", request.getProtocol());
		responseMap.put("method", request.getMethod());
		responseMap.put("headers", headers);
		responseMap.put("cookies", request.getCookies());
		responseMap.put("parameters", request.getParameterMap());
		responseMap.put("path", request.getServletPath());
		responseMap.put("body", rawBody != null ? Base64.getEncoder().encodeToString(rawBody) : null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
	}

    @GetMapping("/something")
    String getSomething() {
        return "HELLO WORLD!!";
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
}