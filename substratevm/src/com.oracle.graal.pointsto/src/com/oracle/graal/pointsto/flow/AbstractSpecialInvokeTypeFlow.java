/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.pointsto.flow;

import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.meta.PointsToAnalysisMethod;
import com.oracle.graal.pointsto.typestate.TypeState;
import com.oracle.graal.pointsto.util.AnalysisError;
import com.oracle.svm.common.meta.MultiMethod.MultiMethodKey;

import jdk.vm.ci.code.BytecodePosition;

public abstract class AbstractSpecialInvokeTypeFlow extends DirectInvokeTypeFlow {
    protected TypeState seenReceiverTypes = TypeState.forEmpty();

    protected AbstractSpecialInvokeTypeFlow(BytecodePosition invokeLocation, AnalysisType receiverType, PointsToAnalysisMethod targetMethod,
                    TypeFlow<?>[] actualParameters, ActualReturnTypeFlow actualReturn, MultiMethodKey callerMultiMethodKey) {
        super(invokeLocation, receiverType, targetMethod, actualParameters, actualReturn, callerMultiMethodKey);
    }

    protected AbstractSpecialInvokeTypeFlow(PointsToAnalysis bb, MethodFlowsGraph methodFlows, AbstractSpecialInvokeTypeFlow original) {
        super(bb, methodFlows, original);
    }

    @Override
    protected void onFlowEnabled(PointsToAnalysis bb) {
        if (getReceiver().isFlowEnabled()) {
            bb.postTask(() -> onObservedUpdate(bb));
        }
    }

    @Override
    public boolean addState(PointsToAnalysis bb, TypeState add, boolean postFlow) {
        throw AnalysisError.shouldNotReachHere("The SpecialInvokeTypeFlow should not be updated directly.");
    }

    @Override
    public void update(PointsToAnalysis bb) {
        throw AnalysisError.shouldNotReachHere("The SpecialInvokeTypeFlow should not be updated directly.");
    }

    @Override
    public abstract void onObservedUpdate(PointsToAnalysis bb);

    @Override
    public void onObservedSaturated(PointsToAnalysis bb, TypeFlow<?> observed) {
        /* When the receiver flow saturates start observing the flow of the receiver type. */
        replaceObservedWith(bb, receiverType);
    }

    @Override
    public String toString() {
        return "SpecialInvoke<" + targetMethod.format("%h.%n") + ">" + ":" + getStateDescription();
    }

}
