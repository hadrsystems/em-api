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

import edu.mit.ll.em.api.rs.Login;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.Usersession;


public class LoginDAO extends BaseDAO {
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static LoginDAO instance = new LoginDAO();
	}

	public static LoginDAO getInstance() {
		return Holder.instance;
	}

	public LoginDAO() {
	}

	@Deprecated
	public Login login(User u, int wsId) throws ICSDatastoreException, LoginException {
		throw new LoginException("Deprecated login method");
		/*
		EntityManager em = this.allocEntityManager();
		PhinicsDbFacade dbi = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
		
		Login login = new Login();
		login.setUsername(u.getUsername());
		login.setUserId(u.getUserId());
		login.setWorkspaceId(wsId);
		login.setUserSessionId(-1);
		
		try {
			if(dbi.findCurrentUserSessionId(em, u.getUserId()) < 0) {
				// Create usersession
				int usersessionid = dbi.createUserSession(em, u.getUserId());
				
				Usersession us = dbi.getUsersession(em, usersessionid);
				
				// Create currentusersession
				CurrentUserSession cus = dbi.createCurrentUserSession(em, u, us, wsId);
				if(cus != null) {
					login.setUserSessionId(usersessionid);
					login.setWorkspaceId(cus.getWorkspaceid());
				} else {
					throw new LoginException("Failed to create current user session.");
				}
			} else {
				throw new LoginException("User is logged in already, please log out.");
			}
		} catch (PhinicsDbException e) {
			throw new ICSDatastoreException(e.getMessage());
		}  finally {
			this.freeEntityManager(em);
		}
		
		return login;
		*/
	}
	
	@Deprecated
	public void logout(User u) throws ICSDatastoreException {
		throw new ICSDatastoreException("Deprecated");
		/*
		EntityManager em = this.allocEntityManager();
		PhinicsDbFacade dbi = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
		
		try {
			if(dbi.findCurrentUserSessionId(em, u.getUserId()) > -1) {
				// Remove currentusersession
				int usId = dbi.removeCurrentUserSession(em, u);
				
				// Modify logout time in usersession
				dbi.logoutUserSession(em, usId);
			} 
		} catch (Exception e) {
			throw new ICSDatastoreException(e.getMessage());
		} finally {
			this.freeEntityManager(em);
		}
		*/
	}
	
}
