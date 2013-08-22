/*
 * The MIT License
 *
 * Copyright 2013 Synopsys Inc., Oleg Nenashev <nenashev@synopsys.com> 
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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.logic.AndJobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.logic.AnyJobRestriction;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Queue;
import java.io.Serializable;
import jenkins.model.Jenkins;

/**
 *
 * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 */
public abstract class JobRestriction implements ExtensionPoint, Describable<JobRestriction>, Serializable {
    
    public static final JobRestriction DEFAULT = new AnyJobRestriction();
    
    public abstract boolean canTake(Queue.BuildableItem item);

    @Override
    public JobRestrictionDescriptor getDescriptor() {
        return (JobRestrictionDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Get list of all registered {@link JobRestriction}s.
     * @return List of {@link UserMacroExtension}s.
     */    
    public static DescriptorExtensionList<JobRestriction,Descriptor<JobRestriction>> all() {
        return Jenkins.getInstance().<JobRestriction,Descriptor<JobRestriction>>getDescriptorList(JobRestriction.class);
    }
}
