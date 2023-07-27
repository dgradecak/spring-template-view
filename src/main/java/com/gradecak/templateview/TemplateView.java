package com.gradecak.templateview;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * should be used on a presentation model class or on a RequestMapping method
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TemplateView {

	String value();

	String model() default "model";

	String error() default "fragment/error";

	// if true will override the model field when the same name is used
	boolean flattenOnMap() default true;

	public static record TemplateViewContext(String view, String itemName, String errorView, boolean flattenMap) {
	}
}
