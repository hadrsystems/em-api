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
package edu.mit.ll.em.api.dataaccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.ll.em.api.rs.Incident;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.TimeUtil;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.entity.IncidentType;


public class IncidentDAO extends BaseDAO {

	private static final String CNAME = IncidentDAO.class.getName();
	//private PhinicsDbFacade dbf = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static IncidentDAO instance = new IncidentDAO();
	}

	public static IncidentDAO getInstance() {
		return Holder.instance;
	}

	public IncidentDAO() {

	}

/*
	public Incident createIncident(Incident incident) throws ICSDatastoreException {
		if (incident == null) {
			throw new NullPointerException(CNAME + ":createIncident called with null incident argument");
		}

		edu.mit.ll.em.common.entity.Incident e = nicsIncidentFromPapiIncident(incident);
		try {
			dbf.createIncident(e);
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to create incident: " + e1.getMessage());
		}
		incident.setIncidentId(e.getIncidentid());
		return incident;
	}	

	public Set<Integer> getAllIncidentIds() {
		// TODO: Needs implementation.
		throw new UnsupportedOperationException("getAllIncidentIds()");
	}
	
	public List<IncidentType> getIncientTypes()throws ICSDatastoreException {
		try{
				return dbf.getIncidentTypes();
		}catch (PhinicsDbException e) {
			throw new ICSDatastoreException("Unable to read incident types: " +
					e.getMessage());
		}
	}

	public List<edu.mit.ll.em.api.rs.Incident> getAllIncidents(
			Integer workspaceId,
			Integer userId)
					throws ICSDatastoreException {
		List<edu.mit.ll.em.api.rs.Incident> papiIncidents = null;
		List<edu.mit.ll.em.common.entity.Incident> dbIncidents = null;

		try {
			dbIncidents = dbf.readAllIncidents(workspaceId);
			if (dbIncidents.size() > 0) {
				//Map<Integer, Set<Integer>> blockedRoomIds = getBlockedRoomIds(accessibleByUserId);
				papiIncidents = new ArrayList<edu.mit.ll.em.api.rs.Incident>
				(dbIncidents.size());
				for (edu.mit.ll.em.common.entity.Incident dbIncident : dbIncidents) {
					edu.mit.ll.em.api.rs.Incident papiIncident =
							papiIncidentFromNicsIncident(dbIncident,
									userId);
					papiIncidents.add(papiIncident);
				}
			} else {
				papiIncidents = new ArrayList<edu.mit.ll.em.api.rs.Incident>
				();			
			}			
		} catch (PhinicsDbException e) {
			throw new ICSDatastoreException("Unable to read all incidents: " +
					e.getMessage());
		}

		return papiIncidents;
	}	
	
	public edu.mit.ll.em.common.entity.Incident getIncidentById(int id) throws ICSDatastoreException {
		edu.mit.ll.em.common.entity.Incident incident = null;
		try {
			incident = dbf.readIncident(id);
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to read incident: " + e1.getMessage());
		}
		return incident;
	}

	public Incident getIncidentById(Integer workspaceId, int id) throws ICSDatastoreException {
		edu.mit.ll.em.common.entity.Incident e = null;
		Incident phinicsIncident = null;
		try {
			// Incident IDs are table keys so workspaceIds are irrelevant when
			// reading an incident by its Id.
			// However, to avoid confusing the caller, if the supplied
			// workspaceId is not associated with the incidentId, do not
			// return the incident.
			e = dbf.readIncident(id);
			if (!e.getWorkspaceid().equals(workspaceId)) {
				e = null;
			}
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to read incident: " + e1.getMessage());
		}
		if (e != null) {
			phinicsIncident = papiIncidentFromNicsIncident(e);
		} else {
			PAPILogger.getInstance().w(CNAME, "Incident with ID " + id + 
					" was not found in workspaceId " + workspaceId + ".");
		}
		return phinicsIncident;
	}

	public void removeIncident(int id) throws ICSDatastoreException {
		try {
			dbf.deleteIncident(id);
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to remove incident: " + e1.getMessage());
		}		
	}

	public Incident updateIncident(int incidentId, Incident other) throws ICSDatastoreException {
		edu.mit.ll.em.common.entity.Incident e = null;
		try {
			// This is necessary because the Phacade is only exposed to
			// edu.mit.ll.em.common.entity.Incident, as it should, and
			// the Incident class has data members that do not allow for
			// null values. The cleanest way around that limitation is to
			// read:
			// 1. Read the most current state from the DB.
			// 2. Apply the incoming changes.
			// 3. Pass the merged Incident to the Phacade for storage.
			// Unfortunately, Hibernate requires that the Phacade do a read
			// of the Incident inside the same transaction that is updating
			// and hence this read here is an extra, yet necessary read.
			// If the Incident supported nullable attributes, this read would
			// not be needed.
			e = dbf.readIncident(incidentId);
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to read incident: " + e1.getMessage());
		}
		if (e != null) {
			safeCopy(e, other);
		} else {
			PAPILogger.getInstance().w(CNAME, "Incident with ID " + incidentId + " was not found.");
		}
		try {
			dbf.updateIncident(e);
		} catch (PhinicsDbException e1) {
			throw new ICSDatastoreException("Unable to read incident: " + e1.getMessage());
		}
		return papiIncidentFromNicsIncident(e);
	}

	public int getIncidentCount() {
		// TODO: Needs implementation.
		throw new UnsupportedOperationException("getAllIncidentIds()");
	}

	private void safeCopy(edu.mit.ll.em.common.entity.Incident dbIncident,
			Incident papiIncident) {

		// NOTE: Makes no sense to copy "created" and "updated" Dates since
		// those should be explicitly set by the lowest DB level; i.e.
		// they should be read-only in this layer.
		if (papiIncident.getIncidentId() != null)
			dbIncident.setIncidentid(papiIncident.getIncidentId());
		if (papiIncident.getActive() != null)
			dbIncident.setActive(papiIncident.getActive());
		if (papiIncident.getBounds() != null)		
			dbIncident.setBounds(papiIncident.getBounds());
		if (papiIncident.getFolder() != null)
			dbIncident.setFolder(papiIncident.getFolder());
		if (papiIncident.getIncidentName() != null)
			dbIncident.setIncidentname(papiIncident.getIncidentName());
		if (papiIncident.getLatitude() != null)
			dbIncident.setLat(papiIncident.getLatitude());
		if (papiIncident.getLongitude() != null)
			dbIncident.setLon(papiIncident.getLongitude());
		if (papiIncident.getUserSessionId() != null)
			dbIncident.setUsersessionid(papiIncident.getUserSessionId());

	}	

	private edu.mit.ll.em.common.entity.Incident nicsIncidentFromPapiIncident(
			Incident papiIncident) {
		edu.mit.ll.em.common.entity.Incident nicsIncident = new edu.mit.ll.em.common.entity.Incident();

		// NOTE: Makes no sense to copy "created" and "updated" Dates since
		// those should be explicitly set by the lowest DB level; i.e.
		// they should be read-only in this layer. 
		nicsIncident.setActive(papiIncident.getActive());
		//TODO: Not used by PAPI? nicsIncident.setBounds(papiIncident.getBounds());
		nicsIncident.setFolder(papiIncident.getFolder());
		nicsIncident.setIncidentid(papiIncident.getIncidentId());
		nicsIncident.setIncidentname(papiIncident.getIncidentName());
		nicsIncident.setLat(papiIncident.getLatitude());		
		nicsIncident.setLon(papiIncident.getLongitude());
		nicsIncident.setUsersessionid(papiIncident.getUserSessionId());
		nicsIncident.setWorkspaceid(papiIncident.getWorkspaceId());

		return nicsIncident;
	}

	private Incident papiIncidentFromNicsIncident(
			edu.mit.ll.em.common.entity.Incident nicsIncident,
			Integer userId) {
		Incident papiIncident = _papiIncidentFromNicsIncident(nicsIncident, userId);
		return papiIncident;
	}

	private Incident papiIncidentFromNicsIncident(
			edu.mit.ll.em.common.entity.Incident nicsIncident) {
		Incident papiIncident = _papiIncidentFromNicsIncident(nicsIncident, null);
		return papiIncident;
	}	

	private Incident _papiIncidentFromNicsIncident(
			edu.mit.ll.em.common.entity.Incident nicsIncident,
			Integer userId) {
		Incident papiIncident = new Incident();

		papiIncident.setActive(nicsIncident.getActive());
		//TODO: PAPI does not use this and furthermore, this causes an issue right now:
		
		//{"envelope":{"envelope":{"envelope":{"<ns1:XMLFault xmlns:ns1="http://cxf.apache.org/bindings/xformat"><ns1:faultstring xmlns:ns1="http://cxf.apache.org/bindings/xformat">javax.ws.rs.InternalServerErrorException: org.codehaus.jackson.map.JsonMappingException: Infinite recursion (StackOverflowError) (through reference chain: com.vividsolutions.jts.geom.Polygon["envelope"]->com.vividsolutions.jts.geom.Polygon["envelope"]-		
		
		// papiIncident.setBounds(nicsIncident.getBounds());

		papiIncident.setCreatedUTC(TimeUtil.getMillisFromDate(
				nicsIncident.getCreated()));
		papiIncident.setFolder(nicsIncident.getFolder());
		papiIncident.setIncidentId(nicsIncident.getIncidentid());
		papiIncident.setIncidentName(nicsIncident.getIncidentname());
		papiIncident.setLastUpdatedUTC(TimeUtil.getMillisFromDate(
				nicsIncident.getLastUpdate()));
		papiIncident.setLatitude(nicsIncident.getLat());		
		papiIncident.setLongitude(nicsIncident.getLon());
		papiIncident.setUserSessionId(nicsIncident.getUsersessionid());

		Set<Incident.CollabRoom> collabrooms = null;
		collabrooms = getCollabRooms(nicsIncident.getIncidentid(), userId);
		papiIncident.setCollabrooms(collabrooms);
		papiIncident.setWorkspaceId(nicsIncident.getWorkspaceid());

		return papiIncident;
	}

	private Set<Incident.CollabRoom> getCollabRooms(int incidentId,
			Integer userId) {
		Set<Incident.CollabRoom> ret = new HashSet<Incident.CollabRoom>();
		EntityManager em = this.allocEntityManager();
		try {
			//List<CollabRoom> collabrooms = dbf.getCollabRoomsByIncidentId(em, incidentId);
			List<CollabRoom> collabrooms = dbf.getAccessibleCollabRooms(em, userId, incidentId);
			if (collabrooms != null && !collabrooms.isEmpty()) {
				for (CollabRoom cr : collabrooms) {
					Incident.CollabRoom c = new Incident.CollabRoom();
					c.collabRoomId = cr.getCollabRoomId();
					c.name = cr.getName();
					c.userSessionId = cr.getUsersessionid();
					ret.add(c);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if (em != null) {
				this.freeEntityManager(em);
			}
		}
		return ret;
	}
	*/
}
