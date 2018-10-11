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
