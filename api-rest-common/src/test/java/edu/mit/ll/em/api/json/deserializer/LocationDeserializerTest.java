package edu.mit.ll.em.api.json.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.ll.em.api.exception.GeocodeException;
import edu.mit.ll.em.api.rs.model.Location;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class LocationDeserializerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void deserializerReturnsLocationSuccessfully() throws IOException {
        String json = "{\n" +
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
        String expectedFormattedAddress = "1600 Amphitheatre Parkway, Mountain View, CA 94043, USA";
        String county = "Santa Clara";
        String state = "California";
        Location location = objectMapper.readValue(json, Location.class);
        assertEquals(location.getSpecificLocation(), expectedFormattedAddress);
        assertEquals(location.getCounty(), county);
        assertEquals(location.getState(), state);
    }

    @Test
    public void deserializerThrowsExceptionWhenStatusIsNotOK() throws IOException {
        String errorJson = "{\n" +
                "   \"error_message\" : \"You have exceeded your daily request quota for this API. If you did not set a custom daily request quota, verify your project has an active billing account: http://g.co/dev/maps-no-account\",\n" +
                "   \"results\" : [],\n" +
                "   \"status\" : \"OVER_QUERY_LIMIT\"\n" +
                "}";
        try {
            objectMapper.readValue(errorJson, Location.class);
        } catch(GeocodeException e) {
            assertEquals(e.getResponseCode(), "OVER_QUERY_LIMIT");
            assertEquals(e.getErrorMessage(), "You have exceeded your daily request quota for this API. If you did not set a custom daily request quota, verify your project has an active billing account: http://g.co/dev/maps-no-account");
        }
    }
}
