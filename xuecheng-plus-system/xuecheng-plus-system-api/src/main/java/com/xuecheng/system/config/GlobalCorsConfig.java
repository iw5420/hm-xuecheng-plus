package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @description: 跨域過慮器
 * @author: Ian Wang
 * @date: 2023/10/31 上午 10:38
 * @version: 1.0
 */
@Configuration
public class GlobalCorsConfig {
    /**
     * 允許跨域調用的過濾器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        //允許白名單域名進行跨域調用
        config.addAllowedOrigin("*");
        //允許跨越發送cookie
        config.setAllowCredentials(true);
        //放行全部原始頭信息
        config.addAllowedHeader("*");
        //允許所有請求方法跨域調用
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
