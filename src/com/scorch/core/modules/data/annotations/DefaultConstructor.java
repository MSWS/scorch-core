package com.scorch.core.modules.data.annotations;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * @deprecated Tried to implement this into the project, didn't get it to work without making it into a jar and use it as extra dependency
 * and making an extra dependency just for an Annotation Processor seems a bit over kill
 *
 * @author Gijs de Jong
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DefaultConstructor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for(TypeElement type : ElementFilter.typesIn(roundEnv.getRootElements())){
            if(needsConstructor(type)){
                checkConstructor(type);
            }
        }
        return false;
    }

    private void checkConstructor (TypeElement type){
        for(ExecutableElement constructor : ElementFilter.constructorsIn(type.getEnclosedElements())){
            if(constructor.getParameters().isEmpty()){
                // default constructor found
                return;
            }
        }

        // no constructor found
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "type is missing a default constructor", type);

    }

    private boolean needsConstructor (TypeElement type){
        return type.getAnnotation(DataObject.class) != null;
    }
}
