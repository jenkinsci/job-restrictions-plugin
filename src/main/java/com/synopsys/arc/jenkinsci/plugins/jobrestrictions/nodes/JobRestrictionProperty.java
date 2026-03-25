/*
 * The MIT License
 *
 * Copyright 2013-2016 Oleg Nenashev, Synopsys Inc.
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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.nodes;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionBlockageCause;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link NodeProperty}, which manages {@link JobRestriction}s for {@link Node}s.
 * Uses a TTL cache for {@link #canTake(Queue.BuildableItem)} to reduce repeated work.
 *
 * @author Oleg Nenashev
 */
public class JobRestrictionProperty extends NodeProperty<Node> {

    private static final Logger LOG = Logger.getLogger(JobRestrictionProperty.class.getName());

    /** Restriction according to buildable item requirements */
    JobRestriction jobRestriction;

    private static final boolean CACHE_DISABLED =
            Boolean.getBoolean(JobRestrictionProperty.class.getName() + ".cacheDisabled");

    private static final long CACHE_TTL_MS =
            Long.getLong(JobRestrictionProperty.class.getName() + ".cacheTtlMs", 30_000);

    private static final int CACHE_MAX_ENTRIES =
            Integer.getInteger(JobRestrictionProperty.class.getName() + ".cacheMaxEntries", 500);

    /**
     * Must be transient: XStream deserializes NodeProperty bypassing constructors
     * and field initializers, so a non-transient field initializer never runs.
     * Lazily initialized via {@link #getCache()}.
     */
    private transient volatile ConcurrentHashMap<String, CachedResult> cache;

    private ConcurrentHashMap<String, CachedResult> getCache() {
        ConcurrentHashMap<String, CachedResult> c = cache;
        if (c == null) {
            synchronized (this) {
                c = cache;
                if (c == null) {
                    c = new ConcurrentHashMap<>(64);
                    cache = c;
                }
            }
        }
        return c;
    }

    @DataBoundConstructor
    public JobRestrictionProperty(JobRestriction jobRestriction) {
        this.jobRestriction = jobRestriction;
    }

    @Override
    public CauseOfBlockage canTake(Queue.BuildableItem item) {
        if (jobRestriction == null) {
            return null;
        }
        if (CACHE_DISABLED) {
            return computeCanTake(item);
        }

        String cacheKey = cacheKey(item);
        long now = System.currentTimeMillis();
        ConcurrentHashMap<String, CachedResult> c = getCache();
        CachedResult cached = c.get(cacheKey);

        if (cached != null && (now - cached.timestampMs) < CACHE_TTL_MS) {
            LOG.log(Level.FINE, "[JobRestrictionProperty] cache hit key={0}", cacheKey);
            return cached.causeOfBlockage;
        }

        CauseOfBlockage result = computeCanTake(item);
        evictIfNeeded(c);
        if (result == null) {
            c.put(cacheKey, new CachedResult(null, now));
        }
        return result;
    }

    private static String cacheKey(Queue.BuildableItem item) {
        Queue.Task task = item.task;
        String taskId = task != null ? task.getFullDisplayName() : "null";
        return item.getId() + "|" + taskId;
    }

    private CauseOfBlockage computeCanTake(Queue.BuildableItem item) {
        boolean allow = jobRestriction.canTake(item);
        LOG.log(Level.FINE, "[JobRestrictionProperty] computeCanTake restriction={0} result={1}", new Object[] {
            jobRestriction.getClass().getSimpleName(), allow ? "ALLOW" : "BLOCK"
        });
        return allow ? null : JobRestrictionBlockageCause.DEFAULT;
    }

    private void evictIfNeeded(ConcurrentHashMap<String, CachedResult> c) {
        if (c.size() < CACHE_MAX_ENTRIES) {
            return;
        }
        long cutoff = System.currentTimeMillis() - CACHE_TTL_MS;
        c.entrySet().removeIf(e -> e.getValue().timestampMs < cutoff);
        if (c.size() >= CACHE_MAX_ENTRIES) {
            int toRemove = Math.max(1, c.size() - CACHE_MAX_ENTRIES / 2);
            List<String> keysToRemove = new ArrayList<>(toRemove);
            for (String key : c.keySet()) {
                if (keysToRemove.size() >= toRemove) break;
                keysToRemove.add(key);
            }
            keysToRemove.forEach(c::remove);
        }
    }

    public JobRestriction getJobRestriction() {
        return jobRestriction;
    }

    private static final class CachedResult {
        @CheckForNull
        final CauseOfBlockage causeOfBlockage;

        final long timestampMs;

        CachedResult(CauseOfBlockage causeOfBlockage, long timestampMs) {
            this.causeOfBlockage = causeOfBlockage;
            this.timestampMs = timestampMs;
        }
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.nodes_JobRestrictionProperty_DisplayName();
        }
    }
}
