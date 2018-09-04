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
package edu.mit.ll.em.api.service;

import edu.mit.ll.em.api.openam.OpenAmGateway;
import edu.mit.ll.em.api.openam.OpenAmGatewayFactory;
import edu.mit.ll.em.api.rs.impl.DatalayerServiceImpl;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.DatalayerDAO;
import edu.mit.ll.nics.nicsdao.DocumentDAO;
import edu.mit.ll.nics.nicsdao.FolderDAO;
import edu.mit.ll.nics.nicsdao.impl.*;
import org.apache.commons.configuration.Configuration;
import org.glassfish.jersey.client.JerseyClient;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatalayerServiceImplTest {
    private static Configuration configuration = mock(Configuration.class);
    private static JerseyClient jerseyClient = mock(JerseyClient.class);
    private static DatalayerDAO datalayerDao = mock(DatalayerDAO.class);
    private static FolderDAO folderDao = mock(FolderDAO.class);
    private static DocumentDAO documentDao = mock(DocumentDAO.class);
    private static UserDAOImpl userDao = mock(UserDAOImpl.class);
    private static UserOrgDAOImpl userOrgDao = mock(UserOrgDAOImpl.class);
    private static UserSessionDAOImpl userSessionDao = mock(UserSessionDAOImpl.class);
    private static RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private static DatalayerServiceImpl datalayerService = new DatalayerServiceImpl(configuration, datalayerDao, folderDao, documentDao, userDao, userOrgDao, userSessionDao, rabbitProducer, jerseyClient);
    private static JerseyWebTarget target = mock(JerseyWebTarget.class);
    private static JerseyInvocation.Builder builder = mock(JerseyInvocation.Builder.class);
    private String internalUrl = "https://apps.intterragroup.com/arcgis/rest/services/NICS/SCCFDAVLResources/MapServer/0";
    private String username = "username";
    private String password = "password";
    private String dataSourceId = "dataSourceId1";
    private List<Map<String, Object>> authData = new ArrayList<Map<String, Object>>();

    @Before
    public void setup() {
        HashMap<String, Object> authMap = new HashMap<String, Object>();
        authMap.put(SADisplayConstants.INTERNAL_URL, internalUrl);
        authMap.put(SADisplayConstants.USER_NAME, username);
        authMap.put(SADisplayConstants.PASSWORD, password);
        authData.add(authMap);

        when(jerseyClient.target( anyString() )).thenReturn(target);
        when(target.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(datalayerDao.getAuthentication(dataSourceId)).thenReturn(authData);
    }

    @Test
    public void getTokenReturnsBadRequestGivenEmptyDataSourceId() {
        Response response = datalayerService.getToken(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("datasourceId is required to request token", response.readEntity(String.class));
    }

    @Test
    public void getTokenReturnsResponseWithErrorDetailsGivenServiceUrlInUnexpectedFormat() {
        String invalidInternalurl = "http://something.not.expected.com";
        authData.get(0).put(SADisplayConstants.INTERNAL_URL, invalidInternalurl);
        Response responseExpected = Response.ok("Unable to construct generateToken request Url from service with internalUrl " + invalidInternalurl).status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        when(datalayerDao.getAuthentication(dataSourceId)).thenReturn(authData);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(responseExpected.getStatus(), response.getStatus());
        assertEquals("Unable to construct generateToken request Url from service with internalUrl : " + invalidInternalurl, response.readEntity(String.class));
    }

    @Test
    public void getTokenReturnsValidResponseWithToken() {
        Response responseExpected = Response.ok("{\"token\":\"xyz\"}").build();
        when(builder.post(any(Entity.class), eq(Response.class))).thenReturn(responseExpected);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(responseExpected.getStatus(), response.getStatus());
        assertEquals(responseExpected.readEntity(String.class), response.readEntity(String.class));
    }

    @Test
    public void getTokenReturnsResponseWithErrorDetailsOnFailingToGetCredentialsFromDB() {
        Response responseExpected = Response.ok("Invalid credentials").status(401).build();
        Exception exception = new RuntimeException("test exception");
        when(datalayerDao.getAuthentication(dataSourceId)).thenThrow(exception);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).contains("Failed to fetch dataSource authentication details with Id : " + dataSourceId));
        assertTrue(response.readEntity(String.class).contains(exception.getMessage()));
    }

    @Test
    public void getTokenReturnsResponseWithErrorDetailsWhenAuthenticationDetailsAreNotFoundInDB() {
        Response responseExpected = Response.ok("Invalid credentials").status(401).build();
        Mockito.reset(datalayerDao);
        when(datalayerDao.getAuthentication(dataSourceId)).thenReturn(null);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Authentication details not found for dataSource with Id : " + dataSourceId, response.getEntity());

        when(datalayerDao.getAuthentication(dataSourceId)).thenReturn(new ArrayList<Map<String, Object>>());
        response = datalayerService.getToken(dataSourceId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Authentication details not found for dataSource with Id : " + dataSourceId, response.getEntity());

        List<Map<String, Object>> authDetailsFromDB = new ArrayList<Map<String,Object>>();
        authDetailsFromDB.add(null);
        when(datalayerDao.getAuthentication(dataSourceId)).thenReturn(authDetailsFromDB);
        response = datalayerService.getToken(dataSourceId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Authentication details not found for dataSource with Id : " + dataSourceId, response.getEntity());
    }

    @Test
    public void getTokenReturnsResponseWithErrorDetailsOnFailingToGetTokenFromTokenService() {
        Response responseExpected = Response.ok("Invalid credentials").status(401).build();
        when(builder.post(any(Entity.class), eq(Response.class))).thenReturn(responseExpected);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(responseExpected.getStatus(), response.getStatus());
        assertEquals(responseExpected.readEntity(String.class), response.readEntity(String.class));
    }

    @Test
    public void getTokenReturnsResponseWithErrorDetailsWhenTokenServiceThrowsException() {
        int index = (internalUrl.indexOf("rest/services") > -1) ? internalUrl.indexOf("rest/services") : internalUrl.indexOf("services");
        String generateTokenUrl = internalUrl.substring(0, index) + "tokens/generateToken";
        Response responseExpected = Response.ok("Invalid credentials").status(401).build();
        Exception exception = new RuntimeException("test exception");
        when(builder.post(any(Entity.class), eq(Response.class))).thenThrow(exception);

        Response response = datalayerService.getToken(dataSourceId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Unable to generate token from service url: " + generateTokenUrl + ", Error: " + exception.getMessage(), response.getEntity());
    }

    @After
    public void tearDown() {
        Mockito.reset(configuration, datalayerDao, folderDao, documentDao, userDao, userOrgDao, userSessionDao, rabbitProducer, jerseyClient, target, builder);
    }
}