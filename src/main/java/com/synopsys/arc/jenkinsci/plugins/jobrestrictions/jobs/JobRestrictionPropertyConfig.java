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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Describable;
import hudson.model.Descriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Stores the configuration for {@link JobRestrictionProperty}.
 * @author Oleg Nenashev
 */
public class JobRestrictionPropertyConfig implements Describable<JobRestrictionPropertyConfig> {
    UpstreamCauseRestriction upstreamCauseRestriction;
    UserIdCauseRestriction userIdCauseRestriction;

    @DataBoundConstructor
    public JobRestrictionPropertyConfig(
            UpstreamCauseRestriction upstreamCauseRestriction,
            UserIdCauseRestriction userIdCauseRestriction) {
        this.upstreamCauseRestriction = upstreamCauseRestriction;
        this.userIdCauseRestriction = userIdCauseRestriction;
    }

    public UpstreamCauseRestriction getUpstreamCauseRestriction() {
        return upstreamCauseRestriction;
    }

    public UserIdCauseRestriction getUserIdCauseRestriction() {
        return userIdCauseRestriction;
    }

    public void validateCause(@NonNull Cause cause, @NonNull BuildListener listener) throws AbortException {
       if (upstreamCauseRestriction != null && cause instanceof Cause.UpstreamCause) {
           upstreamCauseRestriction.validate((Cause.UpstreamCause)cause);
       }    
       
       if (userIdCauseRestriction != null && cause instanceof Cause.UserIdCause) {
           userIdCauseRestriction.validate((Cause.UserIdCause)cause);
       }
       //TODO: checks
       
    }

    @Override
    public Descriptor<JobRestrictionPropertyConfig> getDescriptor() {
        return DESCRIPTOR;
    }
    
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    public static class DescriptorImpl extends Descriptor<JobRestrictionPropertyConfig> {

        @Override
        public String getDisplayName() {
            return "Job restriction property config";
        }
        
    }
}
