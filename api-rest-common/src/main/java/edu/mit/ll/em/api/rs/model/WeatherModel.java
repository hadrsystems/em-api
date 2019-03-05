package edu.mit.ll.em.api.rs.model;

import edu.mit.ll.nics.common.entity.Weather;
import org.hibernate.internal.util.compare.EqualsHelper;

public class WeatherModel {

    private Weather weather;

    public WeatherModel(Weather weather) {
        this.weather = weather;
    }

    public String getObjectId() {
        return this.weather.getObjectId();
    }

    public String getLocation() {
        return this.weather.getLocation();
    }

    public Double getTemperature() {
        return this.weather.getAirTemperature();
    }

    public Float getWindSpeed() {
        return this.weather.getWindSpeed();
    }

    public String getWindDirection() {
        return this.weather.getDescriptiveWindDirection();
    }

    public Float getRelHumidity() {
        return this.weather.getHumidity();
    }

    public String getQCStatus() {
        return this.weather.getQcStatus();
    }

    public Double getDistance() {
        return this.weather.getDistanceInMiles();
    }
}
