package com.genohm.autoproxy;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.javawriter.JavaWriter;

@AutoService(Processor.class)
public class AutoProxyProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoProxy.class);
			for (Element element: elements) {
				processElement((TypeElement)element);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private void processElement(TypeElement element) throws IOException {
		String packageName = TypeHelper.getPackageName(element);
		
		String proxyName = "AutoProxy_" + element.getSimpleName();
		
		JavaFileObject sourceFile = processingEnv.getFiler()
				.createSourceFile(packageName + "." + proxyName, element);
		try (Writer writer = sourceFile.openWriter();
			JavaWriter javaWriter = new JavaWriter(writer)) {
			
			List<String> generics = Lists.newArrayList();
			for (TypeParameterElement generic: element.getTypeParameters()) {
				//TODO this doesn't work with bounds yet
				generics.add(generic.toString());
			}
			
			
			String genericString = "";
			if (generics.size() > 0) {
				genericString = "<" + Joiner.on(",").join(generics) +">";
			}
			
			javaWriter.emitPackage(packageName)
				.emitEmptyLine()
				.beginType(proxyName + genericString, 
						"class", 
						EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), 
						null, 
						element.getSimpleName().toString() + genericString);
			
			
			
			Iterable<ExecutableElement> methods = collectMethods(element);
			
			createConstructor(element, javaWriter, proxyName, genericString);
			proxyMethods(javaWriter, methods);
			
			javaWriter.endType();
		}
	}
	
	private Set<TypeElement> findAllInterfaces(TypeElement element) {
		Set<TypeElement> elements = new LinkedHashSet<>();
		findAllInterfaces(element, elements);
		return elements;
	}

	private void findAllInterfaces(TypeElement element, Set<TypeElement> elements) {
		elements.add(element);
		for (TypeMirror superElement: element.getInterfaces()) {
			if (superElement.getKind() == TypeKind.DECLARED) {
				findAllInterfaces(((TypeElement)((DeclaredType)superElement).asElement()), elements);
			}
		}
	}

	private void createConstructor(Element element, JavaWriter javaWriter, String proxyName, String genericString) throws IOException {
		javaWriter.emitField(element.getSimpleName().toString() + genericString, "instance", Sets.newHashSet(Modifier.PRIVATE, Modifier.FINAL));

		javaWriter.beginConstructor(Sets.newHashSet(Modifier.PUBLIC), 
				element.getSimpleName().toString() + genericString, 
				"instance");
		
		javaWriter.emitStatement("this.instance = instance");
		
		javaWriter.endConstructor();
	}
	
	private void proxyMethods(JavaWriter javaWriter, Iterable<ExecutableElement> methods) throws IOException {
		for (ExecutableElement method: methods) {
			List<String> parameters = Lists.newArrayList();
			List<String> parameterNames = Lists.newArrayList();
			for (VariableElement parameter: method.getParameters()) {
				parameters.add(parameter.asType().toString());
				parameters.add(parameter.getSimpleName().toString());
				parameterNames.add(parameter.getSimpleName().toString());
			}
			
			List<String> throwsTypes = Lists.newArrayList();
			for (TypeMirror thrownType: method.getThrownTypes()) {
				throwsTypes.add(thrownType.toString());
			}
			
			javaWriter.beginMethod(method.getReturnType().toString(), 
				method.getSimpleName().toString(),
				EnumSet.of(Modifier.PUBLIC), 
				parameters,
				throwsTypes);
			
			StringBuilder sb = new StringBuilder();
			if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
				sb.append("return ");
			}
			sb.append("instance.");
			sb.append(method.getSimpleName().toString() + "(");
			sb.append(Joiner.on(',').join(parameterNames));
			sb.append(")");
			javaWriter.emitStatement(sb.toString());

			
			javaWriter.endMethod();
		}
	}
	
	private Iterable<ExecutableElement> collectMethods(TypeElement element) {
		Set<ExecutableElement> methods = Sets.newHashSet();
		System.err.println(findAllInterfaces(element));
		for (TypeElement all: findAllInterfaces(element)) {
			Iterable<ExecutableElement> subMethods = Iterables.filter(TypeHelper.getMethods(all), 
					Predicates.and(
						MethodPredicates.IS_PUBLIC,
						Predicates.not(MethodPredicates.IS_STATIC)));
			for (ExecutableElement subMethod: subMethods) {
				methods.add(subMethod);
			}
		}
		return methods;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Sets.newHashSet(AutoProxy.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
