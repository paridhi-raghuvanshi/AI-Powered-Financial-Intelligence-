package com.aura.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve absolute path to ../aura-platform/public
        File publicDir = new File("../aura-platform/public");
        String publicPath = publicDir.exists()
                ? "file:" + publicDir.getAbsolutePath().replace("\\", "/") + "/"
                : "file:aura-platform/public/";

        registry.addResourceHandler("/**")
                .addResourceLocations(publicPath, "classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // If route is not an API or socket path, return index.html for SPA
                        if (!resourcePath.startsWith("api") && !resourcePath.startsWith("socket.io")) {
                            return location.createRelative("index.html");
                        }
                        return null;
                    }
                });
    }
}
