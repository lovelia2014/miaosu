package com.miaosu.flux.config;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 默认HttpClient配置
 * Created by angus on 15/10/6.
 */
@Configuration
public class DefaultHttpClientConfiguration {

    @Bean(name = "defaultHttpClientConnectionManager")
    public HttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();
        clientConnectionManager.setMaxTotal(1000); // 整个连接池的并发
        clientConnectionManager.setDefaultMaxPerRoute(1000); // 每个主机的并发
        return clientConnectionManager;
    }

    @Bean(name = "defaultHttpClient")
    public HttpClient httpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(httpClientConnectionManager());

        return builder.build();
    }

    @Bean(name = "defaultClientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient());
        clientHttpRequestFactory.setConnectTimeout(2000);
        clientHttpRequestFactory.setReadTimeout(10000);
        return clientHttpRequestFactory;
    }

    @Bean(name = "defaultRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean(name = "callbackRestTemplate")
    public RestTemplate callbackRestTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient());
        clientHttpRequestFactory.setConnectTimeout(2000);
        clientHttpRequestFactory.setReadTimeout(2000);
        return new RestTemplate(clientHttpRequestFactory());
    }
}
