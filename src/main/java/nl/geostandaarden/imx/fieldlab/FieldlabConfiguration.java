package nl.geostandaarden.imx.fieldlab;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class FieldlabConfiguration {

  @Bean
  public CorsWebFilter corsWebFilter() {
    var config = new CorsConfiguration();
    config.addAllowedOrigin("*");
    config.addAllowedMethod(HttpMethod.POST);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
  }
}
