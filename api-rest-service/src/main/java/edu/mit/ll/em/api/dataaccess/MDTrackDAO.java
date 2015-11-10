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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.mit.ll.em.api.entity.JSONPLIEntry;
import edu.mit.ll.em.api.rs.MDTOptionalParms;
import edu.mit.ll.em.api.rs.MDTrack;
import edu.mit.ll.em.api.rs.QueryConstraintHelper;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.TimeUtil;
import edu.mit.ll.nics.common.entity.Mdt;
import edu.mit.ll.nics.common.entity.UserInfo;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.rabbitmq.client.RabbitProducer;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;


public class MDTrackDAO extends BaseDAO {
	
	private static final String CNAME = MDTrackDAO.class.getName();
		
	private static final UserDAOImpl userDao = new UserDAOImpl();
		
	private static GeometryFactory geomFactory= new GeometryFactory(new PrecisionModel(), 4326);

	// Rabbit producer
	private RabbitProducer producer;

	// MDT GML Properties
	
	private String nicsSchemaLocationURI;

	private String srsName;

	private String typeName;

	private String wfsSchemaURI;

	private String wfsServiceURI;
	
	private String gmlTopic;
	
	private boolean validated;
	private boolean valid;
	
	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static MDTrackDAO instance = new MDTrackDAO();
	}

	public static MDTrackDAO getInstance() {
		return Holder.instance;
	}
	
	MDTrackDAO() {
		String host = validateRabbitHostName();
		String username = validateRabbitUsername();
		String userpwd = validateRabbitUserpwd();
		producer = new RabbitProducer(username, userpwd, host, 5672); // TODO:refactor use property for port
		valid = validateMDTProperties();
	}
	
	public MDTrack createMDTrack(MDTrack mdtrack) throws ICSDatastoreException {
		if (mdtrack == null) {
			throw new NullPointerException(CNAME + ":createMDTrack called with null mdtrack argument");
		}
		Mdt dbMDT = makePhiMdtFromMDTrack(mdtrack);
		MDTrack ret = null;
		try {
			
			if(!validated) {
				validated = validateMDTProperties();
			}
			
			if(valid) {
				// TODO:LDDRS-1119 for now just publish it in addition to persisting
				publishMDTGML(dbMDT);
				ret = makeMDTrackFromPhiMdt(dbMDT); // No id, no reason to send back?
			} else {
				APILogger.getInstance().w(CNAME, "Not publishing MDT due to invalid MDT properties");
			}
			
			// TODO:refactor cleanup
			// OLD db way
			// createPhiMdt actually persists it...
			//long mdtId = dbi.createPhiMdt(dbMDT);
			//ret = makeMDTrackFromPhiMdt(dbi.readPhiMdt(mdtId));
			// END OLD db way
			
		} catch (Exception e) {
			//ICSDatastoreException.handle(CNAME, "Unable to persist PhiMDT entity", e);
			APILogger.getInstance().e(CNAME, "Unhandled exception publishing MDT: " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Publishes the MDT as NICS Compatible GML for publishing to
	 * geodatafeed-consumer
	 * 
	 * @param phiMdt
	 * @return
	 */
	private boolean publishMDTGML(Mdt phiMdt) {
		boolean ret = false;
		if(phiMdt == null) {
			// TODO: log
			return false;
		}
		
		try {
			JSONPLIEntry jsonPli = makeJSONPLIEntry(phiMdt);
			
			// TODO:LDDRS-1119 decide whether or not to only specify certain fields, and if
			// the toGML honors it
			// TODO: make topic configurable
			this.producer.sendMessage(gmlTopic, jsonPli.toGML(false));
			ret = true;
		} catch(Exception e) {
			// fail TODO: add logging
		}
		
		return ret;
	}
	
	private JSONPLIEntry makeJSONPLIEntry(Mdt phiMdt) {
		
		JSONPLIEntry mdtPli = new JSONPLIEntry();
		
		Coordinate position = (phiMdt.getLocation() != null) ? phiMdt.getLocation().getCoordinate() : null;
		if(position != null) {
			//mdtPli.setCoordinates(position.x + "," + position.y);
			mdtPli.setCoordinates(position.y + "," + position.x);
			APILogger.getInstance().i(CNAME, "Got position from MDT: " + mdtPli.getCoordinates());
		} else {
			APILogger.getInstance().i(CNAME, "Couldn't extract position from MDT: " + 
					((phiMdt.getLocation() != null) ? phiMdt.getLocation().toString() : null));
		}		
					
		mdtPli.setCourse(phiMdt.getCourse()+"");
		
		//String desc = "Device ID: " + ((phiMdt.getDeviceId() == null) ? "N/A" : phiMdt.getDeviceId()) +
		//		"Accuracy: " + ((phiMdt.getAccuracy() == null) ? "N/A" : phiMdt.getAccuracy());
				
		mdtPli.setDescription(""); // TODO
		mdtPli.setExtended(""); // TODO:
		mdtPli.setId(phiMdt.getUserInfo().getUser().getUsername()); // TODO:
		mdtPli.setName(phiMdt.getUserInfo().getUser().getUsername());
		mdtPli.setNicsSchemaLocationURI(nicsSchemaLocationURI); // http://MAPSERVERHOST/NICS
		mdtPli.setSpeed(phiMdt.getSpeed()+"");
		mdtPli.setSrsName(srsName); // EPSG:4327 or 3857?
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date(phiMdt.getTime());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String strTime = sdf.format(date);
		mdtPli.setTimestamp(strTime);
		
		mdtPli.setTypeName(typeName);
		mdtPli.setVersion("1.2.3"); // TODO: don't include in GML
		mdtPli.setWfsSchemasURI(wfsSchemaURI);
		mdtPli.setWfsServiceURI(wfsServiceURI);
		
		return mdtPli;
	}

	/**
	 * 
	 * Deprecated: Tracks won't be pulled from db any longer, but via WFS from the mapserver they're stored on
	 * 
	 * @param opts
	 * @return
	 * @throws ICSDatastoreException
	 */
	@Deprecated
	public Set<MDTrack> getAllMDTracks(MDTOptionalParms opts) {//throws ICSDatastoreException {
		
		Set<MDTrack> papiMDTs = null;
		if(true) {
			return papiMDTs;
		}
		/*
		int userId = opts.getUserId();
		int maxRowsLimit = PAPIConfig.getInstance().getConfiguration()
				.getInt("nics.api.db.get.maxrows", 1000);
		// Protect the server from requests that yield an unreasonable number of rows.
		if (opts.getLimit() == null || opts.getLimit() > maxRowsLimit) {
			PAPILogger.getInstance().i(CNAME, "Rewriting max. rows LIMIT as " + maxRowsLimit);
			opts.setLimit(maxRowsLimit);
		}
		// Decide what date column we'll be constraining by.
		String dcName = opts.getDateColumn();
		if (dcName == null || dcName.isEmpty() ||
			"createdUTC".equalsIgnoreCase(dcName) ||
			"lastUpdatedUTC".equalsIgnoreCase(dcName)) {
			opts.setDateColumn("time");
		}		
		// Decide what column we'll be sorting on.
		if (userId < 0) {
			// This is a GET ALL MDTs. Force a sort by ID.
			opts.setSortByColumn("id");
			opts.setSortOrder("asc");
		} else {
			String sbName = opts.getSortByColumn();
			if (sbName == null || sbName.isEmpty() ||
					"createdUTC".equalsIgnoreCase(sbName) ||
					"lastUpdatedUTC".equalsIgnoreCase(sbName)				) {
				opts.setSortByColumn("time");
			}
		}
		
		// We need to support the semantics "Give me the N-latest tracks for each 
		// user from a given set (of users)". This is accomplished by having the
		// caller to set the LIMIT option equal to N. Because everything downstream
		// from here already supports this for a single user, we are going to cheat
		// a bit here by calling the downstream code for each userId in the
		// userIdList. Future performance improvements should consider working on
		// the query to do this in a single DB hit.
		Set<Integer> userIds = getSortedUserIdSet(opts.getUserIdList());
		if (! (userId == -1 && userIds.size() > 0) ) {
			userIds.add(userId);
		}
		List<PhiMdt> dbMDTs = new ArrayList<PhiMdt>();
		for (Integer id : userIds) {
			try {
				List<PhiMdt> temp;
				temp = dbi.readAllPhiMdts(
						id,
						QueryConstraintHelper.makeDateRange(opts),
						QueryConstraintHelper.makeResultSetRange(opts),
						QueryConstraintHelper.makeOrderBy(opts)
						);
				if (temp != null && !temp.isEmpty()) {
					dbMDTs.addAll(temp);
				}
			} catch (PhinicsDbException e) {
				ICSDatastoreException.handle(CNAME, "Unable to read PhiMDT entities", e);
			}
		}
		if (dbMDTs == null || dbMDTs.size() < 1) {
			papiMDTs = new LinkedHashSet<MDTrack>();
			return papiMDTs;
		}
		
		papiMDTs = new LinkedHashSet<MDTrack>(dbMDTs.size());
		for (PhiMdt dbMdt : dbMDTs) {
			MDTrack mdtrack = makeMDTrackFromPhiMdt(dbMdt);
			papiMDTs.add(mdtrack);
		}
		return papiMDTs;
		*/
		return null;
	}

	/**
	 * Deprecated: No longer read tracks from db, they'll be queried from WFS
	 * 
	 * @param id
	 * @return
	 * @throws ICSDatastoreException
	 */
	@Deprecated
	public MDTrack getMDTrackById(long id) throws ICSDatastoreException {
		return null;
		/*
		MDTrack ret = null;
		PhiMdt dbMdt = null;
		try {
			dbMdt = dbi.readPhiMdt(id);
		} catch (PhinicsDbException e) {
			ICSDatastoreException.handle(CNAME, "Unable to read PhiMDT entity", e);
		}
		if (dbMdt != null) {
			ret = makeMDTrackFromPhiMdt(dbMdt);
		}
		return ret;*/
	}

	public void removeMDTrack(long id) throws ICSDatastoreException {
		// TODO: Insert implementation here. 
		APILogger.getInstance().w(CNAME, "Unimplemented call removeMDTrack(id) was made. Ignored.");
	}
	
	public MDTrack updateMDTrack(long mdtrackId, MDTrack other) throws ICSDatastoreException {
		MDTrack ret = null;
		if (other == null) {
			throw new NullPointerException(CNAME + ":updateMDTrack called with null mdtrack argument");
		}
		// TODO: Insert implementation here.
		APILogger.getInstance().w(CNAME, "Unimplemented call updateMDTrack(id, otherTrack) was made. Ignored.");
		return ret;
	}

	public int getMDTrackCount() {
		int count = -1;
		// TODO: Insert implementation here.
		APILogger.getInstance().w(CNAME, "Unimplemented call getMDTrackCount() was made. Ignored.");
		return count;
	}

	private Geometry makeGeom(MDTrack mdtrack) {
		if (mdtrack == null) {
			return null;
		}
		Coordinate lla = new Coordinate();
		lla.x = mdtrack.getLongitude();
		lla.y = mdtrack.getLatitude();
		lla.z = mdtrack.getAltitude();
		Geometry geo = geomFactory.createPoint(lla);
		return geo;
	}
	
	/**
	 * This is going to the back-end.
	 */
	private Mdt makePhiMdtFromMDTrack(MDTrack mdtrack) throws ICSDatastoreException {
		UserInfo phiUserInfo = null;
		try {
			// TODO:refactor need username at the least for publishing of message, but need to factor out
			// 	the use of PhiUserInfo entirely
			User user = userDao.getUserById(mdtrack.getUserId());
			if(user != null) {
				phiUserInfo = new UserInfo();
				phiUserInfo.setUser(user);
				phiUserInfo.setUserId(mdtrack.getUserId());
			} else {
				phiUserInfo = new UserInfo();
			}
			//phiUserInfo = dbi.readUserInfo(mdtrack.getUserId());
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unable to read UserInfo: ").append(e.getCause().getMessage()).
				append(" : ").append(e.getMessage());
			throw new ICSDatastoreException(sb.toString());
		}
		Mdt dbMDT = new Mdt();
		dbMDT.setAccuracy(mdtrack.getAccuracy());
		dbMDT.setCourse(mdtrack.getCourse());
		dbMDT.setDeviceId(mdtrack.getDeviceId());
		Geometry geo = makeGeom(mdtrack);
		dbMDT.setLocation(geo);
		dbMDT.setUserInfo(phiUserInfo);
		dbMDT.setSpeed(mdtrack.getSpeed());
		long time = mdtrack.getCreatedUTC();
		if (time <= 0) {
			time = TimeUtil.getNowAsMillis();
		}
		dbMDT.setTime(time);
		return dbMDT;
	}

	/**
	 * This is going back to the client.
	 */	
	private MDTrack makeMDTrackFromPhiMdt(Mdt dbMDT) throws ICSDatastoreException {
		MDTrack mdtrack = new MDTrack();
		
		mdtrack.setAccuracy(dbMDT.getAccuracy());
		mdtrack.setCourse(dbMDT.getCourse());
		mdtrack.setCreatedUTC(dbMDT.getTime());
		// MDTs are never updated. For API consistency, we artificially
		// fill in the last updated UTC field, below.
		mdtrack.setLastUpdatedUTC(dbMDT.getTime());
		mdtrack.setDeviceId(dbMDT.getDeviceId());
		mdtrack.setMdtId(dbMDT.getId());
		mdtrack.setSpeed(dbMDT.getSpeed());
		mdtrack.setUserId(dbMDT.getUserInfo().getUserId());		
		
		Geometry geo = dbMDT.getLocation();
		if (geo != null) {
			Coordinate lla = geo.getCoordinate();
			if (lla != null) {
				mdtrack.setLongitude(lla.x);
				mdtrack.setLatitude(lla.y);
				mdtrack.setAltitude(lla.z);
			} else {
				APILogger.getInstance().w(CNAME,
						"MDT-ID: " + dbMDT.getId() + ". " + 
						"A geometry value was found but could not extract a Coordinate from it.");
			}
		}
		
		return mdtrack;
	}
	
	private Set<Integer> getSortedUserIdSet(String userIdList) {
		Set<Integer> userIds = new TreeSet<Integer>();
		if (userIdList == null || userIdList.isEmpty()) {
			return  userIds;
		}
		String[] tokens = StringUtils.split(userIdList, ',');
		for (int n = 0; n < tokens.length; ++n) {
			try {
				userIds.add(Integer.parseInt(tokens[n]));
			} catch (Exception e) {
				// Just skip the non-integer item in the list.
			}
		}
		return userIds;
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
	
	private boolean validateMDTProperties() {
				
		gmlTopic = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_TOPIC);
		if(gmlTopic == null || gmlTopic.isEmpty()) {
			gmlTopic = "NICS.mdt.gml";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_TOPIC + " in " + APIConfig.API_PROPS_FILE + " file. Using " +
							"default: NICS.mdt.gml");
		}
		
		nicsSchemaLocationURI = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_NICS_SCHEMA_URI);
		if(nicsSchemaLocationURI == null || nicsSchemaLocationURI.isEmpty()) {
			nicsSchemaLocationURI = "http://mapserver.nics.ll.mit.edu/NICS";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_NICS_SCHEMA_URI + " in " + APIConfig.API_PROPS_FILE + 
					" file. Using " + "default: http://mapserver.nics.ll.mit.edu/NICS");
		}
		
		wfsSchemaURI = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_WFS_SCHEMA_URI);
		if(wfsSchemaURI == null || wfsSchemaURI.isEmpty()) {
			
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_WFS_SCHEMA_URI + " in " + APIConfig.API_PROPS_FILE + " file. " +
							"CANNOT continue without this property!");
			return false;
		}
		
		wfsServiceURI = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_WFS_SERVICE_URI);
		if(wfsServiceURI == null || wfsServiceURI.isEmpty()) {
			
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_WFS_SERVICE_URI + " in " + APIConfig.API_PROPS_FILE + " file. " +
							"CANNOT continue without this property!");
			return false;
		}
		
		typeName = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_TYPE_NAME);
		if(typeName == null || typeName.isEmpty()) {
			
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_TYPE_NAME + " in " + APIConfig.API_PROPS_FILE + " file. " +
							"CANNOT continue without this property!");
			
			return false;
		}
		
		srsName = APIConfig.getInstance().getConfiguration().getString(APIConfig.MDT_SRS_NAME);
		if(srsName == null || srsName.isEmpty()) {
			srsName = "EPSG:4326";
			APILogger.getInstance().w(CNAME,
					"No value found for key " + APIConfig.MDT_SRS_NAME + " in " + APIConfig.API_PROPS_FILE + " file. Using " +
							"default: EPSG:4326");
		}
		
		validated = true;
		return true;
	}
}


