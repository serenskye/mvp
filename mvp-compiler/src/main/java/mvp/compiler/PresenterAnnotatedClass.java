package com.joincoup.app.mvp.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;

public class PresenterAnnotatedClass {

  private TypeElement annotatedClassElement;
  private String qualifiedName;
  private TypeMirror view;
  String viewSimpleName;
  private ClassName presenterClassName;

  public PresenterAnnotatedClass(TypeElement classElement, Types typeUtils) throws ProcessingException {

    TypeMirror superclass = classElement.getSuperclass();
    view = getGenericType(superclass);

    annotatedClassElement = classElement;
    qualifiedName = annotatedClassElement.getQualifiedName().toString();

    viewSimpleName = typeUtils.asElement(view).getSimpleName().toString();
    presenterClassName = ClassName.get(annotatedClassElement);
  }

  public void generatePresenterConstructor(MethodSpec.Builder builder) {
    builder.beginControlFlow("if(view instanceof $T)", view);
    builder.addStatement("return new $L()", presenterClassName);
    builder.endControlFlow();
  }

  /**
   * The original element that was annotated with @Factory
   */
  public TypeElement getTypeElement() {
    return annotatedClassElement;
  }

  public CharSequence getQualifiedName() {
    return qualifiedName;
  }

  public static TypeMirror getGenericType(final TypeMirror type)
  {
    final TypeMirror[] result = { null };

    type.accept(new SimpleTypeVisitor6<Void, Void>()
    {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void v)
      {
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty())
        {
          result[0] = typeArguments.get(0);
        }
        return null;
      }
      @Override
      public Void visitPrimitive(PrimitiveType primitiveType, Void v)
      {
        return null;
      }
      @Override
      public Void visitArray(ArrayType arrayType, Void v)
      {
        return null;
      }
      @Override
      public Void visitTypeVariable(TypeVariable typeVariable, Void v)
      {
        return null;
      }
      @Override
      public Void visitError(ErrorType errorType, Void v)
      {
        return null;
      }
      @Override
      protected Void defaultAction(TypeMirror typeMirror, Void v)
      {
        throw new UnsupportedOperationException();
      }
    }, null);

    return result[0];
  }
}
