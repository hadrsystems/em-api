package edu.mit.ll.em.api.rs.model;

import java.util.Date;

import edu.mit.ll.em.api.rs.model.builder.ROCMessageBuilder;
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
                .buildReportDetails("name", "FINAL", "cause", "Planned Event", null)
                .buildReportDates(dateCreatedFirstFinal, rocStartDate, rocStartDate)
                .build();
        rocMessageSecondFinal = new ROCMessageBuilder()
                .buildReportDetails("name", "FINAL", "cause", "Planned Event", null)
                .buildReportDates(dateCreatedSecondFinal, rocStartDate, rocStartDate)
                .build();
        rocMessageFirstUpdate = new ROCMessageBuilder()
                .buildReportDetails("name", "UPDATE", "cause", "Planned Event", null)
                .buildReportDates(dateCreatedFirstUpdate, rocStartDate, rocStartDate)
                .build();
        rocMessageSecondUpdate = new ROCMessageBuilder()
                .buildReportDetails("name", "UPDATE", "cause", "Planned Event", null)
                .buildReportDates(dateCreatedSecondUpdate, rocStartDate, rocStartDate)
                .build();
        rocMessageNullCreateDate = new ROCMessageBuilder()
                .buildReportDetails("name", "UPDATE", "cause", "Planned Event", null)
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
        rocMessageNullCreateDate2.setIncidentCause("cause2");
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
