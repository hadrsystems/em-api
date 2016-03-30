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


public class SystemRoleDAO extends BaseDAO {
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static SystemRoleDAO instance = new SystemRoleDAO();
	}

	public static SystemRoleDAO getInstance() {
		return Holder.instance;
	}

	public SystemRoleDAO() {
	}
/*	
	public int getSystemRoleId(String role) {
		int systemRoleId = -1;
		
		EntityManager em = this.allocEntityManager();
		
		try {
			//int id = dbi.getSystemRoleId(em, role);
			Integer id = (Integer)em.createNativeQuery("select systemroleid from systemrole where rolename=?")
					.setParameter(1, role).getSingleResult();
			
			if(id != null) {
				systemRoleId = id.intValue();
			}
			
		//} catch (PhinicsDbException e) {
		//	throw new ICSDatastoreException("Unable to get Role ID for role: " + role);
		} finally {
			this.freeEntityManager(em);
		}
		
		return systemRoleId;
	}
*/
}
