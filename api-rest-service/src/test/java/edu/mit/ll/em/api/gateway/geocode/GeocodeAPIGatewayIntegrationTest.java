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
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

public class GeocodeAPIGatewayIntegrationTest {
    private Client jerseyClient = ClientBuilder.newClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private String apiKey = "AIzaSyB7Qz4Ws2GgDaThGJ5uhj3GUHNC8itaCRY";
    private String geocodeAPIUrl = "https://maps.googleapis.com/maps/api/geocode/json";
    private GeocodeAPIGateway locationService = new GeocodeAPIGateway(jerseyClient, objectMapper, geocodeAPIUrl, apiKey);

    @Test
    public void verifyGetLocationByCoordinatesReturnsLocationSuccessfullyGivenValidLatitudeLongitude() throws IOException {
        Coordinate coordinate = new Coordinate(-121.45488739013672, 38.574038169691875);
        Location location = locationService.getLocationByGeocode(coordinate);
        assertEquals(location.getSpecificLocation(), "617 37th St, Sacramento, CA 95816, USA");
        assertEquals(location.getState(), "California");
        assertEquals(location.getCounty(), "Sacramento");
    }

    @Test(expected = GeocodeException.class)
    public void verifyGetLocationByCoordinatesThrowsExceptionGivenInvalidLatitudeLongitude() throws IOException {
        Coordinate coordinate = new Coordinate(0.0, 0.0);
        locationService.getLocationByGeocode(coordinate);
    }
}
