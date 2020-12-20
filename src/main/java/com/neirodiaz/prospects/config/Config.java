package com.neirodiaz.prospects.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.neirodiaz.prospects.client.NationalArchivesClient;
import com.neirodiaz.prospects.client.NationalRegistryClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class Config {

    @Value("${external.service.national.basePath}")
    private String externalBasePath;

    @Bean
    public NationalArchivesClient getNationalArchiveClient() {
        return getRetrofitClient(externalBasePath).create(NationalArchivesClient.class);
    }

    @Bean
    public NationalRegistryClient getNationalRegistryClient() {
        return getRetrofitClient(externalBasePath).create(NationalRegistryClient.class);
    }

    private Retrofit getRetrofitClient(String basePath) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        return new Retrofit.Builder()
                .baseUrl(basePath)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(clientBuilder.build())
                .build();
    }
}
