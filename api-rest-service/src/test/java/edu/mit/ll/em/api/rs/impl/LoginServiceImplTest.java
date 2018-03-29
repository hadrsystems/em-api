/**
 * Copyright (c) 2008-2017, Massachusetts Institute of Technology (MIT)
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
package edu.mit.ll.em.api.rs.impl;

import edu.mit.ll.em.api.rs.Login;
import edu.mit.ll.em.api.rs.LoginResponse;
import edu.mit.ll.nics.common.entity.*;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.OrgDAO;
import edu.mit.ll.nics.nicsdao.UserDAO;
import edu.mit.ll.nics.nicsdao.UserOrgDAO;
import edu.mit.ll.nics.nicsdao.WorkspaceDAO;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;

import org.forgerock.openam.utils.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LoginServiceImplTest {

    private UserDAO userDao = mock(UserDAO.class);
    private UserSessionDAOImpl userSessionDao = mock(UserSessionDAOImpl.class);
    private UserOrgDAO userOrgDao = mock(UserOrgDAO.class);
    private OrgDAO orgDao = mock(OrgDAO.class);
    private WorkspaceDAO workspaceDao = mock(WorkspaceDAO.class);
    private RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private Login inputLogin;

    private LoginServiceImpl loginServiceImpl;

    private User user;
    private List<Org> orgs;
    private UserOrg userOrg;
    private CurrentUserSession currentUserSession;
    private String userDisplayName;

    @Before
    public void setup() {
        loginServiceImpl = new LoginServiceImpl(userDao, userSessionDao, userOrgDao, orgDao, workspaceDao, rabbitProducer);
        inputLogin = new Login();
        inputLogin.setUsername("Abc");
        inputLogin.setWorkspaceId(1);

        user = new User(2, "username", "passwordHash", null, new Date(), true, new Date(), new Date());
        user.setFirstname("firstname");
        user.setLastname("lastname");

        orgs = CollectionUtils.asList(new Org(1, "orgname", "prefix", 2.00, 1.00, new Date()));
        userOrg = mock(UserOrg.class);//new UserOrg(2, orgs.get(0), new SystemRole(1, "rolename"), user, new Date());
        when(userOrg.getUserorgid()).thenReturn(1);
        when(userOrg.getSystemroleid()).thenReturn(2);
        currentUserSession = new CurrentUserSession(4, null, user, "displayname", new Date(), new Date());
        currentUserSession.setWorkspaceid(inputLogin.getWorkspaceId());
        userDisplayName = user.getFirstname() + " " + user.getLastname();
    }

    @After
    public void tearDown() {
        Mockito.reset(userDao, userSessionDao, userOrgDao, orgDao, workspaceDao, rabbitProducer);
    }

    @Test
    public void loginFailsOnEmptyUsername() {
        Login login = new Login();

        Response response = loginServiceImpl.postLogin(login);

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(loginResponse.getMessage(), "Invalid username: " + login.getUsername() + ", Please provide valid username.");
        verifyZeroInteractions(userDao, orgDao, userOrgDao, userSessionDao, workspaceDao);
    }

    @Test
    public void loginFailsOnBlankUsername() {
        Login login = new Login();
        login.setUsername("   ");

        Response response = loginServiceImpl.postLogin(login);

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(loginResponse.getMessage(), "Invalid username: " + login.getUsername().trim() + ", Please provide valid username.");
        verifyZeroInteractions(userDao, orgDao, userOrgDao, userSessionDao, workspaceDao);
    }

    @Test
    public void loginFailsOnInvalidUsername() {
        when(userDao.getUser(inputLogin.getUsername())).thenReturn(null);
        Response response = loginServiceImpl.postLogin(inputLogin);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

        LoginResponse loginResponse = (LoginResponse) response.getEntity();

        assertEquals(loginResponse.getMessage(), "Invalid username: " + inputLogin.getUsername().toLowerCase() + ", Please provide valid username.");
        verifyZeroInteractions(orgDao, userOrgDao, userSessionDao, workspaceDao);
    }

    @Test
    public void loginPicksDefaultWorkspaceIfAValidWorkspaceIdIsNotGiven() {
        int newCurrentUserSessionId = 2;
        Login loginWithInvalidWorkspaceId = new Login();
        loginWithInvalidWorkspaceId.setUsername("abc");
        loginWithInvalidWorkspaceId.setWorkspaceId(-1);
        Login responseLogin = new Login();
        responseLogin.setUsername(inputLogin.getUsername().toLowerCase());
        responseLogin.setUserId(user.getUserId());
        responseLogin.setUserSessionId(newCurrentUserSessionId);

        int userOrgId = userOrg.getUserorgid();
        int userId = user.getUserId();
        int systemRoleId = userOrg.getSystemroleid();
        int defaultWorkspaceId = 1; //default workspaceId if provided workspaceId in login is not invalid
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(defaultWorkspaceId)).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), defaultWorkspaceId)).thenReturn(orgs);
        when(userOrgDao.getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), defaultWorkspaceId)).thenReturn(userOrg);
        when(userSessionDao.getCurrentUserSession(defaultWorkspaceId)).thenReturn(null);
        when(userSessionDao.create(
                Mockito.any(String.class),
                eq(userOrgId),
                eq(userDisplayName),
                eq(userId),
                eq(systemRoleId),
                eq(defaultWorkspaceId)
        )).thenReturn(newCurrentUserSessionId);

        Response response = loginServiceImpl.postLogin(loginWithInvalidWorkspaceId);

        verifyZeroInteractions(rabbitProducer);
        verify(userSessionDao, never()).removeCurrentUserSession(user.getUserId());

        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), 200);
        assertEquals(loginResponse.getMessage(), "ok");
        assertEquals(loginResponse.getCount(), 1);
        assertTrue(loginResponse.getLogins().contains(responseLogin));
    }

    @Test
    public void loginSucceedsAndDeletesExistingCurrentUserSession() throws Exception {
        int newCurrentUserSessionId = 2;
        Login responseLogin = new Login();
        responseLogin.setUsername(inputLogin.getUsername().toLowerCase());
        responseLogin.setUserId(user.getUserId());
        responseLogin.setUserSessionId(newCurrentUserSessionId);

        int userOrgId = userOrg.getUserorgid();
        int userId = user.getUserId();
        int systemRoleId = userOrg.getSystemroleid();
        int workspaceId = inputLogin.getWorkspaceId();
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(inputLogin.getWorkspaceId())).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(orgs);
        when(userOrgDao.getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(userOrg);
        when(userSessionDao.getCurrentUserSession(workspaceId, user.getUserId())).thenReturn(currentUserSession);
        when(userSessionDao.removeUserSession(currentUserSession.getCurrentusersessionid())).thenReturn(true);
        when(userSessionDao.create(
                Mockito.any(String.class),
                eq(userOrgId),
                eq(userDisplayName),
                eq(userId),
                eq(systemRoleId),
                eq(workspaceId)
        )).thenReturn(newCurrentUserSessionId);

        Response response = loginServiceImpl.postLogin(inputLogin);

        verify(rabbitProducer).produce(String.format("iweb.NICS.%d.logout", inputLogin.getWorkspaceId()), Long.toString(currentUserSession.getCurrentusersessionid()));

        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), 200);
        assertEquals(loginResponse.getMessage(), "ok");
        assertEquals(loginResponse.getCount(), 1);
        assertTrue(loginResponse.getLogins().contains(responseLogin));
    }

    @Test // this behavior should change as new entry can't be created without deleting old current user sessions
    public void loginFailsOnFailingToDeleteExistingUserSession() throws Exception {
        int newCurrentUserSessionId = 2;
        Login responseLogin = new Login();
        responseLogin.setUsername(inputLogin.getUsername().toLowerCase());
        responseLogin.setUserId(user.getUserId());
        responseLogin.setUserSessionId(newCurrentUserSessionId);

        int userOrgId = userOrg.getUserorgid();
        int userId = user.getUserId();
        int systemRoleId = userOrg.getSystemroleid();
        int workspaceId = inputLogin.getWorkspaceId();
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(inputLogin.getWorkspaceId())).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(orgs);
        when(userOrgDao.getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(userOrg);
        when(userSessionDao.getCurrentUserSession(workspaceId, user.getUserId())).thenReturn(currentUserSession);
        when(userSessionDao.removeUserSession(currentUserSession.getCurrentusersessionid())).thenReturn(false);

        Response response = loginServiceImpl.postLogin(inputLogin);

        verify(rabbitProducer, never()).produce(String.format("iweb.NICS.%d.logout", inputLogin.getWorkspaceId()), Long.toString(currentUserSession.getCurrentusersessionid()));
        verify(userSessionDao, never()).create(
                Mockito.any(String.class),
                eq(userOrgId),
                eq(userDisplayName),
                eq(userId),
                eq(systemRoleId),
                eq(workspaceId)
        );
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), 500);
        assertEquals(loginResponse.getMessage(), "We are not able to process your request currently. Please try again later.");
        assertEquals(loginResponse.getCount(), 0);
        assertTrue(loginResponse.getLogins().isEmpty());
    }

    @Test
    public void loginFailsOnFailingToCreateNewCurrentUserSession() {
        int userOrgId = userOrg.getUserorgid();
        int userId = user.getUserId();
        int systemRoleId = userOrg.getSystemroleid();
        int workspaceId = inputLogin.getWorkspaceId();
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(inputLogin.getWorkspaceId())).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(orgs);
        when(userOrgDao.getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(userOrg);
        when(userSessionDao.getCurrentUserSession(workspaceId, inputLogin.getWorkspaceId())).thenReturn(null);
        when(userSessionDao.create(
                Mockito.any(String.class),
                eq(userOrgId),
                eq(userDisplayName),
                eq(userId),
                eq(systemRoleId),
                eq(workspaceId)
        )).thenReturn(-1);

        Response response = loginServiceImpl.postLogin(inputLogin);

        verifyZeroInteractions(rabbitProducer);
        verify(userSessionDao, never()).removeCurrentUserSession(user.getUserId());

        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(loginResponse.getMessage(), "We are not able to process your request currently. Please try again later.");
        assertTrue(loginResponse.getLogins().isEmpty());
    }

    @Test
    public void loginFailsIfNoOrganizationsAreAssociatedWithLoggingInUser() {
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(inputLogin.getWorkspaceId())).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(Collections.EMPTY_LIST);

        Response response = loginServiceImpl.postLogin(inputLogin);

        verifyZeroInteractions(rabbitProducer);
        verify(userSessionDao, never()).removeCurrentUserSession(user.getUserId());
        verify(userOrgDao, never()).getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), inputLogin.getWorkspaceId());
        verify(userSessionDao, never()).getCurrentUserSession(inputLogin.getWorkspaceId(), user.getUserId());
        verify(userSessionDao, never()).create(
                Mockito.any(String.class),
                anyInt(),
                anyString(),
                anyInt(),
                anyInt(),
                anyInt()
        );

        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), Response.Status.PRECONDITION_FAILED.getStatusCode());
        assertEquals(loginResponse.getMessage(), "No user organizations found for user, Failed to login user with username: " + inputLogin.getUsername().toLowerCase());
        assertTrue(loginResponse.getLogins().isEmpty());
    }

    @Test
    public void loginFailsOnExceptionThrown() {

    }

    @Test
    public void userLoginsSuccessfully() {
        int newCurrentUserSessionId = 2;
        Login responseLogin = new Login();
        responseLogin.setUsername(inputLogin.getUsername().toLowerCase());
        responseLogin.setUserId(user.getUserId());
        responseLogin.setUserSessionId(newCurrentUserSessionId);

        int userOrgId = userOrg.getUserorgid();
        int userId = user.getUserId();
        int systemRoleId = userOrg.getSystemroleid();
        int workspaceId = inputLogin.getWorkspaceId();
        when(userDao.getUser(inputLogin.getUsername().toLowerCase())).thenReturn(user);
        when(workspaceDao.getWorkspaceName(inputLogin.getWorkspaceId())).thenReturn("wsName");
        when(orgDao.getUserOrgs(user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(orgs);
        when(userOrgDao.getUserOrgById(orgs.get(0).getOrgId(), user.getUserId(), inputLogin.getWorkspaceId())).thenReturn(userOrg);
        when(userSessionDao.getCurrentUserSession(workspaceId, userId)).thenReturn(null);
        when(userSessionDao.create(
                Mockito.any(String.class),
                eq(userOrgId),
                eq(userDisplayName),
                eq(userId),
                eq(systemRoleId),
                eq(workspaceId)
        )).thenReturn(newCurrentUserSessionId);

        Response response = loginServiceImpl.postLogin(inputLogin);

        verifyZeroInteractions(rabbitProducer);
        verify(userSessionDao, never()).removeCurrentUserSession(user.getUserId());

        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertEquals(response.getStatus(), 200);
        assertEquals(loginResponse.getMessage(), "ok");
        assertEquals(loginResponse.getCount(), 1);
        assertTrue(loginResponse.getLogins().contains(responseLogin));
    }
}
