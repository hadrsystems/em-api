package edu.mit.ll.em.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.model.DirectProtectionArea;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;
import org.forgerock.openam.utils.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JurisdictionLocatorService {
    private GeoServer geoServer;
    private ObjectMapper mapper = new ObjectMapper();
    public static final String GEOSERVER_SRA_LAYER = "scout:frap_responsibility_areas";
    public static final String GEOSERVER_DPA_LAYER = "scout:dpa_internal";

    public JurisdictionLocatorService(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    public String getStateResponsibilityArea(Coordinate coordinate, String crs) throws Exception {
        List<String> propertiesList = Arrays.asList("name");
        String responseJson = geoServer.getFeatureDetails(GEOSERVER_SRA_LAYER, coordinate, crs, propertiesList, "wkb_geometry");
        JsonNode node = getPropertiesNode(responseJson);
        return node != null ? node.get("name").asText() : null;
    }

    private JsonNode getPropertiesNode(String responseJson) throws IOException {
        if(StringUtils.isBlank(responseJson))
            return null;
        JsonNode node = mapper.readTree(responseJson);
        if (node.get("totalFeatures").asInt() > 0) {
            return node.findValue("properties");
        } else {
            return null;
        }
    }

    public DirectProtectionArea getDirectProtectionArea(Coordinate coordinate, String crs) throws Exception {
        List<String> propertiesList = Arrays.asList("dpa_group", "agreements");
        String responseJson = geoServer.getFeatureDetails(GEOSERVER_DPA_LAYER, coordinate, crs, propertiesList, "geometry");
        JsonNode node = getPropertiesNode(responseJson);
        DirectProtectionArea directProtectionArea = null;
        if(node != null) {
            directProtectionArea = new DirectProtectionArea(node.get("dpa_group").asText(), node.get("agreements").asText());
        }
        return directProtectionArea;
    }

    public Jurisdiction getJurisdiction(Coordinate coordinate, String crs) throws Exception {
        String sra = this.getStateResponsibilityArea(coordinate, crs);
        DirectProtectionArea directProtectionArea = this.getDirectProtectionArea(coordinate, crs);
        Jurisdiction jurisdiction = new Jurisdiction(sra, directProtectionArea, null);
        return jurisdiction;
    }
}
