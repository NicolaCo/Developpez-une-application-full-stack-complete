package com.openclassrooms.mddapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new SortDirectionConverter());
    }

    private static final class SortDirectionConverter implements Converter<String, Sort.Direction> {

        @Override
        public Sort.Direction convert(String source) {
            return Sort.Direction.fromString(source.trim());
        }
    }
}
