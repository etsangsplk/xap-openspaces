package org.openspaces.admin.lus;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.lus.events.LookupServiceAddedEventManager;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface LookupServices extends AdminAware, Iterable<LookupService> {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    Map<String, LookupService> getUids();

    int size();

    boolean isEmpty();

    void addLifecycleListener(LookupServiceLifecycleEventListener eventListener);

    void removeLifecycleListener(LookupServiceLifecycleEventListener eventListener);

    LookupServiceAddedEventManager getLookupServiceAdded();

    LookupServiceRemovedEventManager getLookupServiceRemoved();
}
