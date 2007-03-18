package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterOperationCodes;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link com.j_spaces.core.filters.FilterProvider FilterProvider} factory that accepts
 * a Pojo filter with different operation callbacks that are marked using this factory.
 * For each available operation there is a setter that accepts the method name to be invoked
 * if the operation happened. Not setting a callback means that this filter will not listen
 * to the mentioned operation. For example, if the filter ({@link #setFilter(Object)}) has
 * a method called <code>doSomethingBeforeWrite</code>, the {@link #setBeforeWrite(String)}
 * will need to be set with <code>doSomethingBeforeWrite</code>.
 *
 * <p>The operation callback methods can different arguments. Please see
 * {@link org.openspaces.core.space.filter.FilterOperationDelegateInvoker} for all
 * the different possibilites.
 *
 * <p>For a Pojo adapter that uses annotation please see {@link AnnotationFilterFactoryBean}.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.FilterOperationDelegate
 * @see com.j_spaces.core.filters.FilterProvider
 * @see com.j_spaces.core.filters.ISpaceFilter
 * @see com.j_spaces.core.filters.FilterOperationCodes
 */
public class MethodFilterFactoryBean extends AbstractFilterProviderAdapterFactoryBean {

    private String filterInit;

    private String filterClose;

    private String beforeWrite;

    private String afterWrite;

    private String beforeRead;

    private String beforeTake;

    private String beforeNotify;

    private String beforeCleanSpace;

    private String beforeUpdate;

    private String afteruUpdate;

    private String beforeReadMultiple;

    private String afterReadMultiple;

    private String beforeTakeMultiple;

    private String afterTakeMultiple;

    private String beforeNotifyTrigger;

    private String afterNotifyTrigger;

    private String beforeAllNotifyTrigger;

    private String afterAllNotifyTrigger;

    private String beforeRemoveByLease;

    private String afterRemoveByLease;

    /**
     * Creates an operation code to filter invoker map based on the {@link #getFilter()}
     * delegate and the callbacks set on this factory.
     */
    protected Map<Integer, FilterOperationDelegateInvoker> doGetInvokerLookup() {
        final Map<Integer, FilterOperationDelegateInvoker> invokerLookup = new HashMap<Integer, FilterOperationDelegateInvoker>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeWrite)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_WRITE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterWrite)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_WRITE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeRead)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_READ);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeTake)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_TAKE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeNotify)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_NOTIFY);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeCleanSpace)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_CLEAN_SPACE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeUpdate)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_UPDATE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afteruUpdate)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_UPDATE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeReadMultiple)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_READ_MULTIPLE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterReadMultiple)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_READ_MULTIPLE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeTakeMultiple)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_TAKE_MULTIPLE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterTakeMultiple)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_TAKE_MULTIPLE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeNotifyTrigger)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_NOTIFY_TRIGGER);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterNotifyTrigger)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_NOTIFY_TRIGGER);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeAllNotifyTrigger)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterAllNotifyTrigger)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), beforeRemoveByLease)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_REMOVE);
                }
                if (ObjectUtils.nullSafeEquals(method.getName(), afterRemoveByLease)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_REMOVE);
                }
            }
        });
        return invokerLookup;
    }

    /**
     * Returns the filter lifcycle method set with {@link #setFilterInit(String)}.
     */
    protected Method doGetInitMethod() {
        final AtomicReference<Method> ref = new AtomicReference<Method>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (ObjectUtils.nullSafeEquals(method.getName(), filterInit)) {
                    ref.set(method);
                }
            }
        });
        return ref.get();
    }

    /**
     * Returns the filter lifcycle method set with {@link #setFilterClose(String)}.
     */
    protected Method doGetCloseMethod() {
        final AtomicReference<Method> ref = new AtomicReference<Method>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (ObjectUtils.nullSafeEquals(method.getName(), filterClose)) {
                    ref.set(method);
                }
            }
        });
        return ref.get();
    }

    public void setFilterInit(String filterInit) {
        this.filterInit = filterInit;
    }

    public void setFilterClose(String filterClose) {
        this.filterClose = filterClose;
    }

    public void setBeforeWrite(String beforeWrite) {
        this.beforeWrite = beforeWrite;
    }

    public void setAfterWrite(String afterWrite) {
        this.afterWrite = afterWrite;
    }

    public void setBeforeRead(String beforeRead) {
        this.beforeRead = beforeRead;
    }

    public void setBeforeTake(String beforeTake) {
        this.beforeTake = beforeTake;
    }

    public void setBeforeNotify(String beforeNotify) {
        this.beforeNotify = beforeNotify;
    }

    public void setBeforeCleanSpace(String beforeCleanSpace) {
        this.beforeCleanSpace = beforeCleanSpace;
    }

    public void setBeforeUpdate(String beforeUpdate) {
        this.beforeUpdate = beforeUpdate;
    }

    public void setAfteruUpdate(String afteruUpdate) {
        this.afteruUpdate = afteruUpdate;
    }

    public void setBeforeReadMultiple(String beforeReadMultiple) {
        this.beforeReadMultiple = beforeReadMultiple;
    }

    public void setAfterReadMultiple(String afterReadMultiple) {
        this.afterReadMultiple = afterReadMultiple;
    }

    public void setBeforeTakeMultiple(String beforeTakeMultiple) {
        this.beforeTakeMultiple = beforeTakeMultiple;
    }

    public void setAfterTakeMultiple(String afterTakeMultiple) {
        this.afterTakeMultiple = afterTakeMultiple;
    }

    public void setBeforeNotifyTrigger(String beforeNotifyTrigger) {
        this.beforeNotifyTrigger = beforeNotifyTrigger;
    }

    public void setAfterNotifyTrigger(String afterNotifyTrigger) {
        this.afterNotifyTrigger = afterNotifyTrigger;
    }

    public void setBeforeAllNotifyTrigger(String beforeAllNotifyTrigger) {
        this.beforeAllNotifyTrigger = beforeAllNotifyTrigger;
    }

    public void setAfterAllNotifyTrigger(String afterAllNotifyTrigger) {
        this.afterAllNotifyTrigger = afterAllNotifyTrigger;
    }

    public void setBeforeRemoveByLease(String beforeRemoveByLease) {
        this.beforeRemoveByLease = beforeRemoveByLease;
    }

    public void setAfterRemoveByLease(String afterRemoveByLease) {
        this.afterRemoveByLease = afterRemoveByLease;
    }
}
