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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.QueueHelper;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.util.FormValidation;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Restricts the jobs execution by applying regular expressions to their names.
 * @author Oleg Nenashev
 */
public class RegexNameRestriction extends JobRestriction {
    private static final long serialVersionUID = 1L;

    String regexExpression;
    boolean checkShortName;

    @DataBoundConstructor
    public RegexNameRestriction(String regexExpression, boolean checkShortName) {
        this.regexExpression = regexExpression;
        this.checkShortName = checkShortName;
    }

    public String getRegexExpression() {
        return regexExpression;
    }

    public boolean isCheckShortName() {
        return checkShortName;
    }

    @Override
    public boolean canTake(Queue.BuildableItem item) {
        // FIXME: switch to  the "getFullName" in the future
        return canTake(QueueHelper.getFullName(item));
    }

    @Override
    public boolean canTake(Run run) {
        return canTake(run.getParent().getFullName());
    }

    public boolean canTake(String projectName) {
        try {
            return projectName.matches(regexExpression);
        } catch (PatternSyntaxException ex) {
            return true; // Ignore invalid pattern
        }
    }

    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.restrictions_Job_RegexName();
        }

        @RequirePOST
        public FormValidation doCheckRegexExpression(@QueryParameter String regexExpression) {
            try {
                Pattern.compile(regexExpression);
            } catch (PatternSyntaxException exception) {
                return FormValidation.error(exception.getDescription());
            }
            return FormValidation.ok(Messages.restrictions_Job_RegexName_OkMessage());
        }
    }
}
