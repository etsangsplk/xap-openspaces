/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.executor.support;

import org.openspaces.core.executor.Task;

import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A delegating task that runs under established access control settings.
 *
 * @author kimchy
 */
public class PrivilegedTask<T extends Serializable> extends SimpleDelegatingTask<T> {

    private transient T result;

    private transient Exception exception;

    /**
     * Constructs a new privileged task wrapping the actual task to execute.
     */
    public PrivilegedTask(Task<T> task) {
        super(task);
    }

    /**
     * Exeutes the provided task under access controls.
     */
    public T execute() throws Exception {
        AccessControlContext acc = AccessController.getContext();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    result = getDelegatedTask().execute();
                } catch (Exception ex) {
                    exception = ex;
                }
                return null;
            }
        }, acc);
        if (exception != null)
            throw exception;
        else
            return result;
    }
}
