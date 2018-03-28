package edu.mit.ll.em.api.rs.impl;

import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.ActiveSessionResponse;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.nicsdao.WorkspaceDAO;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;

import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class UserServiceImplTest {

    private int DEFAULT_WORKSPACE_ID = 1;

    private UserDAOImpl userDAO = mock(UserDAOImpl.class);
    private UserSessionDAOImpl userSessionDAO = mock(UserSessionDAOImpl.class);
    private WorkspaceDAO  workspaceDAO = mock(WorkspaceDAO.class);

    private UserServiceImpl userService = null;

    @BeforeClass
    public static void setupClass() throws Exception {
        DataSource mockDataSource = mock(DataSource.class);
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("java:comp/env/jboss/sadisplayDatasource", mockDataSource);
        builder.activate();
    }

    @Before
    public void setup() throws Exception {
        userService = new UserServiceImpl(userDAO, null, userSessionDAO, null, workspaceDAO, null, null, null);
    }

    @Test
    public void verifyActiveSessionSendsBadRequestResponseInCaseOfInvalidUserSessionId() {
        int userSessionId = -1;

        Response response = userService.verifyActiveSession(1, userSessionId, "test.user@hello.com");

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        APIResponse apiResponse  = (APIResponse) response.getEntity();
        assertEquals("Please provide valid userSessionId", apiResponse.getMessage());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), apiResponse.getStatus());
    }

    @Test
    public void verifyActiveSessionSendsForbiddenResponseInCaseOfMissingRequestingUser() {
        int userSessionId = 1;
        String requestingUser = null;

        Response response = userService.verifyActiveSession(1, userSessionId, requestingUser);

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
        APIResponse apiResponse  = (APIResponse) response.getEntity();
        assertEquals("Not authorized for this request", apiResponse.getMessage());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), apiResponse.getStatus());
    }

    @Test
    public void verifyActiveSessionSendsForbiddenResponseInCaseOfInvalidRequestingUser() {
        int userSessionId = 1;
        String requestingUser = "test.user@test.com";
        when(userDAO.getUser(requestingUser)).thenReturn(null);

        Response response = userService.verifyActiveSession(1, userSessionId, requestingUser);

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
        APIResponse apiResponse  = (APIResponse) response.getEntity();
        assertEquals("Not authorized for this request", apiResponse.getMessage());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), apiResponse.getStatus());
    }

    @Test
    public void verifyActiveSessionDefaultsWorkspaceIdInCaseOfInvalidWorkspaceId() {
        int workspaceId = 21;
        int userSessionId = 1;
        int userId = 11;
        String requestingUser = "test.user@test.com";
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
        CurrentUserSession currentUserSession = mock(CurrentUserSession.class);
        when(currentUserSession.getUsersessionid()).thenReturn(1);
        when(userDAO.getUser(requestingUser)).thenReturn(user);
        when(workspaceDAO.getWorkspaceName(workspaceId)).thenReturn(null);
        when(userSessionDAO.getCurrentUserSession(DEFAULT_WORKSPACE_ID, userId)).thenReturn(currentUserSession);

        Response response = userService.verifyActiveSession(workspaceId, userSessionId, requestingUser);
        verify(userSessionDAO).getCurrentUserSession(DEFAULT_WORKSPACE_ID, userId);

        Mockito.reset(userSessionDAO);

        workspaceId = -1;
        when(userSessionDAO.getCurrentUserSession(DEFAULT_WORKSPACE_ID, userId)).thenReturn(currentUserSession);
        response = userService.verifyActiveSession(workspaceId, userSessionId, requestingUser);
        verify(userSessionDAO).getCurrentUserSession(DEFAULT_WORKSPACE_ID, userId);
    }

    @Test
    public void verifyActiveSessionSendsValidResponseGivenActiveUserSessionId() {
        int workspaceId = 2;
        int userSessionId = 1;
        int userId = 11;
        String requestingUser = "test.user@test.com";
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
        CurrentUserSession currentUserSession = mock(CurrentUserSession.class);
        ActiveSessionResponse expectedActiveSessionResponse = new ActiveSessionResponse(200, "ok", true);
        when(currentUserSession.getUsersessionid()).thenReturn(userSessionId);
        when(userDAO.getUser(requestingUser)).thenReturn(user);
        when(workspaceDAO.getWorkspaceName(workspaceId)).thenReturn("WorkspaceName");
        when(userSessionDAO.getCurrentUserSession(workspaceId, userId)).thenReturn(currentUserSession);

        Response response = userService.verifyActiveSession(workspaceId, userSessionId, requestingUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ActiveSessionResponse activeSessionResponse  = (ActiveSessionResponse) response.getEntity();
        assertEquals(expectedActiveSessionResponse, activeSessionResponse);
    }

    @Test
    public void verifyActiveSessionSendsValidResponseGivenInactiveUserSessionId() {
        int workspaceId = 2;
        int userSessionId = 1;
        int userId = 11;
        String requestingUser = "test.user@test.com";
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
        CurrentUserSession currentUserSession = mock(CurrentUserSession.class);
        ActiveSessionResponse expectedActiveSessionResponse = new ActiveSessionResponse(200, "ok", false);
        when(currentUserSession.getUsersessionid()).thenReturn(userSessionId + 1);
        when(userDAO.getUser(requestingUser)).thenReturn(user);
        when(workspaceDAO.getWorkspaceName(workspaceId)).thenReturn("WorkspaceName");
        when(userSessionDAO.getCurrentUserSession(workspaceId, userId)).thenReturn(currentUserSession);

        Response response = userService.verifyActiveSession(workspaceId, userSessionId, requestingUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ActiveSessionResponse activeSessionResponse  = (ActiveSessionResponse) response.getEntity();
        assertEquals(expectedActiveSessionResponse, activeSessionResponse);
    }

    @Test
    public void verifyActiveSessionSendsInternalServerErrorResponseWhenAnExceptionIsThrown() {
        int workspaceId = 2;
        int userSessionId = 1;
        int userId = 11;
        String requestingUser = "test.user@test.com";
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);
        CurrentUserSession currentUserSession = mock(CurrentUserSession.class);
        APIResponse expectedApiResponse = new APIResponse(500, "Unable to process your request, please try again later.");
        when(currentUserSession.getUsersessionid()).thenReturn(userSessionId + 1);
        when(userDAO.getUser(requestingUser)).thenReturn(user);
        when(workspaceDAO.getWorkspaceName(workspaceId)).thenThrow(new RuntimeException("Test Exception"));

        Response response = userService.verifyActiveSession(workspaceId, userSessionId, requestingUser);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse  = (APIResponse) response.getEntity();
        assertEquals(expectedApiResponse.getStatus(), apiResponse.getStatus());
        assertEquals(expectedApiResponse.getMessage(), apiResponse.getMessage());
        verify(userSessionDAO, never()).getCurrentUserSession(workspaceId, userId);
    }
}