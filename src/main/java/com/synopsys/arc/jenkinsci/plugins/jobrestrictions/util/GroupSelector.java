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
import hudson.Functions;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.security.UserMayOrMayNotExistException;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import java.io.Serializable;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.acegisecurity.AuthenticationException;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.dao.DataAccessException;

/**
 * Describable Item, which allows to configure a group.
 * @since 0.4
 */
// TODO: Autocompletion
public class GroupSelector implements Describable<GroupSelector>, Serializable {
    private static final long serialVersionUID = 1L;

    /**ID of the user*/
    @CheckForNull
    String selectedGroupId;

    @DataBoundConstructor
    public GroupSelector(@CheckForNull String selectedGroupId) {
        this.selectedGroupId = hudson.Util.fixEmptyAndTrim(selectedGroupId);
    }

    @CheckForNull
    public String getSelectedGroupId() {
        return selectedGroupId;
    }

    @Override
    public Descriptor<GroupSelector> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupSelector cmp) {
            return Objects.equals(selectedGroupId, cmp.selectedGroupId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (selectedGroupId != null ? selectedGroupId.hashCode() : 0);
        return hash;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends Descriptor<GroupSelector> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "N/A";
        }

        @RequirePOST
        public FormValidation doCheckSelectedGroupId(
                @QueryParameter String selectedGroupId, @AncestorInPath Item item) {
            item.checkPermission(Computer.CONFIGURE);
            selectedGroupId = Util.fixEmptyAndTrim(selectedGroupId);
            SecurityRealm sr = Jenkins.get().getSecurityRealm();
            String eSelectedGroupId = Functions.escape(selectedGroupId);
            if (selectedGroupId == null) {
                return FormValidation.error("Field is empty");
            }

            if (selectedGroupId.equals("authenticated"))
                // system reserved group
                return FormValidation.ok();

            try {
                GroupDetails details = sr.loadGroupByGroupname(selectedGroupId);
                if (details == null)
                    return FormValidation.warning("Group " + selectedGroupId + " is not registered in Jenkins");
                return FormValidation.ok();
            } catch (UserMayOrMayNotExistException e) {
                // undecidable, meaning the group may exist
                return FormValidation.respond(Kind.OK, eSelectedGroupId);
            } catch (DataAccessException | AuthenticationException e) {
                // fall through next
            }
            return FormValidation.warning("Group " + selectedGroupId + " is not registered in Jenkins");
        }
    }
}
