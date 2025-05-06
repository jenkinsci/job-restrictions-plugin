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
package io.jenkins.plugins.jobrestrictions.restrictions.job;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes.JobRestrictionProperty;
import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import io.jenkins.plugins.jobrestrictions.util.ClassSelector;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests of {@link JobClassNameRestriction}.
 * @author Oleg Nenashev
 */
@Issue("JENKINS-38644")
@WithJenkins
class JobClassNameRestrictionTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldRestrictJobClass() throws Exception {
        JobClassNameRestriction jobClassNameRestriction =
                new JobClassNameRestriction(List.of(new ClassSelector(FreeStyleProject.class.getName())));
        JobRestrictionProperty prop = new JobRestrictionProperty(jobClassNameRestriction);

        FreeStyleProject freestylePrj = j.createFreeStyleProject();
        MatrixProject matrixProject = j.createProject(MatrixProject.class);

        assertCanTake(prop, freestylePrj);
        assertCannotTake(prop, matrixProject);
    }

    private void assertCanTake(JobRestrictionProperty prop, Queue.Task task) {
        Queue.BuildableItem item =
                new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), task, Collections.emptyList()));
        assertThat("Job Restriction Property should accept " + task.getClass(), prop.canTake(item), nullValue());
    }

    private void assertCannotTake(JobRestrictionProperty prop, Queue.Task task) {
        Queue.BuildableItem item =
                new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), task, Collections.emptyList()));
        assertThat("Job Restriction Property should not accept " + task.getClass(), prop.canTake(item), notNullValue());
    }
}
