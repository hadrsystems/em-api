package edu.mit.ll.em.api.service;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.model.DirectProtectionArea;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JurisdictionLocatorServiceTest {

    private GeoServer geoServer = mock(GeoServer.class);
    private JurisdictionLocatorService service = new JurisdictionLocatorService(geoServer);
    private Coordinate coordinate = new Coordinate(-121.45488739013672, 38.574038169691875);
    private String crs4326 = "EPSG:4326";
    private List<String> sraPropertiesList = Arrays.asList("name");
    private String sraGeometry = "wkb_geometry";
    private List<String> dpaPropertiesList =  Arrays.asList("dpa_agency", "agreements");
    private String dpaGeometry = "geometry";
    private String sraResponseJson = "{\"type\":\"FeatureCollection\",\"totalFeatures\":1,\"features\":[{\"type\":\"Feature\",\"id\":\"dpa_internal.fid-341dbb22_165b11ac098_b30\",\"geometry\":null,\"properties\":{\"name\":\"Local\"}}],\"crs\":null}";
    private String dpaResponseJson = "{\"type\":\"FeatureCollection\",\"totalFeatures\":1,\"features\":[{\"type\":\"Feature\",\"id\":\"dpa_internal.fid-341dbb22_165b11ac098_b30\",\"geometry\":null,\"properties\":{\"dpa_group\":\"LOCAL\",\"agreements\":\"contract county\"}}],\"crs\":null}";;
    private String emptyResponseJson = "{\"type\":\"FeatureCollection\",\"totalFeatures\":0,\"features\":[],\"crs\":null}";

    @Test
    public void givenValidLocationGetServiceResponsibilityAreaReturnsResponsibilityArea() throws Exception {
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_SRA_LAYER, coordinate, crs4326, sraPropertiesList, sraGeometry)).thenReturn(sraResponseJson);
        String serviceResponsibilityArea = service.getStateResponsibilityArea(coordinate, crs4326);
        assertEquals(serviceResponsibilityArea, "Local");
    }

    @Test(expected = Exception.class)
    public void getServiceResponsibilityAreaThrowsExceptionWhenGeoServerThrowsException() throws Exception {
        Exception exception = new Exception("GeoServer Test Exception");
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_SRA_LAYER, coordinate, crs4326, sraPropertiesList, sraGeometry)).thenThrow(exception);
        service.getStateResponsibilityArea(coordinate, crs4326);
    }

    @Test
    public void getServiceResponsibilityAreaReturnsEmptyStringWhenGeoServerReturnsZeroMatchingFeatures() throws Exception {
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_SRA_LAYER, coordinate, crs4326, sraPropertiesList, sraGeometry)).thenReturn(emptyResponseJson);
        String stateResponsibilityArea = service.getStateResponsibilityArea(coordinate, crs4326);
        assertNull(stateResponsibilityArea);
    }

    @Test
    public void givenValidLocationGetDirectProtectionAreaReturnsValidRepsonse() throws Exception {
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_DPA_LAYER, coordinate, crs4326, dpaPropertiesList, dpaGeometry)).thenReturn(dpaResponseJson);
        DirectProtectionArea directProtectionArea = service.getDirectProtectionArea(coordinate, crs4326);
        assertEquals(directProtectionArea.getDirectProtectionAreaGroup(), "Local");
        assertEquals(directProtectionArea.isContractCounty(), true);
    }

    @Test
    public void getDirectionAreaReturnsNullWhenNoResultsAreFound() throws Exception {
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_DPA_LAYER, coordinate, crs4326, dpaPropertiesList, dpaGeometry)).thenReturn(emptyResponseJson);
        DirectProtectionArea directProtectionArea = service.getDirectProtectionArea(coordinate, crs4326);
        assertNull(directProtectionArea);
    }

    @Test
    public void getJurisdictionReturnsJurisdictionInstance() throws Exception {
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_SRA_LAYER, coordinate, crs4326, sraPropertiesList, sraGeometry)).thenReturn(sraResponseJson);
        when(geoServer.getFeatureDetails(JurisdictionLocatorService.GEOSERVER_DPA_LAYER, coordinate, crs4326, dpaPropertiesList, dpaGeometry)).thenReturn(dpaResponseJson);
        Jurisdiction jurisdiction = service.getJurisdiction(coordinate, crs4326);
        assertEquals(jurisdiction.getSRA(), "Local");
        assertEquals(jurisdiction.getDPA(), "Local");
        assertTrue(jurisdiction.isContractCounty());
        assertNull(jurisdiction.getJurisdictionEntity());
    }

    @After
    public void tearDown() {
        Mockito.reset(geoServer);
    }
}
