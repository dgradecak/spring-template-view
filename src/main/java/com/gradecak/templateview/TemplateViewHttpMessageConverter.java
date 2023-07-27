package com.gradecak.templateview;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;

import com.gradecak.templateview.TemplateView.TemplateViewContext;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class TemplateViewHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

	public static final MediaType APPLICATION_PROBLEM_HTML = new MediaType("application", "problem+html");;
	public static final String APPLICATION_PROBLEM_HTML_VALUE = "application/problem+hml";

	private final ViewResolver contentNegotiatingResolver;
	private final LocaleResolver localeResolver;

	public TemplateViewHttpMessageConverter(ViewResolver contentNegotiatingResolver, LocaleResolver localeResolver,
			MediaType... mediaTypes) {
		setSupportedMediaTypes(List.of(mediaTypes));
		setDefaultCharset(StandardCharsets.UTF_8);

		this.contentNegotiatingResolver = contentNegotiatingResolver;
		this.localeResolver = localeResolver;
	}

	@Override
	public boolean canWrite(@Nullable Type type, @Nullable Class<?> clazz, @Nullable MediaType mediaType) {
		if (!canWrite(clazz, mediaType)) {
			return false;
		}

		RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
		HandlerMethod method = (HandlerMethod) currentRequestAttributes
				.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, 0);

		TemplateView annotation = method != null ? method.getMethodAnnotation(TemplateView.class) : null;
		if (annotation == null) {
			annotation = AnnotationUtils.findAnnotation(clazz, TemplateView.class);
		}

		return annotation != null ? true : false;
	}

	protected void writeInternal(Object obj, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
		HandlerMethod method = (HandlerMethod) currentRequestAttributes
				.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, 0);

		TemplateView annotation = method != null ? method.getMethodAnnotation(TemplateView.class) : null;
		if (annotation == null) {
			annotation = AnnotationUtils.findAnnotation(obj.getClass(), TemplateView.class);
		}

		if (annotation != null) {
			writeHtml(obj, outputMessage, new TemplateViewContext(annotation.value(), annotation.model(),
					annotation.error(), annotation.flattenOnMap()));
		}
	}

	private void writeHtml(Object obj, HttpOutputMessage outputMessage, TemplateViewContext htmlViewContext)
			throws IOException {
		HttpServletRequest request = getCurrentHttpRequest()
				.orElseThrow(() -> new HttpMessageNotWritableException("execution is not bound to a http request"));

		HttpServletResponse response = getCurrentHttpResponse()
				.orElseThrow(() -> new HttpMessageNotWritableException("execution is not bound to a http response"));

		Locale locale = localeResolver.resolveLocale(request);

		HttpServletResponse responseWrapper = new NonClosingHttpServletResponseWrapper(response);

		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put(htmlViewContext.itemName(), obj);

		if (obj != null && Map.class.isAssignableFrom(obj.getClass()) && htmlViewContext.flattenMap()) {

			Set<?> keySet = ((Map<?, ?>) obj).keySet();
			long size = keySet.stream().filter(k -> k.getClass().isAssignableFrom(String.class)).count();

			// TODO instead of rejecting all entries we might only accept entries with keys
			// of type String.class
			if (size == keySet.stream().count()) {
				hashMap.putAll((Map<String, ?>) obj);
			}
		}

		try {
			contentNegotiatingResolver.resolveViewName(htmlViewContext.view(), locale).render(hashMap, request,
					responseWrapper);
		} catch (Throwable ex) {
			// add logger -> cannot execute view

			try {
				hashMap.put("exception", ex);
				hashMap.put("response", responseWrapper);

				contentNegotiatingResolver.resolveViewName(htmlViewContext.errorView(), locale).render(hashMap, request,
						responseWrapper);
				return;
			} catch (Exception e) {
				// add logger -> cannot execute errorView
			}
			throw new HttpMessageNotWritableException("Could not write the template: " + ex.getMessage(), ex);
		}
	}

	public static Optional<HttpServletRequest> getCurrentHttpRequest() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
				.filter(ServletRequestAttributes.class::isInstance).map(ServletRequestAttributes.class::cast)
				.map(ServletRequestAttributes::getRequest);
	}

	public static Optional<HttpServletResponse> getCurrentHttpResponse() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
				.filter(ServletRequestAttributes.class::isInstance).map(ServletRequestAttributes.class::cast)
				.map(ServletRequestAttributes::getResponse);
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	protected boolean canRead(MediaType mediaType) {
		return false;
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
		return (ProblemDetail.class.isAssignableFrom(clazz) ? getMediaTypesForProblemDetail()
				: getSupportedMediaTypes());
	}

	protected List<MediaType> getMediaTypesForProblemDetail() {
		return Collections.singletonList(APPLICATION_PROBLEM_HTML);
	}

	private static class NonClosingServletOutputStream extends ServletOutputStream {
		private final ServletOutputStream targetStream;

		private NonClosingServletOutputStream(ServletOutputStream targetStream) {
			Assert.notNull(targetStream, "Target ServletOutputStream must not be null");
			this.targetStream = targetStream;
		}
		
		@Override
		public void close() throws IOException {
		}
		
		@Override
		public void write(int b) throws IOException {
			targetStream.write(b);
		}

		@Override
		public void flush() throws IOException {
			targetStream.flush();
		}

		@Override
		public boolean isReady() {
			return targetStream.isReady();
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			targetStream.setWriteListener(writeListener);
		}
	}
	
	private static class NonClosingHttpServletResponseWrapper extends HttpServletResponseWrapper {
		private final NonClosingServletOutputStream os;

		private NonClosingHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
			super(response);
			this.os = new NonClosingServletOutputStream(response.getOutputStream());
		}

		public java.io.PrintWriter getWriter() throws IOException {
			return new PrintWriter(os);
		};

		public ServletOutputStream getOutputStream() throws IOException {
			return os;
		};
	};
}
