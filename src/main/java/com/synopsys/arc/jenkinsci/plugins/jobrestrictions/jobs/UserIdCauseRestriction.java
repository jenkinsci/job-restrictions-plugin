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

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.Cause;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Allows to restrict execution of job for {@link Cause.UserIdCause}.
 * @author Oleg Nenashev
 * @since 0.2
 */
public class UserIdCauseRestriction extends JobCauseRestriction<Cause.UserIdCause> {
    boolean prohibitManualLaunch;

    @DataBoundConstructor
    public UserIdCauseRestriction(boolean prohibitManualLaunch) {
        this.prohibitManualLaunch = prohibitManualLaunch;
    }

    public boolean isProhibitManualLaunch() {
        return prohibitManualLaunch;
    }

    @Override
    public void validate(Cause.UserIdCause cause) throws AbortException {
        if (prohibitManualLaunch) {
            throw new AbortException(Messages.jobs_CauseRestrictions_UserID_prohibitedMessage());
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends JobCauseRestrictionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.jobs_CauseRestrictions_UserID_displayName();
        }
    }
}
