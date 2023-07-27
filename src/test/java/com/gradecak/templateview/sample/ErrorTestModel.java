package com.gradecak.templateview.sample;

import com.gradecak.templateview.TemplateView;

@TemplateView(value = "nonexistingview", error = "error_test")
public record ErrorTestModel(String id) {

}
