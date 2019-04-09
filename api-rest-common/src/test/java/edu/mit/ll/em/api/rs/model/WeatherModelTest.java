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
        assertEquals(weatherModel.getTemperature(), weather.getAirTemperature());
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
        assertEquals(weatherModel.getRelHumidity(), weather.getHumidity());
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
