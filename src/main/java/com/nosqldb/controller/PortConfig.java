package com.nosqldb.controller;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import static com.nosqldb.controller.Constants.CONTROLLER_PORT;

@Configuration
public class PortConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  public void customize(ConfigurableServletWebServerFactory factory){
    factory.setPort(CONTROLLER_PORT);
  }
}