package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.StartedByUserRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Result;
import hudson.model.User;
import hudson.security.ACL;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class UserIDCauseRestrictionPipelineTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private static final String TEST_USERNAME = "foo";
    private static final String TEST_USERNAME_2 = "bar";

    @Before
    public void setupSecurityRealm() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
    }

    @Test
    public void nodeShouldAllowPipelineRunsByAcceptedUser_UserIdCause() throws Exception {
        DumbSlave slave = j.createOnlineSlave();
        List<UserSelector> listUserSelector = new ArrayList<UserSelector>(1);
        listUserSelector.add(new UserSelector(TEST_USERNAME));

        // add StartedByUser Restriction property to node
        slave.getNodeProperties()
                .add(new JobRestrictionProperty(new StartedByUserRestriction(listUserSelector, false, true, false)));

        // create pipeline job that will run on the test slave
        WorkflowJob project = j.jenkins.createProject(WorkflowJob.class, "pipeline_demo");
        project.setDefinition(new CpsFlowDefinition(
                "node('" + slave.getNodeName() + "') {\n" + "    sh('echo hello')\n" + "}", true));

        // schedule a build for the pipeline job as the user who is accepted by the test slave
        ACL.impersonate((User.getById(TEST_USERNAME, true)).impersonate(), new Runnable() {
            @Override
            public void run() {
                project.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
            }
        });

        // more than enough time given for the build to finish
        Thread.sleep(10000);

        WorkflowRun build = project.getFirstBuild();
        assertThat("Job restriction should allow pipeline jobs", build.getResult(), equalTo(Result.SUCCESS));
    }

    @Test
    public void nodeShouldNotAllowPipelineRunsByUnacceptedUser_UserIdCause() throws Exception {
        DumbSlave slave = j.createOnlineSlave();
        List<UserSelector> listUserSelector = new ArrayList<UserSelector>(1);
        listUserSelector.add(new UserSelector(TEST_USERNAME_2));

        // add StartedByUser Restriction property to node
        slave.getNodeProperties()
                .add(new JobRestrictionProperty(new StartedByUserRestriction(listUserSelector, false, true, false)));

        WorkflowJob project = j.jenkins.createProject(WorkflowJob.class, "pipeline_demo");
        project.setDefinition(new CpsFlowDefinition(
                "node('" + slave.getNodeName() + "') {\n" + "    sh('echo hello')\n" + "}", true));

        // schedule a build for the pipeline job as the user who is not accepted by the test slave
        ACL.impersonate((User.getById(TEST_USERNAME, true)).impersonate(), new Runnable() {
            @Override
            public void run() {
                project.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
            }
        });

        // more than enough time given for the build to finish
        Thread.sleep(10000);

        // the return value of build.getResult() will be null
        WorkflowRun build = project.getFirstBuild();
        assertThat("Job restriction should allow pipeline jobs", build.getResult(), not(equalTo(Result.SUCCESS)));
    }
}
