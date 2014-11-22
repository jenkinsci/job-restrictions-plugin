/*
 * The MIT License
 *
 * Copyright 2014 Oleg Nenashev <o.v.nenashev@gmail.com>.
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
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Queue;
import hudson.model.Run;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>
 * @since 0.4
 */
public class StartedByUserRestriction  extends JobRestriction {
    
    private final List<UserSelector> usersList; 
    private final boolean checkUsersFromUpstremProjects;
    private final boolean acceptAutomaticRuns;
    private transient Set<String> acceptedUsers = null;

    @DataBoundConstructor
    public StartedByUserRestriction(List<UserSelector> usersList, boolean checkUsersFromUpstremProjects, boolean acceptAutomaticRuns) {
        this.usersList = usersList;
        this.checkUsersFromUpstremProjects = checkUsersFromUpstremProjects;
        this.acceptAutomaticRuns = acceptAutomaticRuns;
    }

    public List<UserSelector> getUsersList() {
        return usersList;
    }

    public boolean isAcceptAutomaticRuns() {
        return acceptAutomaticRuns;
    }

    public boolean isCheckUsersFromUpstremProjects() {
        return checkUsersFromUpstremProjects;
    }
    
    private synchronized @Nonnull Set<String> getAcceptedUsers() {
        if (acceptedUsers == null) {
            final List<UserSelector> selectors = getUsersList();
            acceptedUsers = new HashSet<String>(selectors.size());
            for (UserSelector selector : selectors) {
                acceptedUsers.add(selector.getSelectedUserId()); // merge equal entries
            }
        }
        return acceptedUsers;
    }
    
    private boolean acceptsUser(String userId) {
        return getAcceptedUsers().contains(userId);
    }
    
    /**package*/ boolean canTake(@Nonnull List<Cause> causes) {
        for (Cause cause : causes) {
            if (cause instanceof Cause.UserIdCause) {
                String startedBy = ((Cause.UserIdCause)cause).getUserId();
                if (acceptsUser(startedBy)) {
                    return true;
                }
            }
            if (checkUsersFromUpstremProjects && cause instanceof Cause.UpstreamCause) {
                final List<Cause> upstreamCauses = ((Cause.UpstreamCause)cause).getUpstreamCauses();
                if (canTake(upstreamCauses)) { // Recursive call to iterate through all causes
                    return true;
                }
            }
            //TODO: check acceptAutomaticRuns
        }
        return false;
    }
    
    @Override
    public boolean canTake(Queue.BuildableItem item) {
        return canTake(item.getCauses());
    }

    @Override
    public boolean canTake(Run run) {
        return canTake(run.getCauses());
    }
    
    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {
        
        @Override
        public String getDisplayName() {
            return Messages.restrictions_Job_RegexName();
        }
    }
}
