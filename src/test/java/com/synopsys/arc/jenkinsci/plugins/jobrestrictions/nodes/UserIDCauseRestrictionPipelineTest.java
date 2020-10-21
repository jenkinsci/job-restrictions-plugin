package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hudson.model.Queue;
import hudson.model.queue.QueueTaskFuture;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.job.StartedByUserRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes.JobRestrictionProperty;

import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Result;
import hudson.model.User;
import hudson.security.ACL;
import hudson.slaves.DumbSlave;

import javax.annotation.Nonnull;

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
        slave.getNodeProperties().add(new JobRestrictionProperty(new StartedByUserRestriction(listUserSelector, true, true, false)));
      
        // create pipeline job that will run on the test slave
        final WorkflowJob project = j.jenkins.createProject(WorkflowJob.class, "pipeline_demo");
        project.setDefinition(new CpsFlowDefinition(
                "node('" + slave.getNodeName() + "') {\n" +
                "    sh('echo hello')\n" +
                "}", true));
      
        // schedule a build for the pipeline job as the user who is accepted by the test slave
        WorkflowRun run = runAsUserAndWaitForCompletion(project, TEST_USERNAME);
        assertThat("Job restriction should allow pipeline jobs", run.getResult(), equalTo(Result.SUCCESS));
    }
    
    @Test
    public void nodeShouldNotAllowPipelineRunsByUnacceptedUser_UserIdCause() throws Exception {
        DumbSlave slave = j.createOnlineSlave();
        List<UserSelector> listUserSelector = new ArrayList<UserSelector>(1);
        listUserSelector.add(new UserSelector(TEST_USERNAME_2));
      
        // add StartedByUser Restriction property to node
        slave.getNodeProperties().add(new JobRestrictionProperty(new StartedByUserRestriction(listUserSelector, true, true, false)));
      
        final WorkflowJob project = j.jenkins.createProject(WorkflowJob.class, "pipeline_demo");
        project.setDefinition(new CpsFlowDefinition(
                "node('" + slave.getNodeName() + "') {\n" +
                "    sh('echo hello')\n" +
                "}", true));
      
        // schedule a build for the pipeline job as the user who is not accepted by the test slave
        QueueTaskFuture<WorkflowRun> runFuture = runAsUser(project, TEST_USERNAME);
        WorkflowRun run = runFuture.waitForStart();
        Thread.sleep(2000); // Any way to do it better
        List<StepExecution> executions = run.getExecutionPromise().get().getCurrentExecutions(true).get();
        assertThat("Missing current head", executions.size(), equalTo(1));
        StepExecution currentExecution = executions.get(0);
        assertThat(currentExecution, instanceOf(ExecutorStepExecution.class));
        ExecutorStepExecution exec = ((ExecutorStepExecution)currentExecution);

        // Now we know there is a waiting execution
        List<Queue.BuildableItem> buildableItems = j.jenkins.getQueue().getBuildableItems();
        assertThat("There is no buildable items in the queue. The job is not blocked", buildableItems.size(), equalTo(1));
        assertThat(buildableItems.get(0).task, instanceOf(ExecutorStepExecution.PlaceholderTask.class));
        assertThat("The task is not blocked", buildableItems.get(0).getCauseOfBlockage(), not(nullValue()));
    }

    @Nonnull
    private QueueTaskFuture<WorkflowRun> runAsUser(WorkflowJob job, String username) throws InterruptedException, ExecutionException {
        QueueTaskFuture<WorkflowRun> runFuture;
        SecurityContext old = ACL.impersonate((User.getById(username, true)).impersonate());
        try {
            runFuture = job.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
        } finally {
            SecurityContextHolder.setContext(old);
        }
        Assert.assertNotNull("The run has not been scheduled for the job " + job + " and user " + username, runFuture);
        return runFuture;
    }


    @Nonnull
    private WorkflowRun runAsUserAndWaitForCompletion(WorkflowJob job, String username) throws InterruptedException, ExecutionException {
        // We have a test timeout, extra timeout is not needed
        return runAsUser(job, username).get();
    }
}
