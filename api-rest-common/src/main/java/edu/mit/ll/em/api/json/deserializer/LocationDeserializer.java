package edu.mit.ll.em.api.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
    public Location deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        String status = jsonNode.get("status").asText();
        String errorMessage = jsonNode.get("error_message") == null ? null : jsonNode.get("error_message").asText();
        if(!"OK".equals(status)) {
            throw new GeocodeException(status, errorMessage);
        }
        JsonNode firstResults = ((ArrayNode) jsonNode.get("results")).get(0);
        String formattedAddress = firstResults.get("formatted_address").asText();
        Iterator<JsonNode> addressComponentsIterator = (Iterator<JsonNode>) ((ArrayNode) firstResults.get("address_components")).iterator();
        String county = null, state = null;
        while(addressComponentsIterator.hasNext() && (county == null || state == null)) {
            JsonNode addressComponentNode = addressComponentsIterator.next();
            String types = ((ArrayNode) addressComponentNode.get("types")).toString();
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
