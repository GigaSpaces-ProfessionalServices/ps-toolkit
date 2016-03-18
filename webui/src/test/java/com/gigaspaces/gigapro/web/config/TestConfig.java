package com.gigaspaces.gigapro.web.config;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.DefaultResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Profile("test")
public class TestConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public MustacheResolver mustacheResolver() {
        return new DefaultResolver("templates/");
    }

    @Bean
    public MustacheFactory mustacheFactory() {
        return new DefaultMustacheFactory(mustacheResolver());
    }

    @Bean
    public Mustache setAppEnvShellMustache() {
        return mustacheFactory().compile("set-app-env-shell-template.mustache");
    }

    @Bean
    public Mustache startGridShellMustache() {
        return mustacheFactory().compile("start-grid-shell-template.mustache");
    }
}
