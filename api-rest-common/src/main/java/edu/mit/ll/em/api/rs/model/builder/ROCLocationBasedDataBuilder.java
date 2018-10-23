package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.ROCLocationBasedData;
import edu.mit.ll.nics.common.entity.Weather;

public class ROCLocationBasedDataBuilder {
    private ROCLocationBasedData rocLocationBasedData = new ROCLocationBasedData();

    public ROCLocationBasedDataBuilder buildLocationData(Location location) {
        if(location != null) {
            rocLocationBasedData.setLocation(location.getSpecificLocation());
            rocLocationBasedData.setCounty(location.getCounty());
            rocLocationBasedData.setState(location.getState());
        }
        return this;
    }

    public ROCLocationBasedDataBuilder buildJurisdictionData(Jurisdiction jurisdiction) {
        if(jurisdiction != null) {
            rocLocationBasedData.setSra(jurisdiction.getSra());
            rocLocationBasedData.setDpa(jurisdiction.getDpa());
            rocLocationBasedData.setJurisdiction(jurisdiction.getJurisdiction());
        }
        return this;
    }

    public ROCLocationBasedDataBuilder buildWeatherData(Weather weather) {
        if(weather != null) {
            rocLocationBasedData.setTemperature(weather.getAirTemperature());
            rocLocationBasedData.setRelHumidity(weather.getHumidity());
            rocLocationBasedData.setWindSpeed(weather.getWindSpeed());
            rocLocationBasedData.setWindDirection(weather.getDescriptiveWindDirection());
        }
        return this;
    }

    public ROCLocationBasedData build() {
        return this.rocLocationBasedData;
    }
}