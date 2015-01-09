/*
 * The MIT License
 *
 * Copyright 2014-2015 Christopher Suarez, Oleg Nenashev
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

import hudson.Extension;
import hudson.model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundConstructor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.GroupSelector;

/**
 * {@link JobRestriction}, which checks if the user belongs to the specified groups.
 * @author Christopher Suarez
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>
 * @since 0.4
 */
public class StartedByMemberOfGroupRestriction extends AbstractUserCauseRestriction {

    private final List<GroupSelector> groupList;

    private transient Set<String> acceptedGroups = null;

    @DataBoundConstructor
    public StartedByMemberOfGroupRestriction(List<GroupSelector> groupList,
            boolean checkUpstreamProjects) {
        super(checkUpstreamProjects);
        this.groupList = groupList;
    }

    public List<GroupSelector> getGroupList() {
        return groupList;
    }

    private synchronized @Nonnull Set<String> getAcceptedGroups() {
        if (acceptedGroups == null) {
            final List<GroupSelector> selectors = getGroupList();
            acceptedGroups = new HashSet<String>(selectors.size());
            for (GroupSelector selector : selectors) {
                // merge equal entries
                acceptedGroups.add(selector.getSelectedGroupId());
            }
        }
        return acceptedGroups;
    }

    @Override
    protected boolean acceptsUser(@CheckForNull String userId) {
        if (userId == null) {
            return false;
        }
        final @CheckForNull User usr = User.get(userId, false, null);
        if (usr == null) { // missing user (e.g, has been already deleted)
            return false;
        }
        
        final Set<String> allowedGroups = getAcceptedGroups();        
        for (String groupId : usr.getAuthorities()) {
            if (allowedGroups.contains(groupId)) {
                return true;
            }
        }
        return false;
    }

    

    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages
                    .restrictions_Job_StartedByMemberOfGroupRestriction_displayName();
        }
    }
}
