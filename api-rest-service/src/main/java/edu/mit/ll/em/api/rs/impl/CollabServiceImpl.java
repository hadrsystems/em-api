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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.exception.DuplicateCollabRoomException;
import edu.mit.ll.em.api.rs.CollabRoomPermissionResponse;
import edu.mit.ll.em.api.rs.CollabService;
import edu.mit.ll.em.api.rs.CollabServiceResponse;
import edu.mit.ll.em.api.rs.CollabPresenceStatus;
import edu.mit.ll.em.api.rs.FieldMapResponse;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.OrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;

/**
 * 
 *
 */
public class CollabServiceImpl implements CollabService {

	/** CollabRoom DAO */
	private static final CollabRoomDAOImpl collabDao = new CollabRoomDAOImpl();
	
	/** User DAO */
	private static final UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	private static final UserDAOImpl userDao = new UserDAOImpl();
	private static final OrgDAOImpl orgDao = new OrgDAOImpl();
	private static final IncidentDAOImpl incidentDao = new IncidentDAOImpl();
	
	private static final Log logger = LogFactory.getLog(CollabServiceImpl.class);
	
	private RabbitPubSubProducer rabbitProducer;
	
	private static final int EXPECTED_ROOMS = 20;
	private static final int EXPECTED_PARTICIPANTS = 20;
	private static final String SECURE_ROOMS_ERROR = "One or more users failed to be added to the collaboration room";
	private ConcurrentMap<Integer, ConcurrentMap<String, CollabPresenceStatus>> statuses =
			new ConcurrentHashMap<Integer, ConcurrentMap<String, CollabPresenceStatus>>(EXPECTED_ROOMS);
	
	
	
	/**
	 * Gets collab rooms accessible by the specified user on the specified incident
	 * 
	 * @param incidentId
	 * @param userId
	 * 
	 * @return Response 
	 * @see CollabServiceResponse
	 */
	public Response getCollabRoom(int incidentId, Integer userId, String username) {
		String incidentMap = APIConfig.getInstance().getConfiguration().getString(
				APIConfig.INCIDENT_MAP, SADisplayConstants.INCIDENT_MAP);
				
		Response response = null;
		CollabServiceResponse collabResponse = new CollabServiceResponse();
		
		if(!userDao.validateUser(username, userId)){
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		try {
			List<Integer> adminRooms = new ArrayList<Integer>();
			List<CollabRoom> secureRooms = collabDao.getSecuredRooms(userId, incidentId, incidentMap);
			
			List<CollabRoom> collabRooms = collabDao.getAccessibleCollabRooms(userId, incidentId, incidentMap);
			
			for(CollabRoom room : secureRooms){
				if(room.getName().equalsIgnoreCase(incidentMap)){
					int roleId = collabDao.getCollabRoomSystemRole(room.getCollabRoomId(), userId);
					if(roleId == SADisplayConstants.USER_ROLE_ID){
						room.setReadWriteUsers(Arrays.asList(userId));
					}else if(roleId == SADisplayConstants.ADMIN_ROLE_ID){
						room.setAdminUsers(Arrays.asList(userId));
						adminRooms.add(room.getCollabRoomId());
					}
					room.setIncidentMapAdmins(incidentDao.getIncidentMapAdmins(incidentId, incidentMap));
					collabRooms.add(0, room);
				}else{
					if(collabDao.getCollabRoomSystemRole(room.getCollabRoomId(), userId) ==
						SADisplayConstants.ADMIN_ROLE_ID){
						adminRooms.add(room.getCollabRoomId());
					}
					collabRooms.add(room);
				}
			}
			
			collabResponse.setAdminRooms(adminRooms);
			collabResponse.setMessage(Status.OK.getReasonPhrase());
			collabResponse.setResults(collabRooms);
			collabResponse.setCount(collabRooms.size());
			response = Response.ok(collabResponse).status(Status.OK).build();
		} catch (DataAccessException e) {
			collabResponse.setMessage("Data Access error: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			collabResponse.setMessage("Unhandled exception: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
	
	//Not an endpoint. Method used by other EM-API classes to create a room
	public Response createCollabRoomWithPermissions(int incidentId, int orgId, int workspaceId, CollabRoom collabroom){
		return this.postCollabRoomWithPermissions(incidentId, orgId, workspaceId, collabroom);
	}
	
	//Not an endpoint. Method used by other EM-API classes to create a room
	public CollabRoom createUnsecureCollabRoom(int incidentId, CollabRoom collabroom) throws DataAccessException, DuplicateCollabRoomException, Exception{
		return this.createCollabRoom(incidentId, collabroom);
	}

	public Response postCollabRoom(int userOrgId, int orgId, 
			int workspaceId, int incidentId, CollabRoom collabroom, String username) {
		Response response = null;
		CollabServiceResponse collabResponse = new CollabServiceResponse();
		CollabRoom newCollabRoom = null;
		
		if(collabroom.getAdminUsers() != null &&
				collabroom.getAdminUsers().size() > 0){
			//User the username attached to the token to find the system role Id
			int systemRoleId = userOrgDao.getSystemRoleIdForUserOrg(username, userOrgId);
			if(systemRoleId == SADisplayConstants.ADMIN_ROLE_ID ||
					systemRoleId == SADisplayConstants.SUPER_ROLE_ID){
				return this.postCollabRoomWithPermissions(incidentId, orgId, workspaceId, collabroom);
			}else{
				collabResponse.setMessage("Access Denied. User does not have permission to secure a room");
				response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
				return response;
			}
		}
		
		try {
			newCollabRoom = this.createCollabRoom(incidentId, collabroom);
			
			collabResponse.setMessage(Status.OK.getReasonPhrase());
			collabResponse.setResults(Arrays.asList(newCollabRoom));
			collabResponse.setCount(1);
			response = Response.ok(collabResponse).status(Status.OK).build();
		} catch (DataAccessException e) {
			collabResponse.setMessage("Data Access error: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		} catch(DuplicateCollabRoomException dupe){
			collabResponse.setMessage(dupe.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			collabResponse.setMessage("Unhandled exception: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyChange(newCollabRoom);
			} catch (IOException e) {
				logger.error("Failed to publish CollabServiceImpl collabroom event", e);
			}
		}
		
		return response;
	}
	
	public Response postCollabRoomWithPermissions(int incidentId, int orgId, int workspaceId, CollabRoom collabroom) {
		
		Response response = null;
		CollabRoom newCollabRoom = null;
		CollabRoomPermissionResponse collabResponse = new CollabRoomPermissionResponse();
		try {
			newCollabRoom = this.createCollabRoom(incidentId, collabroom);
			
			collabResponse =
					this.secureRoom(newCollabRoom.getCollabRoomId(), 
							orgId, workspaceId,
							collabroom.getAdminUsers(), collabroom.getReadWriteUsers());
			
			newCollabRoom.setAdminUsers(collabResponse.getAdminUsers());
			newCollabRoom.setReadWriteUsers(collabResponse.getReadWriteUsers());
			
			collabResponse.setMessage(Status.OK.getReasonPhrase());
			collabResponse.setResults(Arrays.asList(newCollabRoom));
			response = Response.ok(collabResponse).status(Status.OK).build();
		} catch (DataAccessException e) {
			collabResponse.setMessage("Data Access error: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		} catch(DuplicateCollabRoomException dupe){
			collabResponse.setMessage(dupe.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			collabResponse.setMessage("Unhandled exception: " + e.getMessage());
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyChange(newCollabRoom);
			} catch (IOException e) {
				logger.error("Failed to publish CollabServiceImpl collabroom event", e);
			}
		}
		
		return response;
	}
	

	@Override
	public Response getCollabRoomPresence(int incidentId, int collabroomId) {
		CollabServiceResponse collabResponse = new CollabServiceResponse();
		
		Date idleTime = getIdleTime();
		Date missingTime = getMissingTime();
		
		ConcurrentMap<String, CollabPresenceStatus> members = statuses.get(collabroomId);
		
		Collection<CollabPresenceStatus> userStatuses = Collections.<CollabPresenceStatus>emptyList();
		if (members != null) {
			userStatuses = members.values(); 
		}
		
		Iterator<CollabPresenceStatus> i = userStatuses.iterator();
		while(i.hasNext()) {
			CollabPresenceStatus status = i.next();
			if (status.getTimestamp().before(missingTime)) {
				i.remove();
			} else if (status.getTimestamp().before(idleTime)) {
				status.setStatus(CollabPresenceStatus.Status.IDLE);
			}
		}
		
		collabResponse.setResults(userStatuses);
		collabResponse.setCount(userStatuses.size());
		collabResponse.setMessage(Status.OK.getReasonPhrase());
		return Response.ok(collabResponse).status(Status.OK).build();
	}


	@Override
	public Response postCollabRoomPresence(int incidentId, int collabroomId, CollabPresenceStatus status) {
		
		if (null == status.getStatus()) {
			status.setStatus(CollabPresenceStatus.Status.ACTIVE);
		}
		status.setTimestamp(new Date());
		
		
		ConcurrentMap<String, CollabPresenceStatus> members = statuses.get(collabroomId);
		if (members == null) {
			members = new ConcurrentHashMap<String, CollabPresenceStatus>(EXPECTED_PARTICIPANTS);
			
			ConcurrentMap<String, CollabPresenceStatus> previous = statuses.putIfAbsent(collabroomId, members);
			if (previous != null) {
				members = previous;
			}
		}
		
		CollabPresenceStatus oldStatus = null;
		if (CollabPresenceStatus.Status.LEAVING.equals(status.getStatus())) {
			oldStatus = members.remove(status.getUsername());
		} else {
			oldStatus = members.put(status.getUsername(), status);
		}
		
		//fire presence change on new users and changes
		if (oldStatus == null || !status.getStatus().equals(oldStatus.getStatus()) ) {
			try {
				notifyChange(incidentId, collabroomId, status);
			} catch (IOException e) {
				logger.error("Failed to publish CollabServiceImpl presence event", e);
			}
		}
		
		return getCollabRoomPresence(incidentId, collabroomId);
	}
	
	public Response validateSubsription(int collabRoomId, String username){
		String incidentMap = APIConfig.getInstance().getConfiguration().getString(
				APIConfig.INCIDENT_MAP, SADisplayConstants.INCIDENT_MAP);
		
		Response response = null;
		CollabRoomPermissionResponse collabResponse = new CollabRoomPermissionResponse();
		long userId = userDao.getUserId(username);
		
		//verify the user has permissions
		if(collabDao.hasPermissions(userId, collabRoomId, incidentMap)){ //Everyone can susbscribe to the incidentmap
			response = Response.ok(collabResponse).status(Status.OK).build();
		}else{
			response = Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		return response;
	}
	
	public Response updateCollabRoomPermission(FieldMapResponse secureUsers, int collabRoomId, long userId, 
			int orgId, int workspaceId, String username){
		if(userDao.getUserId(username) != userId){
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		//TODO: Check for permissions
		
		//Remove current security if there is any...
		collabDao.unsecureRoom(collabRoomId);
		List<Integer> adminUsers = (List<Integer>) secureUsers.getData().get(0).get("admin");
		List<Integer> readWriteUsers = (List<Integer>) secureUsers.getData().get(0).get("readWrite");
		
		CollabRoomPermissionResponse collabResponse = this.secureRoom(
				collabRoomId, orgId, workspaceId, adminUsers, readWriteUsers);
		
		try {
			CollabRoom room = collabDao.getCollabRoomById(collabRoomId);
			room.setAdminUsers(collabResponse.getAdminUsers());
			room.setReadWriteUsers(collabResponse.getReadWriteUsers());
			notifyUpdateChange(room);
		} catch (IOException e) {
			logger.error("Failed to publish CollabServiceImpl collabroom event", e);
		} catch (DataAccessException e) {
			logger.error("Failed to publish CollabServiceImpl collabroom event", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Failed to publish CollabServiceImpl collabroom event", e);
			e.printStackTrace();
		}
		
		return Response.ok(collabResponse).status(Status.OK).build();
	}
	
	public Response unsecureRoom(long collabRoomId, long userId, String username){
		Response response = null;
		CollabRoomPermissionResponse collabResponse = new CollabRoomPermissionResponse();
		
		if(userDao.getUserId(username) != userId){
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		//verify the user has permissions
		if(collabDao.hasPermissions(userId, collabRoomId)){
			if(collabDao.unsecureRoom(collabRoomId)){
				collabResponse.setMessage(Status.OK.getReasonPhrase());
				response = Response.ok(collabResponse).status(Status.OK).build();
			}else{
				collabResponse.setMessage("There was an error unsecuring the room");
				response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}else{
			collabResponse.setMessage("There was an error unsecuring the room");
			response = Response.ok(collabResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyUpdateChange(collabDao.getCollabRoomById(collabRoomId));
			} catch (IOException e) {
				logger.error("Failed to publish CollabServiceImpl collabroom event", e);
			} catch (DataAccessException e) {
				logger.error("Failed to publish CollabServiceImpl collabroom event", e);
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("Failed to publish CollabServiceImpl collabroom event", e);
				e.printStackTrace();
			}
		}
		return response; 
	}
	
	private CollabRoomPermissionResponse secureRoom(int collabRoomId, int orgId, int workspaceId,
			Collection<Integer> adminUsers, Collection <Integer> readWriteUsers){
		CollabRoomPermissionResponse collabResponse = new CollabRoomPermissionResponse();
		Collection<Integer> adminIds = userOrgDao.getSuperUsers();
		
		//Remove duplicates
		adminUsers.removeAll(adminIds);
		//Add Super users back
		adminUsers.addAll(adminIds);
		try{
			for(Integer userId : adminUsers){
				if(!collabDao.secureRoom(collabRoomId, userId, SADisplayConstants.ADMIN_ROLE_ID)){
					collabResponse.addFailedAdmin(userId);
				}else{
					collabResponse.addAdminUser(userId);
				}
			}
			if(readWriteUsers !=null){
				for(Integer userId : readWriteUsers){
					if(!collabDao.secureRoom(collabRoomId, userId, SADisplayConstants.USER_ROLE_ID)){
						collabResponse.addFailedReadWrite(userId);
					}else{
						collabResponse.addReadWriteUser(userId);
					}
				}
			}
			if(collabResponse.getFailedAdmin().size() > 0 ||
					collabResponse.getFailedReadWrite().size() > 0){
				collabResponse.setMessage(SECURE_ROOMS_ERROR);
			}else{
				collabResponse.setMessage(Status.OK.getReasonPhrase());
			}
		}catch(Exception e){
			collabResponse.setMessage("Unhandled exception: " + e.getMessage());
		}
		return collabResponse;
	}
	
	public Response getCollabRoomSecureUsers(int collabRoomId, String username){
		Response response = null;
		
		if(collabDao.hasPermissions(userDao.getUserId(username), collabRoomId)){
			FieldMapResponse dataResponse = new FieldMapResponse();
	        dataResponse.setData(collabDao.getCollabRoomSecureUsers(collabRoomId));
			
			dataResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(dataResponse).status(Status.OK).build();
		}else{
			response = Response.ok(Status.FORBIDDEN.toString()).status(Status.OK).build();
		}

		return response;
	}
	
	public Response getCollabRoomUnSecureUsers(int collabRoomId, int orgId, 
			int workspaceId, String username){
		Response response = null;
		FieldMapResponse dataResponse = new FieldMapResponse();
        
		if(collabRoomId != -1 && collabDao.hasPermissions(userDao.getUserId(username), collabRoomId)){
			dataResponse.setData(collabDao.getUsersWithoutPermission(collabRoomId, orgId, workspaceId));
			
			dataResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(dataResponse).status(Status.OK).build();
		}else if(collabRoomId == -1){
			dataResponse.setData(userDao.getUsers(orgId, workspaceId));
			
			dataResponse.setMessage(Status.OK.getReasonPhrase());
			response = Response.ok(dataResponse).status(Status.OK).build();
		}else{
			response = Response.ok(Status.FORBIDDEN.toString()).status(Status.OK).build();
		}

		return response;
	} 
	
	private CollabRoom createCollabRoom(int incidentId, CollabRoom collabroom) 
			throws DataAccessException, DuplicateCollabRoomException, Exception{
		
		if(collabDao.hasRoomNamed(incidentId, collabroom.getName())){
			throw new DuplicateCollabRoomException(collabroom.getName());
		}	
		// set/override incidentId if set
		collabroom.setIncidentid(incidentId);
		collabroom.setCreated(new Date());
		int newCollabId =  collabDao.create(collabroom);
		return collabDao.getCollabRoomById(newCollabId);
	}
	
	private Date getIdleTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -5);
		return cal.getTime();
	}
	
	private Date getMissingTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -15);
		return cal.getTime();
	}
	
	private void notifyChange(CollabRoom collabroom) throws IOException {
		if (collabroom != null) {
			String topic = String.format("iweb.NICS.incident.%s.newcollabroom", collabroom.getIncidentid());
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(collabroom);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private void notifyUpdateChange(CollabRoom collabroom) throws IOException {
		if (collabroom != null) {
			String topic = String.format("iweb.NICS.incident.%s.updatedcollabroom", collabroom.getIncidentid());
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(collabroom);
			getRabbitProducer().produce(topic, message);
		}
	}

	private void notifyChange(int incidentId, int collabroomId, CollabPresenceStatus status) throws IOException {
		if (status != null) {
			String topic = String.format("iweb.NICS.collabroom.%s.presence", collabroomId);
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(status);
			getRabbitProducer().produce(topic, message);
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
}

