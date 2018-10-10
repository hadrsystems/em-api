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
    private String geocodeAPIUrl = "https://maps.googleapis.com/maps/api/geocode/json?";
    private Client jerseyClient = null;
    private ObjectMapper objectMapper = null;

    public GeocodeAPIGateway(Client jerseyClient, ObjectMapper objectMapper, String geocodeAPIUrl, String geocodeApiKey) {
        this.jerseyClient = jerseyClient;;
        this.objectMapper = objectMapper;
        this.geocodeAPIUrl = geocodeAPIUrl;
        this.geocodeApiKey = geocodeApiKey;
    }

    public Location getLocationByGeocode(Coordinate coordiantesIn4326) throws GeocodeException, IOException {
        Location location = null;
        String url = String.format(geocodeAPIUrl, geocodeApiKey);
        WebTarget target = this.jerseyClient.target(url)
                .queryParam("latlng", String.format("%.10f,%.10f", coordiantesIn4326.y, coordiantesIn4326.x))
                .queryParam("key", geocodeApiKey);
        Invocation.Builder builder = target.request("json");
        String response = builder.get().readEntity(String.class);
        location = getLocation(response);
        return location;
    }

    private Location getLocation(String response) throws IOException {
        Location location = objectMapper.readValue(response, Location.class);
        return location;
    }
}
