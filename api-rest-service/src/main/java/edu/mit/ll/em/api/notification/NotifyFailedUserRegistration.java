/**
 * Copyright (c) 2008-2016, Massachusetts Institute of Technology (MIT)
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

import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.email.JsonEmail;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTimeUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotifyFailedUserRegistration {
    private static final String CNAME = NotifyFailedUserRegistration.class.getName();
    private APILogger log = APILogger.getInstance();
    private Configuration emApiConfiguration;
    private RabbitPubSubProducer rabbitProducer;

    public NotifyFailedUserRegistration(Configuration emApiConfiguration) throws IOException {
        this.emApiConfiguration = emApiConfiguration;
    }

    public NotifyFailedUserRegistration(Configuration emApiConfiguration, RabbitPubSubProducer rabbitProducer) {
        this.emApiConfiguration = emApiConfiguration;
        this.rabbitProducer = rabbitProducer;
    }

    public void notify(User user, Org org) {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String toEmails = this.getSysAdminsEmail();

            if(!StringUtils.isEmpty(toEmails)) {
                JsonEmail email = new JsonEmail(this.getFromEmail(), toEmails,
                        "Alert from RegisterAccount@" + hostname);
                email.setBody(getEmailBody(user, org, hostname));
                this.getRabbitProducer().produce(this.getAlertTopic(), email.toJsonObject().toString());
            }
        } catch (Exception e) {
            log.e(CNAME, "Unable to send failed user registration email notification : ", e);
        }
    }

    private String getEmailBody(User user, Org org, String hostname) throws UnknownHostException {
        StringBuilder builder = new StringBuilder();
        String date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(DateTimeUtils.currentTimeMillis());
        builder.append(date);
        builder.append("\n\nA new user has attempted to register. However, their system user failed to successfully persist,");
        builder.append(" so before they can try to register again with the same email address, their");
        builder.append(" Identity user will need deleted in OpenAM.");
        builder.append("\n\nName: " + user.getFirstname() + " " + user.getLastname());
        builder.append("\nOrganization: " + org.getName());
        builder.append("\nEmail: " + user.getUsername());
        return builder.toString();
    }

    private String getFromEmail() {
        return this.emApiConfiguration.getString(APIConfig.NEW_USER_ALERT_EMAIL);
    }

    private String getAlertTopic() {
        return this.emApiConfiguration.getString(APIConfig.EMAIL_ALERT_TOPIC, "iweb.nics.email.alert");
    }

    private String getSysAdminsEmail() {
        return this.emApiConfiguration.getString(APIConfig.SYSTEM_ADMIN_ALERT_EMAILS, "");
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
}
