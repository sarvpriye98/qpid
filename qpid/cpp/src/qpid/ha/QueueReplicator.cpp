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

#include "QueueReplicator.h"
#include "ReplicatingSubscription.h"
#include "qpid/broker/Bridge.h"
#include "qpid/broker/Broker.h"
#include "qpid/broker/Link.h"
#include "qpid/broker/Queue.h"
#include "qpid/broker/QueueRegistry.h"
#include "qpid/broker/SessionHandler.h"
#include "qpid/framing/SequenceSet.h"
#include "qpid/framing/FieldTable.h"
#include "qpid/log/Statement.h"
#include <boost/shared_ptr.hpp>

namespace {
const std::string QPID_REPLICATOR_("qpid.replicator-");
}

namespace qpid {
namespace ha {
using namespace broker;

QueueReplicator::QueueReplicator(boost::shared_ptr<Queue> q, boost::shared_ptr<Link> l)
    : Exchange(QPID_REPLICATOR_+q->getName(), 0, 0), // FIXME aconway 2011-11-24: hidden from management?
      queue(q), link(l), current(queue->getPosition())
{
    // FIXME aconway 2011-11-24: consistent logging.
    QPID_LOG(debug, "HA: Replicating queue " << q->getName() << " " << q->getSettings());
    queue->getBroker()->getLinks().declare(
        link->getHost(), link->getPort(),
        false,              // durable
        queue->getName(),   // src
        getName(),          // dest
        "",                 // key
        false,              // isQueue
        false,              // isLocal
        "",                 // id/tag
        "",                 // excludes
        false,              // dynamic
        0,                  // sync?
        boost::bind(&QueueReplicator::initializeBridge, this, _1, _2)
    );
}

QueueReplicator::~QueueReplicator() {}

// NB: This is called back ina broker connection thread when the
// bridge is created.
void QueueReplicator::initializeBridge(Bridge& bridge, SessionHandler& sessionHandler) {
    // No lock needed, no mutable member variables are used.
    framing::AMQP_ServerProxy peer(sessionHandler.out);
    const qmf::org::apache::qpid::broker::ArgsLinkBridge& args(bridge.getArgs());
    framing::FieldTable settings;
    // FIXME aconway 2011-11-28: string constants.
    settings.setInt(ReplicatingSubscription::QPID_REPLICATING_SUBSCRIPTION, 1);
    // FIXME aconway 2011-11-28: inconsistent use of _ vs. -
    settings.setInt(ReplicatingSubscription::QPID_HIGH_SEQUENCE_NUMBER, queue->getPosition());
    qpid::framing::SequenceNumber oldest;
    if (queue->getOldest(oldest))
        settings.setInt(ReplicatingSubscription::QPID_LOW_SEQUENCE_NUMBER, oldest);

    peer.getMessage().subscribe(args.i_src, args.i_dest, args.i_sync ? 0 : 1, 0, false, "", 0, settings);
    peer.getMessage().flow(getName(), 0, 0xFFFFFFFF);
    peer.getMessage().flow(getName(), 1, 0xFFFFFFFF);
    QPID_LOG(debug, "Activated route from queue " << args.i_src << " to " << args.i_dest);
}


namespace {
const std::string DEQUEUE_EVENT("dequeue-event");
const std::string REPLICATOR("qpid.replicator-");
}

void QueueReplicator::route(Deliverable& msg, const std::string& key, const qpid::framing::FieldTable* /*args*/)
{
    if (key == DEQUEUE_EVENT) {
        std::string content;
        msg.getMessage().getFrames().getContent(content);
        qpid::framing::Buffer buffer(const_cast<char*>(content.c_str()), content.size());
        qpid::framing::SequenceSet latest;
        latest.decode(buffer);

        //TODO: should be able to optimise the following
        for (qpid::framing::SequenceSet::iterator i = latest.begin(); i != latest.end(); i++) {
            if (current < *i) {
                //haven't got that far yet, record the dequeue
                dequeued.add(*i);
                QPID_LOG(debug, "Recording dequeue of message at " << *i << " from " << queue->getName());
            } else {
                QueuedMessage message;
                if (queue->acquireMessageAt(*i, message)) {
                    queue->dequeue(0, message);
                    QPID_LOG(info, "Dequeued message at " << *i << " from " << queue->getName());
                } else {
                    QPID_LOG(error, "Unable to dequeue message at " << *i << " from " << queue->getName());
                }
            }
        }
    } else {
        //take account of any gaps in sequence created by messages
        //dequeued before our subscription reached them
        while (dequeued.contains(++current)) {
            dequeued.remove(current);
            QPID_LOG(debug, "Skipping dequeued message at " << current << " from " << queue->getName());
            queue->setPosition(current);
        }
        QPID_LOG(info, "Enqueued message on " << queue->getName() << "; currently at " << current);
        msg.deliverTo(queue);
    }
}

bool QueueReplicator::bind(boost::shared_ptr<Queue>, const std::string&, const qpid::framing::FieldTable*) { return false; }
bool QueueReplicator::unbind(boost::shared_ptr<Queue>, const std::string&, const qpid::framing::FieldTable*) { return false; }
bool QueueReplicator::isBound(boost::shared_ptr<Queue>, const std::string* const, const qpid::framing::FieldTable* const) { return false; }

// FIXME aconway 2011-11-28: rationalise string constants.
static const std::string TYPE_NAME("qpid.queue-replicator");

std::string QueueReplicator::getType() const { return TYPE_NAME; }

}} // namespace qpid::broker
