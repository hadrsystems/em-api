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
import edu.mit.ll.nics.common.email.JsonEmail;
import edu.mit.ll.nics.common.email.exception.JsonEmailException;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Matchers.eq;

public class NotifyFailedUserRegistrationTest {
    private NotifyFailedUserRegistration notifyFailedUserRegistration;
    private Configuration emApiConfiguration = mock(Configuration.class);
    private RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private User user = mock(User.class);
    private Org org = mock(Org.class);
    private static final int orgId = 1;
    private static final String fromEmailAddress = "from@happytest.com";
    private static final String adminEmailAddress = "testadmin@happy.com";
    private static final String alertTopic = "alertTopic";
    private static String hostname;
    private static final String userFirstName = "first";
    private static final String userLastName = "last";
    private static final String userEmailAddress = "useremail@happy.com";
    private static final String orgName = "orgName";

    @Before
    public void setup() throws UnknownHostException, IOException {
        when(emApiConfiguration.getString(APIConfig.EMAIL_ALERT_TOPIC, "iweb.nics.email.alert")).thenReturn(alertTopic);
        when(emApiConfiguration.getString(APIConfig.NEW_USER_ALERT_EMAIL)).thenReturn(fromEmailAddress);
        when(emApiConfiguration.getString(APIConfig.RABBIT_HOSTNAME_KEY)).thenReturn("rabbitHost");
        when(emApiConfiguration.getString(APIConfig.RABBIT_EXCHANGENAME_KEY)).thenReturn("rabbitExchangeKey");
        when(emApiConfiguration.getString(APIConfig.RABBIT_USERNAME_KEY)).thenReturn("rabbitUsernameKey");
        when(emApiConfiguration.getString(APIConfig.RABBIT_USERPWD_KEY)).thenReturn("rabbitUserPwdKey");
        when(user.getFirstname()).thenReturn(userFirstName);
        when(user.getLastname()).thenReturn(userLastName);
        when(user.getUsername()).thenReturn(userEmailAddress);
        when(org.getName()).thenReturn(orgName);
        hostname = InetAddress.getLocalHost().getHostName();
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());
        notifyFailedUserRegistration = new NotifyFailedUserRegistration(emApiConfiguration, rabbitProducer);
    }

    @Test
    public void notificationSuccessfullySent() throws IOException, JsonEmailException {
        when(emApiConfiguration.getString(APIConfig.SYSTEM_ADMIN_ALERT_EMAILS, "")).thenReturn(adminEmailAddress);
        notifyFailedUserRegistration.notify(user, org);
        verify(rabbitProducer).produce(alertTopic, this.getJsonEmail().toJsonObject().toString());
    }

    @Test
    public void notificationFailsWhenSystemAdminEmailAddressNotAvailable() throws IOException {
        notifyFailedUserRegistration.notify(user, org);
        verify(rabbitProducer, never()).produce(eq(alertTopic), anyString());
    }

    private JsonEmail getJsonEmail() throws IOException {
        JsonEmail email = new JsonEmail(fromEmailAddress, adminEmailAddress,
                "Alert from RegisterAccount@" + hostname);
        email.setBody(getEmailBody(user, org, hostname));
        return email;
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
}
