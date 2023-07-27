package com.gradecak.templateview.extra;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gradecak.templateview.TemplateViewHttpMessageConverter;

import jakarta.servlet.http.HttpServletRequest;

public class HtmxRequest {
	private final boolean hxRequest;

	private HtmxRequest() {
		this(false);
	}

	private HtmxRequest(boolean hxRequest) {
		this.hxRequest = hxRequest;
	}

	public boolean isHxRequest() {
		return hxRequest;
	}
	
	public static HtmxRequest currentHtmxRequest() {
		return of(TemplateViewHttpMessageConverter.getCurrentHttpRequest().get());
	}

	public static HtmxRequest of(NativeWebRequest webRequest) {
		return of(webRequest.getNativeRequest(HttpServletRequest.class));
	}
	
	public static HtmxRequest of(HttpServletRequest request) {
		return new HtmxRequest(Boolean.valueOf(request.getHeader("HX-Request")));
	}
	
	public static class HtmxRequestArgumentResolver implements HandlerMethodArgumentResolver {

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
			if (parameter.getParameterType().isAssignableFrom(HtmxRequest.class)) {
				return HtmxRequest.of(webRequest);
			}
			return null;
		}

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return HtmxRequest.class == parameter.getParameterType();
		}
	}
}
