package org.openspaces.core.space.filter;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>An {@link com.j_spaces.core.filters.ISpaceFilter} implementation that acts as an adapter
 * delegating the execution of the filter lifecycle methods and specific operation to pluggable
 * reflection based methods.
 *
 * <p>Holds a {@link java.lang.reflect.Method} representing an init callback, and one representing
 * close callback. Both can be <code>null</code> for cases where no delegation is required.
 *
 * <p>Holds a map of {@link FilterOperationDelegateInvoker} per operation code. Once <code>process</code> is
 * called, a {@link FilterOperationDelegateInvoker} is aquired based on the operation code, and if found,
 * the invocation is delegated to it.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.FilterOperationDelegateInvoker
 */
public class FilterOperationDelegate implements ISpaceFilter {

    private Object delegate;

    private Map<Integer, FilterOperationDelegateInvoker> invokerLookup;

    private Method initMethod;

    private Method closeMethod;


    private IJSpace space;

    /**
     * Constructs a new filter operation delegate. Providing the delgate to perform the invocation on and a map
     * of operation per {@link org.openspaces.core.space.filter.FilterOperationDelegateInvoker}.
     */
    public FilterOperationDelegate(Object delegate, Map<Integer, FilterOperationDelegateInvoker> invokerLookup) {
        this.delegate = delegate;
        this.invokerLookup = invokerLookup;
    }

    /**
     * Sets an optional init method callback.
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
        if (initMethod != null) {
            initMethod.setAccessible(true);
        }
    }

    /**
     * Sets an optional close method callback.
     */
    public void setCloseMethod(Method closeMethod) {
        this.closeMethod = closeMethod;
        if (closeMethod != null) {
            closeMethod.setAccessible(true);
        }
    }

    /**
     * If {@link #setInitMethod(java.lang.reflect.Method) initMethod} is supplied, will invoke it.
     * The method signature can have no parameters or can have a single {@link com.j_spaces.core.IJSpace}.
     */
    public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {
        this.space = space;
        if (initMethod == null) {
            return;
        }
        Object[] params = null;
        if (initMethod.getParameterTypes().length == 1) {
            params = new Object[]{space};
        }
        try {
            initMethod.invoke(delegate, params);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access init method [" + initMethod.getName() + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute init method [" + initMethod.getName() + "]", e);
        }
    }

    /**
     * Fetch a {@link org.openspaces.core.space.filter.FilterOperationDelegateInvoker} based on the operation
     * code. If found, delegates to its process method.
     */
    public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
        FilterOperationDelegateInvoker invoker = invokerLookup.get(operationCode);
        if (invoker != null) {
            invoker.invokeProcess(space, delegate, context, entry);
        }
    }

    /**
     * Fetch a {@link org.openspaces.core.space.filter.FilterOperationDelegateInvoker} based on the operation
     * code. If found, delegates to its process method.
     */
    public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {
        FilterOperationDelegateInvoker invoker = invokerLookup.get(operationCode);
        if (invoker != null) {
            invoker.invokeProcess(space, delegate, context, entries);
        }
    }

    /**
     * If {@link #setCloseMethod(java.lang.reflect.Method) closeMethod} is supplied, will invoke it.
     * The method signature should have no parameters.
     */
    public void close() throws RuntimeException {
        if (closeMethod == null) {
            return;
        }
        try {
            closeMethod.invoke(delegate);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access close method [" + closeMethod.getName() + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute close method [" + closeMethod.getName() + "]", e);
        }
    }
}
