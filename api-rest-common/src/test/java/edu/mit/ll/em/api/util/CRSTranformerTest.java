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
package edu.mit.ll.em.api.util;

import com.vividsolutions.jts.geom.Coordinate;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CRSTranformerTest {

    private CRSTransformer crsTransformer = new CRSTransformer();

    @Test
    public void returnsInputCoordinateIfSourceAndTargetCRSAreSame() throws Exception {
        Coordinate inputLocation = new Coordinate(-121.45488739013672, 38.574038169691875);
        assertEquals(crsTransformer.transformCoordinatesToTargetCRS(inputLocation.x, inputLocation.y, "EPSG:4326", "EPSG:4326"), inputLocation);
    }

    @Test
    public void returnsTranformedCoordinates() throws Exception {
        Coordinate locationIn3857CRS = new Coordinate(-13520296.2186244, 4660838.56865971);
        Coordinate tranformedLocationIn4326CRS = new Coordinate(-121.4548873901367, 38.57403816969187);
        assertEquals(crsTransformer.transformCoordinatesToTargetCRS(locationIn3857CRS.x, locationIn3857CRS.y, "EPSG:3857", "EPSG:4326"), tranformedLocationIn4326CRS);
    }

    @Test(expected = RuntimeException.class)
    public void failsToTranformGivenCoordinatesInIncorrectCRS() throws Exception {
        Coordinate locationIn3857CRS = new Coordinate(-13520296.2186244, 4660838.56865971);
        crsTransformer.transformCoordinatesToTargetCRS(locationIn3857CRS.x, locationIn3857CRS.y, "EPSG:4326", "EPSG:3857");
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionOnInvalidSourceCRS() throws Exception {
        crsTransformer.transformCoordinatesToTargetCRS(0.0, 0.0, "EPSG:38577", "EPSG:4326");
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionOnInvalidTargetCRS() throws Exception {
        crsTransformer.transformCoordinatesToTargetCRS(0.0, 0.0, "EPSG:3857", "EPSG:43265");
    }
}
