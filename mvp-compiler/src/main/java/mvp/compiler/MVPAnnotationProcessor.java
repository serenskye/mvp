package com.joincoup.app.mvp.processor;

import com.google.auto.service.AutoService;
import com.joincoup.app.mvp.MVPView;
import com.joincoup.app.mvp.presenter.PresenterFactory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static java.util.Collections.singleton;

@AutoService(Processor.class)
public class MVPAnnotationProcessor extends AbstractProcessor {

  private static final String PACKAGE_NAME = "com.joincoup.app.mvp.presenter";
  private static final String FACTORY_METHOD_NAME = "createPresenter";
  private static final String FACTORY_CLASS_NAME = "PresenterFactoryImpl";

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;
  private Set<PresenterAnnotatedClass> presenterAnnotatedClasses = new HashSet<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return singleton(Presenter.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Presenter.class)) {
      try {

        // Check if a class has been annotated with @Factory
        if (annotatedElement.getKind() != ElementKind.CLASS) {
          throw new ProcessingException(annotatedElement, "Only classes can be annotated with @%s", Presenter.class.getSimpleName());
        }

        // We can cast it, because we know that it of ElementKind.CLASS
        TypeElement typeElement = (TypeElement) annotatedElement;
        System.out.println("annotated element " + typeElement.getSimpleName());

        PresenterAnnotatedClass annotatedClass = new PresenterAnnotatedClass(typeElement, typeUtils);

        Validator.checkValidClass(elementUtils, typeUtils, annotatedClass);

        presenterAnnotatedClasses.add(annotatedClass);
      } catch (ProcessingException e) {
        error(annotatedElement, "MVP processing exception " + e.getMessage());
      }
    }

    System.out.println("write code - MVP processor");
    writeCode();
    presenterAnnotatedClasses.clear();

    return true;
  }

  private void writeCode() {
    System.out.println("presenter annotated clases " + presenterAnnotatedClasses.size());
    if (presenterAnnotatedClasses.size() == 0) {
      return;
    }

    TypeSpec.Builder factoryBuilder = TypeSpec.classBuilder(FACTORY_CLASS_NAME)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addJavadoc("Auto generated class")
        .addSuperinterface(PresenterFactory.class);

    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(FACTORY_METHOD_NAME)
        .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
        .returns(com.joincoup.app.mvp.presenter.Presenter.class)
        .addParameter(MVPView.class, "view", Modifier.FINAL);

    for (PresenterAnnotatedClass clazz : presenterAnnotatedClasses) {
      clazz.generatePresenterConstructor(methodBuilder);
    }

    methodBuilder.addStatement("throw new $T(\"could not find Presenter, please make sure you annotate with @Presenter\")", RuntimeException.class);
    factoryBuilder.addMethod(methodBuilder.build());
    TypeSpec spec = factoryBuilder.build();
    JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, spec).build();
    try {
      javaFile.writeTo(filer);
    } catch (IOException e) {
      error(null, "cannnot write to filer  - MVP processor" + e);
    }
  }

  /**
   * Prints an error message
   *
   * @param e The element which has caused the error. Can be null
   * @param msg The error message
   */
  public void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }
}
