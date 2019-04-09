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

import java.math.BigInteger;

/*import javax.persistence.EntityManager;
import edu.mit.ll.hps.phinics.dbaccess.PhinicsDbFacade;
import edu.mit.ll.hps.phinics.dbaccess.PhinicsDbFactory;*/


public class UserOrgDAO extends BaseDAO {

	//private static PhinicsDbFacade dbi =
		//	PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static UserOrgDAO instance = new UserOrgDAO();
	}

	public static UserOrgDAO getInstance() {
		return Holder.instance;
	}

	public UserOrgDAO() {
	}
	
/*
	public int getNextUserOrgId() {
		int id = -1;
		
		EntityManager em = this.allocEntityManager();
		
		try {
			BigInteger userOrgId = (BigInteger)em.createNativeQuery("select nextVal('user_org_seq')").getSingleResult();
			if(userOrgId.intValue() > 0) {
				id = userOrgId.intValue();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.freeEntityManager(em);
		}
		
		return id; 
	}
*/
}
