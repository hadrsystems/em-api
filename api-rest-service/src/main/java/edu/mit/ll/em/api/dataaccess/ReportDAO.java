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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import edu.mit.ll.em.api.rs.Report;
import edu.mit.ll.em.api.rs.impl.TopicBuilder;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.entity.CurrentUserSession;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.messages.sadisplay.SADisplayMessage;
import edu.mit.ll.nics.common.rabbitmq.client.RabbitProducer;
import edu.mit.ll.nics.nicsdao.impl.FormDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.IncidentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;


public class ReportDAO extends BaseDAO {

	private static final String CNAME = ReportDAO.class.getName();
	
	private static UserDAOImpl userDao = new UserDAOImpl();
	private static IncidentDAOImpl incidentDao = new IncidentDAOImpl();
	private static UserSessionDAOImpl userSessDao = new UserSessionDAOImpl();
	private static FormDAOImpl formDao = new FormDAOImpl();
	
	// Rabbit producer
	private RabbitProducer producer;

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static ReportDAO instance = new ReportDAO();
	}

	public static ReportDAO getInstance() {
		return Holder.instance;
	}

	ReportDAO() {
		String host = validateRabbitHostName();
		String username = validateRabbitUsername();
		String userpwd = validateRabbitUserpwd();
		
		producer = new RabbitProducer(username, userpwd, host, 5672);
	}

	public void finalize() {	
		freeResources();
	}

	public void freeResources() {
		if (producer.isConnected()) {
			producer.destroy();
		}
	}

	/*
	public Set<Report> getReports(int reportTypeId, String userName,
			Map<String, Object> queryConstraints) {
		EntityManager em = this.allocEntityManager();
		Set<Report> papiReports = new LinkedHashSet<Report> ();
		
		// Type of reports being requested.
		Set<Integer> reportTypeIds = new LinkedHashSet<Integer>();
		if (reportTypeId >= 0) {  // A single type is being requested.
			reportTypeIds.add(reportTypeId);
		} else {    // All PHINICS report types are being requested.
			Set<String> phinicsReportTypes = this.getPhinicsFormTypes();
			for (String rt : phinicsReportTypes) {
				try {
					FormType ft = EntityCacheMgr.getInstance().getFormTypeByName(rt);
					if (ft != null) {
						reportTypeIds.add(ft.getFormTypeId());
					}
				} catch (Exception e) {
					PAPILogger.getInstance().e(CNAME, e.getMessage());
				}
			}
		}
		try {
			List<Form> reports = dbi.readReports(em, reportTypeIds, userName,
					queryConstraints);
			User u = null;
			try {
				if (userName != null && !userName.isEmpty()) {
					u = EntityCacheMgr.getInstance().getUserEntityByUsername(userName);
				}
			} catch (Exception e) {
				// Ignore it. We just need the User ID out of the entire entity.
			}
			for(Form form : reports) {
				Report rep = new Report();
				rep.setFormId(form.getFormId());
				rep.setFormTypeId(form.getFormtypeid());
				if (u != null) {
					rep.setSenderUserId(u.getUserId());
				}
				rep.setMessage(form.getMessage());
				rep.setIncidentId(form.getIncidentid());
				rep.setIncidentName(form.getIncidentname());
				rep.setUserSessionId(form.getUsersessionid());
				rep.setSeqTime(form.getSeqtime());
				rep.setCreatedUTC(form.getSeqtime());
				rep.setLastUpdatedUTC(form.getSeqtime());
				rep.setSeqNum(form.getSeqnum());
				papiReports.add(rep);
			}
		} catch (PhinicsDbException e) {
			PAPILogger.getInstance().e(CNAME, e.getMessage());
		} finally {
			this.freeEntityManager(em);
		}
		return papiReports;
	}*/
	
	public Form persistReport(int reportTypeId, Report report) {
	
		Form form = new Form();
		form.setFormtypeid(reportTypeId);
		form.setMessage(report.getMessage()); // TODO: escape/sanitize/validate?
		form.setUsersessionid(report.getUserSessionId());
		
		// TODO: differentiate from updating versis new
		//formDao.updateFormMessage(form)
		
		return null;
	}
	
	public Report postReport(int workspaceId, int reportTypeId, Report report)
			throws ICSDatastoreException {
		SADisplayMessage sadMsg = new SADisplayMessage();
		//EntityManager em = this.allocEntityManager();

		try {
			User u = EntityCacheMgr.getInstance().getUserEntity(report.getSenderUserId());
			//Incident inc = dbi.readIncident(report.getIncidentId());
			Incident inc = incidentDao.getIncident(report.getIncidentId());

			sadMsg.setUser(u.getUsername());
			sadMsg.setTime(Long.toString(Calendar.getInstance().getTimeInMillis()));

			Form form = new Form();
			form.setFormId((int) this.getFormId());
			// TODO: get actual usersessionid...
			//CurrentUserSession cus = dbi.findCurrentUserSession(em, u.getUserId());
			// TODO: TEST LEFT OFF HERE
			CurrentUserSession cus = userSessDao.getCurrentUserSession(workspaceId, u.getUserId());
			int usersessionId = 1; // If unable to find actual usersession, use default TODO: fix
			if(cus != null) {
				// Get the usersessionid associated with the currentUserSession							
				try {
					usersessionId = cus.getUsersession().getUsersessionid();
					APILogger.getInstance().i("ReportDao.postReport", "Got usersessionid: " + usersessionId);
					// Alternatively use cus.getUsersessionId() if it's set...
				} catch(Exception e) {
					APILogger.getInstance().i("ReportDAO", 
							"Exception getting usersessionId from CurrentUserSession object");
				}
				
			}
			APILogger.getInstance().i("ReportDao.postReport", "Setting usersessionid on report: " + usersessionId);
			form.setUsersessionid(usersessionId);
			form.setIncidentid((int) report.getIncidentId());
			form.setFormtypeid(reportTypeId);
			form.setSeqtime(report.getSeqTime());
			form.setSeqnum(this.getMessageSequenceNumber());
			form.setMessage(report.getMessage());

			sadMsg.addEntity(form);
			sadMsg.setMessageType("stat");  // Tell SADisplay it needs to refresh.
			String topic = TopicBuilder.makeOtherRocTopic(workspaceId, inc.getIncidentid());
			this.producer.sendMessage(topic, sadMsg.toJSONString());

			report.setSeqNum(form.getSeqnum());
			report.setFormId(form.getFormId());
		} catch (DataAccessException e) {
			ICSDatastoreException.handle(CNAME, "Unable to read user/incident", e);
		}/* finally {
			this.freeEntityManager(em);
		}*/

		return report;
	}

	private static String validateRabbitHostName() {
		String host = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_HOSTNAME_KEY);
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Undefined configuration key " +
					APIConfig.RABBIT_HOSTNAME_KEY + 
					". No Rabbit BUS communications will be possible. " +
					"Add or fix entry in file " + APIConfig.API_PROPS_FILE);
		}
		return host;
	}

	private static String validateRabbitUsername() {
		String username = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_USERNAME_KEY);
		if (username == null || username.isEmpty()) {
			username = "guest";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.RABBIT_USERNAME_KEY +
					" in " + APIConfig.API_PROPS_FILE + " file. Using " +
					"guest" + " as Rabbit user name.");
		}
		return username;
	}

	private static String validateRabbitUserpwd() {
		String userpwd = APIConfig.getInstance().getConfiguration().
				getString(APIConfig.RABBIT_USERPWD_KEY);
		if (userpwd == null || userpwd.isEmpty()) {
			userpwd = "guest";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.RABBIT_USERPWD_KEY +
					" in " + APIConfig.API_PROPS_FILE + " file. Using " +
					"guest" + " as Rabbit user name.");
		}
		return userpwd;
	}	

	
	private long getMessageSequenceNumber() {
		
		long seqnum = -1;
		
		try {			
			seqnum = userSessDao.getNextMessageSeqNum();
			
		} catch(Exception e) {
			APILogger.getInstance().e(CNAME, "Exception getting messageSeqNum: " + e.getMessage());
			e.printStackTrace();
		}
		
		if(seqnum == -1) {
			APILogger.getInstance().w(CNAME, "Couldn't get next value of message_sequence, returning -1!");
		}
		
		return seqnum;
	}

	
	// TODO:refactor double check how this is being used, and why it's checkinglast value and not next
	private long getFormId() {
		
		long seqnum = -1;
		long retval = -1;
		try {
			//seqnum = (BigInteger) em
				//	.createNativeQuery("select last_value from form_seq").getSingleResult();
			FormDAOImpl formDao = new FormDAOImpl();
			seqnum = formDao.getNextFormId();
			
		} catch(Exception e) {
			APILogger.getInstance().e(CNAME, "Exception getting next sequence in form_seq: " + e.getMessage());
		}

		return retval;
	}
	
	
	private Set<String> getPhinicsFormTypes() {
		Set<String> ret = null;
		try {
			ret = EntityCacheMgr.getInstance().getFormTypeNames();
			String nonPhinicsReportTypes[] = { "215", "9110", "ABC", "RESC", "ROC" };
			for (int i = 0; i < nonPhinicsReportTypes.length; ++i) {
				if (ret.contains(nonPhinicsReportTypes[i])) {
					ret.remove(nonPhinicsReportTypes[i]);
				}
			}
		} catch (Exception e) {
			if (ret == null) {
				ret = new HashSet<String>(0);
				APILogger.getInstance().w(CNAME, "Cannot determine valid Phinics form types: " +
						e.getMessage());
			}
		}
		return ret;
	}
}
