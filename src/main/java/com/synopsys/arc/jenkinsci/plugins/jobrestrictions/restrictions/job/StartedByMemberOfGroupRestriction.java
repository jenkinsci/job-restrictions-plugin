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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.security.SecurityRealm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.kohsuke.stapler.DataBoundConstructor;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.GroupSelector;

/**
 *
 * @author Oleg Nenashev <o.v.nenashev@gmail.com>
 * @since 0.4
 */
public class StartedByMemberOfGroupRestriction extends JobRestriction {

	private final List<GroupSelector> groupList;
	private final boolean checkGroupsFromUpstreamProjects;

	private transient Set<String> acceptedGroups = null;

	@DataBoundConstructor
	public StartedByMemberOfGroupRestriction(List<GroupSelector> groupList,
			boolean checkGroupsFromUpstreamProjects) {
		this.groupList = groupList;
		this.checkGroupsFromUpstreamProjects = checkGroupsFromUpstreamProjects;
	}

	public List<GroupSelector> getGroupList() {
		return groupList;
	}

	public boolean isCheckGroupsFromUpstreamProjects() {
		return checkGroupsFromUpstreamProjects;
	}

	private synchronized @Nonnull
	Set<String> getAcceptedGroups() {
		if (acceptedGroups == null) {
			final List<GroupSelector> selectors = getGroupList();
			acceptedGroups = new HashSet<String>(selectors.size());
			for (GroupSelector selector : selectors) {
				acceptedGroups.add(selector.getSelectedGroupId()); // merge
																	// equal
																	// entries
			}
		}
		return acceptedGroups;
	}

	private boolean acceptsUser(@CheckForNull String userId) {
		if (userId == null) {
			return false;
		}
		SecurityRealm sr = Jenkins.getInstance().getSecurityRealm();
		UserDetails userDetails = sr.loadUserByUsername(userId);
		for (String groupId : getAcceptedGroups()) {
			GrantedAuthority[] authorities = userDetails.getAuthorities();
			for (GrantedAuthority auth : authorities) {
				String authority = auth.getAuthority();
				if (authority.equals(groupId)) {
					return true;
				}
			}
		}
		return false;
	}

	/** package */
	boolean canTake(@Nonnull List<Cause> causes) {
		boolean userIdCause = false;
		boolean rebuildCause = false;
		boolean upstreamCause = false;
		boolean aUserIdWasNotAccepted =false;
		boolean userIdCauseExists=false;;
		for (Cause cause : causes) {
			if (cause.getClass().equals(Cause.UserIdCause.class) && !aUserIdWasNotAccepted) {
				userIdCauseExists=true;
				//if several userIdCauses exists, be defensive and don't allow if one is not accepted.
				final @CheckForNull
				String startedBy = ((Cause.UserIdCause) cause).getUserId();
				if(acceptsUser(startedBy) )
				{
					userIdCause = true;
				}
				else
				{
					aUserIdWasNotAccepted=true;
					userIdCause=false;
				}
			}
			if (checkGroupsFromUpstreamProjects
					&& cause.getClass().equals(Cause.UpstreamCause.class)) {

				final List<Cause> upstreamCauses = ((Cause.UpstreamCause) cause)
						.getUpstreamCauses();

				if (canTake(upstreamCauses)) // Recursive call to iterate
												// through all causes
				{
					upstreamCause = true;
				}
			}

		}

		//userId has preceedence
		if(userIdCauseExists)
		{
			return userIdCause;
		}
		else//If no update cause exists we should also return false...
		{
			return upstreamCause;
		}
	}

	@Override
	public boolean canTake(Queue.BuildableItem item) {
		
		List<Action> allActions = (List<Action>) item.getAllActions();
		List<Cause> causes = new ArrayList<Cause>();
		for (Action action : allActions) {
			try {
				CauseAction causeAction = (CauseAction) action;
				causes.addAll(causeAction.getCauses());
			} catch (ClassCastException cce) {
			}

		}

		return canTake(causes);
	}

	@Override
	public boolean canTake(Run run) {
		return canTake(run.getCauses());
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
