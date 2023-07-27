package com.gradecak.templateview;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.gradecak.templateview.autoconfigure.TemplateViewAutoConfiguration;
import com.gradecak.templateview.sample.PageController;
import com.gradecak.templateview.sample.RestApiController;

@WebMvcTest({ RestApiController.class, PageController.class })
public class RestApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@TestConfiguration
	@ImportAutoConfiguration(TemplateViewAutoConfiguration.class)
	public static class TestConfig {

		@SpringBootApplication
		public static class TestApplication {

		}

//		@Bean
//		TemplateViewHttpMessageConverter TemplateViewHttpMessageConverter(
//				ContentNegotiatingViewResolver contentNegotiatingResolver, LocaleResolver localeResolver) {
//			return new TemplateViewHttpMessageConverter(contentNegotiatingResolver, localeResolver, MediaType.TEXT_HTML);
//		}
	}

	@Test
	void whenRestControllerAndJsonContentTypeIsRequested_expect_jacksonViewAndjsonContentType() throws Exception {
		MockHttpServletRequestBuilder req = get("/json").accept(MediaType.ALL);

		mockMvc.perform(req).andExpectAll(status().isOk(),
				content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
	}

	@Test
	void whenControllerAndHtmlContentTypeIsRequested_expect_thymeleafViewAndHtmlContentType() throws Exception {
		MockHttpServletRequestBuilder req = get("/html").accept(MediaType.ALL);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().string("test html"));
	}

	@Test
	void whenAnnotationOnControllerMethodAndMissingView_expect_configuredErrorViewAnd200() throws Exception {
		MockHttpServletRequestBuilder req = get("/error1").accept(MediaType.TEXT_HTML);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().string(containsString("<div>status: 200")));
	}

	@Test
	void whenAnnotationOnRespresentationModelAndMissingView_expect_configuredErrorViewAnd200() throws Exception {
		MockHttpServletRequestBuilder req = get("/error2").accept(MediaType.TEXT_HTML);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().xml("<div>error</div> "));
	}

	@Test
	void whenAnnotationOnControllerMethod_expect_modelAnnotationIsOverridenAndHtml() throws Exception {
		MockHttpServletRequestBuilder req = get("/requestmethod").accept(MediaType.TEXT_HTML);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().xml("<template>1</template>"));
	}

	@Test
	void whenAnnotationOnRespresentationModel_expect_configuredViewAnd200() throws Exception {
		MockHttpServletRequestBuilder req = get("/view").accept(MediaType.TEXT_HTML);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().xml("<template>aaa</template>"));
	}

	@Test
	void whenResponseTypeMap_expect_combinedTemplateModelAndHtml() throws Exception {
		MockHttpServletRequestBuilder req = get("/combine").accept(MediaType.TEXT_HTML);

		mockMvc.perform(req).andExpectAll(status().isOk(), content().contentTypeCompatibleWith(MediaType.TEXT_HTML),
				content().xml("<template>11</template>"));
	}
}
