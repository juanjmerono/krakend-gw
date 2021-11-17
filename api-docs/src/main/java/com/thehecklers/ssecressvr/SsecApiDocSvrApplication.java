package com.thehecklers.ssecressvr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
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

    @GetMapping("/swagger-config.json")
    SwaggerUrlsConfig swaggerConfig() {
        List<SwaggerUrlsConfig.SwaggerUrl> urls = new ArrayList<SwaggerUrlsConfig.SwaggerUrl>();
        urls.add(new SwaggerUrlsConfig.SwaggerUrl("API1","/swagger-ui/api-docs/api1"));
        urls.add(new SwaggerUrlsConfig.SwaggerUrl("API2","/swagger-ui/api-docs/api2"));
        return new SwaggerUrlsConfig(urls);
    }

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

