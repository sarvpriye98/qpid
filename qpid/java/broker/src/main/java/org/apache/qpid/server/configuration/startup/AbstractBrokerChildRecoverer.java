/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.configuration.startup;

import org.apache.qpid.server.configuration.ConfigurationEntry;
import org.apache.qpid.server.configuration.ConfiguredObjectRecoverer;
import org.apache.qpid.server.configuration.RecovererProvider;
import org.apache.qpid.server.model.Broker;
import org.apache.qpid.server.model.ConfiguredObject;

public abstract class AbstractBrokerChildRecoverer<T extends ConfiguredObject> implements ConfiguredObjectRecoverer<T>
{

    @Override
    public T create(RecovererProvider recovererProvider, ConfigurationEntry entry, ConfiguredObject... parents)
    {
        if (parents == null || parents.length == 0)
        {
            throw new IllegalArgumentException("Broker parent is not passed!");
        }
        if (parents.length != 1)
        {
            throw new IllegalArgumentException("Only one parent is expected!");
        }
        if (!(parents[0] instanceof Broker))
        {
            throw new IllegalArgumentException("Parent is not a broker");
        }
        Broker broker = (Broker)parents[0];
        return createBrokerChild(recovererProvider, entry, broker);
    }

    abstract T createBrokerChild(RecovererProvider recovererProvider, ConfigurationEntry entry, Broker broker);

}
