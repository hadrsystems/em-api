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
package edu.mit.ll.em.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.ll.nics.nicsdao.IncidentDAO;
import edu.mit.ll.em.api.notification.NotifyFailedUserRegistration;
import edu.mit.ll.em.api.notification.NotifySuccessfulUserRegistration;
import edu.mit.ll.em.api.openam.OpenAmGatewayFactory;
import edu.mit.ll.em.api.gateway.geocode.GeocodeAPIGateway;
import edu.mit.ll.em.api.service.UserRegistrationService;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.FormDAO;
import edu.mit.ll.nics.nicsdao.JurisdictionDAO;
import edu.mit.ll.nics.nicsdao.WeatherDAO;
import edu.mit.ll.nics.nicsdao.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;


@Configuration
public class SpringConfiguration {
    private DataSource datafeedsDataSource = null;
    private DataSource datalayersDataSource = null;
    private NamedParameterJdbcTemplate datafeedsJdbcTemplate = null;
    private NamedParameterJdbcTemplate datalayersJdbcTemplate = null;

    @Bean
    public org.apache.commons.configuration.Configuration emApiConfiguration() {
        return APIConfig.getInstance().getConfiguration();
    }

    private DataSource dataFeedsDataSource() throws NamingException {
        if(this.datafeedsDataSource == null) {
            InitialContext e = new InitialContext();
            this.datafeedsDataSource = (DataSource) e.lookup("java:comp/env/jboss/datafeedsDatasource");

        }
        return this.datafeedsDataSource;
    }

    private DataSource dataLayersDatasource() throws NamingException {
        if(this.datalayersJdbcTemplate == null) {
            InitialContext e = new InitialContext();
            this.datalayersDataSource = (DataSource) e.lookup("java:comp/env/jboss/datalayersDatasource");
        }
        return this.datalayersDataSource;
    }

    private NamedParameterJdbcTemplate dataFeedsJdbcTemplate() throws NamingException {
        if(this.datafeedsJdbcTemplate == null) {
            this.datafeedsJdbcTemplate = new NamedParameterJdbcTemplate(this.dataFeedsDataSource());
        }
        return this.datafeedsJdbcTemplate;
    }

    private NamedParameterJdbcTemplate dataLayersJdbcTemplate() throws NamingException {
        if(this.datalayersDataSource == null) {
            this.datalayersJdbcTemplate = new NamedParameterJdbcTemplate(this.dataLayersDatasource());
        }
        return this.datalayersJdbcTemplate;
    }

    @Bean
    public IncidentDAO incidentDao() {
        return new IncidentDAOImpl();
    }

    @Bean
    public JurisdictionDAO jurisdictionDao() throws NamingException {
        return new JurisdictionDAOImpl(dataLayersJdbcTemplate());
    }

    @Bean
    public OrgDAOImpl orgDao() {
        return new OrgDAOImpl();
    }

    @Bean
    public UserDAOImpl userDao() {
        return new UserDAOImpl();
    }

    @Bean
    public UserOrgDAOImpl userOrgDao() {
        return new UserOrgDAOImpl();
    }

    @Bean
    public UserSessionDAOImpl userSessionDao() {
        return new UserSessionDAOImpl();
    }

    @Bean
    public FormDAO formDao() {
        return new FormDAOImpl();
    }

    @Bean
    public OpenAmGatewayFactory openAmGatewayFactory() {
        return new OpenAmGatewayFactory();
    }

    @Bean
    public WorkspaceDAOImpl workspaceDao() {
        return new WorkspaceDAOImpl();
    }

    @Bean
    WeatherDAO weatherDao() throws NamingException {
        return new WeatherDAOImpl(this.dataFeedsJdbcTemplate());
    }

    @Bean
    public UserRegistrationService registrationService() throws IOException {
        return new UserRegistrationService(logger(), userDao(), orgDao(), userOrgDao(), workspaceDao(), openAmGatewayFactory(), successfulUserRegistrationNotification(), failedUserRegistrationNotification());
    }

    @Bean
    public NotifySuccessfulUserRegistration successfulUserRegistrationNotification() throws IOException {
        return new NotifySuccessfulUserRegistration(orgDao(), emApiConfiguration());
    }

    @Bean
    public NotifyFailedUserRegistration failedUserRegistrationNotification() throws IOException {
        return new NotifyFailedUserRegistration(emApiConfiguration());
    }

    @Bean
    public APILogger logger() {
        return APILogger.getInstance();
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    RabbitPubSubProducer rabbitProducer() throws IOException {
        return RabbitFactory.makeRabbitPubSubProducer(
                    APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_HOSTNAME_KEY),
                    APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_EXCHANGENAME_KEY),
                    APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERNAME_KEY),
                    APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERPWD_KEY));
    }

    @Bean
    CRSTransformer crsTransformer() {
        return new CRSTransformer();
    }

    @Bean
    Client jerseyClient() { return ClientBuilder.newClient(); }

    @Bean
    ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    GeocodeAPIGateway geocodeAPIGateway() {
        org.apache.commons.configuration.Configuration emApiConfiguration = emApiConfiguration();
        return new GeocodeAPIGateway(jerseyClient(), objectMapper(), emApiConfiguration.getString(APIConfig.GEOCODE_API_URL), emApiConfiguration.getString(APIConfig.GEOCODE_API_KEY));
    }
}
