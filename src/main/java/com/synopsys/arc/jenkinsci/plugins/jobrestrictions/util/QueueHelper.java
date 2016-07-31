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

import hudson.model.Item;
import hudson.model.Queue;

/**
 * Provides additional for Queue objects.
 * @author Oleg Nenashev
 */
public class QueueHelper {
    /**
     * Generates job-style project name for the buildable item
     * @deprecated Just a hack, will be removed in the future versions
     * @param item
     * @return String in the Job::getFullName() format (a/b/c/d)
     */
    public static String getFullName(Queue.BuildableItem item) {
        Queue.Task current = item.task;
        String res = current.getName();
        //Fetching the full path of the item
        if (current instanceof Item) {
            Item stub = (Item)current;
            return stub.getFullName();
        }
        
        // Default approach via interface
        while (true) {
            Queue.Task parent = current.getOwnerTask();
            if (parent == current || parent == null) {
                break;
            }          
            res = parent.getName() + "/" + res;
            current = parent;
        }        
        return res;
    }
}
