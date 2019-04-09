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
package edu.mit.ll.em.api.gateway.geocode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.exception.GeocodeException;
import edu.mit.ll.em.api.rs.model.Location;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

public class GeocodeAPIGateway {
    private String geocodeApiKey = null;
    private String geocodeApiUrl = null;
    private Client jerseyClient = null;
    private ObjectMapper objectMapper = null;

    public GeocodeAPIGateway(Client jerseyClient, ObjectMapper objectMapper, String geocodeAPIUrl, String geocodeApiKey) {
        this.jerseyClient = jerseyClient;
        this.objectMapper = objectMapper;
        this.geocodeApiUrl = geocodeAPIUrl;
        this.geocodeApiKey = geocodeApiKey;
    }

    public Location getLocationByGeocode(Coordinate coordiantesIn4326) throws GeocodeException, IOException {
        WebTarget target = this.jerseyClient.target(geocodeApiUrl)
                .queryParam("latlng", String.format("%.20f,%.20f", coordiantesIn4326.y, coordiantesIn4326.x))
                .queryParam("key", geocodeApiKey);
        Invocation.Builder builder = target.request("json");
        String response = builder.get().readEntity(String.class);
        return getLocation(response);
    }

    private Location getLocation(String response) throws IOException {
        return objectMapper.readValue(response, Location.class);
    }
}
