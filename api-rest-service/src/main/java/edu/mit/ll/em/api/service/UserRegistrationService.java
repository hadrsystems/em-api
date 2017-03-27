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
package edu.mit.ll.em.api.service;

import edu.mit.ll.em.api.notification.NotifyFailedUserRegistration;
import edu.mit.ll.em.api.notification.NotifySuccessfulUserRegistration;
import edu.mit.ll.em.api.openam.OpenAmGateway;
import edu.mit.ll.em.api.openam.OpenAmGatewayFactory;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.em.api.util.SaltedHashUtil;
import edu.mit.ll.nics.common.entity.*;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;
import org.forgerock.openam.utils.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class UserRegistrationService {

    private static final String CNAME = UserRegistrationService.class.getName();
    private static final String SUCCESS = "success";
    private static final String FAILURE = "An error occurred while registering your account.";

    private APILogger logger;
    private UserDAOImpl userDao;
    private OrgDAOImpl orgDao;
    private UserOrgDAOImpl userOrgDao;
    private WorkspaceDAOImpl workspaceDao;
    private OpenAmGatewayFactory openAmGatewayFactory;
    private RabbitPubSubProducer rabbitProducer;
    private NotifySuccessfulUserRegistration successfulUserRegistrationNotification;
    private NotifyFailedUserRegistration failedUserRegistrationNotification;

    public UserRegistrationService(APILogger logger, UserDAOImpl userDao, OrgDAOImpl orgDao, UserOrgDAOImpl userOrgDao, WorkspaceDAOImpl workspaceDao, OpenAmGatewayFactory openAmGatewayFactory,
                                   NotifySuccessfulUserRegistration successfulUserRegistrationNotification, NotifyFailedUserRegistration failedUserRegistrationNotification) throws IOException {
        this.logger = logger;
        this.userDao = userDao;
        this.orgDao = orgDao;
        this.userOrgDao = userOrgDao;
        this.workspaceDao = workspaceDao;
        this.openAmGatewayFactory = openAmGatewayFactory;
        this.successfulUserRegistrationNotification = successfulUserRegistrationNotification;
        this.failedUserRegistrationNotification = failedUserRegistrationNotification;
    }

    public UserRegistrationService(APILogger logger, UserDAOImpl userDao, OrgDAOImpl orgDao, UserOrgDAOImpl userOrgDao, WorkspaceDAOImpl workspaceDao, OpenAmGatewayFactory openAmGatewayFactory,
                                   RabbitPubSubProducer rabbitProducer,
                                   NotifySuccessfulUserRegistration successfulUserRegistrationNotification, NotifyFailedUserRegistration failedUserRegistrationNotification) {
        this.logger = logger;
        this.userDao = userDao;
        this.orgDao = orgDao;
        this.userOrgDao = userOrgDao;
        this.workspaceDao = workspaceDao;
        this.openAmGatewayFactory = openAmGatewayFactory;
        this.rabbitProducer = rabbitProducer;
        this.successfulUserRegistrationNotification = successfulUserRegistrationNotification;
        this.failedUserRegistrationNotification = failedUserRegistrationNotification;
    }

    public Response postUser(RegisterUser registerUser) {
        Org primaryOrg;
        User user;
        List<UserOrg> userOrgs = new ArrayList<UserOrg>();
        List<UserOrgWorkspace> userOrgWorkspaces = new ArrayList<UserOrgWorkspace>();
        Map<String, String> errors =  new HashMap<String, String>();
        Response response;
		try{
            if(userDao.getUser(registerUser.getEmail()) != null) {
                errors.put("email", "Email already in use. Please provide valid Email Address.");
            }
            primaryOrg = orgDao.getOrganization(registerUser.getOrganizationId());
            if(primaryOrg == null) { // need to fail, can't get org
                errors.put("organizationId", "Please provide valid Organization Id.");
            }
            if(!errors.isEmpty()) {
                ValidationErrorResponse errorResponseEntity = new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid Registration Data", errors);
                logger.e(CNAME, String.format("Invalid registration data : {} , errors: {}", registerUser, errors));
                return Response.ok().status(Response.Status.BAD_REQUEST).entity(errorResponseEntity).build();
            }

            user = createUser(registerUser);
            UserOrg primaryUserOrg = createUserOrg(primaryOrg.getOrgId(), user.getUserId());
            if(primaryUserOrg == null) {
                logger.e(CNAME, "!!! FAILED to create userOrg for user: " + registerUser.getEmail());
                return this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, FAILURE);
            }

            userOrgs.add(primaryUserOrg);
            userOrgs.addAll(getUserOrgTeams(registerUser, user));
            userOrgWorkspaces = getUserOrgWorkspaceTeams(userOrgs);

            List<Contact> contactSet = createContactsList(registerUser.getEmail(), registerUser.getPhone(), user.getUserId());

            response = this.registerUser(registerUser, user, primaryOrg, userOrgs, userOrgWorkspaces, contactSet);
		}catch(Exception e){
			logger.e(CNAME, String.format("Error registering user : %s, Exception: %s", registerUser, e.getMessage()), e);
            return this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, FAILURE);
		}
        return response;
    }

    /**
     *
     * @param registerUser
     * @param user
     * @param primaryOrg
     * @param userOrgs
     * @param userOrgWorkspaces
     * @param contactSet
     * @return Response with information of successful or failed registration
     * @throws Exception
     */
    private Response registerUser(RegisterUser registerUser, User user, Org primaryOrg, List<UserOrg> userOrgs, List<UserOrgWorkspace> userOrgWorkspaces, List<Contact> contactSet) throws Exception {
        Response response = null;
        OpenAmGateway openAmGateway = openAmGatewayFactory.create();
        String successMessage = "Successfully registered user ";
        JSONObject createdIdentity = openAmGateway.createIdentityUser(user, registerUser);
        if(!createdIdentity.optString("status", "").equals(SUCCESS)) {
            logger.e(CNAME, String.format("Failed to create new user %s in OpenAm", registerUser.getEmail()));
            response = this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, FAILURE);
        } else { //create user in database
            if(userDao.registerUser(user, contactSet, userOrgs, userOrgWorkspaces)) {
                try {
                    successfulUserRegistrationNotification.notify(user, primaryOrg);
                    response = this.buildResponse(Response.Status.OK, successMessage + registerUser.getEmail());
                } catch(Exception e) {
                    logger.e(CNAME, String.format("Unable to notify Org admins of successful registration of user : %s", registerUser.getEmail()), e);
                    response = this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, String.format("User registration completed for %s, but failed to notify Org admins to enable your account. Please contact system administrator to enable your account.", registerUser.getEmail()));
                }
            } else {
                if (!this.deleteIdentityUser(registerUser, user, primaryOrg))
                    response = this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to successfully register user with system, but registration with the identity provider succeeded. Before attempting to register with this\n"
                            + " email address again, a system administrator will need to delete your identity user. An email has been sent on your behalf.");
                else
                    response = this.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, FAILURE);
            }
        }
        return response;
    }

    private boolean deleteIdentityUser(RegisterUser registerUser, User user, Org primaryOrg) throws Exception {
        boolean deleteSuccessful = false;
        OpenAmGateway openAmGateway = openAmGatewayFactory.create();
        JSONObject response = openAmGateway.deleteIdentityUser(registerUser.getEmail());
        if(SUCCESS.equals(response.getString("status"))) {
            logger.i(CNAME, String.format("User registration failed for user with email address %s. Successfully rolled back changes from OpenAm", registerUser.getEmail()));
            deleteSuccessful = true;
        } else {
            logger.e(CNAME, String.format("User Registration failure for user %s, Unable to rollback changes from OpenAm", registerUser.getEmail()));
            failedUserRegistrationNotification.notify(user, primaryOrg);
        }
        return deleteSuccessful;
    }

    private List<UserOrg> getUserOrgTeams(RegisterUser registerUser, User user) throws Exception {
        List<UserOrg> userOrgTeams = new ArrayList<UserOrg>();
        Set<Integer> teams = registerUser.getTeams();
        for (Integer teamOrgId : teams) {
                Org teamOrg = orgDao.getOrganization(teamOrgId);
                if (teamOrg == null) {
                    logger.w(CNAME, "Org does not exist: " + teamOrgId);
                } else {
                    UserOrg userOrgTeam = createUserOrg(teamOrg.getOrgId(), user.getUserId());
                    if (userOrgTeam != null)
                        userOrgTeams.add(userOrgTeam);
                }
        }
        return userOrgTeams;
    }

    private List<UserOrgWorkspace> getUserOrgWorkspaceTeams(List<UserOrg> userOrgTeams) throws Exception {
        List<UserOrgWorkspace> userOrgWorkspacesTeams = new ArrayList<UserOrgWorkspace>();
        List<Integer> workspaceIds = workspaceDao.getWorkspaceIds();
        if(CollectionUtils.isEmpty(workspaceIds))
            throw new Exception("No workspaces found during user registration.");
        for(UserOrg userOrgTeam : userOrgTeams) {
            userOrgWorkspacesTeams.addAll(createUserOrgWorkspaceEntities(userOrgTeam, workspaceIds));
        }
        return userOrgWorkspacesTeams;
    }


    private UserOrg createUserOrg(int orgId, int userId) throws Exception {
        UserOrg userorg = null;
        int userOrgId = userOrgDao.getNextUserOrgId();//UserOrgDAO.getInstance().getNextUserOrgId();
        if(userOrgId != -1) {
            userorg = new UserOrg();
            userorg.setUserorgid(userOrgId);
            userorg.setOrgid(orgId);
            userorg.setCreated(Calendar.getInstance().getTime());
            userorg.setSystemroleid(SADisplayConstants.USER_ROLE_ID);
            userorg.setUserid(userId);
        } else {
            logger.e(CNAME, "Unable to get valid userOrgId from DB");
        }
        return userorg;
    }

    private Collection<UserOrgWorkspace> createUserOrgWorkspaceEntities(UserOrg userOrg, List<Integer> workspaceIds) {
        List<UserOrgWorkspace> workspaces = new ArrayList<UserOrgWorkspace>();
        for(Integer workspaceId : workspaceIds){
            UserOrgWorkspace workspace = new UserOrgWorkspace(userOrg.getUserorgid(), workspaceId);
            workspace.setEnabled(false);
            workspace.setDefaultorg(false);
            workspaces.add(workspace);
        }
        return workspaces;
    }

    private User createUser(RegisterUser rUser) throws Exception{
        User user = new User();

        Date currentTime = DateTime.now().toDate();
        int userId = userDao.getNextUserId();
        if(userId != -1) {
            user.setFirstname(rUser.getFirstName());
            user.setLastname(rUser.getLastName());
            user.setUsername(rUser.getEmail());
            user.setUserId(userId);
            user.setPasswordHash(SaltedHashUtil.generateSaltedHash(rUser.getPassword(), rUser.getEmail()));
            user.setEnabled(false);
            user.setActive(true);
            user.setLastupdated(currentTime);
            user.setCreated(currentTime);
            user.setPasswordchanged(currentTime);
        } else {
            throw new RuntimeException("Failed User registration. Unable to fetch a new UserId from DB.");
        }

        return user;
    }

    private List<Contact> createContactsList(String email, String phone, int userId) throws Exception {
        List<Contact> contacts = new ArrayList<Contact>();
        if(StringUtils.isNotBlank(email))
            contacts.add(this.createContact(SADisplayConstants.EMAIL_TYPE, email, userId));
        if(StringUtils.isNotBlank(phone))
            contacts.add(this.createContact(SADisplayConstants.PHONE_OFFICE_TYPE, phone, userId));
        return contacts;
    }

    private Contact createContact(String type, String value, int userId) throws Exception {
        Contact contact = null;
        int contactTypeId = userDao.getContactTypeId(type);
        if(contactTypeId != -1) {
            contact = new Contact();
            contact.setContacttypeid(contactTypeId);
            contact.setContacttype(new ContactType(contactTypeId, type));
            contact.setValue(value);
            contact.setCreated(Calendar.getInstance().getTime());
            contact.setUserid(userId);
            contact.setEnabled(true);
        } else {
            throw new Exception(String.format("Unable to get contact id for contact type : %s", type));
        }
        return contact;
    }

    private Response buildResponse(Response.Status status, String message){
        return Response.ok().status(status).entity(new APIResponse(status.getStatusCode(), message)).build();
    }
}
