package com.gigaspaces.gigapro.web.config;

import com.gigaspaces.gigapro.web.listener.BrowserLauncher;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.DefaultResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Profile("default")
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    @Value("${app.init.url}")
    private String initURL;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public BrowserLauncher browserLauncher() {
        return new BrowserLauncher(initURL);
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
