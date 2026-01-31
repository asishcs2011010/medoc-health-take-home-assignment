package com.asish.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private int maxSlotCapacity;     // total capacity
    private int priorityMaxSlots;    // priority capacity
}
