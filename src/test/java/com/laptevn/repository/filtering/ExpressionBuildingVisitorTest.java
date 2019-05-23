package com.laptevn.repository.filtering;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionBuildingVisitorTest {
    @Test
    public void visitNextNode() {
        assertTrue(new ExpressionBuildingVisitor(null, null, null)
                .shouldVisitNextChild(null, null));
    }

    @Test
    public void notVisitNextNode() {
        assertFalse(new ExpressionBuildingVisitor(null, null, null)
                .shouldVisitNextChild(null, new Object()));
    }
}