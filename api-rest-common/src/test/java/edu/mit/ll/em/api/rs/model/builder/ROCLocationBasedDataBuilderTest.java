package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.*;

import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.Weather;

import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ROCLocationBasedDataBuilderTest {
    private String sra = "sra";
    private DirectProtectionArea directProtectionArea = new DirectProtectionArea("dpa", "contract county", "unitid", "respondid");
    private Jurisdiction jurisdiction = new Jurisdiction("sra", directProtectionArea);

    private Incident incident = new Incident(1, "incidentname", -121.987987, 35.09809, new Date(), new Date(), true, "/root/incident/folder");
    private Weather weather = new Weather("objectId", "-123, 098", 78.9, 10.0f, 214.0, 2.3f, "OK", 10.0);
    private Location location = new Location("county", "state", "000 exact st, xm city, ca, USA, 90000");

    @Test
    public void buildLocationDataBuildsROCLocationBasedDataGivenValidLocationInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildLocationData(location).build();

        assertEquals(location.getSpecificLocation(), rocLocationBasedData.getLocation());
        assertEquals(location.getCounty(), rocLocationBasedData.getCounty());
        assertEquals(location.getState(), rocLocationBasedData.getState());
    }

    @Test
    public void buildLocationDataBuildsROCLocationBasedDataWithEmptyLocationFieldsGivenNullLocationInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildLocationData(null).build();

        assertNull(rocLocationBasedData.getLocation());
        assertNull(rocLocationBasedData.getCounty());
        assertNull(rocLocationBasedData.getState());
    }

    @Test
    public void buildJurisdictionDataBuildsROCLocationBasedDataGivenValidJurisdictionInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildJurisdictionData(jurisdiction).build();

        assertEquals(jurisdiction.getSra(), rocLocationBasedData.getSra());
        assertEquals(jurisdiction.getDpa(), rocLocationBasedData.getDpa());
        assertEquals(jurisdiction.getJurisdiction(), rocLocationBasedData.getJurisdiction());
    }

    @Test
    public void buildJurisdictionDataBuildsROCLocationBasedDataWithEmptyJurisdictionFieldsGivenNullJurisdictionInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildJurisdictionData(null).build();

        assertNull(rocLocationBasedData.getSra());
        assertNull(rocLocationBasedData.getDpa());
        assertNull(rocLocationBasedData.getJurisdiction());
    }

    @Test
    public void buildWeatherDataBuildsROCLocationBasedDataGivenValidWeatherInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildWeatherData(weather).build();

        assertEquals(weather.getAirTemperature(), rocLocationBasedData.getTemperature());
        assertEquals(weather.getHumidity(), rocLocationBasedData.getRelHumidity());
        assertEquals(weather.getWindSpeed(), rocLocationBasedData.getWindSpeed());
        assertEquals(weather.getDescriptiveWindDirection(), rocLocationBasedData.getWindDirection());
    }

    @Test
    public void buildWeatherDataBuildsROCLocationBasedDataWithEmptyWeatherFieldsGivenNullWeatherInstance() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder.buildWeatherData(null).build();

        assertNull(rocLocationBasedData.getTemperature());
        assertNull(rocLocationBasedData.getRelHumidity());
        assertNull(rocLocationBasedData.getWindSpeed());
        assertNull(rocLocationBasedData.getWindDirection());
    }

    @Test
    public void returnsValidROCLocationBasedDataGivenValidLocationJurisdictionAndWeatherInstances() {
        ROCLocationBasedDataBuilder rocLocationBasedDataBuilder = new ROCLocationBasedDataBuilder();
        ROCLocationBasedData rocLocationBasedData = rocLocationBasedDataBuilder
                .buildLocationData(location)
                .buildJurisdictionData(jurisdiction)
                .buildWeatherData(weather)
                .build();

        assertEquals(location.getSpecificLocation(), rocLocationBasedData.getLocation());
        assertEquals(location.getCounty(), rocLocationBasedData.getCounty());
        assertEquals(location.getState(), rocLocationBasedData.getState());

        assertEquals(jurisdiction.getSra(), rocLocationBasedData.getSra());
        assertEquals(jurisdiction.getDpa(), rocLocationBasedData.getDpa());
        assertEquals(jurisdiction.getJurisdiction(), rocLocationBasedData.getJurisdiction());

        assertEquals(weather.getAirTemperature(), rocLocationBasedData.getTemperature());
        assertEquals(weather.getHumidity(), rocLocationBasedData.getRelHumidity());
        assertEquals(weather.getWindSpeed(), rocLocationBasedData.getWindSpeed());
        assertEquals(weather.getDescriptiveWindDirection(), rocLocationBasedData.getWindDirection());
    }
}
