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
package edu.mit.ll.em.api.rs.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.ll.em.api.rs.FeatureService;
import edu.mit.ll.em.api.rs.FeatureServiceResponse;
import edu.mit.ll.em.api.rs.MultipartFeatureResponse;
import edu.mit.ll.em.api.rs.QueryConstraintHelper;
import edu.mit.ll.em.api.rs.QueryConstraintParms;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.em.api.util.rabbitmq.RabbitFactory;
import edu.mit.ll.em.api.util.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.CollabroomFeature;
import edu.mit.ll.nics.common.entity.Feature;
import edu.mit.ll.nics.common.entity.UserFeature;
import edu.mit.ll.nics.common.entity.datalayer.Document;
import edu.mit.ll.nics.nicsdao.CollabRoomDAO;
import edu.mit.ll.nics.nicsdao.DocumentDAO;
import edu.mit.ll.nics.nicsdao.FeatureDAO;
import edu.mit.ll.nics.nicsdao.UserDAO;
import edu.mit.ll.nics.nicsdao.impl.CollabRoomDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.DocumentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.FeatureDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.query.QueryConstraint.UTCRange;

/**
 * 
 * @AUTHOR st23420
 *
 */
public class FeatureServiceImpl implements FeatureService {

	private static final String FEATURES_SUBPATH = "features";
	/** Feature DAO */
	private static final FeatureDAO featureDao = new FeatureDAOImpl();
	/** User CollabRoom DAO */
	private static final CollabRoomDAO collabRoomDao = new CollabRoomDAOImpl();
	/** User Document DAO */
	private static final DocumentDAO documentDao = new DocumentDAOImpl();
	/** User DAO */
	private static final UserDAO userDao = new UserDAOImpl();
	
	private static final Log logger = LogFactory.getLog(FeatureServiceImpl.class);
	
	//The featureId in the constants file is all lower case
	//Does it need to be for Mapping?
	private static final String FEATURE_ID = "featureId"; 
	
	//The property for the collabroom topic - telling users this feature has been deleted
	private static final String DELETED_FEATURE_ID = "deletedFeatureId";
	
	private RabbitPubSubProducer rabbitProducer;
	
	private final String fileUploadPath;
	private final String fileUploadUrl;
	
	
	public FeatureServiceImpl() {
		Configuration config = APIConfig.getInstance().getConfiguration();
		fileUploadPath = config.getString(APIConfig.FILE_UPLOAD_PATH, "/opt/data/nics/upload");
		fileUploadUrl = config.getString(APIConfig.FILE_UPLOAD_URL, "/static/");
	}
	
	/**
	 * Retrieve features for a collaboration room
	 * 
	 * @param collabroomId
	 * @param optionalParams
	 * 
	 * @return Response FeatureServiceResponse containing features
	 * @See FeatureServiceResponse
	 */
	public Response getCollabroomFeatures(int collabroomId, long userId, 
			QueryConstraintParms optionalParams, int geoType, String requestingUser) {
		
		UTCRange dateRange = QueryConstraintHelper.makeDateRange(optionalParams);
		
		if(userDao.getUserId(requestingUser) == userId && 
				collabRoomDao.hasPermissions(userId, collabroomId)){
			List<Feature> features = featureDao.getFeatureState(collabroomId, dateRange, geoType);
			buildDocumentUrls(features);
			
			if(dateRange != null){
				System.out.println("Get deleted features");
				
				return this.buildGetResponse(features,
						featureDao.getDeletedFeatures(collabroomId, dateRange));
			}else{
				return this.buildGetResponse(features);
			}
		}else{
			FeatureServiceResponse featureResponse = new FeatureServiceResponse();
			featureResponse.setMessage("Permission denied to view this room");
			return Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Retrieve features for a user
	 * 
	 * @param userId
	 * 
	 * @return Response FeatureServiceResponse containing features
	 * @See FeatureServiceResponse
	 */
	public Response getUserFeatures(int userId, String requestingUser) {
		if(userDao.getUserId(requestingUser) == userId){
			List<Feature> features = featureDao.getUserFeatureState(userId);
			this.buildDocumentUrls(features);
			return this.buildGetResponse(features);
		}else{
			return getAccessDeniedResponse();
		}
	}
	
	/**
	 * Update the feature
	 * @param collabRoomId - the id of the collaboration room that the feature belongs to
	 * @param feature - JSON string of feature properties
	 * 
	 * @return Response FeatureServiceResponse containing the updated feature
	 * @See FeatureServiceResponse
	 */
	public Response updateFeature(int collabRoomId, String feature, String requestingUser){
		Response response;
		Long featureId = null;
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		
		if(!collabRoomDao.hasPermissions(userDao.getUserId(requestingUser), collabRoomId)){
			return getAccessDeniedResponse();
		}
		
		try{
			featureId = this.persistFeatureChange(feature);
			featureDao.setCollabroomFeatureDeleted(featureId, false);
		
			featureResponse.setMessage(Status.OK.getReasonPhrase());
			
			response = Response.ok(featureResponse).status(Status.OK).build();
		}catch(Exception e){
			featureResponse.setMessage("Unhandled exception while persisting Feature change: " + e.getMessage());
			response = Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if(Status.OK.getStatusCode() == response.getStatus()){
			try {
				Feature newFeature = featureDao.getFeature(featureId); 
				this.buildDocumentUrls(Arrays.asList(newFeature));
				notifyNewFeature(newFeature, 
						String.format("iweb.NICS.collabroom.%s.changefeature", collabRoomId));
			} catch (Exception e) {
				String errorMessage = "Failed to publish a CollabRoom Feature Change message event";
				featureResponse.setMessage(errorMessage);
				
				logger.error(errorMessage, e);
			}
		}
		
		return response;
	}
	
	/**
	 * Update the user feature
	 * @param feature - JSON string of feature properties
	 * 
	 * @return Response FeatureServiceResponse containing the updated feature
	 * @See FeatureServiceResponse
	 */
	public Response updateUserFeature(String feature){
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		try{
			long featureId = this.persistFeatureChange(feature);
			featureDao.setUserFeatureDeleted(featureId, false);
			
			featureResponse.setMessage(Status.OK.getReasonPhrase());
			return Response.ok(featureResponse).status(Status.OK).build();
		}catch(Exception e){
			featureResponse.setMessage("Unhandled exception while persisting Feature change: " + e.getMessage());
		}
		return Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Creates a feature in a collaboration room
	 * 
	 * @param collabroomId
	 * @param feature
	 * 
	 * @return Response A FeatureServiceResponse
	 */
	public Response postCollabRoomFeature(int collabRoomId, String feature, String requestingUser) {
		
		if(!collabRoomDao.hasPermissions(userDao.getUserId(requestingUser), collabRoomId)){
			return getAccessDeniedResponse();
		}
		
		Response response;
		Feature newFeature = null;
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		try {
			newFeature = this.addNewFeature(feature);
			if(newFeature != null){
				//Add CollabRoom Feature
				CollabroomFeature collabroomFeature = new CollabroomFeature();
				collabroomFeature.setFeatureId(newFeature.getFeatureId());
				collabroomFeature.setCollabroomid(collabRoomId);
				
				featureDao.addCollabroomFeature(collabroomFeature);
			}
			this.buildDocumentUrls(Arrays.asList(newFeature));
			this.updatePostResponse(featureResponse, newFeature);
			
			response = Response.ok(featureResponse).status(Status.OK).build();
		} catch (Exception e) {
			featureResponse.setMessage("Unhandled exception while persisting CollabRoom Feature: " + e.getMessage());
			response = Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if(Status.OK.getStatusCode() == response.getStatus()){
			try {
				String topic = String.format("iweb.NICS.collabroom.%s.feature", collabRoomId);
				notifyNewFeature(newFeature, topic);
			} catch (Exception e) {
				String errorMessage = "Failed to publish a CollabRoom Feature message event";
				featureResponse.setMessage(errorMessage);
	
				logger.error(errorMessage, e);
			}
		}
		
		return response;
	}
	
	/**
	 * Creates a feature in the my map space
	 * 
	 * @param userId
	 * @param feature
	 * 
	 * @return Response A FeatureServiceResponse
	 */
	public Response postUserFeature(long userId, String feature, String requestingUser) {
		
		if(userDao.getUserId(requestingUser) != userId){
			return getAccessDeniedResponse();
		}
		
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		try {	
			Feature newFeature = this.addNewFeature(feature);
			if(newFeature != null){
				//Add User Feature
				UserFeature userFeature = new UserFeature();
				userFeature.setFeatureId(newFeature.getFeatureId());
				userFeature.setUserId(userId);
				
				featureDao.addUserFeature(userFeature);
			}
			this.buildDocumentUrls(Arrays.asList(newFeature));
			this.updatePostResponse(featureResponse, newFeature);
			
			return Response.ok(featureResponse).status(Status.OK).build();
		} catch (Exception e) {
			featureResponse.setMessage("Unhandled exception while persisting User Feature: " + e.getMessage());
		}

		return Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Deletes a feature in the my map space
	 * 
	 * @param featureId
	 * 
	 * @return Response A FeatureServiceResponse
	 */
	public Response deleteUserFeature(long featureId){
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		try{
			int numOfRows = featureDao.setUserFeatureDeleted(featureId, true);
			
			if(numOfRows != -1){ //Could probably check for 1 since it should be unique...
				this.updatePostResponse(featureResponse, featureId);
				return Response.ok(featureResponse).status(Status.OK).build();
			}else{
				featureResponse.setMessage("The feature was not successfully removed from the user map");
			}
		}catch(Exception e){
			featureResponse.setMessage("Unhandled exception while deleting User Feature: " + e.getMessage());
		}
		return Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Deletes a feature from a collaboration room
	 * 
	 * @param collabRoomId
	 * @param featureId
	 * 
	 * @return Response A FeatureServiceResponse
	 */
	public Response deleteCollabRoomFeature(int collabRoomId, long featureId, String requestingUser){
		
		if(!collabRoomDao.hasPermissions(userDao.getUserId(requestingUser), collabRoomId)){
			return getAccessDeniedResponse();
		}
		
		Response response;
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		try{
			featureDao.setCollabroomFeatureDeleted(featureId, true);
			
			this.updatePostResponse(featureResponse, featureId);
			
			response = Response.ok(featureResponse).status(Status.OK).build();
		}catch(Exception e){
			featureResponse.setMessage("Unhandled exception while deleting Collab Room Feature: " + e.getMessage());
			response = Response.ok(featureResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if(Status.OK.getStatusCode() == response.getStatus()){
			try {
				notifyDeletedFeature(featureId, 
						String.format("iweb.NICS.collabroom.%s.deletefeature", collabRoomId));
			} catch (Exception e) {
				String errorMessage = "Failed to publish a Delete CollabRoom Feature message event";
				featureResponse.setMessage(errorMessage);
	
				logger.error(errorMessage, e);
			}
		}
		
		return response;
	}
	
	/**
	 * Add a new feature to the database
	 * @param feature
	 * @return
	 * @throws Exception
	 */
	private Feature addNewFeature(String featureProperties) throws Exception {
		//Use JAXSON to parse the JSON object? - since geometry field
		//is a String - we could do this instead
		/*JsonNode node = mapper.readTree(featureProperties);
		Feature feature = mapper.treeToValue(node, Feature.class);*/
		
		JSONObject feature = new JSONObject(featureProperties);
		ObjectMapper mapper = new ObjectMapper();
		
		List<String> fields = mapper.readValue(
	            feature.names().toString(),
	            mapper.getTypeFactory().constructCollectionType(
	                    List.class, String.class));
		
		return featureDao.getFeature(featureDao.addFeature(feature, fields));
	}
	
	/**
	 * Persist a feature change
	 * @param feature - JSON String representing feature properties
	 * @return featureId of the updated feature
	 */
	private long persistFeatureChange(String feature) throws Exception{
		JSONObject properties = new JSONObject(feature);
		
		//Remove the featureId so it doesn't try to persist it again
		long featureId = properties.getLong(FEATURE_ID);
		properties.remove(FEATURE_ID);
		
		//we return documents as part of a feature, but they have their own endpoint
		properties.remove("documents");
		
		//Update the last updated field
		properties.put(SADisplayConstants.LAST_UPDATE, new Date());
		
		featureDao.updateFeature(featureId, properties);
		
		return featureId;
	}
	

	/**
	 * Share all of the user's features with the specified collaboration room.
	 * 
	 * @param userId The id of the user whose features to share
	 * @param collabRoomId The id of the collaboration room to share the feature to 
	 */
	public Response shareWorkspace(int userId, int collabRoomId, String username) {
		if(userDao.getUserId(username) != userId){
			return null;
		}
		
		String topic = String.format("iweb.NICS.collabroom.%s.feature", collabRoomId);
		
		List<Feature> userFeatures = featureDao.getUserFeatureState(userId);
		featureDao.deleteSharedFeatures(userId, collabRoomId);
		featureDao.shareFeatures(userId, collabRoomId);
		for (Feature userFeature : userFeatures) {
			//we hijack topic to flag this feature not to be ignored
			//otherwise a client ignores new features from the current user
			userFeature.setTopic("share");
			try {
				notifyNewFeature(userFeature, topic);
			} catch (Exception e) {
				logger.error("Failed to publish new feature message", e);
			}
		}
		return null;
	}
	
	/**
	 * Stop sharing all of the user's features with the specified collaboration room.
	 * 
	 * @param userId The id of the user whose features to stop sharing
	 * @param collabRoomId The id of the collaboration room to stop sharing with 
	 */
	public Response unshareWorkspace(int userId, int collabRoomId, String username) {
		if(userDao.getUserId(username) != userId){
			return null;
		}
		
		String topic = String.format("iweb.NICS.collabroom.%s.deletefeature", collabRoomId);
		
		List<Long> deletedIds = featureDao.markSharedFeaturesDeleted(userId, collabRoomId);
		for (Long deletedId : deletedIds) {
			try {
				notifyDeletedFeature(deletedId, topic);
			} catch (IOException | JSONException e) {
				logger.error("Failed to publish deleted feature message", e);
			}
		}
		return null;
	}
	
	@Override
	public Response postFeatureDocument(long featureId, int usersessionId, String username, 
			List<Attachment> attachments, String requestingUser) {
		
		if(!username.equalsIgnoreCase(requestingUser)){
			return Response.status(Status.BAD_REQUEST).entity(Status.FORBIDDEN.getReasonPhrase()).build();
		}
		
		Feature feature = featureDao.getFeature(featureId);
		if (feature == null) {
			return Response.status(Status.BAD_REQUEST).entity("Invalid featureId").build();
		}
		
		String description = null;
		Document imageDoc = null;
		MediaType imageMediaType = new MediaType("image","*");
		
		boolean found = false;
		for(Attachment attachment : attachments) {
			MediaType attachmentType = attachment.getContentType();
			
			if (MediaType.TEXT_PLAIN_TYPE.isCompatible(attachmentType)){
				description = attachment.getObject(String.class);
			} else if (imageMediaType.isCompatible(attachmentType)) {
				imageDoc = getDocument(attachment, Paths.get(fileUploadPath).resolve(FEATURES_SUBPATH));
				if (imageDoc != null) {
					imageDoc.setUsersessionid(usersessionId);
					imageDoc.setDescription(description);
					imageDoc = documentDao.addDocument(imageDoc);
					documentDao.addFeatureDocument(feature.getFeatureId(), imageDoc.getDocumentid());
				}
				description = null;
				found = true;
			}
		}
		
		if (!found) {
			return Response.status(Status.BAD_REQUEST).entity("No file attachment found").build();
		}
		
		try {
			JSONObject properties = new JSONObject();
			properties.put(SADisplayConstants.LAST_UPDATE, new Date());
			properties.put(SADisplayConstants.USER_NAME, username);
			properties.put(SADisplayConstants.USERSESSION_ID, usersessionId);
			featureDao.updateFeature(featureId, properties);
		} catch (Exception e) {
			logger.error("Failed to update feature metadata", e);
		}
		
		Feature newFeature = featureDao.getFeature(featureId);
		buildDocumentUrls(Arrays.asList(newFeature));
		try {
			List<CollabroomFeature> rooms = featureDao.getCollabroomFeatures(newFeature.getFeatureId());
			for (CollabroomFeature room : rooms) {
				notifyNewFeature(newFeature, String.format("iweb.NICS.collabroom.%s.changefeature", room.getCollabroomid()));
			}
		} catch (Exception e) {
			logger.error("Failed to publish Change Collabroom Feature message event", e);
		}

		
		MultipartFeatureResponse featureResponse = new MultipartFeatureResponse();
		featureResponse.setFeatures(Arrays.asList(newFeature));
		featureResponse.setCount(1);
		featureResponse.setSuccess(true);
		
		//NOTE: EXTJS requires success:true with text/plain media-type, even though its JSON
		return Response.ok(featureResponse).build();
	}
	

	@Override
	public Response deleteFeatureDocument(long featureId, long documentId, String requestingUser) {
		// TODO Implement delete
		return null;
	}
	
	private void buildDocumentUrls(Collection<Feature> features) {
		for(Feature feature : features) {
			Set<Document> documents = feature.getDocuments();
			if (documents != null) {
				for(Document doc : documents) {
					String path = UriBuilder.fromPath(fileUploadUrl)
							.path(FEATURES_SUBPATH).path(doc.getFilename())
							.build().toString();
					doc.setFilename(path);
				}
			}
		}
		
	}
	
	private byte[] writeAttachmentWithDigest(Attachment attachment, Path path, String digestAlgorithm) throws IOException, NoSuchAlgorithmException {
		try(
			InputStream is = attachment.getDataHandler().getInputStream();
		) {
			MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
			DigestInputStream dis = new DigestInputStream(is, md);
			Files.copy(dis, path, StandardCopyOption.REPLACE_EXISTING);
			return md.digest();	
		}
	}
	
	private String getFileExtension(Attachment attachment) {
		String filename = attachment.getContentDisposition().getParameter("filename");
		
		int idx = filename.lastIndexOf(".");
		if (idx != -1) {
			return filename.substring(idx + 1);
		}
		return null;
	}
	
	private Document getDocument(Attachment attachment, Path directory) {
		Path tempPath = null, path = null;
		
		try {
			Files.createDirectories(directory);

			tempPath = Files.createTempFile(directory, null, null);
			byte[] digest = writeAttachmentWithDigest(attachment, tempPath, "MD5");
			
			String filename = new BigInteger(1, digest).toString();
			String ext = getFileExtension(attachment);
			if (ext != null) {
				filename += "." + ext;
			}
			path = directory.resolve(filename);
			path = Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException|NoSuchAlgorithmException e) {
			logger.error("Failed to save file attachment", e);
			return null;
		} finally {
			//cleanup files
			if (tempPath != null) {
				File file = tempPath.toFile();
				if (file.exists()) {
					file.delete();
				}
			}
		}
		
		Document doc = new Document();
		doc.setDisplayname(attachment.getContentDisposition().getParameter("filename"));
		doc.setFilename(path.getFileName().toString());
		doc.setFiletype(attachment.getContentType().toString());
		doc.setCreated(new Date());
		return doc;
	}
	
	/**
	 * Create a response object for a post
	 * @param feature
	 * @return
	 */
	private void updatePostResponse(
			FeatureServiceResponse featureResponse, Feature feature){
		featureResponse.setMessage(Status.OK.getReasonPhrase());
		featureResponse.setFeatures(Arrays.asList(feature));
		featureResponse.setCount(1);
	}
	
	/**
	 * Create a response object for a post
	 * @param feature
	 * @return
	 */
	private void updatePostResponse(
			FeatureServiceResponse featureResponse, long featureId){
		featureResponse.setMessage(Status.OK.getReasonPhrase());
		featureResponse.setDeletedFeature(Arrays.asList(featureId));
		featureResponse.setCount(1);
	}
	
	private FeatureServiceResponse buildFeatureServiceResponse(List<Feature> features){
		FeatureServiceResponse featureResponse = new FeatureServiceResponse();
		featureResponse.setMessage(Status.OK.getReasonPhrase());
		featureResponse.setFeatures(features);
		featureResponse.setCount(features.size());
		return featureResponse;
	}
	
	/**
	 * Create Response object for a get
	 * @param features
	 * @return
	 */
	private Response buildGetResponse(List<Feature> features){
		return Response.ok(
				this.buildFeatureServiceResponse(features))
				.status(Status.OK).build();
	}
	
	/**
	 * Create Response object for a get
	 * @param features
	 * @return
	 */
	private Response buildGetResponse(
			List<Feature> features, 
			List<Long> deletedFeatures){
		if(deletedFeatures != null){
			System.out.println("Deleted Features: " + deletedFeatures.size());
		}
		FeatureServiceResponse featureResponse = this.buildFeatureServiceResponse(features);
		featureResponse.setDeletedFeature(deletedFeatures);
		return Response.ok(featureResponse).status(Status.OK).build();
	}
 
	/**
	 * Notify users of a new feature in a collaboration room
	 * @param feature
	 * @param topic
	 * @throws IOException
	 */
	private void notifyNewFeature(Feature feature, String topic) throws Exception {
		if (topic != null && feature != null) {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(feature);
			getRabbitProducer().produce(topic, message);
		}else{
			throw new Exception("Could not notify user of a new feature. Feature/Topic was null.");
		}
	}
	
	/**
	 * Notify users of a new feature in a collaboration room
	 * @param feature
	 * @param topic
	 * @throws IOException
	 */
	private void notifyDeletedFeature(long featureId, String topic) throws IOException, JSONException {
		JSONObject message = new JSONObject();
		message.put(DELETED_FEATURE_ID, featureId);
		getRabbitProducer().produce(topic, message.toString());
	}
	
	/**
	 * Get Rabbit producer to send message
	 * @return
	 * @throws IOException
	 */
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer();
		}
		return rabbitProducer;
	}
	
	private Response getAccessDeniedResponse(){
		return Response.status(Status.BAD_REQUEST).entity(
				Status.FORBIDDEN.getReasonPhrase()).build();
	}
}

