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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.ListUtils;
import org.eclipse.jetty.util.log.Log;
import org.json.JSONArray;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

import edu.mit.ll.em.api.rs.MapMarkup;
import edu.mit.ll.em.api.rs.MapMarkupOptionalParams;
import edu.mit.ll.em.api.rs.QueryConstraintHelper;
import edu.mit.ll.em.api.rs.impl.TopicBuilder;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.GeomUtil;
import edu.mit.ll.em.api.util.GeomUtilException;
import edu.mit.ll.em.api.util.NetUtil;
import edu.mit.ll.em.api.util.TimeUtil;
import edu.mit.ll.em.api.util.rabbitmq.RabbitFactory;
import edu.mit.ll.em.api.util.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.CollabRoom;
import edu.mit.ll.nics.common.entity.CollabroomFeature;
import edu.mit.ll.nics.common.entity.DeletedFeature;
import edu.mit.ll.nics.common.entity.Feature;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.SADisplayRemoveEntity;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.messages.sadisplay.SADisplayMessage;


public class MapMarkupDAO extends BaseDAO {

	private static final String CNAME = MapMarkupDAO.class.getName();
	private RabbitPubSubProducer producer;
	private AtomicLong seqNum = new AtomicLong(0);
	private Pattern keysPattern = null;
	private static final String KEYS_PATTERN = "\"keys\":\\[([^\\]]*)";

	private MapMarkupDAO() {
		try {
			producer = RabbitFactory.makeRabbitPubSubProducer();
		} catch (IOException e) {
			throw new ExceptionInInitializerError(
					CNAME
					+ "Unable to create the RabbitPubSubProducer. "
					+ e.getMessage());
		}
		keysPattern = Pattern.compile(KEYS_PATTERN);		
	}

	public void finalize() {	
		freeResources();
	}

	public void freeResources() {
		if (producer != null) {
			producer.destroy();
		}
	}	

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static MapMarkupDAO instance = new MapMarkupDAO();
	}

	public static MapMarkupDAO getInstance() {
		return Holder.instance;
	}

/*
	public MapMarkup createMapMarkup(int workspaceId,
			MapMarkup mapMarkup) throws ICSDatastoreException {
		validateArgs(mapMarkup);
		User user = EntityCacheMgr.getInstance().getUserEntity(mapMarkup.getSenderUserId());
		Incident incident = EntityCacheMgr.getInstance().getIncidentEntity(
				mapMarkup.getIncidentId());
		CollabRoom cr = EntityCacheMgr.getInstance().getCollabRoomEntity(
				mapMarkup.getCollabRoomId());
		MathTransform trans = null;
		try {			
			trans = GeomUtil.getInstance()
					// TODO: Don't assume Android; add a parameter or an attribute to MapMarkup.					
					.createTransform(GeomUtil.NICS_EPSG, GeomUtil.ANDROID_EPSG);
		} catch (GeomUtilException e1) {
			ICSDatastoreException.handle(CNAME, "", e1);
		}		
		for (MapMarkup.Feature f : mapMarkup.getFeatures()) {
			SADisplayMessage sadMsg = new SADisplayMessage();
			// Assemble and add the Feature entity.
			Feature nicsFeature = makeNicsFeature(user, workspaceId, incident,
					cr, f, trans);
			sadMsg.addEntity(nicsFeature);

			// Assemble and add the CollabroomFeature entity.
			CollabroomFeature collabroomFeature = new CollabroomFeature(mapMarkup.getCollabRoomId(),
					nicsFeature.getFeatureid());
			sadMsg.addEntity(collabroomFeature);

			// Assemble and add the messageData needed to trigger a COP refresh.
			Map<String, Object> msgData = makeMessageData(nicsFeature, mapMarkup);
			sadMsg.setMessageData(msgData);
			sadMsg.setMessageType("feat");			
			try {
				producer.produce(nicsFeature.getTopic(), sadMsg.toJSONString());
			} catch (IOException e) {
				ICSDatastoreException.logIt(CNAME,
						"Unable to send SADisplayMessage via NICS BUS.", e);
			}
		}
		return mapMarkup;
	}

	public MapMarkup getMapMarkupByCollabRoom(int collabRoomId,
			MapMarkupOptionalParams opts) 
					throws PhinicsDbException, ICSDatastoreException {
		
		PAPILogger.getInstance().d(CNAME, "\nMAPMARKUP: all by collabroom id\n");
		MapMarkup ret = null;
		EntityManager em = this.allocEntityManager();
		int maxRowsLimit = PAPIConfig.getInstance().getConfiguration()
				.getInt("phinics.papi.db.get.maxrows", 1000);
		// Protect the server from requests that yield an unreasonable number of rows.
		if (opts.getLimit() == null || opts.getLimit() > maxRowsLimit) {
			PAPILogger.getInstance().i(CNAME, "Rewriting max. rows LIMIT as " + maxRowsLimit);
			opts.setLimit(maxRowsLimit);
		}
		// Decide what date column we'll be constraining by.
		if (opts.getDateColumn() == null || opts.getDateColumn().isEmpty()) {
			opts.setDateColumn("seqtime");
		}		
		// Decide what column we'll be sorting by.
		if (opts.getSortByColumn() == null || opts.getSortByColumn().isEmpty()) {
			//opts.setSortByColumn("seqtime");
			opts.setSortByColumn(opts.getDateColumn());
		}		
		try {
			PAPILogger.getInstance().d(CNAME, "\n\nMaking query with options:\n\t" +
					"dateColumn: " + opts.getDateColumn() +
					"\n\tsortBy: " + opts.getSortByColumn() +					
					"\n\tfromDate: " + opts.getFromDate() +
					"\n\ttoDate: " + opts.getToDate()
			);
			// Pick up all Features that meet the provided constraints.
			List<Feature> features = new ArrayList<Feature>(); 
			
			PAPILogger.getInstance().d(CNAME, "readFeatures...");			
			// When this is called with the lastupdate field, as it should, it'll pick
			// up both new features, as well as moved or edited ones.
			features = PhinicsDbFactory.getInstance()
					.getPhinicsDbFacadeSingleton()
					.readFeatures(
							collabRoomId,
							QueryConstraintHelper.makeDateRange(opts),
							QueryConstraintHelper.makeResultSetRange(opts),
							QueryConstraintHelper.makeOrderBy(opts),							
							em);			
			
			PAPILogger.getInstance().d(CNAME, "getDeletedFeatures...");
			// Gets just the deleted feature IDs for the specified collabroom and time range
			Set<String> deletedFeatureIDs = getDeletedFeatures(collabRoomId, opts);			
			
			PAPILogger.getInstance().d(CNAME, "makeMapMarkup...");
			// The moved parameter is null, since there's a fix that updated the lastupdate timestamp, 
			// so moved features should now show up in the main features list, and deleted features are
			// are populated in the list of IDs
			//TODO: refactor makeMapMarkup to not need the moved parameter
			ret = makeMapMarkup(features, collabRoomId, null, deletedFeatureIDs);
			
		} finally {
			this.freeEntityManager(em);
		}
		return ret;
	}

	public Set<Feature> getAllMapMarkupIds() {

		Set<Feature> ret = new LinkedHashSet<Feature>();
		// TODO: Insert implementation here.
		return ret;
	}

	public MapMarkup getMapMarkupById(int id) {
		MapMarkup ret = null;
		// TODO: Insert implementation here. 
		return ret;
	}

	public void removeMapMarkup(int workspaceId, String id) throws ICSDatastoreException {
		// TODO:1119 to support DeletedFeature, also need to add a UpdateEntity for DeletedFeature
		SADisplayMessage sadMsg = new SADisplayMessage();
		sadMsg.setTime(Long.toString(Calendar.getInstance().getTimeInMillis()));

		JSONArray keys = new JSONArray();
		keys.put(id);

		SADisplayRemoveEntity entity = new SADisplayRemoveEntity(keys, Feature.class.getCanonicalName());
		sadMsg.addEntity(entity);

		// Get the incident/collabroom --> topic
		CollabroomFeature feat = PhinicsDbFactory.getInstance().getPhinicsDbFacadeSingleton().readCollabroomFeature(id);
		if(feat == null) {
			throw new ICSDatastoreException("CollabroomFeature not found for featureId: " + id);
		}

		String topic2 = TopicBuilder.makeMarkupTopic(workspaceId,
				feat.getCollabroom().getIncidentid(), feat.getCollabroomid());

		Map<String, Object> msgData = makeRemoveMessageData(topic2, feat);
		sadMsg.setMessageData(msgData);
		sadMsg.setMessageType("feat");	

		try {
			producer.produce(topic2, sadMsg.toJSONString());
		} catch (IOException e) {
			ICSDatastoreException.logIt(CNAME,
					"Unable to send SADisplayMessage via NICS BUS.", e);
		}
	}

	public MapMarkup updateMapMarkup(int mapMarkupId, MapMarkup other) throws ICSDatastoreException {
		MapMarkup ret = null;
		if (other == null) {
			throw new NullPointerException(CNAME + ":updateMapMarkup called with null mapMarkup argument");
		}
		// TODO: Insert implementation here.
		return ret;
	}

	public int getMapMarkupCount() {
		int count = -1;
		// TODO: Insert implementation here.
		return count;
	}

	private static Map<String, Object> makeMessageData(Feature f, MapMarkup m) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("topic", f.getTopic());
		ret.put("usersessionid", f.getUsersessionid());
		ret.put("time", f.getTime());
		ret.put("nickname", f.getNickname());
		ret.put("featureType", "draw");
		ret.put("collabroomid", Integer.toString(m.getCollabRoomId()));
		ret.put("userid", m.getSenderUserId());
		ret.put("seqtime", f.getSeqtime());
		ret.put("seqnum", f.getSeqnum());
		ret.put("ip", f.getIp());
		ret.put("version", f.getVersion());
		return ret;
	}	

	private Map<String, Object> makeRemoveMessageData(String topic, CollabroomFeature f) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("topic", topic);
		ret.put("usersessionid", f.getFeature().getUsersessionid());
		ret.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));
		ret.put("featureType", "remove");
		ret.put("collabroomid", f.getCollabroomid());
		ret.put("seqtime", Long.toString(Calendar.getInstance().getTimeInMillis()));
		ret.put("seqnum", this.getMessageSequenceNumber());
		ret.put("ip", "0.0.0.0");
		ret.put("version", "1.2.3");
		return ret;
	}

	private MapMarkup.Feature[] makePapiFeaturesFromNicsFeatures(
			List<Feature> features, int collabRoomId, MathTransform trans) {
		MapMarkup.Feature[] papiFeatures = new MapMarkup.Feature[features.size()];
		int n = 0;
		for (Feature f : features) {
			MapMarkup.Feature pf = new MapMarkup.Feature();			
			pf.dashStyle = f.getDashStyle();
			pf.featureAttrs = f.getFeatureattributes();
			pf.featureId = f.getFeatureid();
			pf.fillColor = f.getFillColor();
			pf.graphic = f.getGraphic();	
			pf.graphicHeight = f.getGraphicHeight();
			pf.graphicWidth = f.getGraphicWidth();
			pf.ipAddr = f.getIp();
			pf.isGesture = f.isGesture();			
			pf.labelSize = f.getLabelsize();
			pf.labelText = f.getLabelText();
			pf.nickname = f.getNickname();
			pf.opacity = f.getOpacity();
			try {
				pf.points = GeomUtil.getInstance().makePointsFromGeom(
						f.getTheGeom(), trans);
			} catch (GeomUtilException e) {
				PAPILogger.getInstance().w(CNAME,
						"Unable to make Points from Geometry for collabRoomId " + collabRoomId);
				// Keep on truckin'.
			} catch (Exception e) {
				PAPILogger.getInstance().e(CNAME,
						"Unable to make Points from Geometry for collabRoomId " + collabRoomId
						+ ". Unexpected condition: " + e.getMessage());
				// Keep on truckin'.
			}
			pf.radius = f.getPointRadius();
			pf.rotation = f.getRotation();
			pf.seqNum = f.getSeqnum();
			pf.strokeColor = f.getStrokeColor();
			pf.strokeWidth = f.getStrokeWidth();
			pf.seqTime = f.getSeqtime();
			pf.time = f.getTime(); 
			pf.topic = f.getTopic();
			pf.type = f.getType();
			
			// TODO:LDDRS-1119: Mobile wants unix epoch timestamp
			// Make sure this date/gettime returns in UTC
			pf.lastUpdate = f.getLastupdate().getTime();
			
			papiFeatures[n++] = pf;
		}

		return papiFeatures;
	}


	
	 // Constructs a MapMarkup container to return back to the caller.
	// In addition, any Features that have been moved are added to the
	// returned MapMarkup.
	// @param features  List of (new) features.
	// @param collabRoomId Collaboration room ID associated with these features.
	// @param movedFeatureIds IDs of all moved Features.
	// @param deletedFeatureIds IDs of all deleted Features.
	// @return A MapMarkup container with all Features that meet the requested
	// criteria.
	// @throws ICSDatastoreException
	 
	private MapMarkup makeMapMarkup(List<Feature> features, int collabRoomId,
			Set<String> movedFeatureIds, Set<String> deletedFeatureIds)
					throws ICSDatastoreException {

		MapMarkup m = new MapMarkup();
		m.setCollabRoomId(collabRoomId);
		m.setDeletedFeatureIds(deletedFeatureIds);
		m.setMovedFeatureIds(movedFeatureIds);
		//m.setLastUpdatedUTC(lastUpdatedUTC);
		//m.setSenderUserId(senderId);
		//m.setSeqTime(seqTime);

		CollabRoom cr = null;
		try {
			cr = EntityCacheMgr.getInstance().getCollabRoomEntity(collabRoomId);
			m.setIncidentId(cr.getIncidentid());			
		} catch (ICSDatastoreException e) {
			PAPILogger.getInstance().w(CNAME,
					"Unable to obtain entity for collabRoomId " + collabRoomId);
			// Keep on truckin'.
		}

		// Now grab all of the moved Features.
		Set<String> neededFeatureIds = null;
		if (movedFeatureIds != null && !movedFeatureIds.isEmpty()) {
			// If for some reason, a moved feature is already in the features
			// list, do not bother grabbing that one.
			neededFeatureIds = new HashSet<String>(movedFeatureIds);
			for (Feature f : features) {
				if (neededFeatureIds.contains(f.getFeatureid())) {
					neededFeatureIds.remove(f.getFeatureid());
				}
			}			
		}
		List<Feature> movedFeatures = null;		
		if (neededFeatureIds != null && !neededFeatureIds.isEmpty()) {
			EntityManager em = this.allocEntityManager();
			try {
				movedFeatures = PhinicsDbFactory.getInstance().
						getPhinicsDbFacadeSingleton().
						readFeaturesById(neededFeatureIds, em);
			} finally {
				this.freeEntityManager(em);
			}
		}

		// Get new features and moved features into a single array of MapMarkup
		// Features.
		List<Feature> allFeatures = new ArrayList<Feature>();
		if (features != null && !features.isEmpty()) {
			allFeatures.addAll(features);
		}
		if (movedFeatures != null && !movedFeatures.isEmpty()) {
			allFeatures.addAll(movedFeatures);			
		}
		MapMarkup.Feature[] papiFeatures = null;
		if (!allFeatures.isEmpty()) {
			// TODO: We should pass an arg. stating the source Geo Coord System.
			MathTransform trans = null;
			try {
				trans = GeomUtil.getInstance()
						.createTransform(GeomUtil.ANDROID_EPSG, GeomUtil.NICS_EPSG);
			} catch (GeomUtilException e1) {
				ICSDatastoreException.handle(CNAME, "", e1);
			}
			papiFeatures = makePapiFeaturesFromNicsFeatures(allFeatures, collabRoomId, trans);
		} else {
			papiFeatures = new MapMarkup.Feature[0];
		}
		m.setFeatures(papiFeatures);		

		return m;
	}

	private Feature makeNicsFeature(User u, int workspaceId, 
			Incident i, CollabRoom cr, MapMarkup.Feature f,
			MathTransform trans)
					throws ICSDatastoreException {
		Feature ret = new Feature();

		if (f.dashStyle != null) {
			ret.setDashStyle(f.dashStyle);
		}
		if (f.featureAttrs != null) {
			ret.setFeatureattributes(f.featureAttrs);
		}
		if (f.featureId != null) {
			ret.setFeatureid(f.featureId);
		}
		if (f.fillColor != null) {
			ret.setFillColor(f.fillColor);
		}
		if (f.isGesture != null) {
			ret.setGesture(f.isGesture);
		}
		if (f.graphic != null) {
			ret.setGraphic(f.graphic);
			ret.setHasGraphic(true);
		} else {
			ret.setHasGraphic(false);			
		}
		if (f.graphicHeight != null) {
			ret.setGraphicHeight(f.graphicHeight);
		}
		if (f.graphicWidth != null) {
			ret.setGraphicWidth(f.graphicWidth);
		}
		if (f.ipAddr != null) {
			ret.setIp(f.ipAddr);
		} else {
			ret.setIp(NetUtil.getInstance().getExternalAddresses()
					.iterator().next());
		}
		if (f.labelSize != null) {
			ret.setLabelsize(f.labelSize);
		}
		if (f.labelText != null) {
			ret.setLabelText(f.labelText);
		}
		ret.setLastupdate(TimeUtil.getNowAsDate());
		if (f.nickname != null && !f.nickname.isEmpty()) {
			ret.setNickname(f.nickname);
		} else {
			ret.setNickname(u.getFirstname() + " " + u.getLastname());
		}
		if (f.opacity != null) {
			ret.setOpacity(f.opacity);
		}
		if (f.radius != null) {
			ret.setPointRadius(f.radius);
		}
		if (f.rotation != null) {
			ret.setRotation(f.rotation);
		}
		ret.setSeqnum(this.seqNum.getAndIncrement());
		ret.setSeqtime(TimeUtil.getNowAsMillis());
		if (f.strokeColor != null) {
			ret.setStrokeColor(f.strokeColor);
		}
		if (f.strokeWidth != null) {
			ret.setStrokeWidth(f.strokeWidth);
		}
		try {
			Geometry g = makeGeometry(f.points, f.type, f.radius);
			if (trans == null) {
				ret.setTheGeom(g);
			} else {
				ret.setTheGeom(GeomUtil.getInstance()
						.transformGeometry(g, trans));
			}
		} catch (IllegalArgumentException e) {
			ICSDatastoreException.handle(CNAME+"->makeNicsFeature",
					"Bad or missing Geometry feature parameter(s)", e);
		} catch (GeomUtilException e) {
			ICSDatastoreException.handle(CNAME+"->makeNicsFeature",
					"Unable to set Geomerty attribute.", e);
		}
		if (f.seqTime != null) {
			ret.setTime(TimeUtil.getTimeFromMillisAsNICSDate(f.seqTime));
		} else {
			ret.setTime(TimeUtil.getNowAsNICSDate());
		}

		String topic = TopicBuilder.makeMarkupTopic(workspaceId, i.getIncidentid(),
				cr.getCollabRoomId());
		ret.setTopic(topic);
		if (f.type != null) {
			// TODO: sa-datalayer-messages.js:composeFeatureMessage uses messageType: "feat"?
			//       featureType = "draw" : "move"
			ret.setType(toFeatureType(f.type));
		}
		StringBuilder sb = new StringBuilder();
		sb.append(u.getFirstname()).append(" ").append(u.getLastname())
		.append(" (").append(u.getUsername()).append(")");
		ret.setUser(sb.toString());
		ret.setUsersessionid(EntityCacheMgr.getInstance().getUserSessionId(u.getUserId()));
		ret.setVersion(PAPIConfig.getInstance().getConfiguration()
				.getString("phinics.papi.rabbitmq.msgver", "1.2.3"));
		ret.setFeatureid(UUID.randomUUID().toString().toUpperCase());
		return ret;
	}

	private static Geometry makeGeometry(Double[][] points, String type, Double radius)
			throws GeomUtilException {
		Geometry geom = null;
		if (points == null) {
			throw new NullPointerException("Unexpected null argument \"points\"");
		}	
		if (points.length < 1) {
			throw new IllegalArgumentException("points.length must at least one point.");
		}
		if (type == null) {
			throw new NullPointerException("Unexpected null argument \"type\"");
		}			
		if (type.equals("circle") && radius == null) {
			throw new IllegalArgumentException("A radius is necessary for creating a circle.");
		}

		if (type.equals("point")) {
			geom = GeomUtil.getInstance().createPoint(points[0]);
		} else if (type.equals("line")) {
			geom = GeomUtil.getInstance().createLine(points);
		} else if (type.equals("box") || type.equals("polygon")) {
			geom = GeomUtil.getInstance().createPolygon(points);
		} else if (type.equals("circle")) {
			geom = GeomUtil.getInstance().createCircle(points[0], radius);
			// TODO:LDDRS-1119 testing polygon...
			//geom = GeomUtil.getInstance().createPolygon(points);
		} else {
			throw new GeomUtilException("Unknown type[" + type + 
					"]. makeGeometry() ignoring operation");
		}
		return geom;
	}

	private static String toFeatureType(String type) {
		String ret = type.toLowerCase();
		if (ret.equals("rectangle")) {
			ret = "box";
		}
		return ret;
	}

	private void validateArgs(MapMarkup mapMarkup) {
		if (mapMarkup == null) {
			throw new NullPointerException(CNAME + "Unexpected null mapMarkup parameter.");
		}
		if (mapMarkup.getFeatures() == null && mapMarkup.getFeatures().length < 1) {
			throw new IllegalArgumentException("No features attribute found in mapMarkup parameter.");
		}
		if (mapMarkup.getSenderUserId() == null || mapMarkup.getSenderUserId() < 0) {
			throw new IllegalArgumentException("Missing or invalid senderUserId attribute in mapMarkup parameter.");			
		}
	}

	private long getMessageSequenceNumber() {
		EntityManager em = this.allocEntityManager();
		BigInteger seqnum = null;
		long retval = -1;
		try {
			seqnum = (BigInteger) em
					.createNativeQuery("select nextval('message_sequence')").getSingleResult();
			if(seqnum != null) {
				retval = seqnum.longValue();
			}
		} finally {
			this.freeEntityManager(em);
		}
		return retval;
	}

	private static Set<String> getFeatureIds(String s, Pattern pat) {
		Set<String> ret = new HashSet<String>();
		Matcher matcher = pat.matcher(s);
		while (matcher.find()) {
			String val = matcher.group(1);
			val = val.replaceAll("\"", "");
			for (String v : val.split(","))
				ret.add(v);
		}
		return ret;
	}

	private Set<String> getDeletedFeaturesOld(String topic,
			MapMarkupOptionalParams opts)
					throws PhinicsDbException {
		return getMovedOrRemovedFeatures("\"remove\"", topic, opts);
	}
	
	
	 //* Returns a Set of Strings of deleted Feature IDs
	  
	 //* TODO: Refactor to be named property to return FeatureIDs
	 //* 
	 //* @param collabRoomId The collabroomid to check deleted features for
	 //* @param opts Query parameters
	 //* @return 
	 //* @throws PhinicsDbException
	 
	private Set<String> getDeletedFeatures(int collabRoomId,
			MapMarkupOptionalParams opts)
					throws PhinicsDbException {
		
		// DeletedFeatures don't have a seqtime, but do have a timestamp...
		opts.setDateColumn("timestamp");
		opts.setSortByColumn("timestamp");
		
		EntityManager em = this.allocEntityManager();
		
		List<DeletedFeature> deletedFeatures = new ArrayList<DeletedFeature>();
		try {
			deletedFeatures = PhinicsDbFactory.getInstance()
					.getPhinicsDbFacadeSingleton()
					.readDeletedFeatures(
							collabRoomId,
							QueryConstraintHelper.makeDateRange(opts),
							QueryConstraintHelper.makeResultSetRange(opts),
							QueryConstraintHelper.makeOrderBy(opts),							
							em);
			
		} finally {
			this.freeEntityManager(em);
		}
				
		if(deletedFeatures.isEmpty()) {
			return new HashSet<String>();
		}
		
		Set<String> deletedFeatureIDs = new HashSet<String>();
		for(DeletedFeature df : deletedFeatures) {
			deletedFeatureIDs.add(df.getFeatureid());
		}
		
		return deletedFeatureIDs;
	}

	private Set<String> getMovedFeatures(String topic,
			MapMarkupOptionalParams opts)
					throws PhinicsDbException {
		return getMovedOrRemovedFeatures("\"move\"", topic, opts);
	}

	
	// * The MessageArchive table contains all the history of Feature requests.
	// * Use it to determine what features have been changed or deleted within
	// * some window of time.
	// * @param featureType Either "move" or "remove".
	// * @param topic Indicates the Incident and Collabroom of interest
	// * @param opts Options such as a window of time, for example.
	// * @return A set of Feature IDs that fit the request.
	// * @throws PhinicsDbException	
	private Set<String> getMovedOrRemovedFeatures(String featureType, String topic,
			MapMarkupOptionalParams opts)
					throws PhinicsDbException {
		List<String> messages = null;
		Set<String> keys = new HashSet<String>();
		EntityManager em = this.allocEntityManager();
		try {
			// Pick up all Message columns associated with MessageArchive entries
			// that represent a Feature move.
			messages = PhinicsDbFactory.getInstance()
					.getPhinicsDbFacadeSingleton()
					.readArchiveMsgsByCollabRoom(
							topic,
							featureType,
							QueryConstraintHelper.makeDateRange(opts),
							QueryConstraintHelper.makeResultSetRange(opts),
							QueryConstraintHelper.makeOrderBy(opts),							
							em);

			if (messages == null || messages.isEmpty()) {
				return keys;
			}

			// Parse the Feature IDs from the alteredMessages list.
			for (String m : messages) {
				keys.addAll(getFeatureIds(m, keysPattern));
			}

		} finally {
			this.freeEntityManager(em);
		}

		return keys;
	}	
	*/
}

