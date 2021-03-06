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
#include "NullSaslClient.h"
#include "qpid/sys/SecurityLayer.h"
#include "Exception.h"

namespace qpid {
namespace {
const std::string ANONYMOUS("ANONYMOUS");
}

bool NullSaslClient::start(const std::string& mechanisms, std::string&,
           const qpid::sys::SecuritySettings*)
{
    if (mechanisms.find(ANONYMOUS) == std::string::npos) {
        throw qpid::Exception("No suitable mechanism!");
    }
    return false;
}
std::string NullSaslClient::step(const std::string&)
{
    return std::string();
}
std::string NullSaslClient::getMechanism()
{
    return ANONYMOUS;
}
std::string NullSaslClient::getUserId()
{
    return ANONYMOUS;
}
std::auto_ptr<qpid::sys::SecurityLayer> NullSaslClient::getSecurityLayer(uint16_t)
{
    return std::auto_ptr<qpid::sys::SecurityLayer>();
}
} // namespace qpid
