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
package edu.mit.ll.em.api.dataaccess;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.entity.FormType;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.FormDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;

/**
 * Maintains a collection of entities, meant to be used for read-only purposes.
 * Frequent queries for entity attributes known to change infrequently, if ever at all,
 * can avoid hitting the database by going through this cache instead.
 * The cache is refreshed at configurable intervals of time controlled via the
 * api properties file, by the key "...cache.entity.refreshminutes".
 * The default is 60 minutes.
 * 
 * CAUTION: It is imperative that entities in this Cache not be used for updates,
 * nor is it to be relied on for an up-to-date state of entity attributes known to
 * change frequently.
 * @author sa23148
 *
 */
public class EntityCacheMgr {

	private CollabRoomDAOImpl collabRoomDao = null;
	private IncidentDAOImpl incidentDao = null;
	private UserDAOImpl userDao = null;
	private UserSessionDAOImpl userSessDao = null;
	private FormDAOImpl formDao = null;
	
	// Invalidates the user cache every so many minutes.
	TimerTask cacheRecycler;

	// Caches CollabRoom per collabRoomId
	private ConcurrentHashMap<Integer, CollabRoom> collabRoomCache =
			new ConcurrentHashMap<Integer, CollabRoom>();	

	// Caches Incident per incidentId
	private ConcurrentHashMap<Integer, Incident> incidentCache =
			new ConcurrentHashMap<Integer, Incident>();

	// Caches User entries.
	private ConcurrentHashMap<Integer, User> userCache =
			new ConcurrentHashMap<Integer, User>();

	// Caches UserSessionIds per userId
	private ConcurrentHashMap<Integer, Integer> userSessionIdCache =
			new ConcurrentHashMap<Integer, Integer>();
	
	// Caches FormType entries.
	private ConcurrentHashMap<String, FormType> formTypeCache =
			new ConcurrentHashMap<String, FormType>();
	
	private ConcurrentHashMap<Integer, FormType> formTypeIdCache =
			new ConcurrentHashMap<Integer, FormType>();

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static EntityCacheMgr instance = new EntityCacheMgr();
	}

	private void flushCaches() {
		// TODO: Race condition if a user were to be added simultaneously.
		// Totally preventing this condition would cost performance-wise.
		if (collabRoomCache != null && !collabRoomCache.isEmpty()) {
			collabRoomCache.clear();
		}			
		if (incidentCache != null && !incidentCache.isEmpty()) {
			incidentCache.clear();
		}		
		if (userCache != null && !userCache.isEmpty()) {
			userCache.clear();
		}
		if (userSessionIdCache != null && !userSessionIdCache.isEmpty()) {
			userSessionIdCache.clear();
		}
		if (formTypeCache != null && !formTypeCache.isEmpty()) {
			formTypeCache.clear();
		}			
	}

	public void finalize() {
		freeResources();
	}	

	public static EntityCacheMgr getInstance() {
		return Holder.instance;
	}

	public void freeResources() {
		flushCaches();
	}

	// Hide the default constructor.
	private EntityCacheMgr() {
        this.collabRoomDao = new CollabRoomDAOImpl();
        this.incidentDao = new IncidentDAOImpl();
        this.userDao = new UserDAOImpl();
        this.userSessDao = new UserSessionDAOImpl();
        this.formDao = new FormDAOImpl();

		cacheRecycler = new TimerTask() { 
			public void run() {
				EntityCacheMgr.getInstance().flushCaches();
			}
		};
		// Refresh every 60 minutes by default.
		int userCacheRefreshMins = APIConfig.getInstance().getConfiguration()
				.getInt(APIConfig.CACHE_USER_REFRESHMINUTES, 60);
		new Timer(true).scheduleAtFixedRate(cacheRecycler, 0,
				userCacheRefreshMins * 60 * 1000);
	}

	public CollabRoom getCollabRoomEntity(int collabRoomId) throws ICSDatastoreException {
		CollabRoom cr = null;
		if (collabRoomCache.containsKey(collabRoomId)) {
			cr = collabRoomCache.get(collabRoomId);
		} else {
			// We need to hit the database.
			try {
				CollabRoom collabRoomEntity = collabRoomDao.getCollabRoomById(collabRoomId);				
				
				cr = collabRoomEntity;
				collabRoomCache.putIfAbsent(collabRoomId, cr);
			} catch(DataAccessException e) {
				// TODO:refactor keep using, or get rid of ICSDataStoreException?
				throw new ICSDatastoreException("Cannot read CollabRoom entity with id " + 
						collabRoomId + e.getMessage());
			} catch(Exception e) {
				// TODO:refactor keep using, or get rid of ICSDataStoreException?
				throw new ICSDatastoreException("Caught unhandled exception trying to read CollabRoom entity with id " + 
						collabRoomId + e.getMessage());
			}
		}
		return cr;
	}

	public Incident getIncidentEntity(int incidentId) throws ICSDatastoreException {
		Incident incident = null;
		if (incidentCache.containsKey(incidentId)) {
			incident = incidentCache.get(incidentId);
		} else {
			// We need to hit the database.
			try {
				Incident incidentEntity = incidentDao.getIncident(incidentId);
				
				incident = incidentEntity;
				incidentCache.putIfAbsent(incidentId, incident);
			} catch(DataAccessException e) {
				throw new ICSDatastoreException(
						"Cannot read Incident entity with id " + incidentId +
						e.getMessage());
			} catch(Exception e) {
				throw new ICSDatastoreException("Unhandled exception reading Incident entity with id " + 
						incidentId + ": " + e.getMessage());
			}
		}
		return incident;
	}

	public User getUserEntity(int userId) throws ICSDatastoreException {
		User user = null;
		if (userCache.containsKey(userId)) {
			user = userCache.get(userId);
		} else {
			// We need to hit the database.
			
			try {
				User userEntity = userDao.getUserById(userId);
				
				user = userEntity;
				userCache.putIfAbsent(userId, user);
				
			} catch(DataAccessException e) {
				throw new ICSDatastoreException("Cannot read User entity with id " + 
						userId + ": " + e.getMessage());
			} catch(Exception e) {
				throw new ICSDatastoreException("Caught unhandled exception while trying to read User entity with id " + 
						userId + ": " + e.getMessage());
			}
		}
		return user;
	}

	public User getUserEntityByUsername(String username) throws ICSDatastoreException {
		User user = null;
		for (User u : userCache.values()) {
			if (u.getUsername().equals(username)) {
				return u;
			}
		}

		user = userDao.getUser(username);
		if (user != null) {
			userCache.putIfAbsent(user.getUserId(), user);
		}
		return user;
	}		

	public int getUserSessionId(int userId) throws ICSDatastoreException {
		int userSessionId = -1;
		if (userSessionIdCache.containsKey(userId)) {
			userSessionId = userSessionIdCache.get(userId);
		} else {
			// We need to hit the database.			
			try {

				userSessionId = userSessDao.getUserSessionid(userId);
				if (userSessionId < 0) {
					// We need to create a User Session.
					// TODO:refactor do we? If this is just a cache to get one if it exists, I don't think
					//  we should assume one needs created?
					//userSessionId = dbf.createUserSession(em, userId);
					APILogger.getInstance().i("EntityCacheMgr", "No usersession found for userId: " + userId
							+ ", TODO: is this a use case where one should be created?");
				} else {
					userSessionIdCache.putIfAbsent(userId, userSessionId);
				}

			} catch(DataAccessException e) {
				throw new ICSDatastoreException(
						"Unable to find/create UserSession entry for user " + userId +
						e.getMessage());
			}
		}
		return userSessionId;
	}
	
	/**
	 * 
	 * @param formTypeName Name of type being seeked.
	 * @return FormType corresponding to formTypeName or null if not found.
	 * @throws ICSDatastoreException If an abnormal condition occurs
	 * @throws NullPointerException If formTypeName is null
	 */
	public FormType getFormTypeByName(String formTypeName)
			throws ICSDatastoreException, NullPointerException {
		FormType ret = null;
		if (formTypeName == null) {
			throw new NullPointerException("formTypeName cannot be null");
		}
		formTypeName = formTypeName.toUpperCase();
		if (formTypeCache.contains(formTypeName)) {
			return formTypeCache.get(formTypeName);
		}
		try {

			List<FormType> types = formDao.getFormTypes();
			if (types != null && types.size() > 0) {
				for (FormType f : types) {
					formTypeCache.putIfAbsent(f.getFormTypeName(), f);
				}
			}
			ret = formTypeCache.get(formTypeName);
		} catch (DataAccessException e) {
			throw new ICSDatastoreException(
					"Data access exception trying to read FormType entities: " + e.getMessage());
		} catch (Exception e) {
			throw new ICSDatastoreException(
					"Unhandled exception trying to read FormType entities: " + e.getMessage());
		}
		return ret;
	}
	
	public Set<String> getFormTypeNames()
			throws ICSDatastoreException {
		if (formTypeCache.isEmpty()) {
			try {
				/*List<FormType> types = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton()
						.getFormTypes();*/
				List<FormType> types = formDao.getFormTypes();
				
				if (types != null && types.size() > 0) {
					for (FormType f : types) {
						formTypeCache.putIfAbsent(f.getFormTypeName(), f);
					}
				}
			} catch (DataAccessException e) {
				throw new ICSDatastoreException(
						"Data access exception trying to read FormType entities: " + e.getMessage());
			} catch (Exception e) {
				throw new ICSDatastoreException(
						"Unhandled exception trying to read FormType entities: " + e.getMessage());
			}
		}

		return formTypeCache.keySet();
	}

	/**
	 * 
	 * @param formTypeId of type being seeked.
	 * @return FormType corresponding to formTypeName or null if not found.
	 * @throws ICSDatastoreException If an abnormal condition occurs
	 * @throws NullPointerException If formTypeName is null
	 */
	public FormType getFormTypeById(int formTypeId)
			throws ICSDatastoreException {
		FormType ret = null;
		if (formTypeId < 0) {
			throw new ICSDatastoreException("formTypeId cannot be less than zero");
		}
		
		if (formTypeIdCache.contains(formTypeId)) {
			return formTypeIdCache.get(formTypeId);
		}
		
		try {

			List<FormType> types = formDao.getFormTypes();
			if (types != null && types.size() > 0) {
				for (FormType f : types) {
					formTypeIdCache.putIfAbsent(f.getFormTypeId(), f);
				}
			}
			ret = formTypeIdCache.get(formTypeId);
		} catch (DataAccessException e) {
			throw new ICSDatastoreException(
					"Data access exception trying to read FormType entities: " + e.getMessage());
		} catch (Exception e) {
			throw new ICSDatastoreException(
					"Unhandled exception trying to read FormType entities: " + e.getMessage());
		}
		return ret;
	}

	public List<FormType> getFormTypes() {
		List<FormType> types = null;
		try {
			// TODO: could/should just cache these so it's not querying every time, plus once one of the get
			// formtype by id or name has been called, we already have all the data in the two caches
			types = formDao.getFormTypes();
		} catch(Exception e) {
			
		}
		return types;
	}
}
