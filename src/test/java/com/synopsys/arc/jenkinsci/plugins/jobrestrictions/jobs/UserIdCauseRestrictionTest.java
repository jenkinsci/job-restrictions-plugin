package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Ignore;

/**
 * 
 */
public class UserIdCauseRestrictionTest {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    private static final String TEST_USERNAME = "foo";
    
    @Before
    public void setupSecurityRealm() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.getUser(TEST_USERNAME);
    }

    @Test
    public void shouldAllowManualRuns_ifConfigured_UserIdCause() throws Exception {
        FreeStyleProject project = JobRestrictionTestHelper.createJob
            (j, FreeStyleProject.class, new UserIdCauseRestriction(false));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, false);
        assertThat("Job restriction should allow manual runs if the prohibitManualLaunch setting is false", build.getResult(), equalTo(Result.SUCCESS));
    }

    @Test
    public void shouldNotAllowManualRuns_UserIdCause() throws Exception {
        FreeStyleProject project = JobRestrictionTestHelper.createJob
            (j, FreeStyleProject.class, new UserIdCauseRestriction(true));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, false);
        assertThat("Job restriction should have prohibited the manual launch", build.getResult(), equalTo(Result.FAILURE));
    }

    //TODO: Does it really need a fix?
    @Test
    @Ignore
    public void shouldNotAllowManualRuns_UserCause() throws Exception {
        FreeStyleProject project = JobRestrictionTestHelper.createJob
            (j, FreeStyleProject.class, new UserIdCauseRestriction(true));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, true);
        assertThat("Job restriction should have prohibited the manual launch for the Legacy UserCause", build.getResult(), equalTo(Result.FAILURE));
    }
    
    private FreeStyleBuild runAsUser(final FreeStyleProject project, String username, final boolean legacyCause) 
            throws InterruptedException, ExecutionException, TimeoutException {
        User user = j.jenkins.getUser(username);
        final List<QueueTaskFuture<FreeStyleBuild>> scheduled = new ArrayList<QueueTaskFuture<FreeStyleBuild>>(1);
        ACL.impersonate(user.impersonate(), new Runnable() {
            @Override
            public void run() {
                final Cause cause = legacyCause ? new Cause.UserCause() : new Cause.UserIdCause();
                scheduled.add(project.scheduleBuild2(0, cause));
            }
        });
        
        Assert.assertThat(scheduled, not(nullValue()));
        return scheduled.get(0).get(1, TimeUnit.MINUTES);
    }
}
