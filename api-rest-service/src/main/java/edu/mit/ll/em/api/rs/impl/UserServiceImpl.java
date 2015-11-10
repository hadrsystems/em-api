/**
 * Copyright (c) 2008-2015, Massachusetts Institute of Technology (MIT)
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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.dataaccess.ICSDatastoreException;
import edu.mit.ll.em.api.dataaccess.IncidentDAO;
import edu.mit.ll.em.api.dataaccess.MessageBusProducer;
import edu.mit.ll.em.api.dataaccess.OrgDAO;
import edu.mit.ll.em.api.dataaccess.SystemRoleDAO;
import edu.mit.ll.em.api.dataaccess.UserDAO;
import edu.mit.ll.em.api.dataaccess.UserOrgDAO;
import edu.mit.ll.em.api.rs.FieldMapResponse;
import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.em.api.rs.UserOrgResponse;
import edu.mit.ll.em.api.rs.UserProfileResponse;
import edu.mit.ll.em.api.rs.UserResponse;
import edu.mit.ll.em.api.rs.UserSearchParams;
import edu.mit.ll.em.api.rs.UserService;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.NetUtil;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.em.api.util.UserInfoValidator;
import edu.mit.ll.em.api.util.rabbitmq.RabbitFactory;
import edu.mit.ll.em.api.util.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.Chat;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
//import edu.mit.ll.em.api.rs.User;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.Contact;
import edu.mit.ll.nics.common.entity.ContactType;
import edu.mit.ll.nics.common.entity.EntityEncoder;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.SADisplayMessageEntity;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.entity.UserOrgWorkspace;
import edu.mit.ll.nics.common.messages.sadisplay.SADisplayMessage;
import edu.mit.ll.nics.nicsdao.UserSessionDAO;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.SystemRoleDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.WorkspaceDAOImpl;
import edu.mit.ll.nics.sso.util.SSOUtil;
import edu.mit.ll.soa.sso.exception.InitializationException;

/**
 * 
 * @author sa23148
 *
 */
public class UserServiceImpl implements UserService {
	
	private static String FAILURE = "An error occurred while registering your account.";
	private static String FAILURE_NAMES = "A first and last name is required.";
	private static String FAILURE_USERNAME = "*** username already exists ***";
	private static String FAILURE_PASSWORDS = "Passwords do not match";
	private static String FAILURE_PHONE_NUMBERS = "Phone number is not a valid format.";
	private static String FAILURE_USERNAME_INVALID = "Your username must be in a valid email format.";
	private static String FAILURE_ORG_INVALID = "You must choose an organization.";
	private static String COULD_NOT_VALIDATE_REGISTRATION = "Please confirm your registration with your administrator.";
	private static String FAILURE_EMAIL = "An email address is required.";
	private static String FAILURE_OTHER_EMAIL = "The 'Other Email' is invalid";
	private static String SUCCESS = "success";
	
	private static final APILogger log = APILogger.getInstance();
	
	private UserProfileResponse UserProfileResponse;
	
	private final UserDAOImpl userDao = new UserDAOImpl();
	private final UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	private final UserSessionDAOImpl userSessDao = new UserSessionDAOImpl();
	
	private RabbitPubSubProducer rabbitProducer;

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
		if(userId != -1 && userOrgDao.hasEnabledOrgs(userId, workspaceId)){
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

	
	/**
	 * Creation of a single User item.
	 * 
	 * @param User to be created.
	 * @return Response
	 * @see UserResponse
	 */	
	public Response postUser(int workspaceId, RegisterUser registerUser) {
		String successMessage = "Successfully registered user";
		
		try{
			edu.mit.ll.nics.common.entity.User user = createUser(registerUser);
			String validate = validateRegisterUser(registerUser);
			
			if(!validate.equals(SUCCESS)) {
				return Response.ok(validate).status(Status.PRECONDITION_FAILED).build();
			}
			
			//if(!validate.equals(SUCCESS)){ // TODO: bypassing validation for now				
			//	return Response.ok(validate).status(Status.PRECONDITION_FAILED).build();
			//}else{
				Collection<Org> orgs = new ArrayList<Org>();

				Collection<SADisplayMessageEntity> contacts = 
						createContacts(registerUser.getEmail(), 
								registerUser.getOtherEmail(),
								registerUser.getOfficePhone(),
								registerUser.getCellPhone(), 
								registerUser.getOtherPhone(),
								registerUser.getRadioNumber(), user);

				// TODO:SSO get named org with orgDao
				//Org org = OrgDAO.getInstance().getOrganization(registerUser.getOrganization());
				OrgDAOImpl orgDao = new OrgDAOImpl();
				Org org = orgDao.getOrganization(registerUser.getOrganization());
				if(org == null) {
					// need to fail, can't get org
					return Response.ok(FAILURE_ORG_INVALID + ": " + registerUser.getOrganization())
						.status(Status.PRECONDITION_FAILED).build();
				}
				UserOrg userOrg = createUserOrg(org.getOrgId(), user.getUserId(), registerUser);

				if(userOrg == null) {
					// fail since couldn't create userorg?
					log.w("UserService", "!!! FAILED to create userOrg for user: " + registerUser.getEmail());
					return Response.ok("Failed to create UserOrg with orgId '" + org.getOrgId() + 
							"' and userId '" + user.getUserId() + "' for user: " + registerUser.getEmail()).
							status(Status.PRECONDITION_FAILED).build();
				}
				
				orgs.add(org);
				
				String[] teams = registerUser.getTeams();
								
				Collection<SADisplayMessageEntity> userTeams = new ArrayList<SADisplayMessageEntity>();
				List<UserOrg> userOrgsTeams = new ArrayList<UserOrg>();
				List<UserOrgWorkspace> userOrgWorkspacesTeams = new ArrayList<UserOrgWorkspace>();
				if(teams != null && teams.length > 0){
					try{
						for(int i=0; i<teams.length; i++){
							//Org teamOrg = OrgDAO.getInstance().getOrganization(teams[i]);
							Org teamOrg = orgDao.getOrganization(teams[i]);
							if(teamOrg == null) {
								log.i("UserServiceImpl", "Org does not exist: " + teams[i]);
								continue;
							}
							UserOrg team = createUserOrg(teamOrg.getOrgId(), user.getUserId(), registerUser);
							if(team != null) {
								userTeams.add(team);
								userOrgsTeams.add(team);
								
								userTeams.addAll(createUserOrgWorkspace(team.getUserorgid(), false));
								userOrgWorkspacesTeams.addAll(createUserOrgWorkspaceEntities(team, false));
							}
							orgs.add(teamOrg);
						}
					}catch(Exception e){
						e.printStackTrace();
						//return FAILURE_ORG_INVALID;
						
						log.e("UserServiceImpl", "Exception while creating Team UserOrg and UserOrgWorkspace on input: " + 
								Arrays.toString(teams));
						
						return Response.ok("Exception while creating Team UserOrg and UserOrgWorkspace on input: " + 
							Arrays.toString(teams)).status(Status.PRECONDITION_FAILED).build();
					}
				}

				// TODO:SSO use the API's messaging code
				//RabbitProducer producer = new RabbitProducer(rabbituser, rabbitpass, "localhost", 5672);

				// TODO: implementing alternative DAO user creation instead of sending a registration message
				
				boolean useBus = false;
				
			if(useBus) {
				
				Map<String,Object> messageData = new HashMap<String,Object>();
				messageData.put("password", registerUser.getPassword());

				SADisplayMessage message = new SADisplayMessage();
				message.addEntity(user);
				message.addEntity(userOrg);
				message.addEntities(contacts);
				message.setMessageType("newuser");
				message.setTopic(SADisplayConstants.NEW_USER_TOPIC);
				message.setUser(registerUser.getEmail());
				//message.setIp("0.0.0.0"); // TODO: any way to get IP of requestor?
				message.setIp(NetUtil.getInstance().getExternalAddresses().iterator().next());
				message.setTime(Calendar.getInstance().getTime().toString());
				message.setVersion("1.2.3");
				message.setSeqTime(System.currentTimeMillis());
				//PhinicsDbFacade dbi = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
				//message.setSeqNum(dbi.getNextMessageSequence());
				message.setSeqNum(userSessDao.getNextMessageSeqNum());
				message.setMessageData(messageData);
				message.setReturnTopic(SADisplayConstants.NEW_USER_TOPIC + "_status");
				
				Collection<SADisplayMessageEntity> userOrgWorkspaces = 
						createUserOrgWorkspace(userOrg.getUserorgid(),	false);
				
				if(userOrgWorkspaces != null && !userOrgWorkspaces.isEmpty()) {
					message.addEntities(userOrgWorkspaces);
				}
				
				if(userTeams.size() > 0){
					message.addEntities(userTeams);
				}

				System.out.println("************" + message.toJSONString() + "**************");
				
				boolean msgStatus = MessageBusProducer.getInstance().
						sendMessage(SADisplayConstants.NEW_USER_TOPIC, message.toJSONString());				
				
				if(msgStatus) {
					APILogger.getInstance().i("UserServiceImpl", "Successfully SENT user creation message");
				} else {
					APILogger.getInstance().i("UserServiceImpl", "Failed to send creation message");
				}
				/* TODO:SSO implement 
				producer.sendMessage("amq.topic", SADisplayConstants.NEW_USER_TOPIC, message.toJSONString());
				producer.destroy();
				this.sendEmail(user, orgs);
				*/
				
				log.i("UserService", message.toJSONString());
				
			} else { // if(usebus)
				// DBI createUser creates the contacts if they're set, TODO: it gets new ids, but these should already
				// have them
				List<Contact> contactSet = createContactsList(registerUser.getEmail(), 
						registerUser.getOtherEmail(),
						registerUser.getOfficePhone(),
						registerUser.getCellPhone(), 
						registerUser.getOtherPhone(),
						registerUser.getRadioNumber(), user);
								
				
				String createUserStatus = "";
				try {
										
					List<UserOrg> userOrgs = new ArrayList<UserOrg>();
					List<UserOrgWorkspace> userOrgWorkspaces = new ArrayList<UserOrgWorkspace>();
					
					userOrgs.add(userOrg);
					if(userOrgsTeams != null && !userOrgsTeams.isEmpty()) {
						userOrgs.addAll(userOrgsTeams);
					}
					
					userOrgWorkspaces.addAll(createUserOrgWorkspaceEntities(userOrg, false));
					if(userOrgWorkspacesTeams != null && !userOrgWorkspacesTeams.isEmpty()) {
						userOrgWorkspaces.addAll(userOrgWorkspacesTeams);
					}						
					
					//UserDAO.getInstance().registerUser(user, contactSet, userOrgs, userOrgWorkspaces);
					boolean registerSuccess = userDao.registerUser(user, contactSet, userOrgs, userOrgWorkspaces);
					
					if(!registerSuccess) {
						// TODO: return with failure message... to have a meaningful response here, need to
						//			modify registerUser to return success/failure and reasons
						// throw exception? return failure response and reason here?
						throw(new Exception("UserDAO failed to persist User or related entities"));
					}
					
					String propPath = APIConfig.getInstance().getConfiguration()
							.getString("ssoToolsPropertyPath", null);
					
					log.i("SSOManagementService", "Initializing SSOUtils with property path: " + propPath);
					
					
					SSOUtil ssoUtil = null;
					boolean login = false;
					boolean created = false;
					String creationResponse = "";
					String statusMessage = "";
					if(propPath == null) {
						// TODO:SSO throw exception or set flag to return error with sso config
						// send email to admins about sso not configured
						log.w("UserServiceImpl", "Got null SSO configuration, won't be able to make SSO calls!");
					} else {
						System.setProperty("ssoToolsPropertyPath", propPath);
						System.setProperty("openamPropertiesPath", propPath);
						
						ssoUtil = new SSOUtil();
						login = ssoUtil.loginAsAdmin();
						
						if(login) {
							creationResponse = ssoUtil.createUser(user.getUsername(), registerUser.getPassword(),
									user.getFirstname(), user.getLastname(), false);
							
							if(creationResponse != null) {
								JSONObject response = null; 
										
								try {
									response = new JSONObject(creationResponse);
									
									String status = response.optString("status", null);
									statusMessage = response.optString("message", null);
									
									if(status.equals("success")) {
										created = true;
									}
									
								} catch(JSONException e) {
									// can't read response, assume failure
								}
							}
						}
					}
					
					if(!login || !created) {
						successMessage += " (but not identity: " + statusMessage + "): ";
					}
					
				} catch(DataAccessException e) { //catch(ICSDatastoreException e) {
					System.out.println("Exception persisting user: " + e.getMessage());
					log.e("UserServiceImpl", "Exception creating user: " + e.getMessage());
					return Response.ok("Failed to register user: " + e.getMessage()).status(Status.INTERNAL_SERVER_ERROR).build();
				} catch(Exception e) {
					System.out.println("Exception persisting user: " + e.getMessage());
					log.e("UserServiceImpl", "Exception creating user: " + e.getMessage());
					return Response.ok("Failed to register user: " + e.getMessage()).status(Status.INTERNAL_SERVER_ERROR).build();
				}
				
			}
			//} // end if valid
		}catch(Exception e){
			e.printStackTrace();
			return Response.ok(FAILURE).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		//return makeUnsupportedOpRequestResponse();
		return Response.ok(successMessage + registerUser.getEmail()).status(Status.OK).build();
	}
	
	
	/**
	 * Creates UserOrgWorkspace SADisplayMessageEntity objects
	 * for every WorkspaceID in the database
	 * 
	 * <p>Use {@link #createUserOrgWorkspaceEntities(UserOrg, boolean)} instead, for use with DAO, not message bus</p>
	 *  
	 * @param userorgid The ID of the {@link UserOrg} to create workspace entries for
	 * @param enabled Whether or not the {@link UserOrgWorkspace} is enabled or not 
	 * @return A collection of {@link SADisplayMessageEntity} containing {@link UserOrgWorkspace} entities
	 */
	@Deprecated()
	private Collection<SADisplayMessageEntity> createUserOrgWorkspace(int userorgid, boolean enabled) {		
		
		List<SADisplayMessageEntity> workspaces = new ArrayList<SADisplayMessageEntity>();
		
		List<Integer> workspaceIds = getWorkspaceIds();
		if(workspaceIds == null || workspaceIds.isEmpty()) {
			return workspaces; // TODO: caller needs to know an empty one may mean an error occurred, not just
								// that there are no workspaces
		}
		
		for(Integer id : workspaceIds){
			UserOrgWorkspace workspace = new UserOrgWorkspace();
			workspace.setUserorgid(userorgid);
			workspace.setWorkspaceid(id);
			workspace.setEnabled(enabled);
			
			workspaces.add(workspace);
		}
		
		return workspaces;
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
	
	
	/**
	 * Creates a collection of SADisplayMessageEntity objects that
	 * contain Contact entities for each of the specified values
	 * 
	 * @param email
	 * @param otherEmail
	 * @param officePhone
	 * @param cellPhone
	 * @param otherPhone
	 * @param radioNumber
	 * @param user
	 * @return
	 */
	@Deprecated
	private Collection<SADisplayMessageEntity> createContacts(String email,
			String otherEmail, String officePhone, String cellPhone,
			String otherPhone, String radioNumber,
			edu.mit.ll.nics.common.entity.User user) {
		
		List<SADisplayMessageEntity> contacts = new ArrayList<SADisplayMessageEntity>(0);
		
		if(email != null && email != ""){
			contacts.add(this.createContact(SADisplayConstants.EMAIL_TYPE, email, user));
		}
		if(otherEmail != null && otherEmail != ""){
			contacts.add(this.createContact(SADisplayConstants.EMAIL_TYPE, otherEmail, user));
		}
		if(cellPhone != null && cellPhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_CELL_TYPE, cellPhone, user));
		}
		if(officePhone != null && officePhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_OFFICE_TYPE, officePhone, user));
		}
		if(otherPhone != null && otherPhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_OTHER_TYPE, otherPhone, user));
		}
		if(radioNumber != null && radioNumber != ""){
			contacts.add(this.createContact(SADisplayConstants.RADIO_NUMBER_TYPE, radioNumber, user));
		}
		return contacts;
		
	}
	
	
	/**
	 * Creates a list of {@link Contact} objects for the specified values
	 * 
	 * @param email
	 * @param otherEmail
	 * @param officePhone
	 * @param cellPhone
	 * @param otherPhone
	 * @param radioNumber
	 * @param user The user the contact info is associated with
	 * @return A list of {@link Contact} objects
	 */
	private List<Contact> createContactsList(String email,
			String otherEmail, String officePhone, String cellPhone,
			String otherPhone, String radioNumber,
			edu.mit.ll.nics.common.entity.User user) {
		
		List<Contact> contacts = new ArrayList<Contact>();
		
		if(email != null && email != ""){
			contacts.add(this.createContact(SADisplayConstants.EMAIL_TYPE, email, user));
		}
		if(otherEmail != null && otherEmail != ""){
			contacts.add(this.createContact(SADisplayConstants.EMAIL_TYPE, otherEmail, user));
		}
		if(cellPhone != null && cellPhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_CELL_TYPE, cellPhone, user));
		}
		if(officePhone != null && officePhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_OFFICE_TYPE, officePhone, user));
		}
		if(otherPhone != null && otherPhone != ""){
			contacts.add(this.createContact(SADisplayConstants.PHONE_OTHER_TYPE, otherPhone, user));
		}
		if(radioNumber != null && radioNumber != ""){
			contacts.add(this.createContact(SADisplayConstants.RADIO_NUMBER_TYPE, radioNumber, user));
		}
		return contacts;
		
	}
	
	
	/**
	 * Utility method for creating a {@link Contact}
	 * 
	 * @param type The {@link ContactType}
	 * @param value The value for this Contact
	 * @param user The {@link User} this Contact is associated with
	 * @return A {@link Contact} of the specified type and value
	 */
	private Contact createContact(String type, String value, edu.mit.ll.nics.common.entity.User user) {
		Contact contact = null;
		try {
			//int contactTypeId = UserDAO.getInstance().getContactTypeId(type);
			int contactTypeId = userDao.getContactTypeId(type);
			contact = new Contact();
			contact.setContacttypeid(contactTypeId);
			contact.setContacttype(new ContactType(contactTypeId, type));
			contact.setValue(value);
			contact.setCreated(Calendar.getInstance().getTime());
			contact.setUserid(user.getUserId());
			contact.setEnabled(true);
			//contact.setContactid(UserDAO.getInstance().getNextContactId()); // TODO:ID
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception creating contact of type: " + type);
		}
		return contact;
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
		User responseUser = new User();
		UserResponse userResponse = new UserResponse();
		
		int systemRoleId = userOrgDao.getSystemRoleId(requestingUser, userOrgWorkspaceId);
		if(systemRoleId == SADisplayConstants.ADMIN_ROLE_ID ||
				systemRoleId == SADisplayConstants.SUPER_ROLE_ID){
			
			int count = userOrgDao.setUserOrgEnabled(userOrgWorkspaceId, enabled);
			
			responseUser.setUserId(userId);
			if(count == 1){
				if(!enabled && !userOrgDao.hasEnabledOrgs(userId, workspaceId)){
					userDao.setUserEnabled(userId, false);
					responseUser.setEnabled(false);
				}else if(enabled){
					userDao.setUserEnabled(userId, true);
					responseUser.setEnabled(true);
				}
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
			int workspaceId, String orgName, String requestingUser){
		Response response = null;
		UserProfileResponse profileResponse = new UserProfileResponse();
		
		if(!username.equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		try{
			
			OrgDAOImpl orgDao = new OrgDAOImpl();
			Org org = orgDao.getOrganization(orgName); 
			edu.mit.ll.nics.common.entity.User user = userDao.getUser(username);
			
			UserOrg userOrg = userOrgDao.getUserOrgById(org.getOrgId(), user.getUserId(), workspaceId);
			
			IncidentDAOImpl incidentDao = new IncidentDAOImpl();
			
			profileResponse.setIncidentTypes(incidentDao.getIncidentTypes());
			profileResponse.setUserOrgId(userOrgId);
			profileResponse.setUsername(username);
			profileResponse.setOrgName(orgName);
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
	
	public Response postUserProfile(edu.mit.ll.em.api.rs.User user, String requestingUser){
		
		Response response = null;
		UserProfileResponse profileResponse = new UserProfileResponse();
		SSOUtil ssoUtil = null;
		String propPath = APIConfig.getInstance().getConfiguration().getString("ssoToolsPropertyPath", null);
		
		//Only the user can update his/her profile - for now
		if(!userDao.getAllUserInfoById(user.getUserId()).getUsername().equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(
					Status.FORBIDDEN.getReasonPhrase()).build();
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

						String newPWHash = generateSaltedHash(user.getNewPw(),user.getUserName());
						
						updatedProfile =  userDao.updateUserPW(dbUser.getUserId(), newPWHash);
						
						if(updatedProfile){
							
							userDao.updateNames(user.getUserId(),user.getFirstName(),user.getLastName());
							userOrgDao.updateUserOrg(user.getUserOrgId(),user.getJobTitle(),user.getRank(),user.getJobDesc());
						
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
				userOrgDao.updateUserOrg(user.getUserOrgId(),user.getJobTitle(),user.getRank(),user.getJobDesc());
				
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
					profileResponse.setMessage("Failed to update user profile");
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
			OrgDAOImpl orgDao = new OrgDAOImpl();
			List<Map<String, Object>> userOrgs = orgDao.getUserOrgsWithOrgName(Integer.parseInt(""+userId), workspaceId);
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
		
		//PhinicsDbFacade dbi =
			//	PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
		
		//int userid = dbi.getNextUserId();
		int userid = userDao.getNextUserId();
		user.setFirstname(rUser.getFirstName());
		user.setLastname(rUser.getLastName());
		user.setUsername(rUser.getEmail());
		user.setUserId(userid);
		user.setPasswordHash(generateSaltedHash(rUser.getPassword(), rUser.getEmail()));
		user.setEnabled(false);
		user.setActive(true);
		user.setLastupdated(Calendar.getInstance().getTime());
		user.setCreated(Calendar.getInstance().getTime());
		user.setPasswordchanged(Calendar.getInstance().getTime());
		
		return user;
	}
	
	
	/**
	 * Taken from org.jboss.seam.security.management.PasswordHash, and edited
	 * to use Apache commons Base64
	 * 
	 * @param password
	 * @param saltPhrase
	 * @return
	 */
	public String generateSaltedHash(String password, String saltPhrase)
	{
		try {        
			MessageDigest md = MessageDigest.getInstance("sha");

			if (saltPhrase != null)
			{
				md.update(saltPhrase.getBytes());
				byte[] salt = md.digest();

				md.reset();
				md.update(password.getBytes());
				md.update(salt);
			}
			else
			{
				md.update(password.getBytes());
			}

			byte[] raw = md.digest();
			return Base64.encodeBase64String(raw);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);        
		} 
	}
	
	private String validateRegisterUser(RegisterUser user){
			
		
		if(user.getFirstName() == null || StringUtils.isEmpty(user.getFirstName()) ||
				user.getLastName() == null || StringUtils.isEmpty(user.getLastName())){
			return FAILURE_NAMES;
		}

		if(user.getEmail() == null || StringUtils.isEmpty(user.getEmail()) ||
				!EntityEncoder.validateEmailAddress(user.getEmail())) {			
			return FAILURE_EMAIL;
		}

		if((user.getOtherEmail() != null && !StringUtils.isEmpty(user.getOtherEmail()) && 
				!EntityEncoder.validateEmailAddress(user.getOtherEmail()))
				){
			return FAILURE_EMAIL;
		}

		if(!UserInfoValidator.validateUsername(user.getEmail()) || 
				!EntityEncoder.validateEmailAddress(user.getEmail())){
			return FAILURE_USERNAME_INVALID;
		}
		
		//PhinicsDbFacade dbi =
			//	PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
		
		
		try {
			//if(UserDAO.getInstance().getNicsUserByUsername(user.getEmail()) != null) {
			if(userDao.getUser(user.getEmail()) != null) {
				return FAILURE_USERNAME;
			}
		} catch (DataAccessException e) {			
			e.printStackTrace();
			return FAILURE_USERNAME;
		}

		boolean verified = (user.getConfirmPassword() != null && 
				user.getConfirmPassword().equals(user.getPassword()));

		if (!verified) {
			return FAILURE_PASSWORDS;
		}

		String valid = UserInfoValidator.validatePassword(user.getPassword());
		if(!valid.equals(UserInfoValidator.SUCCESS)){
			return valid;
		}

		if(!UserInfoValidator.validatePhoneNumbers(
				user.getCellPhone(), user.getOtherPhone(), user.getOfficePhone())){
			return FAILURE_PHONE_NUMBERS + String.format(": %s, %s, %s", user.getCellPhone(), user.getOtherPhone(), 
					user.getOfficePhone());
		}
		
		
		/* Also need to validate these
		user.getDescription();
		user.getJobTitle();
		user.getOrganization();
		user.getOtherInfo();
		user.getRadioNumber();
		user.getRank();
		user.getTeams();
		*/
		
				
		if(user.getDescription() != null && !user.getDescription().isEmpty() 
				&& !EntityEncoder.validateInputValue(user.getDescription())) {
			return "Invalid input in Description field";
		}
		
		if(user.getJobTitle() != null && !user.getJobTitle().isEmpty() 
				&& !EntityEncoder.validateInputValue(user.getJobTitle())) {
			return "Invalid input in Job Title field";
		}
		
		if(user.getOrganization() != null && !user.getOrganization().isEmpty()
				&& !EntityEncoder.validateInputValue(user.getOrganization())) {
			return "Invalid input in Organization";
		}
		
		if(user.getOtherInfo() != null && !user.getOtherInfo().isEmpty()
				&& !EntityEncoder.validateInputValue(user.getOtherInfo())) {
			return "Invalid input in Other Info field";
		}
		
		if(user.getRadioNumber() != null && !user.getRadioNumber().isEmpty()
				&& !EntityEncoder.validateInputValue(user.getRadioNumber())) {
			return "Invalid input in Radio Number field";
		}
		
		if(user.getRank() != null && !user.getRank().isEmpty() 
				&& !EntityEncoder.validateInputValue(user.getRank())) {
			return "Invalid input in Rank field";
		}
		
		String[] teams = user.getTeams();
		if(teams != null && teams.length > 0) {
			for(String team : teams) {
				if(team != null && !team.isEmpty()
						&& !EntityEncoder.validateInputValue(team)) {
					return "Invalid input in one of the IMT fields: " + team;
				}
			}
		}
		
		
		/* Moving to where we're working with the NICS user
		try{
			this.setOrg(orgDao.getOrganization(user.getOrganization()));
		}catch(Exception e){
			return FAILURE_ORG_INVALID;
		}*/
		return SUCCESS;
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
	public UserOrg createUserOrg(int orgid, int userid, RegisterUser user) throws Exception {
		SystemRoleDAOImpl sysRoleDao = new SystemRoleDAOImpl();		
		//int systemroleid = SystemRoleDAO.getInstance().getSystemRoleId(SADisplayConstants.USER_ROLE);
		int systemroleid = sysRoleDao.getSystemRoleId(SADisplayConstants.USER_ROLE);
		
		if(systemroleid == -1) {
			// failed to get role id...
		}
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
		userorg.setSystemroleid(systemroleid);
		userorg.setUserid(userid);
		
		userorg.setJobTitle(user.getJobTitle());
		userorg.setDescription(user.getDescription());
		userorg.setRank(user.getRank());
		
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
			if(userSessDao.hasCurrentUserSession(workspaceId, (int)userId)){
				long currentUserSessionId = this.userSessDao.getCurrentUserSessionid(userId);
				
				if(this.userSessDao.removeUserSession(currentUserSessionId)){
					this.notifyLogout(currentUserSessionId);
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
	
	private void notifyLogout(long currentUserSessionId) throws IOException {
		String topic = String.format("iweb.NICS.logout.%d", currentUserSessionId);
		getRabbitProducer().produce(topic, "logout");
	}
	
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer();
		}
		return rabbitProducer;
	}
	
	public Response removeUserSession(long currentUserSessionId){
		Response response = null;
		UserResponse  userResponse = new UserResponse();
		
		try{
			if(userSessDao.removeUserSession(currentUserSessionId)){
				userResponse.setCount(1);
				userResponse.setMessage(Status.OK.getReasonPhrase());
				response = Response.ok(userResponse).status(Status.OK).build();
			}
			
			notifyLogout(currentUserSessionId);
			
		} catch(Exception e) {
			APILogger.getInstance().e("UserServiceImpl", "Exception creating UserSession");
			response = Response.ok(userResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
			
		
		return response;
		
	}
		
}
