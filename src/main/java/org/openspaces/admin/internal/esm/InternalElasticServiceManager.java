/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.esm;

import java.rmi.Remote;
import java.util.Map;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.monitor.event.Events;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

import com.gigaspaces.grid.esm.ESM;

/**
 * @author Moran Avigdor
 * @author itaif
 */
public interface InternalElasticServiceManager extends ElasticServiceManager, InternalAgentGridComponent  {

    ServiceID getServiceID();

    ESM getESM();
    
    void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties);
    
    void setProcessingUnitScaleStrategyConfig(ProcessingUnit pu, ScaleStrategyConfig scaleStrategyConfig);

    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(ProcessingUnit pu);

    boolean isManagingProcessingUnit(ProcessingUnit pu);

    boolean isManagingProcessingUnitAndScaleNotInProgress(ProcessingUnit pu);
    
    /**
     * @since 9.0.0
     * This is a workaround for GS-9999
     */
    @Deprecated
    boolean isManagingProcessingUnitAndScaleNotInProgressNoCache(ProcessingUnit pu);
    
    /**
     * @since 8.0.6
     */
    boolean isManagingProcessingUnitAndScaleInProgress(ProcessingUnit pu);
    
    /**
     * @return the list of events generated by the ESM.
     * For the beginning of the list start with cursor = 0
     * @since 8.0.6
     */
    Events getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents);

    /**
     * reflect the ESM state in the admin API object by analyzing the events
     * @since 8.0.6
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);   

    /**
     * expose cloud storage API
     * @return an instance of a class implementing specific cloud storage API's.
     * @param processingUnitName - the processing unit name the cloud driver belongs to. 
     * @since 9.5.0
     * @author elip
     */
    Remote getStorageApi(final String processingUnitName);
}
