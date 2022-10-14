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

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import java.util.ArrayList;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Implements "And" condition with multiple entries.
 * @author Oleg Nenashev
 * @since 0.2
 */
public class MultipleAndJobRestriction extends JobRestriction {
    private static final long serialVersionUID = 1L;
    
    private final ArrayList<JobRestriction> restrictions;

    @DataBoundConstructor
    public MultipleAndJobRestriction(ArrayList<JobRestriction> restrictions) {
        this.restrictions = restrictions != null ? restrictions : new ArrayList<JobRestriction>();
    }

    public ArrayList<JobRestriction> getRestrictions() {
        return restrictions;
    }
    
    @Override
    public boolean canTake(Run run) {
        for (JobRestriction restriction : restrictions) {
            if (!restriction.canTake(run)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTake(Queue.BuildableItem item) {
        for (JobRestriction restriction : restrictions) {
            if (!restriction.canTake(item)) {
                return false;
            }
        }
        return true;
    }
    
    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.restrictions_Logic_And() +" "+ Messages.restirctions_Stuff_MultipleSuffix();
        }
    }
}
