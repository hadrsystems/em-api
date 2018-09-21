package edu.mit.ll.em.api.rs.model;

import edu.mit.ll.nics.common.entity.Weather;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WeatherModelTest {

    private Weather weather = new Weather("objectid", "location",
            80.01, 9.22f, 310.0, 38.85f, "OK", 8.018);
    private WeatherModel weatherModel = new WeatherModel(weather);

    @Test
    public void returnsObjectIDFromGivenWeatherInstance() {
        assertEquals(weatherModel.getObjectId(), weather.getObjectId());
    }

    @Test
    public void returnsLocationFromGivenWeatherInstance() {
        assertEquals(weatherModel.getLocation(), weather.getLocation());
    }

    @Test
    public void returnsAirTemperatureFromGivenWeatherInstance() {
        assertEquals(weatherModel.getAirTemperature(), weather.getAirTemperature());
    }

    @Test
    public void returnsWindSpeedFromGivenWeatherInstance() {
        assertEquals(weatherModel.getWindSpeed(), weather.getWindSpeed());
    }

    @Test
    public void returnsDescriptiveWindDirectionFromGivenWeatherInstance() {
        assertEquals(weatherModel.getWindDirection(), weather.getDescriptiveWindDirection());
    }

    @Test
    public void returnsHumidityFromGivenWeatherInstance() {
        assertEquals(weatherModel.getHumidity(), weather.getHumidity());
    }

    @Test
    public void returnsQCStatusFromGivenWeatherInstance() {
        assertEquals(weatherModel.getQCStatus(), weather.getQcStatus());
    }

    @Test
    public void returnsDistanceInMilesFromGivenWeatherInstance() {
        assertEquals(weatherModel.getDistance(), weather.getDistanceInMiles());
    }
}
