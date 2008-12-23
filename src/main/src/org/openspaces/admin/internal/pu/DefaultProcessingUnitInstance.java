package org.openspaces.admin.internal.pu;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.space.DefaultSpaceInstances;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstances;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.pu.service.ServiceDetails;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstance extends AbstractGridComponent implements InternalProcessingUnitInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puServiceBean;

    private final PUDetails puDetails;

    private volatile InternalProcessingUnit processingUnit;

    private volatile GridServiceContainer gridServiceContainer;

    private volatile ProcessingUnitPartition processingUnitPartition;

    private final SpaceServiceDetails[] embeddedSpacesDetails;

    private final SpaceServiceDetails[] spacesDetails;

    private final JeeServiceDetails jeeDetails;

    private final Map<String, ServiceDetails[]> servicesDetailsByServiceId;

    private final InternalSpaceInstances spaceInstances;

    public DefaultProcessingUnitInstance(ServiceID serviceID, PUDetails puDetails, PUServiceBean puServiceBean, InternalAdmin admin) {
        super(admin);
        this.serviceID = serviceID;
        this.uid = serviceID.toString();
        this.puDetails = puDetails;
        this.puServiceBean = puServiceBean;

        this.spaceInstances = new DefaultSpaceInstances(admin);

        ArrayList<SpaceServiceDetails> embeddedSpacesEmbeddedList = new ArrayList<SpaceServiceDetails>();
        ArrayList<SpaceServiceDetails> spacesDetailsList = new ArrayList<SpaceServiceDetails>();
        JeeServiceDetails jeeDetailsX = null;

        Map<String, List<ServiceDetails>> servicesDetailsByServiceIdList = new HashMap<String, List<ServiceDetails>>();

        for (ServiceDetails serviceDetails : puDetails.getDetails()) {

            List<ServiceDetails> list = servicesDetailsByServiceIdList.get(serviceDetails.getServiceType());
            if (list == null) {
                list = new ArrayList<ServiceDetails>();
                servicesDetailsByServiceIdList.put(serviceDetails.getServiceType(), list);
            }
            list.add(serviceDetails);

            if (serviceDetails instanceof SpaceServiceDetails) {
                SpaceServiceDetails spaceDetails = (SpaceServiceDetails) serviceDetails;
                spacesDetailsList.add(spaceDetails);
                if (spaceDetails.getSpaceType() == SpaceType.EMBEDDED) {
                    embeddedSpacesEmbeddedList.add((SpaceServiceDetails) serviceDetails);
                }
            } else if (serviceDetails instanceof JeeServiceDetails) {
                jeeDetailsX = (JeeServiceDetails) serviceDetails;
            }
        }

        jeeDetails = jeeDetailsX;
        embeddedSpacesDetails = embeddedSpacesEmbeddedList.toArray(new SpaceServiceDetails[embeddedSpacesEmbeddedList.size()]);
        spacesDetails = spacesDetailsList.toArray(new SpaceServiceDetails[spacesDetailsList.size()]);

        Map<String, ServiceDetails[]> servicesDetailsTemp = new HashMap<String, ServiceDetails[]>();
        for (Map.Entry<String, List<ServiceDetails>> entry : servicesDetailsByServiceIdList.entrySet()) {
            servicesDetailsTemp.put(entry.getKey(), entry.getValue().toArray(new ServiceDetails[entry.getValue().size()]));
        }
        servicesDetailsByServiceId = servicesDetailsTemp;
    }

    public String getUid() {
        return this.uid;
    }

    public int getInstanceId() {
        return puDetails.getClusterInfo().getInstanceId();
    }

    public int getBackupId() {
        if (puDetails.getClusterInfo().getBackupId() == null) {
            return 0;
        }
        return puDetails.getClusterInfo().getBackupId();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public ProcessingUnit getProcessingUnit() {
        return this.processingUnit;
    }

    public String getName() {
        return puDetails.getClusterInfo().getName();
    }

    public void setProcessingUnit(ProcessingUnit processingUnit) {
        this.processingUnit = (InternalProcessingUnit) processingUnit;
    }

    public ClusterInfo getClusterInfo() {
        return puDetails.getClusterInfo();
    }

    public Iterator<ServiceDetails> iterator() {
        return Arrays.asList(puDetails.getDetails()).iterator();
    }

    public SpaceServiceDetails[] getSpaceServiceDetails() {
        return spacesDetails;
    }

    public SpaceServiceDetails getEmbeddedSpaceServiceDetails() {
        if (embeddedSpacesDetails.length == 0) {
            return null;
        }
        return embeddedSpacesDetails[0];
    }

    public SpaceServiceDetails[] getEmbeddedSpacesServiceDetails() {
        return embeddedSpacesDetails;
    }

    public ServiceDetails[] getServicesDetails() {
        return puDetails.getDetails();
    }

    public ServiceID getGridServiceContainerServiceID() {
        return puDetails.getGscServiceID();
    }

    public void setGridServiceContainer(GridServiceContainer gridServiceContainer) {
        this.gridServiceContainer = gridServiceContainer;
    }

    public GridServiceContainer getGridServiceContainer() {
        return this.gridServiceContainer;
    }

    public void setProcessingUnitPartition(ProcessingUnitPartition processingUnitPartition) {
        this.processingUnitPartition = processingUnitPartition;
    }

    public ProcessingUnitPartition getPartition() {
        return this.processingUnitPartition;
    }

    public PUServiceBean getPUServiceBean() {
        return this.puServiceBean;
    }

    public boolean isEmbeddedSpaces() {
        return spaceInstances.size() != 0;
    }

    public SpaceInstance getSpaceInstance() {
        Iterator<SpaceInstance> it = spaceInstances.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.getSpaceInstances();
    }

    public void addSpaceInstnaceIfMatching(SpaceInstance spaceInstance) {
        for (SpaceServiceDetails spaceDetails : embeddedSpacesDetails) {
            if (((InternalSpaceInstance) spaceInstance).getServiceID().equals(spaceDetails.getServiceID())) {
                spaceInstances.addSpaceInstance(spaceInstance);
                processingUnit.addEmbeddedSpace(spaceInstance.getSpace());
            }
        }
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.removeSpaceInstance(uid);
    }

    public boolean isJee() {
        return jeeDetails != null;
    }

    public JeeServiceDetails getJeeDetails() {
        return jeeDetails;
    }

    public ServiceDetails[] getServicesDetailsByServiceType(String serviceType) {
        return servicesDetailsByServiceId.get(serviceType);
    }

    public Map<String, ServiceDetails[]> getServiceDetailsByServiceType() {
        return Collections.unmodifiableMap(servicesDetailsByServiceId);
    }

    public void destroy() {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).destroyInstance(this);
    }

    public void relocate(GridServiceContainer gridServiceContainerToRelocateTo) {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).relocate(this, gridServiceContainerToRelocateTo);
    }

    public void decrement() {
        if (!processingUnit.isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) processingUnit.getManagingGridServiceManager()).decrementInstance(this);
    }

    // info providers

    public NIODetails getNIODetails() throws RemoteException {
        return puServiceBean.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return puServiceBean.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return puServiceBean.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return puServiceBean.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return puServiceBean.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return puServiceBean.getJVMStatistics();
    }
}
