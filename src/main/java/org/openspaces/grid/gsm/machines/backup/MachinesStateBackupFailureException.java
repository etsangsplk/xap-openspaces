/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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

package org.openspaces.grid.gsm.machines.backup;

import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementLoggerBehavior;


/**
 * @author itaif
 * @since 9.7
 */
public class MachinesStateBackupFailureException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure , SlaEnforcementLoggerBehavior {

    private static final long serialVersionUID = 1L;
    
    public MachinesStateBackupFailureException(ProcessingUnit pu, Throwable cause) {
        super(pu, "Failed to write machines state to space", cause);
    }
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticGridServiceAgentProvisioningFailureEvent event = new DefaultElasticGridServiceAgentProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }

    @Override
    public boolean isAlwaysLogStackTrace() {
        //space write stack trace is important
        return true;
    }

    @Override
    public boolean isAlwaysLogDuplicateException() {
        return false;
    }
}
