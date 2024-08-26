/*
 * Copyright 2009 Joseph Fifield
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.programmerplanet.sshtunnel.model

import java.io.Serial

/**
 * Exception thrown when an error occurs attempting to connect a session.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class ConnectionException(cause: Throwable?) : Exception(cause) {
    companion object {
        /**
         *
         */
        @Serial
        private const val serialVersionUID = 9062945876082754273L
    }
}
