/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.transform;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

class CaptureClassGenerator extends GroupClassGenerator {

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsCaptures();
    }

    @Override
    protected boolean appliesTo(InstructionGraphNode node) {
        return node.isCaptureRoot();
    }

    @Override
    protected Type getBaseType() {
        return BASE_CAPTURE;
    }

    @Override
    protected void generateMethod(InstructionGroup group, ClassWriter cw) {
        InstructionGraphNode rootNode = group.getRoot();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", "()" + rootNode.getResultValue().getType().getDescriptor(),
                null, null);

        // check the context
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, BASE_CAPTURE.getInternalName(), "checkContext", "()V");

        fixContextSwitches(group);
        insertSetContextCalls(group);
        convertXLoads(group);

        group.getInstructions().accept(mv);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
    }

}