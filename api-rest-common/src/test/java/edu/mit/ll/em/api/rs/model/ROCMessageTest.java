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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import edu.mit.ll.em.api.rs.model.builder.ROCMessageBuilder;
import edu.mit.ll.nics.common.entity.IncidentType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ROCMessageTest {

    private ROCMessage rocMessageFirstFinal;
    private ROCMessage rocMessageSecondFinal;
    private ROCMessage rocMessageFirstUpdate;
    private ROCMessage rocMessageSecondUpdate;
    private ROCMessage rocMessageNullCreateDate;
    private Date rocStartDate = new Date();
    private Date dateCreatedFirstFinal = rocStartDate;
    private Date dateCreatedSecondFinal = new Date(dateCreatedFirstFinal.getTime() + 1000);
    private Date dateCreatedFirstUpdate = rocStartDate;
    private Date dateCreatedSecondUpdate = new Date(dateCreatedFirstUpdate.getTime() + 1000);

    @Before
    public void setup() {
        rocMessageFirstFinal = new ROCMessageBuilder()
                .buildReportDetails("FINAL", null, "generalLocation", Arrays.asList(new String[] {"brass"}), "other fuel type")
                .buildReportDates(dateCreatedFirstFinal, rocStartDate, rocStartDate)
                .build();
        rocMessageSecondFinal = new ROCMessageBuilder()
                .buildReportDetails("FINAL", "county1, county2", "general location1", Arrays.asList(new String[] {"brass"}), "other fuel type1")
                .buildReportDates(dateCreatedSecondFinal, rocStartDate, rocStartDate)
                .build();
        rocMessageFirstUpdate = new ROCMessageBuilder()
                .buildReportDetails("UPDATE", "county1, county2", "general location2", Arrays.asList(new String[] {"brass"}), "other fuel type2")
                .buildReportDates(dateCreatedFirstUpdate, rocStartDate, rocStartDate)
                .build();
        rocMessageSecondUpdate = new ROCMessageBuilder()
                .buildReportDetails("UPDATE", "county1, county2", "general location3", Arrays.asList(new String[] {"brass"}), "other fuel type3")
                .buildReportDates(dateCreatedSecondUpdate, rocStartDate, rocStartDate)
                .build();
        rocMessageNullCreateDate = new ROCMessageBuilder()
                .buildReportDetails("UPDATE", "county1, county2", "general location4", Arrays.asList(new String[] {"brass"}), "other fuel type4")
                .buildReportDates(null, null, null)
                .build();
        rocMessageNullCreateDate.setDateCreated(null);
    }

    @Test
    public void compareToOrdersFinalROCMessageHigher() {
        assertEquals(1, rocMessageFirstFinal.compareTo(rocMessageFirstUpdate));
        assertEquals(-1, rocMessageFirstUpdate.compareTo(rocMessageFirstFinal));
    }

    @Test
    public void compareToOrdersNewestROCMessageHigherIfComparingNonFinalROCMessages() {
        assertEquals(1, rocMessageSecondUpdate.compareTo(rocMessageFirstUpdate));
    }

    @Test
    public void compareToOrdersFinalROCMessagesByCreateDates() {
        assertEquals(1, rocMessageSecondFinal.compareTo(rocMessageFirstFinal));
        assertEquals(-1, rocMessageFirstFinal.compareTo(rocMessageSecondFinal));
    }

    @Test
    public void compareToOrdersNullMessageLast() {
        assertEquals(1, rocMessageFirstUpdate.compareTo(null));
    }

    @Test
    public void compareToOrdersROCMessageWithNullCreateDateLast(){
        assertEquals(1, rocMessageFirstUpdate.compareTo(rocMessageNullCreateDate));
        assertEquals(-1, rocMessageNullCreateDate.compareTo(rocMessageFirstUpdate));
    }

    @Test
    public void compareToReturnsZeroOnComparingROCMessagesWithNullCreateDates() throws Exception {
        ROCMessage rocMessageNullCreateDate2 = (ROCMessage) rocMessageNullCreateDate.clone();
        rocMessageNullCreateDate2.setGeneralLocation("general location2");
        assertEquals(0, rocMessageNullCreateDate2.compareTo(rocMessageNullCreateDate));
        assertEquals(0, rocMessageNullCreateDate.compareTo(rocMessageNullCreateDate2));
    }

    @Test
    public void compareToReturnsEqualIfComparingSameInstances() {
        assertEquals(0, rocMessageFirstFinal.compareTo(rocMessageFirstFinal));
    }

    @Test
    public void compareToOrdersNonFinalROCMessagesByCreateDates() throws Exception {
        assertEquals(1, rocMessageSecondUpdate.compareTo(rocMessageFirstUpdate));
        assertEquals(-1, rocMessageFirstUpdate.compareTo(rocMessageSecondUpdate));

        ROCMessage rocMessageFirstUpdateClone = (ROCMessage) rocMessageFirstUpdate.clone();
        assertEquals(0, rocMessageFirstUpdateClone.compareTo(rocMessageFirstUpdate));
    }
}
