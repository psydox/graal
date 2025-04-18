/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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
package jdk.graal.compiler.hotspot;

import static jdk.graal.compiler.hotspot.meta.HotSpotHostForeignCallsProvider.G1WBPOSTCALL_STACK_ONLY;
import static jdk.graal.compiler.hotspot.meta.HotSpotHostForeignCallsProvider.G1WBPRECALL_STACK_ONLY;

import jdk.graal.compiler.core.common.spi.ForeignCallDescriptor;
import jdk.graal.compiler.core.common.spi.ForeignCallLinkage;
import jdk.graal.compiler.hotspot.meta.HotSpotProviders;
import jdk.graal.compiler.hotspot.replacements.HotSpotReplacementsUtil;
import jdk.graal.compiler.lir.gen.G1BarrierSetLIRTool;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.meta.InvokeTarget;

/**
 * Shared HotSpot specific values required for G1 assembler barrier emission.
 */
public abstract class HotSpotG1BarrierSetLIRTool implements G1BarrierSetLIRTool {

    public HotSpotG1BarrierSetLIRTool(GraalHotSpotVMConfig config, HotSpotProviders providers) {
        this.config = config;
        this.providers = providers;
        this.threadRegister = providers.getRegisters().getThreadRegister();
    }

    protected final Register threadRegister;
    protected final GraalHotSpotVMConfig config;
    protected final HotSpotProviders providers;

    @Override
    public int satbQueueMarkingActiveOffset() {
        return HotSpotReplacementsUtil.g1SATBQueueMarkingActiveOffset(config);
    }

    @Override
    public int satbQueueBufferOffset() {
        return HotSpotReplacementsUtil.g1SATBQueueBufferOffset(config);
    }

    @Override
    public int satbQueueIndexOffset() {
        return HotSpotReplacementsUtil.g1SATBQueueIndexOffset(config);
    }

    @Override
    public byte cleanCardValue() {
        return HotSpotReplacementsUtil.cleanCardValue(config);
    }

    @Override
    public int cardQueueBufferOffset() {
        return HotSpotReplacementsUtil.g1CardQueueBufferOffset(config);
    }

    @Override
    public int cardQueueIndexOffset() {
        return HotSpotReplacementsUtil.g1CardQueueIndexOffset(config);
    }

    @Override
    public byte dirtyCardValue() {
        return config.dirtyCardValue;
    }

    @Override
    public boolean supportsLowLatencyBarriers() {
        return HotSpotReplacementsUtil.supportsG1LowLatencyBarriers(config);
    }

    @Override
    public boolean useConditionalCardMarking() {
        return HotSpotReplacementsUtil.useCondCardMark(config);
    }

    @Override
    public byte youngCardValue() {
        return HotSpotReplacementsUtil.g1YoungCardValue(config);
    }

    @Override
    public long cardTableAddress() {
        return HotSpotReplacementsUtil.cardTableStart(config);
    }

    @Override
    public int logOfHeapRegionGrainBytes() {
        return HotSpotReplacementsUtil.logOfHeapRegionGrainBytes(config);
    }

    @Override
    public ForeignCallDescriptor preWriteBarrierDescriptor() {
        return G1WBPRECALL_STACK_ONLY;
    }

    @Override
    public ForeignCallDescriptor postWriteBarrierDescriptor() {
        return G1WBPOSTCALL_STACK_ONLY;
    }

    @Override
    public InvokeTarget getCallTarget(ForeignCallLinkage callTarget) {
        return callTarget;
    }
}
