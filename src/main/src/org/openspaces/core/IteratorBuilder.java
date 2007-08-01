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

package org.openspaces.core;

import com.j_spaces.core.client.GSIterator;
import com.j_spaces.core.client.Query;
import net.jini.core.lease.Lease;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility builder class for {@link com.j_spaces.core.client.GSIterator}. Allows to use method
 * chaining for simple configuration of an iterator and then call {@link #iterate()} to get the
 * actual iterator.
 *
 * <p>By default, when no template is added (using {@link #addTemplate(Object)} or
 * {@link #addTemplate(com.j_spaces.core.client.Query)}, a null template will be used to iterate
 * over all the content of the Space.
 *
 * <p>The iterator can also iterate on history entries. By default it will only iterate on future
 * entries in the Space (entries that match the given template(s)). When calling {@link #withHistory()}
 * the iterator will also iterate over existing entries.
 *
 * <p>Lease for the iterator can be controlled using {@link #leaseDuration(long)}. A leased iterator
 * which expires is considered as <em>invalidated</em>. A cancelled iterator is an exhausted iterator
 * and will have no more entities added to it. Calling <code>next</code> on an iterator with either
 * state always returns <code>null</code> or it may throw one of the allowed exceptions. In particular
 * <code>next(timeout)</code> may throw {@link java.rmi.NoSuchObjectException} to indicate that no entity
 * has been found during the allowed timeout. There is no guarantee that once <code>next(timeout)</code>
 * throws a <code>NoSuchObjectException</code>, or <code>next</code> returns <code>null</code>, all future
 * calls on that instance will do the same.
 *
 * <p>If there is a possibility that an iterator may become invalidated, it must
 * be leased. If there is no possibility that the iterator will become
 * invalidated, implementations should not lease it (i.e. use
 * {@link Lease#FOREVER}). If there is no further interest an iterator may
 * be <code>cancelled</code>.
 *
 * <p>An active lease on an iterator serves as a hint to the space that the
 * client is still interested in matching entities, and as a hint to the
 * client that the iterator is still functioning. There are cases, however,
 * where this may not be possible in particular, it is not expected that
 * iteration will maintain across crashes. If the lease expires or is
 * canceled, the iterator is invalidated. Clients should <em>not</em>
 * assume that the resources associated with a leased match set will be freed
 * if the match set reaches the exhausted state, and should instead cancel
 * the lease.
 *
 * <p>The maximum number of entries to pull from the space can be controlled using {@link #bufferSize(int)}
 * and defaults to <code>100</code>.
 *
 * @author kimchy
 * @see GigaSpace#iterator()
 */
public class IteratorBuilder {

    private GigaSpace gigaSpace;

    private List<Object> templates = new ArrayList<Object>();

    private boolean withHistory;

    private int bufferSize = 100;

    private long leaseDuration = Lease.FOREVER;

    /**
     * Constructs a new iterator builder using the given GigaSpace.
     */
    public IteratorBuilder(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * Sets to initially contain all of the visible matching entities in the space. If not called,
     * will contain only visible matching entities thereafter.
     */
    public IteratorBuilder withHistory() {
        withHistory = true;
        return this;
    }

    /**
     * <p>The maximum number of entries to pull from the space can be controlled using {@link #bufferSize(int)}
     * and defaults to <code>100</code>.
     */
    public IteratorBuilder bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * The requested initial lease time on the resulting
     * match set. Defaults to <code>FOREVER</code>.
     */
    public IteratorBuilder leaseDuration(long leaseDuration) {
        this.leaseDuration = leaseDuration;
        return this;
    }

    /**
     * Adds a template that will be used to control the matching entries the iterator
     * will iterate over.
     */
    public IteratorBuilder addTemplate(Object template) {
        templates.add(template);
        return this;
    }

    /**
     * Adds a template that will be used to control the matching entries the iterator
     * will iterate over.
     */
    public IteratorBuilder addTemplate(Query query) {
        templates.add(query);
        return this;
    }

    /**
     * Returns a new {@link com.j_spaces.core.client.GSIterator} based on the builder
     * configuration. If no templates were added, a null template will be added which
     * will cause the iterator to iterate over all the entries in the Space.
     */
    public GSIterator iterate() {
        if (templates.isEmpty()) {
            templates.add(null);
        }
        try {
            return new GSIterator(gigaSpace.getSpace(), templates, bufferSize, withHistory, leaseDuration);
        } catch (Exception e) {
            throw gigaSpace.getExceptionTranslator().translate(e);
        }
    }
}
