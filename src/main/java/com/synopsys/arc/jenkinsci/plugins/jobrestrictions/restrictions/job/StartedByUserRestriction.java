/*
 * The MIT License
 *
 * Copyright 2014-2016 Oleg Nenashev
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
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Handles restrictions from User causes.
 * @author Oleg Nenashev
 * @since 0.4
 */
// TODO: it's a real issue, needs some love
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", 
        justification = "XStream does actually need serialization, the code needs refactoring in 1.0")
public class StartedByUserRestriction extends AbstractUserCauseRestriction {
    
    private final List<UserSelector> usersList; 
    
    /**
     * @deprecated Not implemented
     */
    private final @Deprecated boolean acceptAutomaticRuns;
    private final boolean acceptAnonymousUsers;
    private transient Set<String> acceptedUsers = null;

    @DataBoundConstructor
    public StartedByUserRestriction(List<UserSelector> usersList, boolean checkUpstreamProjects, 
            boolean acceptAutomaticRuns, boolean acceptAnonymousUsers) {
        super(checkUpstreamProjects);
        this.usersList = usersList;
        this.acceptAutomaticRuns = acceptAutomaticRuns;
        this.acceptAnonymousUsers = acceptAnonymousUsers;
    }

    public List<UserSelector> getUsersList() {
        return usersList;
    }

    public @Deprecated boolean isAcceptAutomaticRuns() {
        return acceptAutomaticRuns;
    }
    
    public boolean isAcceptAnonymousUsers() {
        return acceptAnonymousUsers;
    }
        
    private synchronized @NonNull Set<String> getAcceptedUsers() {
        if (acceptedUsers == null) {
            final List<UserSelector> selectors = getUsersList();
            acceptedUsers = new HashSet<String>(selectors.size());
            for (UserSelector selector : selectors) {
                acceptedUsers.add(selector.getSelectedUserId()); // merge equal entries
            }
        }
        return acceptedUsers;
    }
    
    @Override
    protected boolean acceptsUser(@CheckForNull String userId) {
        return userId == null
                ? acceptAnonymousUsers
                : getAcceptedUsers().contains(userId);
    }
    
    @Extension
    public static class DescriptorImpl extends JobRestrictionDescriptor {
        
        @Override
        public String getDisplayName() {
            return Messages.restrictions_Job_StartedByUserRestriction_displayName();
        }
    }
}
