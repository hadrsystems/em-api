package edu.mit.ll.em.api.rs;

import edu.mit.ll.em.api.rs.model.WeatherModel;

public class WeatherResponse extends APIResponse {

    private WeatherModel weatherData = null;

    public WeatherResponse(int status, String message, WeatherModel weatherData) {
        super(status, message);
        this.weatherData = weatherData;
    }

    public WeatherModel getWeatherData() {
        return this.weatherData;
    }
}
