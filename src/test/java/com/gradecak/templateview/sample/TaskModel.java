package com.gradecak.templateview.sample;

import com.gradecak.templateview.TemplateView;

@TemplateView(value = "view2", error = "error_test")
public record TaskModel(String id) {

}
