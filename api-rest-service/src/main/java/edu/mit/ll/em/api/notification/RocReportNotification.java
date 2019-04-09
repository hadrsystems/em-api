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
package edu.mit.ll.em.api.notification;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import edu.mit.ll.em.api.json.deserializer.ROCMessageDeserializer;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.email.JsonEmail;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.messages.NICSMessage;
import edu.mit.ll.nics.common.messages.parser.SADisplayMessageParser;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.common.entity.Form;

import org.apache.commons.configuration.Configuration;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RocReportNotification {
    private static final String CNAME = RocReportNotification.class.getName();
    private APILogger log = APILogger.getInstance();
    private Configuration emApiConfiguration;
    private RabbitPubSubProducer rabbitProducer;

    public RocReportNotification(Configuration emApiConfiguration, RabbitPubSubProducer rabbitProducer){
        this.emApiConfiguration=emApiConfiguration;
        this.rabbitProducer=rabbitProducer;
    }


    public void notify(Form form) {
        String newRegisteredUserEmailAddresses = emApiConfiguration.getString(APIConfig.NEW_REGISTERED_USER_EMAIL);
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String toEmails = "anish.bokka@tabordasolutions.com";
            JsonEmail email = new JsonEmail(this.getFromEmail(),toEmails, getSubject(form));
            email.setBody(getEmailBody(form));
            log.i("Sinu-Test sending roc email to: " , this.getAlertTopic());
//            log.i();
            log.i("SINU-MESSAGE-EMAIL : ", getEmailBody(form));
            StringBuilder builder = new StringBuilder();
            ObjectMapper mapper = new ObjectMapper();
            String message = mapper.writeValueAsString(form);
            log.i("SINU-FORM : ", message);

            this.rabbitProducer.produce("iweb.nics.email.alert", email.toJsonObject().toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.e(CNAME, "Failed to publish new User email alerts to RabbitMQ topic : ", e);
        }
    }

    private String getEmailBody(Form form) throws IOException{
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE HTML>");
        builder.append("<html><body>");
        builder.append("<p> Intel - for internal use only. Numbers subject to change </p>");
        builder.append("<br/><br/>");
        builder.append("<br/><br/>");
        builder.append("<br/><br/>");
        String date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(new Date());
        builder.append("SINU-DATE: " +date);
        builder.append("<h4>"+getReportHeading(form)+"</h4>");
        builder.append("<ul style=\"list-style-type:disc;\">");
        List<String> conditions = getReportConditions(form);
        for (String condition:conditions){
            builder.append("<li>"+ condition +"<//li>");
        }
        builder.append("</ul>");
        builder.append("<br/><br/>");
        builder.append("<br/><br/>");
        builder.append("<br/><br/>");
        builder.append("This e-mail was sent automatically by the Situation Awareness &amp; Collaboration Tool (SCOUT).Do not reply.");
        builder.append("<br/><br/>");
        builder.append("</html></body>");
        return builder.toString();
    }

    private String getEmailBody1(Form form) throws UnknownHostException, IOException {
        StringBuilder builder = new StringBuilder();
//        ObjectMapper mapper = new ObjectMapper();
//        String message = mapper.writeValueAsString(form);
        builder.append("<html><body>");
        builder.append("Intel - for internal use only. Numbers subject to change");
        builder.append("<br><br>");
        String date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(new Date());
        builder.append("SINU-DATE: " +date+ "\n");
        builder.append("<br/><br/>");
        builder.append("SINU-INCIDENT :" + form.getIncident() + "\n");
        builder.append("SINU-USER-ID :" + form.getUsersession().getSessionid());
        builder.append("SINU-REPORT-TYPE : "  + getROCMessage(form.getMessage()).getReportType()+ "\n");
        builder.append("SINU-COUNTY : "  + getROCMessage(form.getMessage()).getCounty()+ "\n");
        builder.append("SINU-LOCATION IS : "  + getROCMessage(form.getMessage()).getLocation()+ "\n");
        builder.append("SINU-GENERAL-LOCATION : "  + getROCMessage(form.getMessage()).getGeneralLocation()+ "\n");
        builder.append("SINU-DPA : "  + getROCMessage(form.getMessage()).getDpa()+ "\n");
        builder.append("SINU-SRA : "  + getROCMessage(form.getMessage()).getSra()+ "\n");
        builder.append("SINU-JURISDICTION : "  + getROCMessage(form.getMessage()).getJurisdiction()+ "\n");
        builder.append("SINU-SCOPE : "  + getROCMessage(form.getMessage()).getScope()+ "\n");
        builder.append("SINU-SCOPE : "  + getROCMessage(form.getMessage()).getPercentageContained()+ "\n");
        builder.append("SINU-FUEL-TYPES : "  + getROCMessage(form.getMessage()).getFuelTypes()+ "\n");
        builder.append("SINU-REPORT-TYPE1 : "  + getROCMessage(form.getMessage()).getReportType()+ "\n");
        builder.append("SINU-REPORT-TYPE2 : "  + getROCMessage(form.getMessage()).getEvacuations()+ "\n");
        builder.append("SINU-EVAC-LIST1 :" +  getROCMessage(form.getMessage()).getEvacList().get(1)+ "\n");
        builder.append("<br/><br/>");
        builder.append("Please review their registration request and, if approved, enable their account in SCOUT.");
        builder.append("<br><br>");
        builder.append("The user will receive a Welcome email upon activation.");
        builder.append("</body></html>");
        return builder.toString();
    }

    private String getFromEmail() {
        return this.emApiConfiguration.getString(APIConfig.NEW_USER_ALERT_EMAIL);
    }

    private String getAlertTopic() {
        return this.emApiConfiguration.getString(APIConfig.EMAIL_ALERT_TOPIC, "iweb.nics.email.alert");
    }

    private RabbitPubSubProducer getRabbitProducer() throws IOException {
        if(this.rabbitProducer == null) {
            this.rabbitProducer = RabbitFactory.makeRabbitPubSubProducer(
                    emApiConfiguration.getString(APIConfig.RABBIT_HOSTNAME_KEY),
                    emApiConfiguration.getString(APIConfig.RABBIT_EXCHANGENAME_KEY),
                    emApiConfiguration.getString(APIConfig.RABBIT_USERNAME_KEY),
                    emApiConfiguration.getString(APIConfig.RABBIT_USERPWD_KEY));
        }
        return rabbitProducer;
    }
    private ROCMessage getROCMessage(String message) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ROCMessage.class, new ROCMessageDeserializer());
        objectMapper.registerModule(simpleModule);
        System.out.println("SINU SINU FORM MESSAGE:" + message);

        return objectMapper.readValue(message, ROCMessage.class);
    }
    private String getSubject(Form form) throws IOException{
        StringBuilder subject = new StringBuilder();
//        subject.append(form.getUsersession().getSessionid()+ ", ");
        subject.append("Vegetation Fire, ");
        subject.append(getROCMessage(form.getMessage()).getCounty() + ", ");
        subject.append(getROCMessage(form.getMessage()).getReportType());
        return subject.toString();
    }
    private String getReportHeading(Form form) throws IOException{
        StringBuilder heading = new StringBuilder();
        heading.append(getROCMessage(form.getMessage()).getLocation()+ ", ");
        heading.append(getROCMessage(form.getMessage()).getGeneralLocation()+ ", ");
        heading.append(getROCMessage(form.getMessage()).getDpa()+ ", ");
        heading.append(getROCMessage(form.getMessage()).getSra()+ ", ");
        if(getROCMessage(form.getMessage()).getReportType().equals("NEW")){
            heading.append(getROCMessage(form.getMessage()).getSra()+ ", ");
            heading.append(getROCMessage(form.getMessage()).getStartTime());
        }
        heading.append(getROCMessage(form.getMessage()).getSra());
        return heading.toString();
    }
    private List<String> getReportConditions(Form form) throws IOException{
        List<String> bullets = new LinkedList<String>();
        bullets.add(getROCMessage(form.getMessage()).getScope() + " acres, " + getROCMessage(form.getMessage()).getPercentageContained() + "% contained");

        /*
        List<String> resourcesAssigned = getROCMessage(form.getMessage()).getResourcesAssigned();
        for (String resource : resourcesAssigned) {
            bullets.add(resource);
        }*/
        List<String> evacs = getROCMessage(form.getMessage()).getEvacList();
        for (String evac : evacs) {
            bullets.add(evac);
        }
        List<String> structureThreats = getROCMessage(form.getMessage()).getStructuresThreats();
        for (String structureThreat : structureThreats) {
            bullets.add(structureThreat);
        }

        List<String> infstructureThreats = getROCMessage(form.getMessage()).getInfrastructuresThreats();
        for (String infstructureThreat : infstructureThreats) {
            bullets.add(infstructureThreat);
        }

        return bullets;
    }


}