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
package jdk.graal.compiler.core.test;

import jdk.graal.compiler.core.common.NativeImageSupport;
import jdk.graal.compiler.nodes.StructuredGraph;
import jdk.graal.compiler.nodes.java.MethodCallTargetNode;
import jdk.graal.compiler.nodes.spi.CoreProviders;
import jdk.graal.compiler.phases.VerifyPhase;

import jdk.graal.compiler.serviceprovider.GraalServices;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.services.Services;

/**
 * Checks against calls to {@link System#getProperty(String)},
 * {@link System#getProperty(String, String)} and {@link System#getProperties()}.
 * <p>
 * System properties can be modified by application code so {@link GraalServices#getSavedProperty}
 * should be used instead.
 * <p>
 * In the context of LibGraal, JVMCI code is run at image build time in a non-boot class loader and
 * so JVMCI native methods will fail to link (they are only linked by the boot loader). As
 * {@link Services#getSavedProperties()} calls a JVMCI native method, it cannot be used during build
 * time initialization. Instead, {@link GraalServices#getSavedProperties()} must be used.
 */
public class VerifySystemPropertyUsage extends VerifyPhase<CoreProviders> {

    static final Class<?>[] BOXES = {Integer.class, Long.class, Boolean.class, Float.class, Double.class};

    @Override
    protected void verify(StructuredGraph graph, CoreProviders context) {
        MetaAccessProvider metaAccess = context.getMetaAccess();
        final ResolvedJavaType servicesType = metaAccess.lookupJavaType(Services.class);
        final ResolvedJavaType graalServicesType = metaAccess.lookupJavaType(GraalServices.class);
        final ResolvedJavaType systemType = metaAccess.lookupJavaType(System.class);
        final ResolvedJavaType[] boxTypes = new ResolvedJavaType[BOXES.length];
        for (int i = 0; i < boxTypes.length; i++) {
            boxTypes[i] = metaAccess.lookupJavaType(BOXES[i]);
        }

        ResolvedJavaMethod caller = graph.method();
        String holderQualified = caller.format("%H");
        String holderUnqualified = caller.format("%h");
        String packageName = holderQualified.equals(holderUnqualified) ? "" : holderQualified.substring(0, holderQualified.length() - holderUnqualified.length() - 1);
        if (holderQualified.equals("jdk.graal.compiler.hotspot.JVMCIVersionCheck") && caller.getName().equals("main")) {
            // The main method in JVMCIVersionCheck is only called from the shell
            return;
        } else if (packageName.startsWith("com.oracle.truffle") || packageName.startsWith("org.graalvm.polyglot") ||
                        packageName.startsWith("org.graalvm.home") || packageName.equals("com.oracle.truffle.runtime.hotspot")) {
            // Truffle, SDK and Truffle runtime cannot use GraalServices
            return;
        } else if (packageName.startsWith("com.oracle.svm")) {
            // SVM must read system properties in:
            // * its JDK substitutions to mimic required JDK semantics
            // * native-image for config info
            return;
        } else if (packageName.startsWith("jdk.jfr")) {
            // JFR for SVM must read system properties in:
            // * its JDK substitutions to mimic required JDK semantics
            // * native-image for config info
            return;
        } else if (packageName.startsWith("ai.onnxruntime")) {
            // Do not verify the ONNX Java Inference Runtime.
            return;
        } else if (holderQualified.equals("jdk.graal.compiler.hotspot.HotSpotReplacementsImpl") && caller.getName().equals("registerSnippet")) {
            // We allow opening snippet registration in jargraal unit tests.
            return;
        } else if (holderQualified.equals(NativeImageSupport.class.getName())) {
            // Called as part of initializing GraalServices
            return;
        }
        for (MethodCallTargetNode t : graph.getNodes(MethodCallTargetNode.TYPE)) {
            ResolvedJavaMethod callee = t.targetMethod();
            if (forbiddenCallee(callee, servicesType, systemType)) {
                if (caller.getDeclaringClass().equals(graalServicesType)) {
                    continue;
                }
                throw new VerificationError(t, "call to %s is prohibited. Call GraalServices.%s instead.",
                                callee.format("%H.%n(%p)"), callee.getName());
            } else {
                for (int i = 0; i < boxTypes.length; i++) {
                    ResolvedJavaType boxType = boxTypes[i];
                    if (callee.getDeclaringClass().equals(boxType)) {
                        String simpleName = boxType.toJavaName(false);
                        if (callee.getName().equals("get" + simpleName)) {
                            throw new VerificationError(t, "call to %s is prohibited. Call %s.parse%s(GraalServices.getSavedProperty(String)) instead.",
                                            callee.format("%H.%n(%p)"),
                                            simpleName, simpleName);
                        }
                    }
                }
            }
        }
    }

    private static boolean forbiddenCallee(ResolvedJavaMethod callee, ResolvedJavaType servicesType, ResolvedJavaType systemType) {
        if (callee.getDeclaringClass().equals(systemType)) {
            return callee.getName().equals("getProperty") || callee.getName().equals("getProperties");
        } else if (callee.getDeclaringClass().equals(servicesType)) {
            return callee.getName().equals("getSavedProperty") || callee.getName().equals("getSavedProperties");
        }
        return false;
    }
}
