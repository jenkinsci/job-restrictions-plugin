/*
 * The MIT License
 *
 * Copyright 2015-2016 Christopher Suarez, Oleg Nenashev
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

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Abstract class, which defines the logic of UserCause-based restrictions.
 * @author Christopher Suarez
 * @author Oleg Nenashev
 * @since 0.4
 * @see StartedByUserRestriction
 * @see StartedByMemberOfGroupRestriction
 */
public abstract class AbstractUserCauseRestriction extends JobRestriction {
    
    /**
     * Enables the check of upstream projects
     */
    private final boolean checkUpstreamProjects;

    public AbstractUserCauseRestriction(boolean checkUpstreamProjects) {
        this.checkUpstreamProjects = checkUpstreamProjects;
    }

    public final boolean isCheckUpstreamProjects() {
        return checkUpstreamProjects;
    }
    
    /**
     * Check if the method accepts the specified user.
     * The user will be automatically extracted by the logic in {@link AbstractUserCauseRestriction}.
     * @param userId User id
     * @return true if the restriction accepts the user
     */
    abstract protected boolean acceptsUser(@CheckForNull String userId); 
    
    /* package */ boolean canTake(@NonNull List<Cause> causes) {
        boolean userIdCause = false;
        boolean rebuildCause = false;
        boolean upstreamCause = false;
        
        boolean aUserIdWasNotAccepted = false;
        boolean userIdCauseExists = false;
        
        for (@CheckForNull Cause cause : causes) { 
            if (cause == null) {
                continue; // Protection from the bug in old core versions
            }
                   
            // Check user causes
            if (cause.getClass().equals(Cause.UserIdCause.class) && !aUserIdWasNotAccepted) {
                userIdCauseExists = true;
                //if several userIdCauses exists, be defensive and don't allow if one is not accepted.
                final @CheckForNull String startedBy = ((Cause.UserIdCause) cause).getUserId();
                if (acceptsUser(startedBy)) {
                    userIdCause = true;
                } else {
                    aUserIdWasNotAccepted = true;
                    userIdCause = false;
                }
            }
                      
            // Check upstream projects if required
            if (checkUpstreamProjects && cause.getClass().equals(Cause.UpstreamCause.class)) {
                final List<Cause> upstreamCauses = ((Cause.UpstreamCause) cause).getUpstreamCauses();
                // Recursive call to iterate through all above
                if (canTake(upstreamCauses)) {
                    upstreamCause = true;
                }
            }

            // TODO: Check rebuild causes
        }

        //userId has preceedence
        if (userIdCauseExists) {
            return userIdCause;
        } else { //If no update cause exists we should also return false...
            return upstreamCause;
        }
    }

    @Override
    public boolean canTake(Queue.BuildableItem item) {
        final List<Cause> causes = new ArrayList<Cause>();
        for (Action action : item.getActions()) {
            if (action instanceof CauseAction) {
                CauseAction causeAction = (CauseAction) action;
                causes.addAll(causeAction.getCauses());
            } 
        }
        return canTake(causes);
    }

    @Override
    public boolean canTake(Run run) {
        return canTake(run.getCauses());
    }
}
