package com.gradecak.templateview.sample;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradecak.templateview.TemplateView;
import com.gradecak.templateview.extra.HtmxRequest;

@RestController
public class RestApiController {
	
	@GetMapping(value = "json")
	public ResponseEntity<TaskModel> task(HtmxRequest r) {
		return ResponseEntity.ok(new TaskModel("1"));
	}

	@GetMapping(value = "view")
	public ResponseEntity<ViewModel> nonview() {
		HtmxRequest currentHtmxRequest = HtmxRequest.currentHtmxRequest();
		return ResponseEntity.ok(new ViewModel("11"));
	}


	@GetMapping(value = "error1")
	@TemplateView(value = "nonexistingview", error = "fragment/error")
	public ResponseEntity<ErrorTestModel> errorTest() {
		return ResponseEntity.ok(new ErrorTestModel("1"));
	}

	@GetMapping(value = "error2")
	public ResponseEntity<ErrorTestModel> errorTest2() {
		return ResponseEntity.ok(new ErrorTestModel("1"));
	}

	@GetMapping(value = "requestmethod")
	@TemplateView(value = "requestmethod")
	public ResponseEntity<ErrorTestModel> requestmethod(HtmxRequest r) {
		return ResponseEntity.ok(new ErrorTestModel("1"));
	}
	
	@GetMapping(value = "combine")
	@TemplateView(value = "combine")
	public ResponseEntity<Map<Object, Object>> combine() {
		return ResponseEntity.ok(Map.of( "model", new ViewModel("11"), "error", new ErrorTestModel("1")));
	}
}
