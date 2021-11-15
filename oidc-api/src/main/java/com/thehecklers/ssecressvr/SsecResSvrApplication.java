package com.thehecklers.ssecressvr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@Configuration
class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {// @formatter:off
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

}