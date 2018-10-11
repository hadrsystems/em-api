package edu.mit.ll.em.api.rs.model.mapper;

import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.ROCData;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.Weather;
import org.apache.commons.lang.StringUtils;

public class ROCDataModelMapper {

    public ROCData convertToROCData(Incident incident, Jurisdiction jurisdiction, Location location, Weather weather, String latestReportType, String incidentCause, String generalLocation) {
        Integer incidentId = null;
        String incidentName = null;
        Double longitude = null;
        Double latitude = null;
        String incidentTypes = null;
        String incidentDescription = null;
        String specificLocation = null;
        String county = null;
        String state = null;
        String sra = null;
        String dpa = null;
        String jurisdictionEntity = null;
        Double temperature = null;
        Float relHumidity = null;
        Float windSpeed = null;
        String windDirection = null;
        if(incident != null) {
            incidentId = incident.getIncidentid() ;
            incidentName = incident.getIncidentname();
            longitude = incident.getLon();
            latitude = incident.getLat();
            incidentTypes = StringUtils.join(incident.getIncidentIncidenttypes(), ',');
            incidentDescription = incident.getDescription();
        }
        if(location != null) {
            specificLocation = location.getSpecificLocation();
            county = location.getCounty();
            state = location.getState();
        }
        if(jurisdiction != null) {
            sra = jurisdiction.getSRA();
            dpa = jurisdiction.getDPA();
            jurisdictionEntity = jurisdiction.getJurisdiction();
        }
        if(weather != null) {
            temperature = weather.getAirTemperature();
            relHumidity = weather.getHumidity();
            windSpeed = weather.getWindSpeed();
            windDirection = weather.getDescriptiveWindDirection();
        }
        ROCData rocFormData = new ROCData(incidentId, incidentName, longitude, latitude, incidentTypes,
        incidentDescription, incidentCause, latestReportType,
                specificLocation, generalLocation, county, state,
                sra, dpa, jurisdictionEntity,
                temperature, relHumidity, windSpeed, windDirection
         );
        return rocFormData;
    }

    public ROCData convertToROCData(Jurisdiction jurisdiction, Location location, Weather weather) {
        return convertToROCData(null, jurisdiction, location, weather, null, null, null);
    }
}