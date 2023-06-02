package io.jenkins.plugins.jobrestrictions.pipeline;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.Run;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Injects {@link hudson.model.Cause.UpstreamCause}s to {@link org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution.PlaceholderTask}s.
 * if they are missing.
 *
 * @author msqb
 * @author oleg-nenashev
 */
@Extension(optional = true)
@Restricted({NoExternalUse.class})
public class PlaceHolderTaskActionFactory extends TransientActionFactory<Queue.BuildableItem> {

    private static final Logger LOGGER = Logger.getLogger(PlaceHolderTaskActionFactory.class.getName());

    static {
        LOGGER.log(Level.FINE, "Instantiated a PlaceholderTask action factory");
    }

    @Override
    public Class<Queue.BuildableItem> type() {
        return Queue.BuildableItem.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull Queue.BuildableItem item) {
        if (item.task instanceof ExecutorStepExecution.PlaceholderTask) {
            // => We may cause duplicate actions, but it is better than stack overflow
            // CauseAction cause = item.getAction(CauseAction.class);
            //  Cause.UpstreamCause upstreamCause = cause != null ? cause.findCause(Cause.UpstreamCause.class) : null;
            //  if (upstreamCause == null) {
            // There is no appended upstream cause, so we create a new one
            final ExecutorStepExecution.PlaceholderTask placeholderTask =
                    (ExecutorStepExecution.PlaceholderTask) item.task;
            Run<?, ?> placeholderTaskRun = placeholderTask.run();
            if (placeholderTaskRun == null) {
                placeholderTaskRun = placeholderTask.runForDisplay();
            }

            if (placeholderTaskRun != null) {
                // We found the parent task
                return Collections.singleton(new CauseAction(new Cause.UpstreamCause(placeholderTaskRun)));
            }

            // Failed to resolve the  cause
            LOGGER.severe(MessageFormat.format(
                    "PlaceholderTask {0} from plugin {1} could not have its causes retrieved.",
                    placeholderTask, "workflow-durable-task-step"));
            //  }
        }
        return Collections.emptyList();
    }
}
