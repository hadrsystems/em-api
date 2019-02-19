package edu.mit.ll.em.api.json.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.ll.em.api.rs.model.ROCMessage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ROCMessageDeserializerTest {

    private SimpleDateFormat dateCreatedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat startDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private String dateCreatedStr = "2018-09-10 10:32:16";
    private String reportType = "FINAL";
    private String rocDisplayName = "NAME";
    private String incidentCause = "CAUSE";
    private String incidentType = "Planned event";
    private String startDateStr = "2018-09-10T17:10:49.881Z";
    private String county = "sacramento";
    private String state = "state";
    private String location = "88 hello dr, Folsom, ca";
    private String generalLocation = "20 miles south of x";
    private String sra = "SRA";
    private String dpa = "DPA";
    private String jurisdiction = "contract county";
    private Double temperature = 92.0;
    private Float relHumidity = 10.0f;
    private Float windSpeed = 8.0f;
    private Double windDirection = 180.0;
    private Date dateCreated, startDate;


    private String rocJson = "{\"message\":{\"datecreated\":\"" + dateCreatedStr + "\"" +
            ",\"report\":{\"reportType\":\"" + reportType + "\"" +
            ",\"date\":\"" + startDateStr + "\",\"starttime\":\"" + startDateStr + "\"" +
            ",\"formTypeId\":1,\"reportBy\":\"sneha nagole\",\"email\":\"sneha.nagole@tabordasolutions.com\"" +
            ",\"rocDisplayName\":\"" + rocDisplayName + "\",\"county\":\"" + county + "\"" +
            ",\"state\":\"" + state + "\"" +
            ",\"location\":\"" + location + "\"" +
            ",\"generalLocation\":\"" + generalLocation + "\"" +
            ",\"sra\":\"" + sra + "\"" +
            ",\"dpa\":\"" + dpa + "\"" +
            ",\"jurisdiction\":\"" + jurisdiction + "\"" +
            ",\"incidentType\":\"" + incidentType + "\"" +
            ",\"incidentCause\":\"" + incidentCause + "\"" +
            ",\"scope\":\"2\",\"spreadRate\":\"2\",\"percentContained\":\"1\"" +
            ",\"temperature\":\"" + temperature + "\"" +
            ",\"relHumidity\":\"" + relHumidity + "\"" +
            ",\"windSpeed\":\"" + windSpeed + "\"" +
            ",\"windDirection\":\"" + windDirection + "\",\"predictedWeather\":\"mild\",\"evacuations\":\"0\",\"structuresThreat\":\"0\",\"infrastructuresThreat\":\"0\",\"comments\":\"lkjljklj - Final 1\",\"simplifiedEmail\":true,\"airAttack\":\"none\"" +
            "}}}\"";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws ParseException {
        dateCreated = dateCreatedFormat.parse(dateCreatedStr);
        startDate = startDateFormat.parse(startDateStr);
    }

    @Test
    public void deserializesValidROCMessageJsonSuccessfull() throws IOException {
        ROCMessage rocMessage = objectMapper.readValue(rocJson, ROCMessage.class);
        assertEquals(rocMessage.getDateCreated(), dateCreated);
        assertEquals(rocMessage.getReportType(), reportType);
        assertEquals(rocMessage.getRocDisplayName(), rocDisplayName);
        assertEquals(rocMessage.getIncidentCause(), incidentCause);
        assertTrue(rocMessage.getIncidentTypes().isEmpty());
        assertEquals(rocMessage.getDate(), startDate);
        assertEquals(rocMessage.getStartTime(), startDate);
        assertEquals(rocMessage.getLocation(), location);
        assertEquals(rocMessage.getCounty(), county);
        assertEquals(rocMessage.getState(), state);
        assertEquals(rocMessage.getGeneralLocation(), generalLocation);
        assertEquals(rocMessage.getSra(), sra);
        assertEquals(rocMessage.getDpa(), dpa);
        assertEquals(rocMessage.getJurisdiction(), jurisdiction);

        assertEquals(rocMessage.getTemperature(), temperature);
        assertEquals(rocMessage.getRelHumidity(), relHumidity);
        assertEquals(rocMessage.getWindSpeed(), windSpeed);
        assertEquals(rocMessage.getWindDirection(), windDirection);
    }

    @Test
    public void deserializesROCMessageWithEmptyReportSection() throws IOException
    {
        String rocJsonWithEmptyReportSection  = "{\"message\":{\"datecreated\":\"" + dateCreatedStr + "\"" +
                                        ",\"report\":{}}}\"";
        ROCMessage rocMessage = objectMapper.readValue(rocJsonWithEmptyReportSection, ROCMessage.class);
        assertEquals(rocMessage.getDateCreated(), dateCreated);
        assertNull(rocMessage.getReportType());
        assertNull(rocMessage.getRocDisplayName());
        assertNull(rocMessage.getIncidentCause());
        assertTrue(rocMessage.getIncidentTypes().isEmpty());
        assertNull(rocMessage.getDate());
        assertNull(rocMessage.getStartTime());
        assertNull(rocMessage.getLocation());
        assertNull(rocMessage.getCounty());
        assertNull(rocMessage.getState());
        assertNull(rocMessage.getGeneralLocation());
        assertNull(rocMessage.getSra());
        assertNull(rocMessage.getDpa());
        assertNull(rocMessage.getJurisdiction());

        assertNull(rocMessage.getTemperature());
        assertNull(rocMessage.getRelHumidity());
        assertNull(rocMessage.getWindSpeed());
        assertNull(rocMessage.getWindDirection());
    }

    @Test
    public void deserializerReturnsROCMessageWithBlankReportFields() throws IOException {
        String rocJson = "{\"message\":{\"datecreated\":\"" + dateCreatedStr + "\"}}";
        ROCMessage rocMessage = objectMapper.readValue(rocJson, ROCMessage.class);
        assertEquals(rocMessage.getDateCreated(), dateCreated);
    }
}
