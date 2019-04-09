/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

        String reportType = reportNode.get("reportType") == null ? null : reportNode.get("reportType").asText();
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
        String additionalAffectedCounties = reportNode.get("additionalAffectedCounties") == null ? null : reportNode.get("additionalAffectedCounties").asText();
        String state = reportNode.get("state") == null ? null : reportNode.get("state").asText();
        String sra = reportNode.get("sra") == null ? null : reportNode.get("sra").asText();
        String dpa = reportNode.get("dpa") == null ? null : reportNode.get("dpa").asText();
        String scope = reportNode.get("scope") == null ? null : reportNode.get("scope").asText();
        String percentageContained = reportNode.get("percentContained") == null ? null : reportNode.get("percentContained").asText();
        String evacuations = reportNode.get("evacuations") == null ? null : reportNode.get("evacuations").asText();

        System.out.println("SINU-SCOPE ::::::::" + scope);
        System.out.println("SINU-PERCENT ::::::::" + percentageContained);
        System.out.println("SINU-EVACUATIONS::::::::" + evacuations);

        JsonNode evacProgress = reportNode.get("evacuationsInProgress") == null ? null : reportNode.get("evacuationsInProgress");
//        System.out.println("SINU SINU SIUN-EVACPROGRESS-JSON ::: " + evacProgress.toString());
        JsonNode evacuationsPorgess = (evacProgress == null) || evacProgress.get("evacuations") == null ? null : evacProgress.get("evacuations");
        List<String> evacList =  evacuationsPorgess==null ? null : getJsonNdeAsList(evacuationsPorgess);
//        System.out.println("SINU SINU SIUN-EVACPROGRESS2 ::: " + evacProgress.get("evacuationsPorgess"));

        String structuresThreat = reportNode.get("evacuations") == null ? null : reportNode.get("structuresThreat").asText();
        JsonNode structuresThreatInProgress = reportNode.get("structuresThreatInProgress") == null ? null : reportNode.get("structuresThreatInProgress");
        JsonNode structuresThreats = (structuresThreatInProgress == null) || structuresThreatInProgress.get("structuresThreat") == null ? null : structuresThreatInProgress.get("structuresThreat");
        List<String> structuresThreatsLst =  structuresThreats==null ? null : getJsonNdeAsList(structuresThreats);

        String infrastructuresThreat = reportNode.get("evacuations") == null ? null : reportNode.get("infrastructuresThreat").asText();
        JsonNode infrastructuresThreatInProgress = reportNode.get("infrastructuresThreatInProgress") == null ? null : reportNode.get("infrastructuresThreatInProgress");
        JsonNode infrastructuresThreats = (infrastructuresThreatInProgress == null) || infrastructuresThreatInProgress.get("infrastructuresThreat") == null ? null : infrastructuresThreatInProgress.get("infrastructuresThreat");
        List<String> infrastructuresThreatsLst =  structuresThreats==null ? null : getJsonNdeAsList(infrastructuresThreats);

        JsonNode resourcesAssignedRoot = reportNode.get("resourcesAssigned") == null ? null : reportNode.get("resourcesAssigned");
        JsonNode resourcesAssignedChild = (resourcesAssignedRoot == null) || resourcesAssignedRoot.get("resourcesAssigned") == null ? null : resourcesAssignedRoot.get("resourcesAssignedRoot");
        List<String> resourcesAssignedLst =  resourcesAssignedChild==null ? null : getJsonNdeAsList(resourcesAssignedChild);

        String jurisdiction = reportNode.get("jurisdiction") == null ? null : reportNode.get("jurisdiction").asText();
        Double temperature = reportNode.get("temperature") == null ? null : reportNode.get("temperature").asDouble();
        JsonNode relHumidityJsonNode = reportNode.get("relHumidity");
        Float relHumidity = (relHumidityJsonNode == null || relHumidityJsonNode.isNull() || StringUtils.isBlank(relHumidityJsonNode.asText()) ) ? null : Float.parseFloat(relHumidityJsonNode.asText());
        JsonNode windSpeedJsonNode = reportNode.get("windSpeed");
        Float windSpeed = (windSpeedJsonNode == null || windSpeedJsonNode.isNull() || StringUtils.isBlank(windSpeedJsonNode.asText())) ? null : Float.parseFloat(windSpeedJsonNode.asText());
        Double windDirection = reportNode.get("windDirection") == null ? null : reportNode.get("windDirection").asDouble();
        JsonNode fuelTypesJSN = reportNode.get("fuelTypes") == null ? null : reportNode.get("fuelTypes");
        System.out.println("fuelTypesJSN.isArray() : " +fuelTypesJSN);
        List<String> fuelTypes = fuelTypesJSN==null ? null : getJsonNdeAsList(fuelTypesJSN);
        System.out.println("SINU-FUEL-TYPES : " + fuelTypes);

        String otherFuelTypes = reportNode.get("otherFuelTypes") == null ? null : reportNode.get("otherFuelTypes").asText();
        return new ROCMessage(dateCreated, reportType, date, startTime,
                location, generalLocation, county, additionalAffectedCounties, state,
                sra, dpa, jurisdiction, temperature, relHumidity, windSpeed, windDirection, percentageContained, scope,
                fuelTypes, otherFuelTypes, evacuations,evacList, structuresThreat,
                structuresThreatsLst, infrastructuresThreat, infrastructuresThreatsLst, resourcesAssignedLst);
    }

    private List<String> getJsonNdeAsList(JsonNode jsonNode){
        ArrayList<String> list = new ArrayList<String>();
        if(jsonNode.isArray()){
            for(JsonNode node : jsonNode){
                list.add(node.asText());
            }
        }else{
            list.add(jsonNode.asText());
        }
        return list;
    }


}
