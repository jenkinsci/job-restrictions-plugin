/*
 * The MIT License
 *
 * Copyright 2016 Oleg Nenashev
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

package io.jenkins.plugins.jobrestrictions.restrictions.job;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import io.jenkins.plugins.jobrestrictions.util.ClassSelector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Handles job class restrictions.
 * @author Oleg Nenashev
 * @see ClassSelector
 * @since TODO
 */
// TODO: it's a real issue, needs some love
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", 
        justification = "XStream does actually need serialization, the code needs refactoring in 1.0")
public class JobClassNameRestriction extends JobRestriction {
    
    private final List<ClassSelector> jobClasses; 
    
    private transient Set<String> acceptedClassesHash = null;

    @DataBoundConstructor
    public JobClassNameRestriction(List<ClassSelector> jobClasses) {
        this.jobClasses = jobClasses;
    }

    @NonNull
    public List<ClassSelector> getJobClasses() {
        return jobClasses;
    }
        
    @NonNull 
    private synchronized Set<String> getAcceptedJobClasses() {
        if (acceptedClassesHash == null) {
            final List<ClassSelector> selectors = getJobClasses();
            acceptedClassesHash = new HashSet<String>(selectors.size());
            for (ClassSelector selector : selectors) {
                acceptedClassesHash.add(selector.getSelectedClass()); // merge equal entries
            }
        }
        return acceptedClassesHash;
    }

    @Override
    public boolean canTake(Queue.BuildableItem item) {
        return getAcceptedJobClasses().contains(item.task.getClass().getName());
    }

    @Override
    public boolean canTake(Run run) {
        return getAcceptedJobClasses().contains(run.getParent().getClass().getName());
    }
    
    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {
        
        @Override
        public String getDisplayName() {
            return Messages.restrictions_Job_JobClassNameRestriction_displayName();
        }
    }
}
