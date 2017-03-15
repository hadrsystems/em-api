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

import edu.mit.ll.em.api.openam.OpenAmGateway;
import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.email.JsonEmail;
import edu.mit.ll.nics.common.entity.*;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RegistrationService {
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
//    private Response registerUser(RegisterUser registerUser, User user, Org primaryOrg, List<UserOrg> userOrgs, List<UserOrgWorkspace> userOrgWorkspaces, List<Contact> contactSet) throws Exception {
//        OpenAmGateway openAmGateway = getOpenAmGatewayInstance();
//        Response response = null;
//        JSONObject createdIdentity = openAmGateway.createIdentityUser(user, registerUser);
//        if(!createdIdentity.optString("status", "").equals(SUCCESS)) {
//            response = Response.ok("Failed to create identity. " + createdIdentity.optString("message", "unknown"))
//                    .status(Response.Status.PRECONDITION_FAILED).build();
//        } else { //delete user from OpenAm
//            response = this.createUserInDB(registerUser, user, primaryOrg, userOrgs, userOrgWorkspaces, contactSet);
//            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
//                boolean deleteSuccessful = this.deleteIdentityUser(registerUser, user, primaryOrg);
//                if (!deleteSuccessful)
//                    response = Response.ok("Failed to successfully register user with system, but registration with"
//                            + " the identity provider succeeded. Before attempting to register with this"
//                            + " email address again, a system administrator will need to delete your identity user."
//                            + " An email has been sent on your behalf.")
//                            .status(Response.Status.EXPECTATION_FAILED).build();
//            }
//        }
//        return response;
//    }
//
//    private Response createUserInDB(RegisterUser registerUser, User user, Org primaryOrg, List<UserOrg> userOrgs, List<UserOrgWorkspace> userOrgWorkspaces, List<Contact> contactSet) throws Exception {
//        String successMessage = "Successfully registered user";
//
//        String createUserStatus = "";
//        try {
//            boolean registerSuccess = userDao.registerUser(user, contactSet, userOrgs, userOrgWorkspaces);
//            if (registerSuccess) {
//                notifySuccessfulRegistration(registerUser, user, primaryOrg);
//            } else {
//                return Response.ok("Failed to register user with system. Please try again later.").status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            }
//        } catch(Exception e) {
//            System.out.println("Exception persisting user: " + e.getMessage());
//            log.e("UserServiceImpl", "Exception creating user: " + e.getMessage());
//            return Response.ok("Failed to register user: " + e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//
//        return Response.ok(successMessage + registerUser.getEmail()).status(Response.Status.OK).build();
//    }
//
//    /**
//     *
//     * @return OpenAmGateway instance
//     * @throws Exception if ssoTools property path is not set or construction of OpenAmGateway instance fails
//     */
//    private OpenAmGateway getOpenAmGatewayInstance() throws Exception {
//        OpenAmGateway openAmGateway = null;
//        String propPath = APIConfig.getInstance().getConfiguration()
//                .getString("ssoToolsPropertyPath", null);
//
//        log.i("UserServiceImpl:getOpenAmGatewayInstance", "Initializing SSOUtils with property path: " + propPath);
//        if(propPath == null) {
//            log.w("UserServiceImpl", "Got null SSO configuration, won't be able to make SSO calls!");
//            throw new Exception("Failed to create identity. 'ssoToolsPropertyPath' not set, cannot make SSO related calls.");
//        } else {
//            System.setProperty("ssoToolsPropertyPath", propPath);
//            System.setProperty("openamPropertiesPath", propPath);
//            openAmGateway = new OpenAmGateway(new SSOUtil());
//        }
//        return openAmGateway;
//    }
//
//    private boolean deleteIdentityUser(RegisterUser registerUser, User user, Org primaryOrg) throws Exception {
//        boolean deleteSuccessful = false;
//        OpenAmGateway openAmGateway = getOpenAmGatewayInstance();
//        JSONObject response = openAmGateway.deleteIdentityUser(registerUser.getEmail());
//        if(SUCCESS.equals(response.getString("status"))) {
//            log.i("UserServiceImpl", "Successfully deleted identity user with uid " + registerUser.getEmail() + "from OpenAm");
//            deleteSuccessful = true;
//        } else {
//            this.notifyFailedRegistration(registerUser, user, primaryOrg);
//        }
//        return deleteSuccessful;
//    }
//
//    private List<UserOrg> getUserOrgTeams(RegisterUser registerUser, User user) throws Exception {
//        List<UserOrg> userOrgTeams = new ArrayList<UserOrg>();
//        String[] teams = registerUser.getTeams();
//        if(teams != null && teams.length > 0) {
//            for (int i = 0; i < teams.length; i++) {
//                Org teamOrg = orgDao.getOrganization(teams[i]);
//                if (teamOrg == null) {
//                    log.i("UserServiceImpl", "Org does not exist: " + teams[i]);
//                    continue;
//                } else {
//                    UserOrg userOrgTeam = createUserOrg(teamOrg.getOrgId(), user.getUserId(), registerUser);
//                    if (userOrgTeam != null) {
//                        userOrgTeams.add(userOrgTeam);
//                    }
//                }
//            }
//        }
//        return userOrgTeams;
//    }
//
//    private List<UserOrgWorkspace> getUserOrgWorkspaceTeams(List<UserOrg> userOrgTeams) {
//        List<UserOrgWorkspace> userOrgWorkspacesTeams = new ArrayList<UserOrgWorkspace>();
//        for(UserOrg userOrgTeam : userOrgTeams) {
//            userOrgWorkspacesTeams.addAll(createUserOrgWorkspaceEntities(userOrgTeam, false));
//        }
//        return userOrgWorkspacesTeams;
//    }
//
//    private void notifySuccessfulRegistration(RegisterUser registerUser, User user, Org org) {
//        try {
//            String fromEmail = APIConfig.getInstance().getConfiguration().getString(APIConfig.NEW_USER_ALERT_EMAIL);
//            String date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(new Date());
//            String alertTopic = APIConfig.getInstance().getConfiguration().getString(APIConfig.EMAIL_ALERT_TOPIC,
//                    "iweb.nics.email.alert");
//            String newRegisteredUsers = APIConfig.getInstance().getConfiguration().getString(APIConfig.NEW_REGISTERED_USER_EMAIL);
//            String hostname = InetAddress.getLocalHost().getHostName();
//            List<String>  disList = orgDao.getOrgAdmins(org.getOrgId());
//            String toEmails = disList.toString().substring(1, disList.toString().length() - 1) + ", " + newRegisteredUsers;
//
//            if(disList.size() > 0 && !fromEmail.isEmpty()){
//                JsonEmail email = new JsonEmail(fromEmail,toEmails,
//                        "Alert from RegisterAccount@" + hostname);
//                email.setBody(date + "\n\n" + "A new user has registered: " + user.getUsername() + "\n" +
//                        "Name: " + user.getFirstname() + " " + user.getLastname() + "\n" +
//                        "Organization: " + org.getName() + "\n" +
//                        "Email: " + user.getUsername() + "\n" +
//                        "Other Information: " + registerUser.getOtherInfo());
//
//                notifyNewUserEmail(email.toJsonObject().toString(),alertTopic);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            APILogger.getInstance().e(CNAME,"Failed to send new User email alerts");
//        }
//    }
//
//    private void notifyFailedRegistration(RegisterUser registerUser, User user, Org org) {
//        try {
//            String fromEmail = APIConfig.getInstance().getConfiguration().getString(APIConfig.NEW_USER_ALERT_EMAIL);
//            String date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(new Date());
//            String alertTopic = APIConfig.getInstance().getConfiguration().getString(APIConfig.EMAIL_ALERT_TOPIC,
//                    "iweb.nics.email.alert");
//            String sysAdmins = APIConfig.getInstance().getConfiguration()
//                    .getString(APIConfig.SYSTEM_ADMIN_ALERT_EMAILS, "");
//            String hostname = InetAddress.getLocalHost().getHostName();
//            //List<String>  disList = orgDao.getOrgAdmins(org.getOrgId());
//            //String toEmails = disList.toString().substring(1, disList.toString().length() - 1) + ", " + sysAdmins;
//            String toEmails = sysAdmins;
//
//            //if(disList.size() > 0 && !fromEmail.isEmpty()){
//            if(!sysAdmins.isEmpty()) {
//                JsonEmail email = new JsonEmail(fromEmail, toEmails,
//                        "Alert from RegisterAccount@" + hostname);
//                email.setBody(date + "\n\n" + "A new user has attempted to register" +
//                        ". However, their system user failed to successfully persist, so before" +
//                        " they can try to register again with the same email address, their" +
//                        " Identity user will need deleted in OpenAM.\n\n" +
//                        "Name: " + user.getFirstname() + " " + user.getLastname() + "\n" +
//                        "Organization: " + org.getName() + "\n" +
//                        "Email: " + user.getUsername() + "\n" +
//                        "Other Information: " + registerUser.getOtherInfo());
//
//                notifyNewUserEmail(email.toJsonObject().toString(),alertTopic);
//            }
//
//        } catch (Exception e) {
//            APILogger.getInstance().e(CNAME,"Failed to send registration failed email to Org Admins and System Admins");
//        }
//    }
}
