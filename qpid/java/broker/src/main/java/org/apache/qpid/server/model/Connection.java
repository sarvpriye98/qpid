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
package org.apache.qpid.server.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public interface Connection extends ConfiguredObject
{

    // Statistics

    // Attributes

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_VERSION = "clientVersion";
    public static final String INCOMING = "incoming";
    public static final String LOCAL_ADDRESS = "localAddress";
    public static final String PRINCIPAL = "principal";
    public static final String PROPERTIES = "properties";
    public static final String REMOTE_ADDRESS = "remoteAddress";
    public static final String REMOTE_PROCESS_NAME = "remoteProcessName";
    public static final String REMOTE_PROCESS_PID = "remoteProcessPid";
    public static final String SESSION_COUNT_LIMIT = "sessionCountLimit";

    public static final Collection<String> AVAILABLE_ATTRIBUTES =
            Collections.unmodifiableCollection(
                    Arrays.asList(  CLIENT_ID,
                                    CLIENT_VERSION,
                                    INCOMING,
                                    LOCAL_ADDRESS,
                                    PRINCIPAL,
                                    PROPERTIES,
                                    REMOTE_ADDRESS,
                                    REMOTE_PROCESS_NAME,
                                    REMOTE_PROCESS_PID,
                                    SESSION_COUNT_LIMIT));

    //children
    Collection<Session> getSessions();

    void delete();
}
