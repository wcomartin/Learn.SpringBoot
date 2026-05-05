package com.learn.springboot.config

import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

@Configuration
class SpaConfig {

    @Bean
    fun webServerCustomizer() = WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> { factory ->
        factory.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/index.html"))
    }
}
