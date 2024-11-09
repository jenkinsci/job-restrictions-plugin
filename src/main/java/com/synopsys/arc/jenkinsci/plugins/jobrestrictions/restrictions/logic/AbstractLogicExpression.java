/*
 * The MIT License
 *
 * Copyright 2013-2016 Oleg Nenashev, Synopsys Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.logic;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import hudson.model.Queue;
import hudson.model.Run;

/**
 * Provides logic wrapper for all expressions,
 * which don't utilize contents of RunnableItems.
 * @deprecated In the current state, this class is just a stub for future.
 * @author Oleg Nenashev
 */
abstract class AbstractLogicExpression extends JobRestriction {

    @Override
    public boolean canTake(Run run) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canTake(Queue.BuildableItem item) {
        throw new UnsupportedOperationException(
                "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }

    // TODO: add a generalized canTake method
}
