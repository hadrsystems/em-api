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
package edu.mit.ll.em.api.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import edu.mit.ll.em.api.rs.User;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.TimeUtil;
import edu.mit.ll.nics.common.entity.Contact;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.entity.UserOrgWorkspace;

public class UserDAO extends BaseDAO {

	private static final String CNAME = UserDAO.class.getName();
	//private static PhinicsDbFacade dbi =
			//PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
	private static final String CONTACT_TYPE_EMAIL = "email";
	private static final String CONTACT_TYPE_HOME_PHONE = "phone_home";
	private static final String CONTACT_TYPE_MOBILE_PHONE = "phone_cell";
	private static final String CONTACT_TYPE_OFFICE_PHONE = "phone_office";
	private static final String CONTACT_TYPE_OTHER_PHONE = "phone_other";
	private static final String CONTACT_TYPE_RADIO_NUMBER = "radio_number";

	static class PrimaryContactInfo {
		String email;
		String homePhone;
		String mobilePhone;
		String officePhone;
		String otherPhone;
		String radioNumber;
	}

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static UserDAO instance = new UserDAO();
	}

	public static UserDAO getInstance() {
		return Holder.instance;
	}

	public UserDAO() {
	}
/*
	public String createUser(edu.mit.ll.em.common.entity.User user, String password) throws ICSDatastoreException {
		EntityManager em = this.allocEntityManager();
		
		String userResult = "fail";
		
		try {			
			userResult = dbi.createUser(em, user, password);
			
			
		} catch(PhinicsDbException e) {
			throw makeException("Error creating User: " + user.getUsername(), e);
		} finally {
			this.freeEntityManager(em);
		}
		
		return userResult;
	}
	
	public boolean registerUser(edu.mit.ll.em.common.entity.User user, List<Contact> contacts, List<UserOrg> userOrgs, 
			 List<UserOrgWorkspace> userOrgWorkspaces)  throws ICSDatastoreException {
		
		List<Object> entities = new ArrayList<Object>();			
		
		entities.add(user);		
		entities.addAll(contacts);
		entities.addAll(userOrgs);
		entities.addAll(userOrgWorkspaces);
		
		EntityManager em = this.allocEntityManager();
		try {
			dbi.persistEntitiesAsTransaction(entities, em);
		} catch(PhinicsDbException e) {
			throw makeException("Error registering User: " + user.getUsername(), e);
		} finally {
			this.freeEntityManager(em);
		}
		
		return true;
	}
	
	public void removeUser(int id) throws ICSDatastoreException {
		try {
			dbi.deleteUserInfo(id);
			dbi.deleteUser(id);
		} catch (PhinicsDbException e) {
			throw makeException("Unable to delete user with ID" + id, e);
		}
	}

	public Set<User> getAllUsers(int workspaceId) throws ICSDatastoreException {
		Set<User> papiUsers = new HashSet<User>();
		List<edu.mit.ll.em.common.entity.User> nicsUsers = null;
		EntityManager em = this.allocEntityManager();
		try {
			nicsUsers = dbi.readAllUsers(em, workspaceId);
			if (nicsUsers != null) {
				for ( edu.mit.ll.em.common.entity.User nicsUser : nicsUsers) {
					User papiUser = makePapiUser(nicsUser);
					if (papiUser!= null) {
						papiUsers.add(papiUser);
					}
				}
			}
		} catch (PhinicsDbException e) {
			throw makeException("Unable to read users.", e);
		} finally {
			this.freeEntityManager(em);
		}
		return papiUsers;
	}

	public long getUserCount() throws ICSDatastoreException {
		int bogus = -1;
		long count = -1;
		try {
			count = dbi.getUserInfoCount(bogus);  // TODO: Fix the API!!!
		} catch (PhinicsDbException e) {
			throw makeException("Unable to retrieve a count of UserInfo", e);
		}		
		return count;
	}

	public User getUserById(int id) throws ICSDatastoreException {
		User papiUser = null;
		edu.mit.ll.em.common.entity.User nicsUser = null;
		EntityManager em = this.allocEntityManager();
		try {
			nicsUser = dbi.readUserEntity(em, id);
			if (nicsUser != null) {
				papiUser = makePapiUser(nicsUser);
			}
		} catch (PhinicsDbException e) {
			throw makeException("Unable to read user with ID" + id, e);
		} finally {
			this.freeEntityManager(em);
		}
		return papiUser;
	}

	public User getUserByUsername(String username) throws ICSDatastoreException {
		User papiUser = null;
		edu.mit.ll.em.common.entity.User nicsUser = getNicsUserByUsername(username);
		if (nicsUser != null) {
			papiUser = makePapiUser(nicsUser);
		}
		return papiUser;
	}

	public edu.mit.ll.em.common.entity.User getNicsUserByUsername(String username) throws ICSDatastoreException {
		edu.mit.ll.em.common.entity.User user = null;
		EntityManager em = this.allocEntityManager();

		try {
			user = dbi.readNicsUserByUsername(em, username);
		} catch (PhinicsDbException e) {
			throw makeException("Unable to read user with username " + username, e);
		} finally {
			this.freeEntityManager(em);
		}

		return user;
	}

	public edu.mit.ll.em.common.entity.User getNicsUser(int userId) throws ICSDatastoreException {
		edu.mit.ll.em.common.entity.User user = null;
		EntityManager em = this.allocEntityManager();

		try {
			user = dbi.readNicsUser(em, userId);
		} catch (PhinicsDbException e) {
			throw makeException("Unable to read user with userId " + userId, e);
		} finally {
			this.freeEntityManager(em);
		}

		return user;
	}
	
	public int getCurrentUsersessionId(int userId){
		EntityManager em = this.allocEntityManager();
		try{
			CurrentUserSession cus =  dbi.findCurrentUserSession(em, userId);
			return cus.getUsersessionid();
		} catch (PhinicsDbException e) {
			//throw makeException("Unable to read user with userId " + userId, e);
			return -1;
		} finally {
			this.freeEntityManager(em);
		}
	}
*/
	private static ICSDatastoreException makeException(String msg, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg).append(": ")
		.append(e.getMessage());
		if (e.getCause() != null && e.getCause().getMessage() != null) {
			sb.append(": ").append(e.getCause().getMessage());
		}
		return new ICSDatastoreException(sb.toString());		
	}
/*
	private static User makePapiUser(edu.mit.ll.em.common.entity.User nicsUser) {
		User papiUser = new User();
		papiUser.setFirstName(nicsUser.getFirstname());
		papiUser.setLastName(nicsUser.getLastname());
		papiUser.setCreatedUTC(TimeUtil.getMillisFromDate(nicsUser.getCreated()));
		papiUser.setLastUpdatedUTC(0L);
		papiUser.setUserId(nicsUser.getUserId());		

		PrimaryContactInfo pci = UserDAO.getPrimaryContactInfoFromBackendEntity(
				nicsUser);
		if (pci.email != null) {
			papiUser.setPrimaryEmailAddr(pci.email);
		}
		if (pci.homePhone != null) {
			papiUser.setPrimaryHomePhone(pci.homePhone);
		}
		if (pci.mobilePhone != null) {
			papiUser.setPrimaryMobilePhone(pci.mobilePhone);
		}
		if (pci.officePhone != null) {
			papiUser.setPrimaryOfficePhone(pci.officePhone);
		}
		if (pci.otherPhone != null) {
			papiUser.setPrimaryOtherPhone(pci.otherPhone);
		}
		if (pci.radioNumber != null) {
			papiUser.setRadioNumber(pci.radioNumber);
		}

		int[] qpos = new int[0];
		papiUser.setQualifiedPositions(qpos);

		return papiUser;
	}
*/
	private static Map<String, Contact> getPrimaryContactsByType(edu.mit.ll.nics.common.entity.User u) {
		HashMap<String, Contact> ret = new HashMap<String, Contact>();
		Set<Contact> contacts = u.getContacts();
		if (contacts == null || contacts.isEmpty()) {
			return ret;
		}

		for (Contact c : contacts) {
			final String contactType = c.getContacttype().getType();
			if (contactType == null || contactType.isEmpty()) {
				continue;
			}
			if (ret.containsKey(contactType)) {
				DateTime tcur = new DateTime (ret.get(contactType).getCreated());
				DateTime tnew = new DateTime(c.getCreated());
				if (tnew.isBefore(tcur)) {
					ret.put(contactType, c);
				}
			} else {
				ret.put(contactType, c);
			}
		}		

		return ret;
	}

	private static PrimaryContactInfo getPrimaryContactInfoFromBackendEntity(
			edu.mit.ll.nics.common.entity.User u) {
		if (u == null) {
			throw new NullPointerException("Unexpected null user argument");
		}
		PrimaryContactInfo pci = new PrimaryContactInfo();
		Map<String, Contact> contacts = getPrimaryContactsByType(u);
		if (contacts == null || contacts.isEmpty()) {
			APILogger.getInstance().w(CNAME, "No Contact entities loaded for user with ID "
					+ u.getUserId());
			return pci;
		}

		for (Contact c : contacts.values()) {
			String value = c.getValue();
			if (value == null || value.isEmpty()) {
				continue;
			}
			final String contactType = c.getContacttype().getType();
			if (contactType == null || contactType.isEmpty()) {
				continue;
			}
			if (UserDAO.CONTACT_TYPE_EMAIL.equals(contactType)) {
				pci.email = value;
			} else if (UserDAO.CONTACT_TYPE_HOME_PHONE.equals(contactType)) {
				pci.homePhone = value;
			} else if (UserDAO.CONTACT_TYPE_MOBILE_PHONE.equals(contactType)) {
				pci.mobilePhone = value;
			} else if (UserDAO.CONTACT_TYPE_OFFICE_PHONE.equals(contactType)) {
				pci.officePhone = value;
			} else if (UserDAO.CONTACT_TYPE_OTHER_PHONE.equals(contactType)) {
				pci.otherPhone = value;
			} else if (UserDAO.CONTACT_TYPE_RADIO_NUMBER.equals(contactType)) {
				pci.radioNumber = value;
			} else {
				String msg = "Contact info entry ignored: Unexpected Contact-Type value "
						+ contactType;  
				APILogger.getInstance().w(CNAME, msg);
			}
		}
		return pci;
	}
/*	
	
	public int getContactTypeId(String type) {
		
		EntityManager em = this.allocEntityManager();
		
		try {
			Integer contactType = (Integer)em.createNativeQuery("select contacttypeid from " +
					"contacttype where type=:type").setParameter("type", type).getSingleResult();
			if(contactType != null) {
				return contactType.intValue();
			}
		} catch(Exception e) {
			PAPILogger.getInstance().i("UserDAO", "Exception retrieving ContactTypeID for type: " + 
					type + ": " + e.getMessage());
		}		
		
		this.freeEntityManager(em);
		
		return -1;
	}
	
	public int getNextContactId() {
		int contactId = -1;
		EntityManager em = this.allocEntityManager();
		try {
			contactId = dbi.getNextContactId(em);
		} catch(Exception e) {
			PAPILogger.getInstance().i("UserDAO", "Exception retrieving NextContactId: " + 
					e.getMessage());
		}
		
		this.freeEntityManager(em);
		
		return contactId;
	}
*/
}
