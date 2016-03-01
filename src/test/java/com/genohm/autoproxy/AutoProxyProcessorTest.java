package com.genohm.autoproxy;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.genohm.autoproxy.AutoProxyProcessor;
import com.google.testing.compile.JavaFileObjects;

import javax.tools.JavaFileObject;
import java.io.IOException;

@RunWith(JUnit4.class)
public class AutoProxyProcessorTest {

	@Test
	public void compileSimpleMethod() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld", 
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"",
				"@AutoProxy",
				"public interface HelloWorld {",
				"",
				"	public String hello();",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld", 
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{", 
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public String hello() {",
				"		return instance.hello()",
				"	}",
				"",
				"}"));
	}
	
	
	@Test
	public void compilesVoidMethod() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld", 
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"",
				"@AutoProxy",
				"public interface HelloWorld {",
				"",
				"	public void hello();",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld", 
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{", 
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public void hello() {",
				"		instance.hello()",
				"	}",
				"",
				"}"));
	}
	
	
	@Test
	public void compilesWithArguments() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld", 
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"import java.util.List;",
				"",
				"@AutoProxy",
				"public interface HelloWorld {",
				"",
				"	public void hello(String test, int blah, List<Long> list);",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld", 
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{", 
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public void hello(String test, int blah, java.util.List<Long> list) {",
				"		instance.hello(test, blah, list);",
				"	}",
				"",
				"}"));
	}
	
	@Test
	public void compileExceptionMethod() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld", 
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"",
				"@AutoProxy",
				"public interface HelloWorld {",
				"",
				"	public String hello() throws Exception;",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld", 
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{", 
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public String hello() throws Exception {",
				"		return instance.hello()",
				"	}",
				"",
				"}"));
	}
	
	@Test
	public void compilesExtendedInterfaces() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld", 
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"",
				"@AutoProxy",
				"public interface HelloWorld extends Runnable {",
				"",
				"	public String hello();",
				"",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld", 
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{", 
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public String hello() {",
				"		return instance.hello();",
				"	}",
				"",
				"	public void run() {",
				"		instance.run();",
				"	}",
				"",
				"}"));
	}
	
	@Test
	public void compilesWithGenericType() {
		ASSERT.about(javaSource())
			.that(JavaFileObjects.forSourceLines("good/HelloWorld",
				"package good;",
				"",
				"import com.genohm.autoproxy.AutoProxy;",
				"",
				"@AutoProxy",
				"public interface HelloWorld<E> {",
				"",
				"	public E hello();",
				"",
				"	public void goodbye(E element);",
				"",
				"}"))
			.processedWith(new AutoProxyProcessor())
			.compilesWithoutError()
			.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld",
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld<E> implements HelloWorld<E>{",
				"",
				"	private final HelloWorld<E> instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld<E> instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public E hello() {",
				"		return instance.hello();",
				"	}",
				"",
				"	public void goodbye(E element) {",
				"		instance.goodbye(element);",
				"	}",
				"",
				"}"));
	}

	@Test
	public void compilesWithGenericArguments() throws IOException {
		ASSERT.about(javaSource())
				.that(JavaFileObjects.forSourceLines("good/HelloWorld",
						"package good;",
						"",
						"import com.genohm.autoproxy.AutoProxy;",
						"",
						"@AutoProxy",
						"public interface HelloWorld {",
						"",
						"	public <E> E hello();",
						"",
						"	public <E> void goodbye(E element);",
						"",
						"	public <E, F> void goodbye(E element, F otherElement);",
						"",
						"}"))
				.processedWith(new AutoProxyProcessor())
				.compilesWithoutError()
				.and().generatesSources(JavaFileObjects.forSourceLines("good.AutoProxy_HelloWorld",
				"package good;",
				"",
				"public final class AutoProxy_HelloWorld implements HelloWorld{",
				"",
				"	private final HelloWorld instance",
				"",
				"	public AutoProxy_HelloWorld(HelloWorld instance) {",
				"		this.instance = instance;",
				"	}",
				"",
				"	public <E> E hello() {",
				"		return instance.hello();",
				"	}",
				"",
				"	public <E> void goodbye(E element) {",
				"		instance.goodbye(element);",
				"	}",
				"",
				"	public <E, F> void goodbye(E element, F otherElement) {",
				"		instance.goodbye(element, otherElement);",
				"	}",
				"",
				"}"));
	}
}
