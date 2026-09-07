package com.example.urfu.cake.shop.user.profile.configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.beans.factory.BeanRegistrarDslMarker;
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration;


@Configuration 
public class SwaggerConfig{

    @Bean
    public OpenAPI userProfileServiceOpenAPI (){
        return new OpenAPI().info(new Info()
                .title("User Profile Service API")
                .version("1.0.0")
                .description("User Profile Service"));
    }
}