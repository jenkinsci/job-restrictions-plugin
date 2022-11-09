/*
 * The MIT License
 *
 * Copyright 2013 Oleg Nenashev, Synopsys Inc.
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
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Queue;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Provides additional for Queue objects.
 * @author Oleg Nenashev
 */
@Restricted(NoExternalUse.class)
public class QueueHelper {

    //TODO: Optimize by StringBuilder
    /**
     * Generates job-style project name for the buildable item
     * @deprecated Just a hack, will be removed in the future versions
     * @param item Item, for which the name should be retrieved
     * @return String in the {@link Job#getFullName()} format (a/b/c/d)
     */
    @Deprecated
    public static String getFullName(@NonNull Queue.BuildableItem item) {
        Queue.Task current = item.task;
        String res = getItemName(current);

        // this is only executed if we didn't call Item.getFullName() in getItemName
        while (!(current instanceof Item)) {
            Queue.Task parent = current.getOwnerTask();
            if (parent == current) {
                break;
            }
            res = getItemName(parent) + "/" + res;
            current = parent;
        }
        return res;
    }

    private static String getItemName(Queue.Task task) {
        if (task instanceof Item) {
            Item stub = (Item)task;
            return stub.getFullName();
        } else {
            return task.getName();
        }
    }
}
