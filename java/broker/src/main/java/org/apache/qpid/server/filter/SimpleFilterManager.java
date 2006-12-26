/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.    
 *
 * 
 */
package org.apache.qpid.server.filter;

import org.apache.qpid.server.queue.AMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleFilterManager implements FilterManager
{
    private final Logger _logger = LoggerFactory.getLogger(SimpleFilterManager.class);

    private final ConcurrentLinkedQueue<MessageFilter> _filters;

    public SimpleFilterManager()
    {
        _logger.debug("Creating SimpleFilterManager");
        _filters = new ConcurrentLinkedQueue<MessageFilter>();
    }

    public void add(MessageFilter filter)
    {
        _filters.add(filter);
    }

    public void remove(MessageFilter filter)
    {
        _filters.remove(filter);
    }

    public boolean allAllow(AMQMessage msg)
    {
        for (MessageFilter filter : _filters)
        {
            try
            {
                if (!filter.matches(msg))
                {
                    return false;
                }
            }
            catch (JMSException e)
            {
                //fixme
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            }
        }
        return true;
    }

    public boolean hasFilters()
    {
        return !_filters.isEmpty();
    }
}
