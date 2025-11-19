package com.phuc.notification.configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ObservationRegistry.class)
public class ObservationConfig {

      @Bean
      @ConditionalOnBean(ObservationRegistry.class)
      ObservedAspect observedAspect(ObservationRegistry registry) {
            return new ObservedAspect(registry);
      }

}
