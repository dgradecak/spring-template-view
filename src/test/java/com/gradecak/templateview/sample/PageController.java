package com.gradecak.templateview.sample;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/html")
	String testView2() {
		return "page/test";
	}
}
