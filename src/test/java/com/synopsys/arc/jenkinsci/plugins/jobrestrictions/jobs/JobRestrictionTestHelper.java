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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import java.io.IOException;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Provides helper methods for {@link JobRestrictionProperty} test management.
 * @author Oleg Nenashev
 */
public class JobRestrictionTestHelper {

    private JobRestrictionTestHelper() {
    }
    
    /**
     * Creates a job with specified cause restrictions.
     * @param <T> Class of the job to be created
     * @param j Jenkins rule
     * @param type Type of the job for class automatch
     * @param restrictions Array of restrictions to be passed
     * @return Created project
     * @throws IOException 
     */
    public static <T extends TopLevelItem> T createJob(JenkinsRule j, Class<T> type, JobCauseRestriction ... restrictions) 
            throws IOException {
        T job = j.jenkins.<T>createProject(type, "testProject");
        //TODO: prettify conversion
        JobRestrictionPropertyBuider builder = JobRestrictionPropertyBuider.create();
        for (JobCauseRestriction r : restrictions) {
            builder.addCauseRestriction(r);
        }            
        builder.applyTo((Job)job);
        return job;
    }
}
