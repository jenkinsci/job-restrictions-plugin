/*
 * The MIT License
 *
 * Copyright (c) 2016 Oleg Nenashev.
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

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes.JobRestrictionProperty;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.ClassSelector;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Tests of {@link JobClassNameRestriction}.
 * @author Oleg Nenashev
 */
@Issue("JENKINS-38644")
public class JobClassNameRestrictionTest {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void shouldRestrictJobClass() throws Exception {
        JobClassNameRestriction jobClassNameRestriction = new JobClassNameRestriction(
                Arrays.asList(new ClassSelector(FreeStyleProject.class.getName())));
        JobRestrictionProperty prop = new JobRestrictionProperty(jobClassNameRestriction);
        
        FreeStyleProject freestylePrj = j.createFreeStyleProject();
        MatrixProject matrixProject = j.createProject(MatrixProject.class);
        
        assertCanTake(prop, freestylePrj);
        assertCannotTake(prop, matrixProject);
    }
    
    private void assertCanTake(JobRestrictionProperty prop, Queue.Task task) {
        Queue.BuildableItem item = new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), 
                task, Collections.<Action>emptyList()));
        assertThat("Job Restriction Property should accept " + task.getClass(), 
                prop.canTake(item), nullValue());
    }
    
    private void assertCannotTake(JobRestrictionProperty prop, Queue.Task task) {
        Queue.BuildableItem item = new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), 
                task, Collections.<Action>emptyList()));
        assertThat("Job Restriction Property should not accept " + task.getClass(), 
                prop.canTake(item), notNullValue());
    }
}
