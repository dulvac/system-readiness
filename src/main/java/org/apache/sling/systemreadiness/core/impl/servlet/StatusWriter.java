/*
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
 */
package org.apache.sling.systemreadiness.core.impl.servlet;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.sling.systemreadiness.core.Status;

public class StatusWriter {

    private XMLStreamWriter writer;

    public StatusWriter(XMLStreamWriter writer) throws XMLStreamException {
        this.writer = writer;
    }
    
    public void write(Map<String, Status> stateMap) throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement("systemstatus");
        for (Entry<String, Status> entry : stateMap.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void write(String name, Status status) throws XMLStreamException {
        writer.writeStartElement("status");
        writer.writeAttribute("check", name);
        writer.writeAttribute("status", status.getState().name());
        writer.writeCharacters(status.getDetails());
        writer.writeEndElement();
    }

    public void close() throws XMLStreamException {
        writer.close();
    }
    
}
