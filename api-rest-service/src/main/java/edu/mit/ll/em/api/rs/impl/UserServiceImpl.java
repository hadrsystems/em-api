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
package edu.mit.ll.em.api.rs.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.validation.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.rs.*;
import edu.mit.ll.em.api.service.UserRegistrationService;
import edu.mit.ll.em.api.util.*;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;

import edu.mit.ll.nics.nicsdao.WorkspaceDAO;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;

import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.Contact;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.entity.UserOrgWorkspace;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import edu.mit.ll.nics.common.email.JsonEmail;

/**
 * 
 * @author sa23148
 *
 */
public class UserServiceImpl implements UserService {
	
	/** CNAME - the name of this class for referencing in loggers */
	private static final String CNAME = UserServiceImpl.class.getName();

	private static final APILogger log = APILogger.getInstance();
    private static final int DEFAULT_WORKSPACE_ID = 1;

	private UserDAOImpl userDao = new UserDAOImpl();
	private UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	private UserSessionDAOImpl userSessDao = new UserSessionDAOImpl();
	private OrgDAOImpl orgDao = new OrgDAOImpl();
    private WorkspaceDAO workspaceDAO = null;
	private RabbitPubSubProducer rabbitProducer;
    private UserRegistrationService userRegistrationService;
    private Validator validator;

    public UserServiceImpl(WorkspaceDAO workspaceDAO, UserRegistrationService userRegistrationService, Validator validator) {
        this.workspaceDAO = workspaceDAO;
        this.userRegistrationService = userRegistrationService;
        this.validator = validator;
    }

    public UserServiceImpl(UserDAOImpl userDao, UserOrgDAOImpl userOrgDao, UserSessionDAOImpl userSessionDao, OrgDAOImpl orgDao, WorkspaceDAO workspaceDAO,
                           RabbitPubSubProducer rabbitProducer, UserRegistrationService userRegistrationService, Validator validator) {
        this.userDao = userDao;
        this.userOrgDao = userOrgDao;
        this.userSessDao = userSessionDao;
        this.orgDao = orgDao;
        this.workspaceDAO = workspaceDAO;
        this.rabbitProducer = rabbitProducer;
        this.userRegistrationService = userRegistrationService;
        this.validator = validator;
    }

    /**
	 * Read and return all User items in workspace
	 * 
	 * @return Response
	 * @see UserResponse
	 */
	public Response getUsers(int workspaceId) {
		Response response = null;
		UserResponse userResponse = new UserResponse();
				
		List<edu.mit.ll.nics.common.entity.User> users = null;		
		try {
			
			users = userDao.getEnabledUsersInWorkspace(workspaceId);
			if(users != null && users.size() > 0) {
				APILogger.getInstance().i("UserServiceImpl", "GOT enabled users: " + users.size());				
				userResponse.setUsers(users);
			} else {
				APILogger.getInstance().i("UserServiceImpl", "No enabled users in workspace with id " + workspaceId);
			}
		
		} catch (DataAccessException e) {
			userResponse.setMessage("error. " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		} catch (Exception e) {
			userResponse.setMessage("error. Unhandled exception: " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}
		
		userResponse.setMessage("ok");
		response = Response.ok(userResponse).status(Status.OK).build();
		
		return response;
	}
	
	/**
	 * Read and return all Active User items in workspace
	 * 
	 * @return Response
	 * @see UserResponse
	 */
	public Response getActiveUsers(int workspaceId) {
		Response response = null;
		UserResponse userResponse = new UserResponse();
				
		List<edu.mit.ll.nics.common.entity.User> users = null;		
		try {
			
			users = userDao.getActiveUsers(workspaceId);
			userResponse.setUsers(users);
			if(users != null && users.size() > 0) {
				APILogger.getInstance().i("UserServiceImpl", "GOT active users: " + users.size());
			} else {
				APILogger.getInstance().i("UserServiceImpl", "No active users in workspace with id " + workspaceId);
			}
		
		} catch (DataAccessException e) {
			userResponse.setMessage("error. " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		} catch (Exception e) {
			userResponse.setMessage("error. Unhandled exception: " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}
		
		userResponse.setMessage("ok");
		response = Response.ok(userResponse).status(Status.OK).build();
		
		return response;
	}
	
	public Response isAdmin(int userOrgId){
		Response response = null;
		UserResponse userResponse = new UserResponse();
		int systemRoleId = userOrgDao.getSystemRoleId(userOrgId);
		if(systemRoleId == SADisplayConstants.ADMIN_ROLE_ID ||
				systemRoleId == SADisplayConstants.SUPER_ROLE_ID){
			userResponse.setCount(1);
			userResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(userResponse).status(Status.OK).build();
		}else{
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		return response;
	}
	
	public Response getLoginStatus(int workspaceId, String username){
		Response response = null;
		UserResponse userResponse = new UserResponse();
		int userId = userDao.isEnabled(username);
		if(userId != -1 && userOrgDao.hasEnabledOrgs(userId, workspaceId) > 0){
			userResponse.setCount(1);
			userResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(userResponse).status(Status.OK).build();
		}else{
			userResponse.setMessage(Status.FORBIDDEN.getReasonPhrase());
			response = Response.ok(userResponse).status(Status.OK).build();
		}
		return response;
	}

	
	/**
	 * Delete all User items.
	 * 
	 * <p>Unsupported Operation!</p>
	 * 
	 * @return Response
	 * @see UserResponse
	 */
	public Response deleteUsers() {
		return makeUnsupportedOpRequestResponse();
	}

	
	/**
	 * Bulk creation of User items.
	 * 
	 * <p>Unsupported Operation!</p>
	 * 
	 * @param A collection of User items to be created.
	 * @return Response
	 * @see UserResponse
	 */
	public Response putUsers(Collection<User> users) {
		
		return makeUnsupportedOpRequestResponse();
	}
	
	public Response addUserToOrg(Collection<Integer> userIds, int orgId, int workspaceId){
		NewUserOrgResponse userResponse = new NewUserOrgResponse();
		List<Integer> users = new ArrayList<Integer>();
		List<Integer> failedUsers = new ArrayList<Integer>();
		try{
			for(Integer userId : userIds){
				if(userOrgDao.getUserOrgById(orgId, userId, workspaceId) == null){
					UserOrg userorg = createUserOrg(orgId, userId);
	
					List<UserOrgWorkspace> userOrgWorkspaces = new ArrayList<UserOrgWorkspace>();
					userOrgWorkspaces.addAll(createUserOrgWorkspaceEntities(userorg, true));
					
					if(userDao.addUserToOrg((new Integer(userId)).longValue(), Arrays.asList(userorg), userOrgWorkspaces)){
						users.add(userId);
					}else{
						failedUsers.add(userId);
					}
				}else{
					failedUsers.add(userId);
				}
			}
			userResponse.setFailedUsers(failedUsers);
			userResponse.setUsers(users);
		}catch(Exception e){
			e.printStackTrace();
		}
		return Response.ok(userResponse).status(Status.OK).build();
	}

	/**
	 * Creation of a single User item.
	 * 
	 * @param registerUser to be created.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response postUser(int workspaceId, RegisterUser registerUser) {
        return this.userRegistrationService.postUser(registerUser);
	}
	
	/**
	 * Creates {@link UserOrgWorkspace} entities for every {@link Workspace} in the database
	 * 
	 * @param userOrg The {@link UserOrg} to create workspace entries for
	 * @param enabled Whether or no the {@link UserOrgWorkspace} entry is enabled or not
	 * @return A collection of {@link UserOrgWorkspace} entities
	 */
	private Collection<UserOrgWorkspace> createUserOrgWorkspaceEntities(UserOrg userOrg, boolean enabled) {
				
		List<UserOrgWorkspace> workspaces = new ArrayList<UserOrgWorkspace>();
		
		List<Integer> workspaceIds = getWorkspaceIds();
		if(workspaceIds == null || workspaceIds.isEmpty()) {
			return workspaces; // TODO: caller needs to know an empty one may mean an error occurred, not just
								// that there are no workspaces
		}
		
		for(Integer id : workspaceIds){
			UserOrgWorkspace workspace = new UserOrgWorkspace();			
			workspace.setUserorgid(userOrg.getUserorgid());
			workspace.setWorkspaceid(id);
			workspace.setEnabled(enabled);
			workspace.setDefaultorg(false);
			
			workspaces.add(workspace);
		}
		
		return workspaces;
	}

	public Response getSystemRoles(){
		return Response.ok(userOrgDao.getSystemRoles()).status(Status.OK).build();
	}
	
	/**
	 *  Read a single User item.
	 * @param ID of User item to be read.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response getUser(int userId) {
		Response response = null;
		UserResponse userResponse = new UserResponse();

		if (userId < 1) {
			userResponse.setMessage("Invalid userId value: " + userId) ;
			response = Response.ok(userResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		//User u = null;
		edu.mit.ll.nics.common.entity.User u = null;
		try {
			//u = UserDAO.getInstance().getUserById(userId);
			u = userDao.getUserById(userId);
		} catch (DataAccessException e) {
			userResponse.setMessage("error. DataAccessException: " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		} catch (Exception e) {
			userResponse.setMessage("error. Unhandled Exception: " + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}

		if (u == null) {
			userResponse.setMessage("No user found for userId value: " + userId) ;
			response = Response.ok(userResponse).status(Status.NOT_FOUND).build();
			return response;			
		}

		userResponse.getUsers().add(u);
		userResponse.setCount(1);
		userResponse.setMessage("ok");
		response = Response.ok(userResponse).status(Status.OK).build();

		return response;
	}
	
	public Response findUser(String firstName, String lastName, boolean exact){
		Response response = null;
		UserResponse userResponse = new UserResponse();
		List<User> foundUsers = null;
		
		if(firstName != null){
			if(lastName != null){
				foundUsers = userDao.findUser(firstName, lastName, exact);
			}else{
				foundUsers = userDao.findUserByFirstName(firstName, exact);
			}
		}else if(lastName != null){
			foundUsers = userDao.findUserByLastName(lastName, exact);
		}
		
		if(foundUsers != null){
			userResponse.setUsers(foundUsers);
			userResponse.setCount(foundUsers.size());
			userResponse.setMessage(Status.OK.toString());
			response = Response.ok(userResponse).status(Status.OK).build();
		}else{
			userResponse.setMessage("No users found") ;
			response = Response.ok(userResponse).status(Status.NOT_FOUND).build();	
		}

		return response;	
	}

	public Response setUserActive(int userOrgWorkspaceId, int userId, boolean active, String requestingUser){
		Response response = null;
		UserResponse userResponse = new UserResponse();

		int systemRoleId = userOrgDao.getSystemRoleId(requestingUser, userOrgWorkspaceId);

		if ((systemRoleId == SADisplayConstants.ADMIN_ROLE_ID || systemRoleId == SADisplayConstants.SUPER_ROLE_ID) ||
				userOrgDao.isUserRole(requestingUser, SADisplayConstants.SUPER_ROLE_ID)) {

			userDao.setUserActive(userId, active);

			User responseUser = userDao.getUserById(userId);
			userResponse.setUsers(Arrays.asList(responseUser));

			userResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(userResponse).status(Status.OK).build();
		} else {
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}

		return response;
	}

	
	/**
	 *  Delete a single User item.
	 * @param ID of User item to be read.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response deleteUser(int userId) {
		
		return makeUnsupportedOpRequestResponse();
	}

	
	/**
	 * Update a single User item.
	 * 
	 * @param ID of User item to be read.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response putUser(int userId, User user) {
		
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 *  Post a single User item.
	 *  This is an illegal operation. 
	 * @param ID of User item to be read.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response postUser(int userId) {
		return makeIllegalOpRequestResponse();
	}

	/**
	 * Return the number of User items stored.
	 *  
	 * @return Response
	 * @see UserResponse
	 */		
	public Response getUserCount(int workspaceId) {
		UserResponse userResponse = new UserResponse();
		Response response = null;
		long count = -1;
		try {
			//count = UserDAO.getInstance().getUserCount();
			count = userDao.getUserCountInWorkspace(workspaceId);
		} catch (DataAccessException e) {
			userResponse.setMessage("error. DataAccessException" + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();			
			return response;
		} catch (Exception e) {
			userResponse.setMessage("error. Unhandled Exception" + e.getMessage());
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();			
			return response;
		}

		userResponse.setMessage("ok");
		userResponse.setCount(count);
		userResponse.setUsers(null);
		response = Response.ok(userResponse).status(Status.OK).build();		
		return response;
	}
	
	public Response getEnabledUsers(int workspaceId, int orgId){
		Response response = null;
		FieldMapResponse dataResponse = new FieldMapResponse();
		dataResponse.setData(userOrgDao.getEnabledUserOrgs(orgId, workspaceId));
		
		dataResponse.setMessage(Status.OK.getReasonPhrase());
		response = Response.ok(dataResponse).status(Status.OK).build();

		return response;
	}
	
	public Response getDisabledUsers(int workspaceId, int orgId){
		Response response = null;
		FieldMapResponse dataResponse = new FieldMapResponse();
		dataResponse.setData(userOrgDao.getDisabledUserOrgs(orgId, workspaceId));
		
		dataResponse.setMessage(Status.OK.getReasonPhrase());
		response = Response.ok(dataResponse).status(Status.OK).build();

		return response;
	}
	
	public Response setUserEnabled(int userOrgWorkspaceId, int userId, 
			int workspaceId, boolean enabled, String requestingUser){
		Response response = null;
		UserResponse userResponse = new UserResponse();
		
		int systemRoleId = userOrgDao.getSystemRoleId(requestingUser, userOrgWorkspaceId);
		
		//TO DO: Check to see if user is admin in org or super admin in ANY org
		if((systemRoleId == SADisplayConstants.ADMIN_ROLE_ID ||
				systemRoleId == SADisplayConstants.SUPER_ROLE_ID) ||
				userOrgDao.isUserRole(requestingUser, SADisplayConstants.SUPER_ROLE_ID)){
			
			int count = userOrgDao.setUserOrgEnabled(userOrgWorkspaceId, enabled);
			
			if(count == 1){
				userResponse.setOrgCount(userOrgDao.hasEnabledOrgs(userId, workspaceId));
				if(!enabled && userResponse.getOrgCount() == 0){
					userDao.setUserEnabled(userId, false);
				}else if(enabled){
					userDao.setUserEnabled(userId, true);
					
					try {
						String fromEmail = APIConfig.getInstance().getConfiguration().getString(APIConfig.NEW_USER_ENABLED_EMAIL);
						String alertTopic = String.format("iweb.nics.email.alert");
						User newUser = userDao.getUserById(userId);
						String emailTemplate = APIConfig.getInstance().getConfiguration()
								.getString(APIConfig.NEW_USER_BODY_TEMPLATE);
						String emailBody;
						String emailSubject = APIConfig.getInstance().getConfiguration()
								.getString(APIConfig.NEW_USER_BODY_SUBJECT,"Welcome to Team SCOUT!");
						if(emailTemplate != null){
							emailBody = new String(Files.readAllBytes(Paths.get(emailTemplate)));
						}
						else{
                            emailBody = "<!doctype html>\n" +
                                    "<html>\n" +
                                    "<head>\n" +
                                    "<meta charset=\"utf-8\">\n" +
                                    "<title>Welcome to SCOUT!</title>\n" +
                                    "<style>\n" +
                                    "        body {font-family: calibri, Helvetica, Arial, sans-serif}\n" +
                                    "        h1, h2 {\n" +
                                    "                color: #1a4276;\n" +
                                    "                margin-bottom: 0;\n" +
                                    "        }\n" +
                                    "        .light-blue {color: #5d7f90}\n" +
                                    "        p {display:inline}\n" +
                                    "        img {\n" +
                                    "                display:block;\n" +
                                    "                float:right;\n" +
                                    "        }\n" +
                                    "</style>\n" +
                                    "</head>\n" +
                                    "\n" +
                                    "<body>\n" +
                                    "\n" +
                                    "<img src=\"https://www.scout.stg.tabordasolutions.net/nics/login/images/scout_logo.png\">\n" +
                                    "\n" +
                                    "<p style=\"display:block\">Welcome to the Situation Awareness &amp; Collaboration Tool (<strong>SCOUT</strong>) for California emergency responders. Your account is now active.</p>\n" +
                                    "\n" +
                                    "<p style=\"display:block\">Please review the <a href=\"http://www.caloes.ca.gov/RegionalOperationsSite/Documents/2016_04_25%20SCOUT%20Concept%20of%20Operations.pdf\" target=\"_blank\">SCOUT Concept of Operations</a>, <a href=\"http://www.caloes.ca.gov/RegionalOperationsSite/Documents/2016_08 SCOUT Support Plan.pdf\">Technical Support Plan</a>, and your agency's specific SCOUT standard operating procedures before using SCOUT.</p>\n" +
                                    "\n" +
                                    "<p style=\"display:block\">For additional training materials, visit <a href=\"www.scout.ca.gov/scouthelp\" target=\"_blank\">www.scout.ca.gov/scouthelp</a>. Please be aware some training materials still reference the NICS v.5 user interface. They are being  updated over the next year to reference the SCOUT user interface.</p>\n" +
                                    "\n" +
                                    "<h3>Important Information</h3>\n" +
                                    "<ul>\n" +
                                    "        <li>Recommended browser is Chrome.\n" +
                                    "            <ul>\n" +
                                    "                <li>The Census App tool does not function in Internet Explorer.</li>\n" +
                                    "            </ul>\n" +
                                    "        </li>\n" +
                                    "        <li>SCOUT iOS mobile app is available via the App Store.</li>\n" +
                                    "        <li>For technical support, contact your Agency's SCOUT Administrator.</li>\n" +
                                    "        <li>For the most current information about SCOUT, visit <a href=\"www.caloes.ca.gov/scout\" target=\"_blank\">www.caloes.ca.gov/scout</a>.</li>\n" +
                                    "</ul>\n" +
                                    "\n" +
                                    "<span>V/r</span><br>\n" +
                                    "<span>The SCOUT Support Team</span>\n" +
                                    "\n" +
                                    "<image\n" +
                                    "</body>\n" +
                                    "</html>\n";
						}
						
						JsonEmail email = new JsonEmail(fromEmail,newUser.getUsername(),emailSubject);
						
						email.setBody(emailBody);
						
						notifyNewUserEmail(email.toJsonObject().toString(),alertTopic);
						
					} catch (Exception e) {
						e.printStackTrace();
						APILogger.getInstance().e(CNAME,"Failed to send new User email alerts");
					}
					
				}
				User responseUser = userDao.getUserById(userId);
				userResponse.setUsers(Arrays.asList(responseUser));
			}
			userResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(userResponse).status(Status.OK).build();
		}else{
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
		}

		return response;
	}

	public Response searchUserResources(UserSearchParams searchParams) {
		Response response = null;
		UserResponse userResponse = new UserResponse();
		
		if (searchParams == null) {
			userResponse.setMessage("Missing search parameters.") ;
			response = Response.ok(userResponse).status(Status.BAD_REQUEST).build();
			return response;
		}

		//User u = null;
		edu.mit.ll.nics.common.entity.User u = null;
		String username = searchParams.getUsername();
		if (username != null && !username.isEmpty()) {
			try {
				//u = UserDAO.getInstance().getUserByUsername(username);
				u = userDao.getUser(username);
			} catch (DataAccessException e) {
				userResponse.setMessage("Error. " + e.getMessage());
				userResponse.setCount(0);
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
				return response;
			}
		}

		if (u == null) {
			userResponse.setMessage("No user found for username value: " + username) ;
			response = Response.ok(userResponse).status(Status.NOT_FOUND).build();
			return response;			
		}

		userResponse.getUsers().add(u);
		userResponse.setCount(1);
		userResponse.setMessage("ok");
		response = Response.ok(userResponse).status(Status.OK).build();

		return response;
	}
	
	public Response getUserProfile(String username, int userOrgId, 
			int workspaceId, int orgId, int rUserOrgId, String requestingUser){
		Response response = null;
		UserProfileResponse profileResponse = new UserProfileResponse();
		
		if(!username.equalsIgnoreCase(requestingUser)){
			
			//User is not requesting their own profile
			int requestingUserRole = userOrgDao.getSystemRoleIdForUserOrg(requestingUser, rUserOrgId);
			
			//Verify the request user is an admin for the organization or a super user for any other organization
			if(requestingUserRole != SADisplayConstants.ADMIN_ROLE_ID &&
					!userOrgDao.isUserRole(requestingUser, SADisplayConstants.SUPER_ROLE_ID)){
			
				return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
			}
		}
		
		try{
			
			OrgDAOImpl orgDao = new OrgDAOImpl();
			Org org = orgDao.getOrganization(orgId); 
			edu.mit.ll.nics.common.entity.User user = userDao.getUser(username);
			
			UserOrg userOrg = userOrgDao.getUserOrgById(org.getOrgId(), user.getUserId(), workspaceId);
			
			IncidentDAOImpl incidentDao = new IncidentDAOImpl();
			
			profileResponse.setIncidentTypes(incidentDao.getIncidentTypes());
			profileResponse.setUserOrgId(userOrgId);
			profileResponse.setUsername(username);
			profileResponse.setOrgName(org.getName());
			profileResponse.setOrgId(org.getOrgId());
			profileResponse.setOrgPrefix(org.getPrefix());
			profileResponse.setWorkspaceId(workspaceId);
			profileResponse.setUserId(user.getUserId());
			profileResponse.setUserFirstname(user.getFirstname());
			profileResponse.setUserLastname(user.getLastname());
			profileResponse.setRank(userOrg.getRank());
			profileResponse.setDescription(userOrg.getDescription());
			profileResponse.setJobTitle(userOrg.getJobTitle());
			profileResponse.setSysRoleId(userOrg.getSystemroleid());
			profileResponse.setIsSuperUser(userOrgDao.isUserRole(username, SADisplayConstants.SUPER_ROLE_ID));
			profileResponse.setIsAdminUser(userOrgDao.isUserRole(username, SADisplayConstants.ADMIN_ROLE_ID));
			profileResponse.setMessage("ok");
			
			response = Response.ok(profileResponse).status(Status.OK).build();
		}catch(DataAccessException ex){
			ex.printStackTrace();
			return Response.ok("error. DAO exception getting user profile: " + ex.getMessage()).
					status(Status.INTERNAL_SERVER_ERROR).build();
			
		}catch(Exception e) {
			e.printStackTrace();
			return Response.ok("error. Unhandled exception getting user profile: " + e.getMessage()).
					status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
	
	public Response postUserProfile(edu.mit.ll.em.api.rs.User user, String requestingUser, int rUserOrgId){
		
		Response response = null;
		UserProfileResponse profileResponse = new UserProfileResponse();
		SSOUtil ssoUtil = null;
		String propPath = APIConfig.getInstance().getConfiguration().getString("ssoToolsPropertyPath", null);
		
		if(!user.getUserName().equalsIgnoreCase(requestingUser)){
			
			//User is not requesting their own profile
			int requestingUserRole = userOrgDao.getSystemRoleIdForUserOrg(requestingUser, rUserOrgId);
			if( (requestingUserRole != SADisplayConstants.SUPER_ROLE_ID &&
				requestingUserRole != SADisplayConstants.ADMIN_ROLE_ID) &&
				//Verify the request user is a super user for any other organization
				!userOrgDao.isUserRole(requestingUser,SADisplayConstants.SUPER_ROLE_ID)){
			
				return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
			}
		}
		
		try{
			
			Boolean updatedProfile = false;
			Boolean wrongPW = false;
			User dbUser = userDao.getAllUserInfoById(user.getUserId());
			UserOrg userOrg = userOrgDao.getUserOrg(user.getUserOrgId());
			
			if(user.getOldPw().length() > 0  || user.getNewPw().length() > 0 ){	
				
				String token = "";
				
				if(propPath == null) {
					log.w("UserServiceImpl", "Got null SSO configuration, won't be able to make SSO calls!");
					updatedProfile = false;
				} else {
					System.setProperty("ssoToolsPropertyPath", propPath);
					System.setProperty("openamPropertiesPath", propPath);
					
					ssoUtil = new SSOUtil();
					
					token = ssoUtil.login(user.getUserName(),user.getOldPw());
					
					if(!(updatedProfile = (token != null))){
						wrongPW = true;
					}
					
				}		
				
				if(updatedProfile){			

					updatedProfile = ssoUtil.changeUserPassword(user.getUserName(), user.getNewPw());

					if(updatedProfile){

						String newPWHash = SaltedHashUtil.generateSaltedHash(user.getNewPw(),user.getUserName());
						
						updatedProfile =  userDao.updateUserPW(dbUser.getUserId(), newPWHash);
						
						if(updatedProfile){
							
							userDao.updateNames(user.getUserId(),user.getFirstName(),user.getLastName());
							userOrgDao.updateUserOrg(user.getUserOrgId(),user.getJobTitle(),user.getRank(),user.getJobDesc(), user.getSysRoleId());
						
							dbUser = userDao.getAllUserInfoById(user.getUserId());
							userOrg = userOrgDao.getUserOrg(user.getUserOrgId());
							
						}
						else{
							
							ssoUtil.changeUserPassword(user.getUserName(), user.getOldPw());
						}
					}
				}
				
				if(ssoUtil != null){
					ssoUtil.logout();
					
				}
			}
			else{
				
				updatedProfile = true;
				
				userDao.updateNames(user.getUserId(),user.getFirstName(),user.getLastName());
				userOrgDao.updateUserOrg(user.getUserOrgId(),user.getJobTitle(),user.getRank(),user.getJobDesc(), user.getSysRoleId());
				
				dbUser = userDao.getAllUserInfoById(user.getUserId());
				userOrg = userOrgDao.getUserOrg(user.getUserOrgId());
			}
			
			if(updatedProfile){

				profileResponse.setUserOrgId(userOrg.getUserorgid());
				profileResponse.setOrgId(userOrg.getOrgid());
				profileResponse.setUserId(dbUser.getUserId());
				profileResponse.setUserFirstname(dbUser.getFirstname());
				profileResponse.setUserLastname(dbUser.getLastname());
				profileResponse.setRank(userOrg.getRank());
				profileResponse.setDescription(userOrg.getDescription());
				profileResponse.setJobTitle(userOrg.getJobTitle());
				profileResponse.setMessage("ok");

				response = Response.ok(profileResponse).status(Status.OK).build();
			}
			else{
				if(wrongPW){
					profileResponse.setMessage("Incorrect password");
				}
				else{
					profileResponse.setMessage("Error updating user profile");
				}
				response = Response.ok(profileResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
		}catch(DataAccessException ex){
			ex.printStackTrace();
			return Response.ok("error. DAO exception updating user profile: " + ex.getMessage()).
					status(Status.INTERNAL_SERVER_ERROR).build();
			
		}catch(Exception e) {
			e.printStackTrace();
			return Response.ok("error. Unhandled exception updating user profile: " + e.getMessage()).
					status(Status.INTERNAL_SERVER_ERROR).build();
		}
			
			
		return response;
	}
	
	// TODO: breaks mobile
	public Response getUserOrgs(int workspaceId, String username, String requestingUser){
		Response response = null;
		UserOrgResponse userOrgResponse = new UserOrgResponse();
		
		if(!username.equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		try{
			//int userId = UserDAO.getInstance().getNicsUserByUsername(username).getUserId();
			// TODO: fix int vs. long issue
			long userId = userDao.getMyUserID(username);
			//List<Object[]> userOrgs = OrgDAO.getInstance().getUserOrgs(workspaceId, userId);
			
			List<Map<String, Object>> userOrgs = this.orgDao.getUserOrgsWithOrgName(Integer.parseInt(""+userId), workspaceId);
			// TODO: remove old hibernate 
			userOrgResponse.setUserOrgs(userOrgs);
			
			//userOrgResponse.setUserOrgs(userOrgs);
			userOrgResponse.setUserId(userId);
			response = Response.ok(userOrgResponse).status(Status.OK).build();
		} catch(DataAccessException e) {
			e.printStackTrace();
			return Response.ok("error. DAO exception getting UserOrgs: " + e.getMessage())
					.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch(Exception e) {
			e.printStackTrace();
			return Response.ok("error. Unhandled exception getting UserOrgs: " + e.getMessage())
					.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}

	private Response makeIllegalOpRequestResponse() {
		UserResponse mdtrackResponse = new UserResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
				status(Status.FORBIDDEN).build();
		return response;
	}	
	
	private Response makeUnsupportedOpRequestResponse() {
		UserResponse mdtrackResponse = new UserResponse();
		mdtrackResponse.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
				status(Status.NOT_IMPLEMENTED).build();
		return response;
	}

	/**
	 * Utility method for creating NICS User from an API RegisterUser
	 * 
	 * @param rUser a valid RegisterUser object
	 * @return a valid NICS User object if successful, null otherwise
	 * @throws Exception
	 */
	private edu.mit.ll.nics.common.entity.User createUser(RegisterUser rUser) throws Exception{
		edu.mit.ll.nics.common.entity.User user = new edu.mit.ll.nics.common.entity.User();
				
		int userid = userDao.getNextUserId();
		user.setFirstname(rUser.getFirstName());
		user.setLastname(rUser.getLastName());
		user.setUsername(rUser.getEmail());
		user.setUserId(userid);
		user.setPasswordHash(SaltedHashUtil.generateSaltedHash(rUser.getPassword(), rUser.getEmail()));
		user.setEnabled(false);
		user.setActive(true);
		user.setLastupdated(Calendar.getInstance().getTime());
		user.setCreated(Calendar.getInstance().getTime());
		user.setPasswordchanged(Calendar.getInstance().getTime());

		return user;
	}
	

	/**
	 * Create User Org, taken from SADisplay
	 * 
	 * @param orgid
	 * @param userid
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public UserOrg createUserOrg(int orgid, int userid) throws Exception {
		UserOrg userorg = new UserOrg();

		// TODO:ID user_org_workspace needs it, so can't let DAO get it?
		//int userorgid =  UserOrgDAO.getInstance().getNextUserOrgId(); 
		
		UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
		int userorgid = userOrgDao.getNextUserOrgId();//UserOrgDAO.getInstance().getNextUserOrgId();
		if(userorgid == -1) {
            // failed to get userorgid
		}
		userorg.setUserorgid(userorgid);
		userorg.setOrgid(orgid);
		userorg.setCreated(Calendar.getInstance().getTime());
		userorg.setSystemroleid(SADisplayConstants.USER_ROLE_ID);
		userorg.setUserid(userid);
		
		return userorg;
	}
	
	private List<Integer> getWorkspaceIds() {
		//List<Integer> workspaceIds = WorkspaceDAO.getInstance().getWorkspaceIds();
		WorkspaceDAOImpl workspaceDao = new WorkspaceDAOImpl();
		List<Integer> workspaceIds = workspaceDao.getWorkspaceIds();
		log.i("UserServiceImpl", "got workspaceids: " + Arrays.toString(workspaceIds.toArray()));
		return workspaceIds;
	}

	public Response createUserSession(long userId, String displayName, int userorgId,
			int systemRoleId, int workspaceId, String sessionId, String requestingUser) {
		
		Response response = null;
		UserResponse  userResponse = new UserResponse();

		if(userDao.getUserId(requestingUser) != userId){
            return Response.status(Status.BAD_REQUEST).entity(
                    Status.FORBIDDEN.getReasonPhrase()).build();
        }

		CurrentUserSession session = null;
		boolean existing = false;
		try{
			CurrentUserSession cus = userSessDao.getCurrentUserSession(userId);
			if(cus != null){
				if(this.userSessDao.removeUserSession(cus.getCurrentusersessionid())){
					this.notifyLogout(workspaceId, cus.getCurrentusersessionid());
				}else{
					//This will fall through to the null session exception
					existing = true;
				}
			}
			
			if(!existing){
				session = userSessDao.createUserSession(userId, displayName, userorgId, systemRoleId, workspaceId, sessionId);
				if(session != null){
					userResponse.setCount(1);
					userResponse.setMessage(Status.OK.getReasonPhrase());
					userResponse.setUserSession(session);
					response = Response.ok(userResponse).status(Status.OK).build();
					
					try {
						User user = userDao.getUserWithSession(userId);
						notifyLogin(workspaceId, user);
					} catch (IOException e) {
						APILogger.getInstance().e("UserServiceImpl", "Failed to publish ChatMsgService message event" + e.getMessage());
					}
				}
			}
			
			if(session == null){
				APILogger.getInstance().e("UserServiceImpl", "The usersession was not created for " + displayName);
				userResponse.setCount(1);
				userResponse.setMessage(Status.NOT_FOUND.getReasonPhrase());
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception creating UserSession");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}
	public Response updateUserSession(long userId, String displayName, int userorgId,
			int systemRoleId, int workspaceId, String sessionId, String requestingUser) {
		
		Response response = null;
		UserResponse  userResponse = new UserResponse();

		if(userDao.getUserId(requestingUser) != userId){
            return Response.status(Status.BAD_REQUEST).entity(
                    Status.FORBIDDEN.getReasonPhrase()).build();
        }

		CurrentUserSession session = null;
		boolean existing = false;
		try{
			CurrentUserSession cus = userSessDao.getCurrentUserSession(userId);
			if(cus != null){
				if(this.userSessDao.removeUserSession(cus.getCurrentusersessionid())){
					//this.notifyLogout(workspaceId, cus.getCurrentusersessionid());
				}else{
					existing = true;
				}
			}
			
			if(!existing){
				session = userSessDao.createUserSession(userId, displayName, userorgId, systemRoleId, workspaceId, sessionId);
				if(session != null){
					userResponse.setCount(1);
					userResponse.setMessage(Status.OK.getReasonPhrase());
					userResponse.setUserSession(session);
					response = Response.ok(userResponse).status(Status.OK).build();
					
					try {
						User user = userDao.getUserWithSession(userId);
						notifyLogin(workspaceId, user);
					} catch (IOException e) {
						APILogger.getInstance().e("UserServiceImpl", "Failed to publish ChatMsgService message event" + e.getMessage());
					}
				}
			}
			
			if(session == null){
				APILogger.getInstance().e("UserServiceImpl", "The usersession was not created for " + displayName);
				userResponse.setCount(1);
				userResponse.setMessage(Status.NOT_FOUND.getReasonPhrase());
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception creating UserSession");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}
	
	public Response getUsersContactInfo(int workspaceId,  String userName){
		
		Response response = null;
		UserResponse  userResponse = new UserResponse();
		List<Contact> contacts = null;
		User user = null;
		
		try{
			contacts = userDao.getAllUserContacts(userName);
			
			if(contacts != null){
				user = new User();
				user.setContacts(new HashSet<Contact>(contacts));
				userResponse.getUsers().add(user);
				userResponse.setMessage(Status.OK.getReasonPhrase());
				userResponse.setCount(contacts.size());
				response = Response.ok(userResponse).status(Status.OK).build();
			}
			else{
				userResponse.setMessage(Status.EXPECTATION_FAILED.getReasonPhrase());
				userResponse.setCount(0);
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
			
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception getting Contacts");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		
		return response;
	}
	
	public Response addContactInfo(int workspaceId, String userName, int contactId, String value , String requestingUser){
			
		Response response = null;
		UserResponse  userResponse = new UserResponse();
		User user = null;
		Contact contact = null;
		HashSet<Contact> hashSet = null;
		
		if(!userName.equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		try{
			
			int dbContactId = userDao.addContact(userName, contactId, value);
			
			
			if(dbContactId != -1){
				user = new User();
				contact = new Contact();
				hashSet = new HashSet<Contact>();
				contact.setContactid(dbContactId);
				hashSet.add(contact);
				user.setContacts(hashSet);
				userResponse.getUsers().add(user);
				userResponse.setMessage(Status.OK.getReasonPhrase());
				userResponse.setCount(1);
				response = Response.ok(userResponse).status(Status.OK).build();
			}
			else{
				userResponse.setMessage(Status.EXPECTATION_FAILED.getReasonPhrase());
				userResponse.setCount(0);
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
			
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception updating Contacts");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		
		return response;
	}
	
	public Response deleteContactInfo(int workspaceId, String userName, int contactId, String requestingUser){
		
		Response response = null;
		UserResponse  userResponse = new UserResponse();
		
		if(!userName.equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		try{
			
			boolean deleted = userDao.deleteContact(contactId);
			
			if(deleted){
				userResponse.setMessage(Status.OK.getReasonPhrase());
				userResponse.setCount(1);
				response = Response.ok(userResponse).status(Status.OK).build();
			}
			else{
				userResponse.setMessage(Status.EXPECTATION_FAILED.getReasonPhrase());
				userResponse.setCount(0);
				response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception updating Contacts");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}
	
	private void notifyLogin(int workspaceId, User user) throws IOException {
		if (user != null) {
			String topic = String.format("iweb.NICS.%d.login", workspaceId);
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(user);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private void notifyLogout(int workspaceId, long currentUserSessionId) throws IOException {
		String topic = String.format("iweb.NICS.%d.logout", workspaceId);
		getRabbitProducer().produce(topic, Long.toString(currentUserSessionId));
	}
	
	private void notifyNewUserEmail(String email, String topic) throws IOException {
		if (email != null) {
			getRabbitProducer().produce(topic, email);
		}
	}
	
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer(
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_HOSTNAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_EXCHANGENAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERNAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERPWD_KEY));
		}
		return rabbitProducer;
	}
	
	public Response removeUserSession(int workspaceId, long currentUserSessionId){
		Response response = null;
		UserResponse  userResponse = new UserResponse();
		
		try{
			if(userSessDao.removeUserSession(currentUserSessionId)){
				userResponse.setCount(1);
				userResponse.setMessage(Status.OK.getReasonPhrase());
				response = Response.ok(userResponse).status(Status.OK).build();
			}
			
			notifyLogout(workspaceId, currentUserSessionId);
			
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception creating UserSession");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
			
		
		return response;
		
	}

    public Response verifyEmailAddress(int workspaceId, String email) {
        boolean validEmail = validator.validateValue(RegisterUser.class, "email", email).isEmpty();
        validEmail = validEmail ? !(userDao.getUser(email) != null) : validEmail;
        int status = validEmail ? Response.Status.OK.getStatusCode(): Status.BAD_REQUEST.getStatusCode();
        VerifyEmailResponse responseEntity = new VerifyEmailResponse(status, "OK", validEmail);
        return Response.ok(responseEntity).status(status).build();
    }

    public Response verifyActiveSession(int workspaceId, int userSessionId, String requestingUser) {
        User user = null;
        try {
            if(StringUtils.isBlank(requestingUser) || (user = userDao.getUser(requestingUser)) == null) {
                APILogger.getInstance().e(CNAME, "Invalid requestingUser : " + requestingUser + ", Forbidden request");
                APIResponse apiResponse =  new APIResponse(Status.FORBIDDEN.getStatusCode(), "Not authorized for this request");
                return Response.ok(apiResponse).status(Status.FORBIDDEN).build();
            }
            if(userSessionId <= 0) {
                APIResponse apiResponse =  new APIResponse(Status.BAD_REQUEST.getStatusCode(), "Please provide valid userSessionId");
                APILogger.getInstance().e(CNAME, "Invalid userSessionId provided: " + userSessionId);
                return Response.ok(apiResponse).status(Status.BAD_REQUEST).build();
            }
            if(workspaceId <= 0 || this.workspaceDAO.getWorkspaceName(workspaceId) == null) {
                APILogger.getInstance().e(CNAME, "Invalid workspaceId : " + userSessionId + ", defaulting to use workspaceId: " + DEFAULT_WORKSPACE_ID);
                workspaceId = DEFAULT_WORKSPACE_ID;
            }

            CurrentUserSession currentUserSession = userSessDao.getCurrentUserSession(workspaceId, user.getUserId());
            boolean activeSession = currentUserSession != null && currentUserSession.getUsersessionid() == userSessionId;
            ActiveSessionResponse activeSessionResponse = new ActiveSessionResponse(Status.OK.getStatusCode(), "ok", activeSession);
            return Response.ok(activeSessionResponse).build();
        } catch(Exception e) {
            APILogger.getInstance().e(CNAME, "Unable to process verifyActiveSession request (workspaceId : " + workspaceId + ", userSessionId: " + userSessionId + ", requestingUser: " + requestingUser + ")", e);
            return Response.ok(new APIResponse(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Unable to process your request, please try again later.")).status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}