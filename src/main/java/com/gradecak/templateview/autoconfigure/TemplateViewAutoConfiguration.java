package com.gradecak.templateview.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.gradecak.templateview.TemplateViewHttpMessageConverter;
import com.gradecak.templateview.extra.HtmxRequest.HtmxRequestArgumentResolver;

@AutoConfiguration(after = WebMvcAutoConfiguration.class)
public class TemplateViewAutoConfiguration implements WebMvcConfigurer {

	@AutoConfiguration
	@ConditionalOnClass(HtmxRequestArgumentResolver.class)
	@ConditionalOnProperty(name = "templateview.htmx.enabled", havingValue = "true", matchIfMissing = true)
	public class HtmxConfiguration implements WebMvcConfigurer {

		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			resolvers.add(new HtmxRequestArgumentResolver());
		}
	}

	@AutoConfiguration
	@ConditionalOnBean({ ContentNegotiatingViewResolver.class, LocaleResolver.class })
	@ConditionalOnClass(TemplateViewHttpMessageConverter.class)
	@ConditionalOnProperty(name = "templateview.enabled", havingValue = "true", matchIfMissing = true)
	public class TemplateViewConfiguration implements WebMvcConfigurer {

		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			configurer.defaultContentType(MediaType.APPLICATION_JSON).mediaType("html", MediaType.TEXT_HTML);
		}

		@Bean
		@ConditionalOnMissingBean(TemplateViewHttpMessageConverter.class)
		TemplateViewHttpMessageConverter TemplateViewHttpMessageConverter(
				@Qualifier("viewResolver") ContentNegotiatingViewResolver contentNegotiatingResolver,
				LocaleResolver localeResolver) {
			return new TemplateViewHttpMessageConverter(contentNegotiatingResolver, localeResolver, MediaType.TEXT_HTML,
					new MediaType("text", "*+html"));
		}
	}
}
