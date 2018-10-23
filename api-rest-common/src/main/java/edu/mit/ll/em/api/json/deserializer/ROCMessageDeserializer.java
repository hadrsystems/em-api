package edu.mit.ll.em.api.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.mit.ll.em.api.rs.model.ROCMessageUnused;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ROCMessageDeserializer extends StdDeserializer<ROCMessage>  {
    private static final SimpleDateFormat dateCreatedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public ROCMessageDeserializer() {
        this(null);
    }

    public ROCMessageDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ROCMessage deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        String dateCreatedStr = jsonNode.findValue("datecreated").asText();
        Date dateCreated = null;
        try {
            dateCreated = StringUtils.isBlank(dateCreatedStr) ? null : dateCreatedFormat.parse(dateCreatedStr);
        } catch(ParseException e) {
            dateCreated = null;
        }
        JsonNode reportNode = jsonNode.findValue("report");
        if(reportNode == null) {
            ROCMessage rocMessage = new ROCMessage();
            rocMessage.setDateCreated(dateCreated);
            return rocMessage;
        }

        String rocDisplayName = reportNode.get("rocDisplayName") == null ? null : reportNode.get("rocDisplayName").asText();
        String reportType = reportNode.get("reportType") == null ? null : reportNode.get("reportType").asText();
        String incidentCause = reportNode.get("incidentCause") == null ? null : reportNode.get("incidentCause").asText();
        String incidentType = reportNode.get("incidentType") == null ? null : reportNode.get("incidentType").asText();
        String dateStr = reportNode.get("date") == null ? null : reportNode.get("date").asText();
        Date date, startTime;
        try {
            date = StringUtils.isBlank(dateStr) ? null : dateFormat.parse(dateStr);
        } catch(ParseException e) {
            date = null;
        }
        String startTimeStr = reportNode.get("starttime") == null ? null : reportNode.get("starttime").asText();
        try {
            startTime = StringUtils.isBlank(startTimeStr) ? null : dateFormat.parse(startTimeStr);
        } catch(ParseException e) {
            startTime = null;
        }
        String location = reportNode.get("location") == null ? null : reportNode.get("location").asText();
        String generalLocation = reportNode.get("generalLocation") == null ? null : reportNode.get("generalLocation").asText();
        String county = reportNode.get("county") == null ? null : reportNode.get("county").asText();
        String state = reportNode.get("state") == null ? null : reportNode.get("state").asText();
        String sra = reportNode.get("sra") == null ? null : reportNode.get("sra").asText();
        String dpa = reportNode.get("dpa") == null ? null : reportNode.get("dpa").asText();
        String jurisdiction = reportNode.get("jurisdiction") == null ? null : reportNode.get("jurisdiction").asText();
        Double temperature = reportNode.get("temperature") == null ? null : reportNode.get("temperature").asDouble();
        Float relHumidity = reportNode.get("relHumidity") == null ? null : Float.parseFloat(reportNode.get("relHumidity").asText());
        Float windSpeed = reportNode.get("windSpeed") == null ? null : Float.parseFloat(reportNode.get("windSpeed").asText());
        String windDirection = reportNode.get("windDirection") == null ? null : reportNode.get("windDirection").asText();
        return new ROCMessage(dateCreated, rocDisplayName, reportType, date, startTime,
                incidentCause, incidentType,
                location, generalLocation, county, state,
                sra, dpa, jurisdiction,
                temperature, relHumidity, windSpeed, windDirection);
    }
}
