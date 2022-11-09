package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import hudson.model.Job;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Created by nenashev on 31/07/16.
 */
public class JobRestrictionPropertyBuider {

    private final List<JobCauseRestriction> causeRestrictions;

    private JobRestrictionPropertyBuider() {
        causeRestrictions = new LinkedList<JobCauseRestriction>();
    }

    @NonNull
    public static JobRestrictionPropertyBuider create() {
        return new JobRestrictionPropertyBuider();
    }

    @NonNull
    public JobRestrictionPropertyBuider addCauseRestriction(@NonNull JobCauseRestriction restriction) {
        causeRestrictions.add(restriction);
        return this;
    }

    public void applyTo(Job job) throws IOException {
        UpstreamCauseRestriction upstreamCauseRestriction = null;
        UserIdCauseRestriction userIdCauseRestriction = null;
        for (JobCauseRestriction restriction : causeRestrictions) {
            if (restriction instanceof UpstreamCauseRestriction) {
                upstreamCauseRestriction = (UpstreamCauseRestriction) restriction;
            } else if (restriction instanceof UserIdCauseRestriction) {
                userIdCauseRestriction = (UserIdCauseRestriction) restriction;
            }
        }

        JobRestrictionPropertyConfig config = new JobRestrictionPropertyConfig(upstreamCauseRestriction, userIdCauseRestriction);
        JobRestrictionProperty prop = new JobRestrictionProperty(config);

        job.addProperty(prop);
    }
}
