package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 *
 */
@WithJenkins
class UserIdCauseRestrictionTest {

    private JenkinsRule j;

    private static final String TEST_USERNAME = "foo";

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        User.getById(TEST_USERNAME, true);
    }

    @Test
    void shouldAllowManualRuns_ifConfigured_UserIdCause() throws Exception {
        FreeStyleProject project =
                JobRestrictionTestHelper.createJob(j, FreeStyleProject.class, new UserIdCauseRestriction(false));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, false);
        assertThat(
                "Job restriction should allow manual runs if the prohibitManualLaunch setting is false",
                build.getResult(),
                equalTo(Result.SUCCESS));
    }

    @Test
    void shouldNotAllowManualRuns_UserIdCause() throws Exception {
        FreeStyleProject project =
                JobRestrictionTestHelper.createJob(j, FreeStyleProject.class, new UserIdCauseRestriction(true));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, false);
        assertThat(
                "Job restriction should have prohibited the manual launch", build.getResult(), equalTo(Result.FAILURE));
    }

    // TODO: Does it really need a fix?
    @Test
    @Disabled
    void shouldNotAllowManualRuns_UserCause() throws Exception {
        FreeStyleProject project =
                JobRestrictionTestHelper.createJob(j, FreeStyleProject.class, new UserIdCauseRestriction(true));
        FreeStyleBuild build = runAsUser(project, TEST_USERNAME, true);
        assertThat(
                "Job restriction should have prohibited the manual launch for the Legacy UserCause",
                build.getResult(),
                equalTo(Result.FAILURE));
    }

    private FreeStyleBuild runAsUser(final FreeStyleProject project, String username, final boolean legacyCause)
            throws InterruptedException, ExecutionException, TimeoutException {
        User user = j.jenkins.getUser(username);
        final List<QueueTaskFuture<FreeStyleBuild>> scheduled = new ArrayList<>(1);
        try (ACLContext ignored = ACL.as2(user.impersonate2())) {
            final Cause cause = legacyCause ? new Cause.UserCause() : new Cause.UserIdCause();
            scheduled.add(project.scheduleBuild2(0, cause));
        }

        assertThat(scheduled, not(nullValue()));
        return scheduled.get(0).get(1, TimeUnit.MINUTES);
    }
}
