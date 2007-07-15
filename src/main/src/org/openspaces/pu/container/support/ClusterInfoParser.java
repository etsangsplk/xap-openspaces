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

package org.openspaces.pu.container.support;

import org.openspaces.core.cluster.ClusterInfo;

/**
 * {@link org.openspaces.core.cluster.ClusterInfo} parser that parses -cluster parameter and
 * transforms it into a cluster info.
 * 
 * <p>
 * The following arguments to the -cluster parameters are allowed: <code>total_members=1,1</code>
 * (1,1 is an example value), <code>id=1</code> (1 is an example value), <code>backupid=1</code>
 * (1 is an example value) and <code>schema=primary_backup</code> (primary_backup is an example
 * value).
 * 
 * @author kimchy
 */
public abstract class ClusterInfoParser {

    public static final String CLUSTER_PARAMETER_TOTALMEMBERS = "total_members";
    public static final String CLUSTER_PARAMETER_INSTANCEID = "id";
    public static final String CLUSTER_PARAMETER_BACKUPID = "backup_id";
    public static final String CLUSTER_PARAMETER_CLUSTERSCHEMA = "schema";

    public static ClusterInfo parse(CommandLineParser.Parameter[] params) throws IllegalArgumentException {
        ClusterInfo clusterInfo = null;
        for (CommandLineParser.Parameter param : params) {
            if (!param.getName().equalsIgnoreCase("cluster")) {
                continue;
            }

            if (clusterInfo == null) {
                clusterInfo = new ClusterInfo();
            }

            if (param.getArguments().length == 0) {
                throw new IllegalArgumentException("cluster parameter should have at least one parameter");
            }

            for (int j = 0; j < param.getArguments().length; j++) {
                String clusterParameter = param.getArguments()[j];
                int equalsIndex = clusterParameter.indexOf("=");
                if (equalsIndex == -1) {
                    throw new IllegalArgumentException("Cluster paramter [" + clusterParameter
                            + "] is mailformed, must have a name=value syntax");
                }
                String clusterParamName = clusterParameter.substring(0, equalsIndex);
                String clusterParamValue = clusterParameter.substring(equalsIndex + 1);
                if (CLUSTER_PARAMETER_TOTALMEMBERS.equalsIgnoreCase(clusterParamName)) {
                    int commaIndex = clusterParamValue.indexOf(',');
                    if (commaIndex == -1) {
                        clusterInfo.setNumberOfInstances(Integer.valueOf(clusterParamValue));
                    } else {
                        String numberOfInstances = clusterParamValue.substring(0, commaIndex);
                        String numberOfBackups = clusterParamValue.substring(commaIndex + 1);
                        clusterInfo.setNumberOfInstances(Integer.valueOf(numberOfInstances));
                        clusterInfo.setNumberOfBackups(Integer.valueOf(numberOfBackups));
                    }
                } else if (CLUSTER_PARAMETER_INSTANCEID.equalsIgnoreCase(clusterParamName)) {
                    clusterInfo.setInstanceId(Integer.valueOf(clusterParamValue));
                } else if (CLUSTER_PARAMETER_BACKUPID.equalsIgnoreCase(clusterParamName)) {
                    clusterInfo.setBackupId(Integer.valueOf(clusterParamValue));
                } else if (CLUSTER_PARAMETER_CLUSTERSCHEMA.equalsIgnoreCase(clusterParamName)) {
                    clusterInfo.setSchema(clusterParamValue);
                } else {
                    throw new IllegalArgumentException("deploy parameter property name [" + clusterParamName
                            + "] is invalid");
                }
            }
        }
        return clusterInfo;
    }
}
