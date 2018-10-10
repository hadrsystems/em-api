package edu.mit.ll.em.api.gateway.geocode;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.exception.GeocodeException;
import edu.mit.ll.em.api.rs.model.Location;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeocodeAPIGatewayTest {
    private String geocodeApiKey = null;
    private String geocodeAPIUrl = "https://maps.googleapis.com/maps/api/geocode/json?";
    private Client jerseyClient = mock(Client.class);
    private ObjectMapper objectMapper = mock(ObjectMapper.class);
    private GeocodeAPIGateway geocodeAPIGateway = new GeocodeAPIGateway(jerseyClient, objectMapper, geocodeAPIUrl, geocodeApiKey);

    private WebTarget urlWebTarget = mock(WebTarget.class);
    private WebTarget urlWebTargetWithProperty = mock(WebTarget.class);
    private Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
    private Coordinate coordiantesIn4326 = new Coordinate(-121.45488739013672, 38.574038169691875);
    private Response response = mock(Response.class);
    private String successfulResponseStr = "{\n" +
            "   \"results\" : [\n" +
            "      {\n" +
            "         \"address_components\" : [\n" +
            "            {\n" +
            "               \"long_name\" : \"1600\",\n" +
            "               \"short_name\" : \"1600\",\n" +
            "               \"types\" : [ \"street_number\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"Amphitheatre Pkwy\",\n" +
            "               \"short_name\" : \"Amphitheatre Pkwy\",\n" +
            "               \"types\" : [ \"route\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"Mountain View\",\n" +
            "               \"short_name\" : \"Mountain View\",\n" +
            "               \"types\" : [ \"locality\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"Santa Clara County\",\n" +
            "               \"short_name\" : \"Santa Clara County\",\n" +
            "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"California\",\n" +
            "               \"short_name\" : \"CA\",\n" +
            "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"United States\",\n" +
            "               \"short_name\" : \"US\",\n" +
            "               \"types\" : [ \"country\", \"political\" ]\n" +
            "            },\n" +
            "            {\n" +
            "               \"long_name\" : \"94043\",\n" +
            "               \"short_name\" : \"94043\",\n" +
            "               \"types\" : [ \"postal_code\" ]\n" +
            "            }\n" +
            "         ],\n" +
            "         \"formatted_address\" : \"1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : 37.4224764,\n" +
            "               \"lng\" : -122.0842499\n" +
            "            },\n" +
            "            \"location_type\" : \"ROOFTOP\",\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : 37.4238253802915,\n" +
            "                  \"lng\" : -122.0829009197085\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : 37.4211274197085,\n" +
            "                  \"lng\" : -122.0855988802915\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"place_id\" : \"ChIJ2eUgeAK6j4ARbn5u_wAGqWA\",\n" +
            "         \"types\" : [ \"street_address\" ]\n" +
            "      }\n" +
            "   ],\n" +
            "   \"status\" : \"OK\"\n" +
            "}";
    private String overQueryLimitResponseStr = "{\n" +
            "   \"error_message\" : \"You have exceeded your daily request quota for this API. If you did not set a custom daily request quota, verify your project has an active billing account: http://g.co/dev/maps-no-account\",\n" +
            "   \"results\" : [],\n" +
            "   \"status\" : \"OVER_QUERY_LIMIT\"\n" +
            "}";

    @Before
    public void setup() {
        when(jerseyClient.target(geocodeAPIUrl)).thenReturn(urlWebTarget);
        when(urlWebTarget.queryParam("latlng", String.format("%.10f,%.10f", coordiantesIn4326.y, coordiantesIn4326.x))).thenReturn(urlWebTargetWithProperty);
        when(urlWebTargetWithProperty.queryParam("key", geocodeApiKey)).thenReturn(urlWebTargetWithProperty);
        when(urlWebTargetWithProperty.request("json")).thenReturn(invocationBuilder);
        when(invocationBuilder.get()).thenReturn(response);
    }

    @Test
    public void verifyGetLocationByGeocodeReturnsLocationSuccessfullyGivenValidLatitudeLongitude() throws IOException {
        Location location = new Location("34 alsk st, abc city, CA, USA", "Sacramento", "CA");
        when(response.readEntity(String.class)).thenReturn(successfulResponseStr);
        when(objectMapper.readValue(successfulResponseStr, Location.class)).thenReturn(location);
        Location locationReturned = geocodeAPIGateway.getLocationByGeocode(coordiantesIn4326);
        assertEquals(locationReturned, location);
    }

    @Test(expected = GeocodeException.class)
    public void verifyGetLocationByGeocodeThrowsExceptionGivenInvalidLatitudeLongitude() throws IOException {
        when(response.readEntity(String.class)).thenReturn(overQueryLimitResponseStr);
        when(objectMapper.readValue(overQueryLimitResponseStr, Location.class)).thenThrow(new GeocodeException("OVER_QUERY_LIMIT", "Over query limit, try after x min"));
        geocodeAPIGateway.getLocationByGeocode(coordiantesIn4326);
    }
}
