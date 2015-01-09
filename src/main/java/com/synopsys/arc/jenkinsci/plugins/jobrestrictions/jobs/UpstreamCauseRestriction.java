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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Descriptor;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Allows to restrict execution according to upstream cause.
 * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 */
public class UpstreamCauseRestriction extends JobCauseRestriction<Cause.UpstreamCause> {

    JobRestriction jobRestriction;
    final boolean skipCheckForMissingInfo;

    @DataBoundConstructor
    public UpstreamCauseRestriction(JobRestriction jobRestriction, 
            boolean skipCheckForMissingInfo) {
        this.jobRestriction = jobRestriction;
        this.skipCheckForMissingInfo = skipCheckForMissingInfo;
    }
    
    public UpstreamCauseRestriction(JobRestriction jobRestriction) {
        this(jobRestriction, false);
    }

    public JobRestriction getJobRestriction() {
        return jobRestriction;
    }

    public boolean isSkipCheckForMissingInfo() {
        return skipCheckForMissingInfo;
    }
    
    @Override
    public void validate(Cause.UpstreamCause cause) throws AbortException {
       final Run upstreamRun = cause.getUpstreamRun();
       if (upstreamRun == null) {
           if (!skipCheckForMissingInfo) {
               throw new AbortException("Upstream build info is missing");
           } else {
               return;
           }
       } 
       if (jobRestriction != null && !jobRestriction.canTake(upstreamRun)) {
           throw new AbortException("Job can't be executed due to upstream restrictions");
       } 
    }

    @Override
    public Descriptor<JobCauseRestriction<? extends Cause>> getDescriptor() {
        return DESCRIPTOR;
    }
      
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    public static class DescriptorImpl extends JobCauseRestrictionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.jobs_CauseRestrictions_Upstream();
        }
        
    }
}
