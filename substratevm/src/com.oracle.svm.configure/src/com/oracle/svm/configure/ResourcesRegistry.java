/*
 * Copyright (c) 2019, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.configure;

import java.util.Collection;
import java.util.Locale;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.impl.ConfigurationCondition;
import org.graalvm.nativeimage.impl.RuntimeResourceSupport;

public interface ResourcesRegistry<C> extends RuntimeResourceSupport<C> {

    @SuppressWarnings("unchecked")
    static ResourcesRegistry<ConfigurationCondition> singleton() {
        return ImageSingletons.lookup(ResourcesRegistry.class);
    }

    void addClassBasedResourceBundle(C condition, String basename, String className);

    /**
     * Although the interface-methods below are already defined in the super-interface
     * {@link RuntimeResourceSupport} they are also needed here for legacy code that accesses them
     * reflectively.
     */
    @Deprecated
    default void addResources(C condition, String pattern) {
        addResources(condition, pattern, "unknown");
    }

    @Override
    void ignoreResources(C condition, String pattern);

    @Override
    void addResourceBundles(C condition, String name);

    @Override
    void addResourceBundles(C condition, String basename, Collection<Locale> locales);
}
