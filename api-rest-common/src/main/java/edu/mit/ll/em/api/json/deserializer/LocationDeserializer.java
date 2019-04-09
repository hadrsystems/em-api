/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.em.api.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.mit.ll.em.api.exception.GeocodeException;
import edu.mit.ll.em.api.rs.model.Location;

import java.io.IOException;
import java.util.Iterator;

public class LocationDeserializer extends StdDeserializer<Location> {
    public LocationDeserializer() {
        this(null);
    }

    public LocationDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Location deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        String status = jsonNode.get("status").asText();
        String errorMessage = jsonNode.get("error_message") == null ? null : jsonNode.get("error_message").asText();
        if(!"OK".equals(status)) {
            throw new GeocodeException(status, errorMessage);
        }
        JsonNode firstResults = jsonNode.get("results").get(0);
        String formattedAddress = firstResults.get("formatted_address").asText();
        Iterator<JsonNode> addressComponentsIterator = firstResults.get("address_components").iterator();
        String county = null, state = null;
        while(addressComponentsIterator.hasNext() && (county == null || state == null)) {
            JsonNode addressComponentNode = addressComponentsIterator.next();
            String types = addressComponentNode.get("types").toString();
            if(types.contains("administrative_area_level_2")) {
                county = addressComponentNode.get(("long_name")).asText().replace("County" , "").trim();
            }
            if(types.contains("administrative_area_level_1")) {
                state = addressComponentNode.get(("long_name")).asText();
            }
        }
        return new Location(county, state, formattedAddress);
    }
}
