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

import org.json.JSONObject;
import edu.mit.ll.em.api.notification.NotifyFailedUserRegistration;
import edu.mit.ll.em.api.notification.NotifySuccessfulUserRegistration;
import edu.mit.ll.em.api.openam.OpenAmGateway;
import edu.mit.ll.em.api.openam.OpenAmGatewayFactory;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

public class UserRegistrationServiceTest {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "An error occurred while registering your account.";
    private static final String SUCCESSFUL_REGISTRATION = "Successfully registered user %s";
    private static final String CNAME = UserRegistrationService.class.getName();

    private UserRegistrationService service;
    private APILogger logger = mock(APILogger.class);
    private UserDAOImpl userDao = mock(UserDAOImpl.class);
    private OrgDAOImpl orgDao = mock(OrgDAOImpl.class);
    private UserOrgDAOImpl userOrgDao = mock(UserOrgDAOImpl.class);
    private WorkspaceDAOImpl workspaceDao = mock(WorkspaceDAOImpl.class);
    private OpenAmGatewayFactory openAmGatewayFactory = mock(OpenAmGatewayFactory.class);
    private OpenAmGateway openAmGateway = mock(OpenAmGateway.class);
    private RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private NotifySuccessfulUserRegistration successfulUserRegistrationNotification = mock(NotifySuccessfulUserRegistration.class);
    private NotifyFailedUserRegistration failedUserRegistrationNotification = mock(NotifyFailedUserRegistration.class);
    private RegisterUser registerUser = new RegisterUser(1, 8, "first", "last", "first@last.com", "phone", "password", Arrays.asList(2, 6));
    private Org primaryOrg, teamOrg2, teamOrg6;
    private UserOrg primaryUserOrg;
    private int userId = 2;
    private List<Integer> workspaceIds = Arrays.asList(1, 2);
    private int emailContactTypeId = 2;
    private int officePhoneContactTypeId = 2;
    private JSONObject successJson;
    private JSONObject failureJson;

    @Before
    public void setup() throws Exception {
        service = new UserRegistrationService(logger, userDao, orgDao, userOrgDao, workspaceDao, openAmGatewayFactory, rabbitProducer, successfulUserRegistrationNotification, failedUserRegistrationNotification);
        when(openAmGatewayFactory.create()).thenReturn(openAmGateway);
        primaryOrg = new Org(1, "name", "prefix", 0.0, 0.0, DateTime.now().toDate());
        teamOrg6 = new Org(2, "name2", "prefix2", 0.0, 0.0, DateTime.now().toDate());
        teamOrg6 = new Org(6, "name6", "prefix6", 0.0, 0.0, DateTime.now().toDate());
        primaryUserOrg = new UserOrg();
        primaryUserOrg.setUserorgid(12);
        primaryUserOrg.setOrgid(registerUser.getOrganizationId());
        primaryUserOrg.setCreated(Calendar.getInstance().getTime());
        primaryUserOrg.setSystemroleid(SADisplayConstants.USER_ROLE_ID);
        primaryUserOrg.setUserid(userId);
        successJson = new JSONObject();
        successJson.put("status", SUCCESS);
        successJson.put("message", "ok");
        failureJson = new JSONObject();
        failureJson.put("status", "fail");
        failureJson.put("message", "Failure to create user in openam");
    }

    @Test
    public void userRegistrationFailsInCaseOfExistingUserName() {
        Map<String, String> errors = new HashMap<String, String>();
        errors.put("email", "Email already in use. Please provide valid Email Address.");

        when(userDao.getUser(registerUser.getEmail())).thenReturn(mock(User.class));
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(mock(Org.class));
        Response response = service.postUser(registerUser);
        verify(logger).e(eq(CNAME), eq(String.format("Invalid registration data : {} , errors: {}", registerUser, errors)));
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ValidationErrorResponse errorResponseEntity = (ValidationErrorResponse) response.getEntity();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), errorResponseEntity.getStatus());
        assertEquals("Invalid Registration Data", errorResponseEntity.getMessage());
        assertEquals(errors, errorResponseEntity.getValidationErrors());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsInCaseOfInvalidOrganizationId() {
        Map<String, String> errors = new HashMap<String, String>();
        errors.put("organizationId", "Please provide valid Organization Id.");

        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(null);
        Response response = service.postUser(registerUser);
        verify(logger).e(eq(CNAME), eq(String.format("Invalid registration data : {} , errors: {}", registerUser, errors)));
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ValidationErrorResponse errorResponseEntity = (ValidationErrorResponse) response.getEntity();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), errorResponseEntity.getStatus());
        assertEquals("Invalid Registration Data", errorResponseEntity.getMessage());
        assertEquals(errors, errorResponseEntity.getValidationErrors());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsWhenUnableToCreateUserInstance() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(userDao.getNextUserId()).thenReturn(-1);
        Response response = service.postUser(registerUser);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsWhenUnableToCreatePrimaryUserOrgInstance() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(userDao.getNextUserId()).thenReturn(userId);
        when(userOrgDao.getNextUserOrgId()).thenReturn(-1);
        Response response = service.postUser(registerUser);
        verify(logger).e(CNAME, "Unable to get valid userOrgId from DB");
        verify(logger).e(CNAME, "!!! FAILED to create userOrg for user: " + registerUser.getEmail());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsWhenNoWorkspacesAreFoundInDB() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(null);
        Response response = service.postUser(registerUser);
        verify(logger).e(eq(CNAME), eq("Error registering user : " + registerUser + ", Exception: No workspaces found during user registration."), any(Throwable.class));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsWhenCreateContactInstanceFails() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(-1);
        Response response = service.postUser(registerUser);
        verify(logger).e(eq(CNAME), eq(String.format("Error registering user : %s, Exception: %s", registerUser, String.format("Unable to get contact id for contact type : %s", SADisplayConstants.EMAIL_TYPE))), any(Throwable.class));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());

        verify( openAmGateway, never()).createIdentityUser(any(User.class), eq(registerUser));
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsWhenFailsToAddUserInOpenAm() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(emailContactTypeId);
        when(userDao.getContactTypeId(SADisplayConstants.PHONE_OFFICE_TYPE)).thenReturn(officePhoneContactTypeId);
        when( openAmGateway.createIdentityUser(any(User.class), eq(registerUser)) ).thenReturn(failureJson);
        Response response = service.postUser(registerUser);

        verify(logger).e(CNAME, String.format("Failed to create new user %s in OpenAm", registerUser.getEmail()));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
        verify(userDao, never()).registerUser(any(User.class), any(List.class), any(List.class), any(List.class));
    }

    @Test
    public void userRegistrationFailsInDBAndRollsBackChangesFromOpenAm() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(emailContactTypeId);
        when(userDao.getContactTypeId(SADisplayConstants.PHONE_OFFICE_TYPE)).thenReturn(officePhoneContactTypeId);
        when( openAmGateway.createIdentityUser(any(User.class), eq(registerUser)) ).thenReturn(successJson);
        when(openAmGateway.deleteIdentityUser(registerUser.getEmail())).thenReturn(successJson);
        when(userDao.registerUser(any(User.class), any(List.class), any(List.class), any(List.class))).thenReturn(false);
        Response response = service.postUser(registerUser);

        verify(logger).i(CNAME, String.format("User registration failed for user with email address %s. Successfully rolled back changes from OpenAm", registerUser.getEmail()));
        //verify(successfulUserRegistrationNotification).notify(any(User.class), any(Org.class));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(FAILURE, (String) apiResponse.getMessage());
    }

    @Test
    public void userRegistrationFailsInDBAndRollBackFromOpenAmFailureResultsInEmailNotification() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(emailContactTypeId);
        when(userDao.getContactTypeId(SADisplayConstants.PHONE_OFFICE_TYPE)).thenReturn(officePhoneContactTypeId);
        when( openAmGateway.createIdentityUser(any(User.class), eq(registerUser)) ).thenReturn(successJson);
        when(openAmGateway.deleteIdentityUser(registerUser.getEmail())).thenReturn(failureJson);
        when(userDao.registerUser(any(User.class), any(List.class), any(List.class), any(List.class))).thenReturn(false);

        Response response = service.postUser(registerUser);

        verify(failedUserRegistrationNotification).notify(any(User.class), any(Org.class));
        verify(logger).e(CNAME, String.format("User Registration failure for user %s, Unable to rollback changes from OpenAm", registerUser.getEmail()));
        //verify(successfulUserRegistrationNotification).notify(any(User.class), any(Org.class));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals("Failed to successfully register user with system, but registration with the identity provider succeeded. Before attempting to register with this\n"
                + " email address again, a system administrator will need to delete your identity user. An email has been sent on your behalf.", apiResponse.getMessage());
    }

    @Test
    public void userRegistrationSuccessfullyCompletes() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(emailContactTypeId);
        when(userDao.getContactTypeId(SADisplayConstants.PHONE_OFFICE_TYPE)).thenReturn(officePhoneContactTypeId);
        when( openAmGateway.createIdentityUser(any(User.class), eq(registerUser)) ).thenReturn(successJson);

        when(userDao.registerUser(any(User.class), any(List.class), any(List.class), any(List.class))).thenReturn(true);

        Response response = service.postUser(registerUser);
        verify(successfulUserRegistrationNotification).notify(any(User.class), any(Org.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(String.format(SUCCESSFUL_REGISTRATION, registerUser.getEmail()), ((APIResponse) response.getEntity()).getMessage());

        //delete openAm user is called only in case of user registration failures
        verify(openAmGateway, never()).deleteIdentityUser(registerUser.getEmail());
    }

    @Test
    public void userRegistrationCompletesButFailsToNotifyOrganizationAdmins() {
        when(userDao.getUser(registerUser.getEmail())).thenReturn(null);
        when(orgDao.getOrganization(registerUser.getOrganizationId())).thenReturn(primaryOrg);
        when(orgDao.getOrganization(2)).thenReturn(teamOrg2);
        when(orgDao.getOrganization(6)).thenReturn(teamOrg6);
        when(userOrgDao.getNextUserOrgId()).thenReturn(primaryUserOrg.getUserorgid());
        when(userDao.getNextUserId()).thenReturn(userId);
        when(workspaceDao.getWorkspaceIds()).thenReturn(workspaceIds);
        when(userDao.getContactTypeId(SADisplayConstants.EMAIL_TYPE)).thenReturn(emailContactTypeId);
        when(userDao.getContactTypeId(SADisplayConstants.PHONE_OFFICE_TYPE)).thenReturn(officePhoneContactTypeId);
        when( openAmGateway.createIdentityUser(any(User.class), eq(registerUser)) ).thenReturn(successJson);
        doThrow(new RuntimeException("Test exception")).when(successfulUserRegistrationNotification).notify(any(User.class), any(Org.class));

        when(userDao.registerUser(any(User.class), any(List.class), any(List.class), any(List.class))).thenReturn(true);
        Response response = service.postUser(registerUser);
        verify(logger).e(eq(CNAME), eq(String.format("Unable to notify Org admins of successful registration of user : %s", registerUser.getEmail())), any(Exception.class));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(String.format("User registration completed for %s, but failed to notify Org admins to enable your account. Please contact system administrator to enable your account.", registerUser.getEmail()), ((APIResponse) response.getEntity()).getMessage());
    }
}
