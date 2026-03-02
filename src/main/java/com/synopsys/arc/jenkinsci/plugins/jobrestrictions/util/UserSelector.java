/*
 * The MIT License
 *
 * Copyright 2014 Oleg Nenashev, Synopsys Inc.
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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import java.io.Serializable;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Describable Item, which allows to configure a user.
 * @since 0.4
 */
// TODO: Autocompletion
public class UserSelector implements Describable<UserSelector>, Serializable {
    private static final long serialVersionUID = 1L;

    /**ID of the user*/
    @CheckForNull
    String selectedUserId;

    @DataBoundConstructor
    public UserSelector(@CheckForNull String selectedUserId) {
        this.selectedUserId = hudson.Util.fixEmptyAndTrim(selectedUserId);
    }

    @CheckForNull
    public String getSelectedUserId() {
        return selectedUserId;
    }

    @Override
    public Descriptor<UserSelector> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserSelector cmp) {
            return Objects.equals(selectedUserId, cmp.selectedUserId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (selectedUserId != null ? selectedUserId.hashCode() : 0);
        return hash;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<UserSelector> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "N/A";
        }

        @Restricted(NoExternalUse.class) // Stapler only
        @RequirePOST
        public FormValidation doCheckSelectedUserId(@QueryParameter String selectedUserId) {
            Jenkins.get().checkPermission(Jenkins.READ);
            selectedUserId = Util.fixEmptyAndTrim(selectedUserId);
            if (selectedUserId == null) {
                return FormValidation.error("Field is empty");
            }

            User user = User.getById(selectedUserId, false);
            if (user == null) {
                return FormValidation.warning("User " + selectedUserId + " is not registered in Jenkins");
            }

            return FormValidation.ok();
        }
    }
}
