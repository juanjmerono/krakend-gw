package com.thehecklers.ssecressvr;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SsecResSvrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsecResSvrApplication.class, args);
    }

}

@RestController
@RequestMapping("/resources")
class ResourceController {

    @Value("${SNAME}")
    private String sName;

    @GetMapping("/hello")
    Map<String,String> getHello() {
        return (Map<String, String>) Collections.singletonMap("response", "HELLO WORLD ["+sName+"]");
    }

    @GetMapping("/bearer")
    Map getBearer(@RequestHeader (name="Authorization") String token) {
        String[] chunks = token.replaceFirst("Bearer ","").split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();

        //String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        HashMap<String,String> hmap = new HashMap<String,String>();
        hmap.put("server",sName);
        hmap.put("sub",JsonPath.parse(payload).read("$.sub"));
        hmap.put("aud",JsonPath.parse(payload).read("$.aud"));
        return hmap;
    }

}