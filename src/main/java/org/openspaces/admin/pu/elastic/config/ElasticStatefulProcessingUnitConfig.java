/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.config.AbstractElasticProcessingUnitConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.topology.DedicatedMachineProvisioningInternal;
import org.openspaces.admin.pu.elastic.topology.SharedMachineProvisioningInternal;
import org.openspaces.admin.pu.topology.ElasticStatefulProcessingUnitConfigHolder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name = "elastic-stateful-pu")
public class ElasticStatefulProcessingUnitConfig 
    extends AbstractElasticProcessingUnitConfig 
    implements ElasticStatefulProcessingUnitConfigHolder {

    public static final String MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "max-memory-capacity-megabytes";
    public static final String MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "min-memory-capacity-megabytes";
    
    Map<String,String> scaleStrategy;
    private long maxMemoryCapacityInMB;
    private int numberOfBackupInstancesPerPartition = 1;
    private int numberOfPartitions;
    private int maxProcessingUnitInstancesFromSamePartitionPerMachine = 1;
    private double maxNumberOfCpuCores;
    private double minNumberOfCpuCoresPerMachine;
    private Admin admin;
    private boolean allowAboveAverageMemoryPerMachine;

    public ElasticStatefulProcessingUnitConfig() {
        super();
        
        // add an elastic property indicating the cluster schema partitioned-sync2backup
        new ProcessingUnitSchemaConfig(super.getElasticProperties()).setPartitionedSync2BackupSchema();        
    }
    
    @Override
    public ProcessingUnitConfig toProcessingUnitConfig() {
      
        ProcessingUnitConfig config = super.toProcessingUnitConfig();

        if (getMachineProvisioning() != null && getMinNumberOfCpuCoresPerMachine() <= 0) {
            // try to figure out from machine provisioning
            setMinNumberOfCpuCoresPerMachine(super.getMachineProvisioning().getMinimumNumberOfCpuCoresPerMachine());
            if (getMinNumberOfCpuCoresPerMachine() <= 0 &&
                !(getMachineProvisioning() instanceof DiscoveredMachineProvisioningConfig)) {
                
                throw new IllegalStateException("Elastic Machine Provisioning configuration must supply the expected minimum number of CPU cores per machine.");
            }
        }
        
        
        if (this.getMaxMemoryCapacityInMB() == 0 && this.getNumberOfPartitions() == 0) {
            throw new IllegalStateException("maxMemoryCapacity must be defined.");
        }
        
        if (this.getMaxMemoryCapacityInMB() != 0 && this.getNumberOfPartitions() != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxMemoryCapacity. Please specify only one of these properties.");
        }
        
        if (this.getMaxNumberOfCpuCores() != 0 && this.getNumberOfPartitions() != 0) {
            throw new IllegalStateException("numberOfPartitions conflicts with maxNumberOfCpuCores. Please specify only one of these properties.");
        }

        int numberOfInstances = this.getNumberOfPartitions();
        if (numberOfInstances == 0) {
            numberOfInstances = Math.max(calcNumberOfPartitionsFromMemoryRequirements(),calcNumberOfPartitionsFromCpuRequirements(admin));
        }
        
        if (getNumberOfBackupInstancesPerPartition() == 0) {
            // allow instances from DIFFERENT partitions to deploy on same Container
            config.setMaxInstancesPerMachine(0);   
            config.setMaxInstancesPerVM(0);
        }
        else {
            // disallow instances from SAME partition to deploy on same Container
            config.setMaxInstancesPerVM(1);
            // allow or disallow instances from SAME partition to deploy on same Container
            config.setMaxInstancesPerMachine(this.getMaxProcessingUnitInstancesFromSamePartitionPerMachine());
        }
        
        config.setClusterSchema("partitioned-sync2backup");
        config.setNumberOfInstances(numberOfInstances);
        config.setNumberOfBackups(getNumberOfBackupInstancesPerPartition());
        
        return config;
    }
    
    protected int calcNumberOfPartitionsFromMemoryRequirements() {
        
        long maximumMemoryCapacityInMB = new GridServiceContainerConfig(super.getElasticProperties()).getMaximumMemoryCapacityInMB();
                
        if (maximumMemoryCapacityInMB <= 0) {
            throw new IllegalStateException("memoryCapacityPerContainer is undefined.");    
        }
                
        double totalNumberOfInstances = Math.ceil(((double)getMaxMemoryCapacityInMB())/maximumMemoryCapacityInMB);
        int numberOfPartitions = (int) Math.ceil(totalNumberOfInstances / (getNumberOfBackupInstancesPerPartition()+1));
                
        return Math.max(1, numberOfPartitions);
    }

    protected int calcNumberOfPartitionsFromCpuRequirements(Admin admin) {
        
        int maximumNumberOfPrimaryInstances = 1;
        
        if (getMaxNumberOfCpuCores() > 0) {
            
            if (getMinNumberOfCpuCoresPerMachine() <= 0) {
                if (admin == null) {
                    throw new IllegalStateException("call #setAdmin() or #setNumberOfPartitions() before calling toProcessingUnitConfig()");
                }
                setMinNumberOfCpuCoresPerMachine(DiscoveredMachineProvisioningConfig.detectMinimumNumberOfCpuCoresPerMachine(admin));
            }
            
            maximumNumberOfPrimaryInstances =(int) Math.ceil(this.getMaxNumberOfCpuCores() / getMinNumberOfCpuCoresPerMachine());
        }
        return maximumNumberOfPrimaryInstances; 
    }

    public long getMaxMemoryCapacityInMB() {
        return maxMemoryCapacityInMB;
    }

    public int getNumberOfBackupInstancesPerPartition() {
        return numberOfBackupInstancesPerPartition;
    }

    @XmlAttribute(name = "number-of-backups-per-partition")
    public void setNumberOfBackupInstancesPerPartition(int numberOfBackupInstancesPerPartition) {
        this.numberOfBackupInstancesPerPartition = numberOfBackupInstancesPerPartition;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public int getMaxProcessingUnitInstancesFromSamePartitionPerMachine() {
        return maxProcessingUnitInstancesFromSamePartitionPerMachine;
    }

    public void setMaxProcessingUnitInstancesFromSamePartitionPerMachine(
            int maxProcessingUnitInstancesFromSamePartitionPerMachine) {
        this.maxProcessingUnitInstancesFromSamePartitionPerMachine = maxProcessingUnitInstancesFromSamePartitionPerMachine;
    }

    public double getMaxNumberOfCpuCores() {
        return maxNumberOfCpuCores;
    }



    @Deprecated
    public double getMinNumberOfCpuCoresPerMachine() {
        return minNumberOfCpuCoresPerMachine;
    }

    @Deprecated
    public void setMinNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        this.minNumberOfCpuCoresPerMachine = minNumberOfCpuCoresPerMachine;
    }

    public boolean isAllowAboveAverageMemoryPerMachine() {
        return allowAboveAverageMemoryPerMachine;
    }

    public void setAllowAboveAverageMemoryPerMachine(boolean allowAboveAverageMemoryPerMachine) {
        this.allowAboveAverageMemoryPerMachine = allowAboveAverageMemoryPerMachine;
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @XmlAttribute(name = "max-number-of-cpu-cores")
    public void setMaxNumberOfCpuCores(double maxNumberOfCpuCores) {
        this.maxNumberOfCpuCores = maxNumberOfCpuCores;
    }

    @XmlAttribute(name = "number-of-partitions")
    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    @XmlAttribute(name = "file")
    public void setProcessingUnitFile(String processingUnitFilePath) {
        super.setProcessingUnit(processingUnitFilePath);
    }

    @XmlAttribute(name = "puname")
    public void setProcessingUnitName(String processingUnitName) {
        super.setProcessingUnit(processingUnitName);
    }

    @XmlAttribute(name = "max-memory-capacity-in-mb")
    public void setMaxMemoryCapacityInMB(long maxMemoryCapacityInMB) {
        this.maxMemoryCapacityInMB = maxMemoryCapacityInMB;
    }

    @Override
    @XmlElement(type = UserDetailsConfig.class)
    public void setUserDetails(UserDetailsConfig userDetails) {
        super.setUserDetails(userDetails);
    }

    @Override
    @XmlAttribute(name = "secured")
    public void setSecured(Boolean secured) {
        super.setSecured(secured);
    }

    @Override
    @XmlAttribute(name = "memory-capacity-per-container-in-mb")
    public void setMemoryCapacityPerContainerInMB(long memoryInMB) {
        super.setMemoryCapacityPerContainerInMB(memoryInMB);
    }

    @Override
    @XmlElement(type = ElasticMachineProvisioningConfig.class)
    public void setMachineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig) {
        super.setMachineProvisioning(machineProvisioningConfig);
    }

    @Override
    @XmlElement(type = ScaleStrategyConfig.class)
    public void setScaleStrategy(ScaleStrategyConfig scaleStrategy) {
        super.setScaleStrategy(scaleStrategy);
    }

    /**
     * @see org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment#highlyAvailable(boolean)
     */
    @XmlAttribute(name = "highly-available")
    public void setHighlyAvailable(boolean highlyAvailable) {
        setNumberOfBackupInstancesPerPartition((highlyAvailable? 1:0));
    }

    /**
     * @see org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment#singleMachineDeployment()
     */
    @XmlAttribute(name = "single-machine-deployment")
    public void setSingleMachineDeployment(boolean singleMachineDeployment) {
        if (singleMachineDeployment) {
            setMaxProcessingUnitInstancesFromSamePartitionPerMachine(0);
        }
    }

    @Override
    @XmlElement(type = ProcessingUnitDependency.class)
    public void setDeploymentDependencies(ProcessingUnitDependency[] dependencies) {
        super.setDeploymentDependencies(dependencies);
    }

    /**
     * Parse the shared-machine-provisioning bean, get its data and apply them to the relevant methods to enable shared machine provisioning
     */
    @XmlElement(type = SharedMachineProvisioningInternal.class)
    public void setSharedMachineProvisioning(SharedMachineProvisioningInternal sharedMachineProvisioningInternal) {
        this.setSharedIsolation(sharedMachineProvisioningInternal.getSharingId());
        this.setMachineProvisioning(sharedMachineProvisioningInternal.getElasticMachineProvisioningConfig());
    }

    /**
     * Parse the dedicated-machine-provisioning bean, get its data and apply them to the relevant methods to enable dedicated machine provisioning
     */
    @XmlElement(type = DedicatedMachineProvisioningInternal.class)
    public void setDedicatedMachineProvisioning(DedicatedMachineProvisioningInternal dedicatedMachineProvisioningInternal) {
        this.setDedicatedIsolation();
        this.setMachineProvisioning(dedicatedMachineProvisioningInternal.getElasticMachineProvisioningConfig());
    }
}
