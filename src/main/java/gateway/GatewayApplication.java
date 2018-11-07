package gateway;


import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.google.common.collect.ImmutableList;
import com.netflix.zuul.constants.ZuulConstants;

import gateway.filters.pre.AuthPreFilter;


/**
 * @author Terry
 */
@SpringBootApplication
@EnableZuulProxy
public class GatewayApplication {

    public static void main(String[] args) {
        String connectTimeout = Optional.ofNullable(System.getenv("ZUUL_CONNECT_TIMEOUT"))
                .orElseGet(() -> System.getenv("zuul_connect_timeout"));
        String socketTimeout = Optional.ofNullable(System.getenv("ZUUL_SOCKET_TIMEOUT"))
                .orElseGet(() -> System.getenv("zuul_socket_timeout"));

        System.setProperty(ZuulConstants.ZUUL_DEBUG_REQUEST, "false");
        System.setProperty(ZuulConstants.ZUUL_HOST_CONNECT_TIMEOUT_MILLIS, Optional.ofNullable(connectTimeout).orElse("10000"));
        System.setProperty(ZuulConstants.ZUUL_HOST_SOCKET_TIMEOUT_MILLIS, Optional.ofNullable(socketTimeout).orElse("300000"));

        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public AuthPreFilter getAuthPreFilter() {
        return new AuthPreFilter();
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(ImmutableList.of("*"));
        config.setAllowedHeaders(ImmutableList.of("*"));
        config.setAllowedMethods(ImmutableList.of("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

}
