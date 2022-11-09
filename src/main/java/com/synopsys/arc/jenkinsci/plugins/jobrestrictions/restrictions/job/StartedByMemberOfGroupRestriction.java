/*
 * The MIT License
 *
 * Copyright 2014-2016 Christopher Suarez, Oleg Nenashev
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
import hudson.security.SecurityRealm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import jenkins.model.Jenkins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.GroupSelector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Queue;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * {@link JobRestriction}, which checks if a user belongs to the specified groups.
 * In several cases the extension may load user data from {@link SecurityRealm},
 * so there can be significant delays in Jenkins {@link Queue}.
 * @author Christopher Suarez
 * @author Oleg Nenashev
 * @since 0.4
 * @see StartedByUserRestriction
 */
// TODO: it's a real issue, needs some love
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", 
        justification = "XStream does actually need serialization, the code needs refactoring in 1.0")
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

    private synchronized @NonNull Set<String> getAcceptedGroups() {
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
        final @CheckForNull List<String> authorities = getAuthorities(userId);
        if (authorities == null) {
            return false;
        }

        final Set<String> allowedGroups = getAcceptedGroups();
        for (String groupId : authorities) {
            if (allowedGroups.contains(groupId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads group lists for the user.
     * {@link User} info has a high priority. If there is no info, {@link UserDetails}
     * will be loaded from the current {@link SecurityRealm}.
     * @param userId User ID
     * @return List of effective groups. {@code null} if there's no info
     */
    private static @CheckForNull List<String> getAuthorities(@NonNull String userId) {
        final @CheckForNull User usr = User.getById(userId, false);
        if (usr == null) { // User is not registered in Jenkins (e.g. deleted)
            return getAuthoritiesFromRealm(userId);
        }
         
        List<String> authorities = usr.getAuthorities();
        if (authorities.isEmpty()) {
            return getAuthoritiesFromRealm(userId);
        }
        return authorities;
    }

    /**
     * Extracts user groups from {@link SecurityRealm}.
     * @param userId
     * @return List of effective groups. Null if there's no info
     */
    private static @CheckForNull List<String> getAuthoritiesFromRealm(@NonNull String userId) {
        final Jenkins instance = Jenkins.getInstance();

        @CheckForNull UserDetails userDetails = null;
        try {
            final SecurityRealm sr = instance.getSecurityRealm();   
            userDetails = sr.loadUserByUsername(userId);
        } catch (DataAccessException | UsernameNotFoundException ex) {
            // fallback to null handler
        }

        if (userDetails == null) {
            return null;
        }

        GrantedAuthority[] authorities = userDetails.getAuthorities();
        List<String> authorityList = new ArrayList<String>(authorities.length);
        for (GrantedAuthority auth : authorities) {
            authorityList.add(auth.getAuthority());
        }
        return authorityList;
    }

    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.restrictions_Job_StartedByMemberOfGroupRestriction_displayName();
        }
    }
}
