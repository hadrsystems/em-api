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
package edu.mit.ll.em.api.configuration;


import edu.mit.ll.em.api.notification.NotifyFailedUserRegistration;
import edu.mit.ll.em.api.notification.NotifySuccessfulUserRegistration;
import edu.mit.ll.em.api.openam.OpenAmGatewayFactory;
import edu.mit.ll.em.api.service.UserRegistrationService;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;

@Configuration
public class SpringConfiguration {

    @Bean
    public org.apache.commons.configuration.Configuration emApiConfiguration() {
        return APIConfig.getInstance().getConfiguration();
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
    public OpenAmGatewayFactory openAmGatewayFactory() {
        return new OpenAmGatewayFactory();
    }

    @Bean
    public WorkspaceDAOImpl workspaceDao() {
        return new WorkspaceDAOImpl();
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
}
