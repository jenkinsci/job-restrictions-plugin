/*
 * The MIT License
 *
 * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionBlockageCause;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 */
public class JobRestrictionProperty extends NodeProperty<Node> {

    /**Restriction according to buildable item requirements*/
    JobRestriction jobRestriction;

    @DataBoundConstructor
    public JobRestrictionProperty(JobRestriction jobRestriction) {
        this.jobRestriction = jobRestriction;
    }
    
    @Override
    public CauseOfBlockage canTake(Queue.BuildableItem item) {
        if (jobRestriction != null) {
            if (!jobRestriction.canTake(item)) {
                return JobRestrictionBlockageCause.DEFAULT;
            }
        }
        
        // Can take
        return null; 
    }

    public JobRestriction getJobRestriction() {
        return jobRestriction;
    }
         
    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.nodes_JobRestrictionProperty_DisplayName();
        } 
    }
}
