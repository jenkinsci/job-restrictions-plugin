/*
 * The MIT License
 *
 * Copyright 2013-2016 Synopsys Inc., Oleg Nenashev
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

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.logic.AnyJobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.JenkinsHelper;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Run;
import java.io.Serializable;
import java.util.List;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The extension point, which allows to restrict job executions.
 * 
 * @author Oleg Nenashev
 */
public abstract class JobRestriction implements ExtensionPoint, Describable<JobRestriction>, Serializable {
    
    public static final JobRestriction DEFAULT = new AnyJobRestriction();
    
    /**
     * Check if the {@link Queue} item can be taken.
     * This method is being used to check if the {@link Node} can take the
     * specified job. If the job cannot be taken, Jenkins will try to launch 
     * the job on other nodes.
     * @param item An item to be checked
     * @return true if the node can take the item
     */
    public abstract boolean canTake(@NonNull Queue.BuildableItem item);
        
    /**
     * Check if the {@link Job} can be executed according to the specified {@link Run}.
     * If the job cannot be executed, it will be aborted by the plugin.
     * @param run A {@link Run} to be checked 
     * @return true if the build can be executed
     */
    public abstract boolean canTake(@NonNull Run run);

    @Override
    public JobRestrictionDescriptor getDescriptor() {
        return (JobRestrictionDescriptor) JenkinsHelper.getInstanceOrDie().getDescriptorOrDie(getClass());
    }

    /**
     * Get list of all registered {@link JobRestriction}s.
     * @return List of {@link JobRestriction}s.
     */    
    public static DescriptorExtensionList<JobRestriction,JobRestrictionDescriptor> all() {
        return JenkinsHelper.getInstanceOrDie().<JobRestriction,JobRestrictionDescriptor>getDescriptorList
            (JobRestriction.class);
    }
    
    /**
     * Returns list of {@link JobRestrictionDescriptor}s.
     * @return List of available descriptors.
     * @since 0.2
     */
    public static List<JobRestrictionDescriptor> allDescriptors() {
        return all().reverseView();
    }
    
}
