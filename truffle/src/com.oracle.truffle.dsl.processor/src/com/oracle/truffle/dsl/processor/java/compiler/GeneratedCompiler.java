/*
 * Copyright (c) 2018, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.dsl.processor.java.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.oracle.truffle.dsl.processor.java.model.CodeElement;
import com.oracle.truffle.dsl.processor.java.model.CodeTypeElement;

/**
 * Compiler implementation for classes that are generated. All classes that extend CodeElement.
 */
public class GeneratedCompiler extends AbstractCompiler {

    public static boolean isValidElement(Element currentElement) {
        return currentElement instanceof CodeElement;
    }

    public List<? extends Element> getEnclosedElementsInDeclarationOrder(TypeElement type) {
        return ((CodeTypeElement) type).getEnclosedElements();
    }

    public List<? extends Element> getAllMembersInDeclarationOrder(ProcessingEnvironment environment, TypeElement type) {
        List<Element> elements = new ArrayList<>();

        TypeElement currentType = type;
        while (currentType != null) {
            elements.addAll(CompilerFactory.getCompiler(currentType).getEnclosedElementsInDeclarationOrder(currentType));
            TypeMirror superClass = currentType.getSuperclass();
            if (superClass != null && superClass.getKind() == TypeKind.DECLARED) {
                currentType = (TypeElement) ((DeclaredType) currentType.getSuperclass()).asElement();
            } else {
                currentType = null;
            }
        }

        return elements;
    }

    @Override
    protected boolean emitDeprecationWarningImpl(ProcessingEnvironment environment, Element element) {
        return false;
    }

    @Override
    public File getEnclosingSourceFile(ProcessingEnvironment processingEnv, Element element) {
        throw new UnsupportedOperationException("generated element");
    }

}
