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
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.Org;
import edu.mit.ll.nics.common.entity.OrgType;
import edu.mit.ll.nics.common.entity.UserOrg;


public class OrgDAO extends BaseDAO {

	//private static PhinicsDbFacade dbi =
		//	PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton();
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static OrgDAO instance = new OrgDAO();
	}

	public static OrgDAO getInstance() {
		return Holder.instance;
	}

	public OrgDAO() {
	}
/*	
	public Org getOrganization(String organization) {
	
		Org org = null;
		
		EntityManager em = this.allocEntityManager();
		
		try {
			org = (Org) em.createQuery("from Org where name=:orgname")
					.setParameter("orgname", organization).getSingleResult();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.freeEntityManager(em);
		}
		
		return org;
	}
	
	public List<Object[]> getUserOrgs(Integer workspaceId, Integer userId) throws ICSDatastoreException {
		EntityManager em = this.allocEntityManager();
		
		try {
			return em.createNativeQuery(
					"select userorgid,org.name,orgid,systemroleid from userorg join userorg_workspace using(userorgid) join org using(orgid) where userid=:userId and workspaceid=:workspaceid")
				.setParameter("workspaceid", workspaceId)
				.setParameter("userId", userId).getResultList();
		} catch(Exception e) {
			throw new ICSDatastoreException(e.getMessage());
		} finally {
			this.freeEntityManager(em);
			em = null;
		}
	}

	public List<Org> getOrganizationForUser(Integer workspaceId, Integer userId) throws ICSDatastoreException {
		List<Org> userOrgs = new ArrayList<Org>();
		EntityManager em = this.allocEntityManager();
		
		try {
			@SuppressWarnings("unchecked")
			List<Object[]> orgs = em.createNativeQuery("select e1.* from org e1, userorg e2, userorg_workspace e3 where e2.userid=:userId and e3.workspaceid=:workspaceId and e3.enabled='1' and e3.userorgid=e2.userorgid and e2.orgid = e1.orgid order by e1.name;").setParameter("userId", userId).setParameter("workspaceId",  workspaceId).getResultList();
	
			for(Object[] o : orgs) {
				String name = o[1] != null ? o[1].toString() : "";
				String county = o[2] != null ? o[2].toString() : "";
				String state = o[3] != null ? o[3].toString() : "";
				String timezone = o[4] != null ? o[4].toString() : "";
				String prefix = o[5] != null ? o[5].toString() : "";
				String distribution = o[6] != null ? o[6].toString() : "";
				String country = o[10] != null ? o[10].toString() : "";
				
				Org t = new Org((Integer) o[0], name, county, state, timezone, prefix, distribution, (Double) o[7], (Double) o[8], (Integer) o[9], country, (Date)o[11], new HashSet<UserOrg>());
				userOrgs.add(t);
			}
			
			// Hibernate query doesnt work for some reason... infinite recursion exception
			//ArrayList<Org> orgs = (ArrayList<Org>) em.createQuery("SELECT e1 FROM Org e1, UserOrg e2, UserOrgWorkspace e3 where e2.userid=:userId and e3.workspaceid=:workspaceId and e3.enabled='t' and e3.userorgid=e2.userorgid and e2.orgid = e1.orgId order by e1.name").setParameter("userId", userId).setParameter("workspaceId",  workspaceId).getResultList();
	
			// same as: em.createNativeQuery("select e1.* from org e1, userorg e2, userorg_workspace e3 where e2.userid=:userId and e3.workspaceid=:workspaceId and e3.enabled='1' and e3.userorgid=e2.userorgid and e2.orgid = e1.orgid order by e1.name;", Org.class).setParameter("userId", userId).setParameter("workspaceId",  workspaceId).getResultList();
		} catch(Exception e) {
			throw new ICSDatastoreException(e.getMessage());
		} finally {
			this.freeEntityManager(em);
			em = null;
		}
		
		return userOrgs;
	}

	public List<Object[]> getOrgs() {
		List<Object[]> orgs = null;
		EntityManager em = this.allocEntityManager();
		
		try {
			
			orgs = em.createNativeQuery("select org.orgid, org.name, oot.orgtypeid from org org, org_orgtype oot where oot.orgid = org.orgid order by org.name").getResultList();
								
		} catch(Exception e) {
			PAPILogger.getInstance().e("OrgDAO", "Exception while querying for all Orgs: " + e.getMessage());
		} finally {
			this.freeEntityManager(em);
			em = null;
		}
		
		return orgs;
	}
	
	public List<Object[]> getOrgTypes() {
		List<Object[]> orgTypes = null;
		EntityManager em = this.allocEntityManager();
		
		try {
			
			orgTypes = em.createNativeQuery("select orgtypeid, orgtypename from orgtype").getResultList();
								
		} catch(Exception e) {
			PAPILogger.getInstance().e("OrgDAO", "Exception while querying for OrgTypes: " + e.getMessage());
		} finally {
			this.freeEntityManager(em);
			em = null;
		}
		
		return orgTypes;
	}
*/
}
