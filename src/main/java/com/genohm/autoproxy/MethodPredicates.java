package com.genohm.autoproxy;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import com.google.common.base.Predicate;

public class MethodPredicates {

	public static final Predicate<ExecutableElement> IS_PUBLIC = hasModifier(Modifier.PUBLIC);
	public static final Predicate<ExecutableElement> IS_STATIC = hasModifier(Modifier.STATIC);

	private MethodPredicates() {
		
	}
	
	public static Predicate<ExecutableElement> hasModifier(final Modifier modifier) {
		return new Predicate<ExecutableElement>() {
			@Override
			public boolean apply(ExecutableElement input) {
				return input.getModifiers().contains(modifier);
			}
		};
	}
	
}
