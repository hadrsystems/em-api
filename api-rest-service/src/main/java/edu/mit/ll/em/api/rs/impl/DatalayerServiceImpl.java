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
package edu.mit.ll.em.api.rs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.codehaus.jackson.map.ObjectMapper;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.referencing.FactoryException;

import edu.mit.ll.em.api.dataaccess.ShapefileDAO;
import edu.mit.ll.em.api.dataaccess.UserOrgDAO;
import edu.mit.ll.em.api.rs.DatalayerDocumentServiceResponse;
import edu.mit.ll.em.api.rs.DatalayerService;
import edu.mit.ll.em.api.rs.DatalayerServiceResponse;
import edu.mit.ll.em.api.rs.FieldMapResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.FileUtil;
import edu.mit.ll.em.api.util.SADisplayConstants;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.entity.datalayer.Datalayer;
import edu.mit.ll.nics.common.entity.datalayer.Datalayerfolder;
import edu.mit.ll.nics.common.entity.datalayer.Datalayersource;
import edu.mit.ll.nics.common.entity.datalayer.Datasource;
import edu.mit.ll.nics.common.entity.datalayer.Document;
import edu.mit.ll.nics.common.entity.datalayer.Rootfolder;
import edu.mit.ll.nics.common.geoserver.api.GeoServer;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.DatalayerDAO;
import edu.mit.ll.nics.nicsdao.DocumentDAO;
import edu.mit.ll.nics.nicsdao.FolderDAO;
import edu.mit.ll.nics.nicsdao.UserDAO;
import edu.mit.ll.nics.nicsdao.impl.DatalayerDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.DocumentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.FolderDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserSessionDAOImpl;

/**
 * 
 * @AUTHOR st23420
 *
 */
public class DatalayerServiceImpl implements DatalayerService {

	private static final Log logger = LogFactory.getLog(DatalayerServiceImpl.class);
	
	/** Folder DAO */
	private static final DatalayerDAO datalayerDao = new DatalayerDAOImpl();
	private static final FolderDAO folderDao = new FolderDAOImpl();
	private static final DocumentDAO documentDao = new DocumentDAOImpl();
	private static final UserDAO userDao = new UserDAOImpl();
	private static final UserOrgDAOImpl userOrgDao = new UserOrgDAOImpl();
	private static final UserSessionDAOImpl usersessionDao = new UserSessionDAOImpl();
	
	private static String fileUploadPath;
	private static String mapserverURL;
	private static String geoserverWorkspace;
	private static String geoserverDatastore;
	private static String webserverURL;
	
	private RabbitPubSubProducer rabbitProducer;
	
	private final Client jerseyClient;

	public DatalayerServiceImpl() {
		Configuration config = APIConfig.getInstance().getConfiguration();
		fileUploadPath = config.getString(APIConfig.FILE_UPLOAD_PATH, "/opt/data/nics/upload");
		geoserverWorkspace = config.getString(APIConfig.IMPORT_SHAPEFILE_WORKSPACE, "nics");
		geoserverDatastore = config.getString(APIConfig.IMPORT_SHAPEFILE_STORE, "shapefiles");
		mapserverURL = config.getString(APIConfig.EXPORT_MAPSERVER_URL);
		webserverURL = config.getString(APIConfig.EXPORT_WEBSERVER_URL);
		jerseyClient = ClientBuilder.newClient();
	}
	
	@Override
	public Response getDatalayers(String folderId) {
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		try{
			datalayerResponse.setDatalayerfolders(datalayerDao.getDatalayerFolders(folderId));
		}catch(Exception e){
			logger.error("Failed to retrieve data layers", e);
			datalayerResponse.setMessage("Failed to retrieve data layers");
			return Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return Response.ok(datalayerResponse).status(Status.OK).build();
	}

	@Override
	public Response getDatasources(String type) {
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		try{
			datalayerResponse.setDatasources(datalayerDao.getDatasources(type));
		}catch(Exception e){
			logger.error("Failed to retrieve data sources", e);
			datalayerResponse.setMessage("Failed to retrieve data sources");
			return Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return Response.ok(datalayerResponse).status(Status.OK).build();
	}
	
	@Override
	public Response postDatasource(String type, Datasource source) {
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		Response response = null;
		
		try{
			int dataSourceTypeId = datalayerDao.getDatasourceTypeId(type);
			source.setDatasourcetypeid(dataSourceTypeId);
			
			String dataSourceId = datalayerDao.insertDataSource(source);
			Datasource newSource = datalayerDao.getDatasource(dataSourceId);
			datalayerResponse.setDatasources(Arrays.asList(newSource));
			datalayerResponse.setMessage("ok");
			response = Response.ok(datalayerResponse).status(Status.OK).build();
		} catch(Exception e) {
			logger.error("Failed to insert data source", e);
			datalayerResponse.setMessage("failed");
			response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}

	@Override
	public Response postDataLayer(int workspaceId, String dataSourceId, Datalayer datalayer) {
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		Response response = null;
		Datalayerfolder newDatalayerFolder = null;
		
		try{
			datalayer.setCreated(new Date());
			datalayer.getDatalayersource().setCreated(new Date());
			datalayer.getDatalayersource().setDatasourceid(dataSourceId);
			
			String datalayerId = datalayerDao.insertDataLayer(dataSourceId, datalayer);

			//Currently always uploads to Data
			Rootfolder folder = folderDao.getRootFolder("Data", workspaceId);
			int nextFolderIndex = datalayerDao.getNextDatalayerFolderIndex(folder.getFolderid());
				
			datalayerDao.insertDataLayerFolder(folder.getFolderid(), datalayerId, nextFolderIndex);
			newDatalayerFolder = datalayerDao.getDatalayerfolder(datalayerId, folder.getFolderid());
			
			datalayerResponse.setDatalayerfolders(Arrays.asList(newDatalayerFolder));
			datalayerResponse.setMessage("ok");
			response = Response.ok(datalayerResponse).status(Status.OK).build();
		}
		catch(Exception e) {
			logger.error("Failed to insert data layer", e);
			datalayerResponse.setMessage("failed");
			response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyNewChange(newDatalayerFolder, workspaceId);
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		return response;
	}
	
	@Override
	public Response deleteDataLayer(int workspaceId, String dataSourceId){
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		Response response = null;
		boolean deleteDatalayer = false;
		
		try{
		
			deleteDatalayer = datalayerDao.removeDataLayer(dataSourceId);
		
			if(deleteDatalayer){
				datalayerResponse.setMessage("OK");
				response = Response.ok(datalayerResponse).status(Status.OK).build();	
			}
			else{
				datalayerResponse.setMessage("Failed to delete datalayer");
				response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
		
		}catch(Exception e){
			logger.error("Failed to delete data layer", e);
			datalayerResponse.setMessage("Failed to delete datalayer");
			response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyDeleteChange(dataSourceId);
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		return response;
	}
	
	public Response updateDataLayer(int workspaceId, Datalayer datalayer){
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		Response response = null;
		Datalayer dbDatalayer = null;
		
		try{
		
			dbDatalayer = datalayerDao.updateDataLayer(datalayer);
		
			if(dbDatalayer != null){
				datalayerResponse.setCount(1);
				datalayerResponse.setMessage("OK");
				response = Response.ok(datalayerResponse).status(Status.OK).build();	
			}
			else{
				datalayerResponse.setMessage("Failed to update datalayer");
				response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
			
		
		}catch(Exception e){
			logger.error("Failed to delete data layer", e);
			datalayerResponse.setMessage("Failed to delete datalayer");
			response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyUpdateChange(dbDatalayer);
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		datalayerResponse.setMessage("OK");
		response = Response.ok(datalayerResponse).status(Status.OK).build();	
		
		return response;
	}
	
	public Response postShapeDataLayer(int workspaceId, String displayName, MultipartBody body, String username) {
		if(!userOrgDao.isUserRole(username, SADisplayConstants.SUPER_ROLE_ID) &&
				!userOrgDao.isUserRole(username, SADisplayConstants.ADMIN_ROLE_ID) &&
				!userOrgDao.isUserRole(username, SADisplayConstants.GIS_ROLE_ID)){
			return getInvalidResponse();
		}
			
		ShapefileDAO geoserverDao = ShapefileDAO.getInstance();
		GeoServer geoserver = getGeoServer(APIConfig.getInstance().getConfiguration());
		String dataSourceId = getMapserverDatasourceId();
		if (dataSourceId == null) {
			throw new WebApplicationException("Failed to find configured SCOUT wms datasource");
		}
		
		Attachment aShape = body.getAttachment("shpFile");
		if (aShape == null) {
			throw new WebApplicationException("Required attachment 'shpFile' not found");
		}
		String shpFilename = aShape.getContentDisposition().getParameter("filename");
		String batchName = shpFilename.replace(".shp", "").replace(" ", "_");
		String layerName = batchName.concat(String.valueOf(System.currentTimeMillis()));
		
		//write all the uploaded files to the filesystem in a temp directory
		Path shapesDirectory = Paths.get(fileUploadPath, "shapefiles");
		Path batchDirectory = null;
		try {
			Files.createDirectories(shapesDirectory);
			
			batchDirectory = Files.createTempDirectory(shapesDirectory, batchName);
			List<Attachment> attachments = body.getAllAttachments();
			for(Attachment attachment : attachments) {
				String filename = attachment.getContentDisposition().getParameter("filename");
				String extension = FileUtil.getFileExtension(filename);
				if (extension != null) {
					Path path = batchDirectory.resolve(batchName.concat(extension));
					InputStream is = attachment.getDataHandler().getInputStream();
					Files.copy(is, path);
				}
			}
			
			//attempt to read our shapefile and accompanying files
			Path shpPath = batchDirectory.resolve(batchName.concat(".shp"));
			FileDataStore store = FileDataStoreFinder.getDataStore(shpPath.toFile());
			SimpleFeatureSource featureSource = store.getFeatureSource();
			
			//attempt to insert our features into their own table
			geoserverDao.insertFeatures(layerName, featureSource);
		} catch (IOException | FactoryException e) {
			try {
				geoserverDao.removeFeaturesTable(layerName);
			} catch (IOException ioe) { /* bury */}
			throw new WebApplicationException("Failed to import shapefile", e);
		} finally {
			//always clean up our temp directory
			if (batchDirectory != null) {
				try {
					FileUtil.deleteRecursively(batchDirectory);
				} catch (IOException e) {
					logger.error("Failed to cleanup shapefile batch directory", e);
				}
			}
		}
		
		//add postgis layer to map server
		if(!geoserver.addFeatureType(geoserverWorkspace, geoserverDatastore, layerName, "EPSG:3857")){
			try {
				geoserverDao.removeFeaturesTable(layerName);
			} catch (IOException e) { /* bury */}
			throw new WebApplicationException("Failed to create features " + layerName);
		}
		
		//apply styling default or custom sld
		String defaultStyleName = "defaultShapefileStyle";
		Attachment aSld = body.getAttachment("sldFile");
		if (aSld != null) {
			String sldXml = aSld.getObject(String.class);
			if (geoserver.addStyle(layerName, sldXml) ) {
				defaultStyleName = layerName;
			}
		}
		geoserver.updateLayerStyle(layerName, defaultStyleName);
		geoserver.updateLayerEnabled(layerName, true);

		//create datalayer and datalayersource for our new layer 
		int usersessionid = usersessionDao.getUserSessionid(username);
		
		Datalayer datalayer = new Datalayer(); 
		datalayer.setCreated(new Date());
		datalayer.setBaselayer(false);
		datalayer.setDisplayname(displayName);
		datalayer.setUsersessionid(usersessionid);
		
		Datalayersource dlsource = new Datalayersource();
		dlsource.setLayername(layerName);
		dlsource.setCreated(new Date());
		dlsource.setDatasourceid(dataSourceId);
		datalayer.setDatalayersource(dlsource);
		
		String datalayerId = datalayerDao.insertDataLayer(dataSourceId, datalayer);
		Rootfolder folder = folderDao.getRootFolder("Data", workspaceId);
		int nextFolderIndex = datalayerDao.getNextDatalayerFolderIndex(folder.getFolderid());
		datalayerDao.insertDataLayerFolder(folder.getFolderid(), datalayerId, nextFolderIndex);

		//retrieve the new datalayerfolder to return to the client and broadcast
		Datalayerfolder newDatalayerFolder = datalayerDao.getDatalayerfolder(datalayerId, folder.getFolderid());
		
		try {
			notifyNewChange(newDatalayerFolder, workspaceId);
		} catch (IOException e) {
			logger.error("Failed to publish DatalayerService message event", e);
		}
		
		DatalayerDocumentServiceResponse datalayerResponse = new DatalayerDocumentServiceResponse();
		datalayerResponse.setSuccess(true);
		datalayerResponse.setCount(1);
		datalayerResponse.setDatalayerfolders(Arrays.asList(newDatalayerFolder));
		return Response.ok(datalayerResponse).status(Status.OK).build();
	}
	
	public Response postDataLayerDocument(int workspaceId, String fileExt, int userOrgId, int refreshRate, MultipartBody body, String username){
		
		DatalayerDocumentServiceResponse datalayerResponse = new DatalayerDocumentServiceResponse();
		Response response = null;
		Datalayerfolder newDatalayerFolder = null;
		Datalayer datalayer = new Datalayer();
		datalayer.setDatalayersource(new Datalayersource());
		String dataSourceId = null;
		Document doc = null;
		ZipInputStream zipStream;
		ZipEntry entry;
		Boolean uploadedDataLayer = false;
		String fileName = null;
		String filePath = null;
		Boolean valid = false;
		User user = null;
		
		try{
			
			user = userDao.getUser(username);
			Set<UserOrg> userOrgs = user.getUserorgs();
			Iterator<UserOrg> iter = userOrgs.iterator();
			
			while(iter.hasNext()){
				
				UserOrg userOrg = (UserOrg)iter.next();
				
				if(userOrg.getUserorgid() == userOrgId && 
						(userOrg.getSystemroleid() == SADisplayConstants.SUPER_ROLE_ID || 
						userOrg.getSystemroleid() == SADisplayConstants.GIS_ROLE_ID ||
						userOrg.getSystemroleid() == SADisplayConstants.ADMIN_ROLE_ID	)){
					valid = true;
				}
				
			}
			
			if(!valid){
				return getInvalidResponse();
			}
			
		
			for(Attachment attachment : body.getAllAttachments()) {
	
				Object propValue = attachment.getObject(String.class).toString();
				
				if(MediaType.TEXT_PLAIN_TYPE.isCompatible(attachment.getContentType())){
					
					if(attachment.getContentDisposition().getParameter("name").toString().equals("usersessionid")){
						datalayer.setUsersessionid(Integer.valueOf(propValue.toString()));
					}
					else if(attachment.getContentDisposition().getParameter("name").toString().equals("displayname")){
						datalayer.setDisplayname(propValue.toString());
					}	
					else if(attachment.getContentDisposition().getParameter("name").toString().equals("baselayer")){
						datalayer.setBaselayer(Boolean.parseBoolean(propValue.toString()));
					}
				}
				else{
					
					if(attachment.getContentDisposition().getParameter("filename").endsWith(".kmz")){
						filePath = APIConfig.getInstance().getConfiguration().getString(APIConfig.KMZ_UPLOAD_PATH,"/opt/data/nics/upload/kmz");
					}else if(attachment.getContentDisposition().getParameter("filename").endsWith(".gpx")){
						filePath = APIConfig.getInstance().getConfiguration().getString(APIConfig.GPX_UPLOAD_PATH,"/opt/data/nics/upload/gpx");
					}else if(attachment.getContentDisposition().getParameter("filename").endsWith(".json") || 
							attachment.getContentDisposition().getParameter("filename").endsWith(".geojson")){
						filePath = APIConfig.getInstance().getConfiguration().getString(APIConfig.JSON_UPLOAD_PATH,"/opt/data/nics/upload/geojson");
					}else if(attachment.getContentDisposition().getParameter("filename").endsWith(".kml")){
						filePath = APIConfig.getInstance().getConfiguration().getString(APIConfig.KML_UPLOAD_PATH,"/opt/data/nics/upload/kml");
					}
					
					if(filePath != null){
						doc = getDocument(attachment, Paths.get(filePath));
					}
				}
			}
			
			if(doc != null){
				
				doc.setUsersessionid(datalayer.getUsersessionid());
				doc = documentDao.addDocument(doc);
				
				dataSourceId = getFileDatasourceId(fileExt);
				
				if(uploadedDataLayer = (dataSourceId != null)){
				
					datalayer.setCreated(new Date());
					datalayer.getDatalayersource().setCreated(new Date());
					datalayer.getDatalayersource().setDatasourceid(dataSourceId);
					datalayer.getDatalayersource().setRefreshrate(refreshRate);
				}
				
				uploadedDataLayer = doc.getFilename().endsWith(".kmz");
				
				if(uploadedDataLayer){
					
					try
					{
						zipStream = new ZipInputStream(new FileInputStream(filePath + doc.getFilename()));
						
						byte[] buf = new byte[2048];
					
						while((entry = zipStream.getNextEntry()) != null)
				        {
				            String outpath = filePath + "/" + entry.getName();
				            FileOutputStream output = null;
				            try
				            {
				            	if(entry.getName().endsWith(".kml")){
				            		fileName = entry.getName();
				            	}
				            	
				            	if(entry.getName().contains("/")){
				            		
				            		String folders = entry.getName().substring(0,entry.getName().lastIndexOf('/'));
				            		File file = new File(filePath+folders);
				            		
				            		if(!file.exists()){
				            			file.mkdirs();
				            		}
				            		
				            	}
				            	
				                output = new FileOutputStream(outpath);
				                int len = 0;
				                while ((len = zipStream.read(buf)) > 0)
				                {
				                    output.write(buf, 0, len);
				                }
				            
				            }
				            finally
				            {
				                if(output!=null) output.close();
				            }
				        }
						
						
						zipStream.close();
					}
					catch(IOException ex) {
						logger.error("Failed to unzip file", ex);
						uploadedDataLayer = false;
			        }
				
				}
				else if(uploadedDataLayer = doc.getFilename().endsWith(".gpx")){
					fileName = doc.getFilename();
				}
				else if(uploadedDataLayer = doc.getFilename().endsWith(".json")){
					fileName = doc.getFilename();
				}
				else if(uploadedDataLayer = doc.getFilename().endsWith(".geojson")){
					fileName = doc.getFilename();
				}
				else if(uploadedDataLayer = doc.getFilename().endsWith(".kml")){
					fileName = doc.getFilename();
				}
				
			}
			
			if(uploadedDataLayer){
				datalayer.getDatalayersource().setLayername(fileName);
				
				String datalayerId = datalayerDao.insertDataLayer(dataSourceId, datalayer);
				
				Rootfolder folder = folderDao.getRootFolder("Data", workspaceId);
				int nextFolderIndex = datalayerDao.getNextDatalayerFolderIndex(folder.getFolderid());
					
				datalayerDao.insertDataLayerFolder(folder.getFolderid(), datalayerId, nextFolderIndex);
				newDatalayerFolder = datalayerDao.getDatalayerfolder(datalayerId, folder.getFolderid());

				datalayerResponse.setDatalayerfolders(Arrays.asList(newDatalayerFolder));
				datalayerResponse.setMessage("ok");
				datalayerResponse.setSuccess(true);
				response = Response.ok(datalayerResponse).status(Status.OK).build();
				
			}
			else{
				datalayerResponse.setSuccess(false);
				datalayerResponse.setMessage("Failed to Upload file.");
				response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		catch(Exception e) {
			logger.error("Failed to insert data layer", e);
			datalayerResponse.setSuccess(false);
			datalayerResponse.setMessage("Failed to add data layer.");
			response = Response.ok(datalayerResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			try {
				notifyNewChange(newDatalayerFolder, workspaceId);
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		return response;
	}
	
	public Response getToken(String url, String username, String password){
		return Response.ok(this.requestToken(url, username, password)).status(Status.OK).build();
	}
	
	public Response getToken(String datasourceId){
		List<Map<String, Object>> data = datalayerDao.getAuthentication(datasourceId);
		
		//https://apps.intterragroup.com/arcgis2/tokens/generateToken?username={0}&password={1}&f=json
		//https://apps.intterragroup.com/arcgis2/services/NICS/VCFDAVLResources/MapServer/WFSServer
		//https://apps.intterragroup.com/arcgis2/rest/services/NICS/VCFDAVLResources/MapServer
		
		if(data.get(0) != null){
			String internalUrl = (String) data.get(0).get(SADisplayConstants.INTERNAL_URL);
			String token = this.requestToken(internalUrl, 
					(String) data.get(0).get(SADisplayConstants.USER_NAME),
					(String) data.get(0).get(SADisplayConstants.PASSWORD)
			);
			if(token != null){
				return Response.ok(token).status(Status.OK).build();
			}
		}

		return Response.ok().status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	private String requestToken(String internalUrl, String username, String password){
		int index = internalUrl.indexOf("rest/services");
		if(index == -1){ 
			index = internalUrl.indexOf("services"); 
		}
		
		if(index > -1){
			StringBuffer url = new StringBuffer(internalUrl.substring(0, index));
			url.append("tokens/generateToken?");
			url.append("username=");
			url.append(username);
			url.append("&password=");
			url.append(password);
			url.append("&f=json");
			
			WebTarget target = jerseyClient.target(url.toString());
			Builder builder = target.request("json");
			return builder.get().readEntity(String.class);
		}
		
		return null;
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
	
	
	private String getMapserverDatasourceId() {
		if(mapserverURL == null) {
			return null;
		}
		String wmsMapserverURL = mapserverURL.concat("/wms");
		
		String datasourceId = datalayerDao.getDatasourceId(wmsMapserverURL);
		if (datasourceId == null) {
			int datasourcetypeid = datalayerDao.getDatasourceTypeId("wms");
			if (datasourcetypeid != -1) {
				Datasource ds = new Datasource();
				ds.setInternalurl(wmsMapserverURL);
				ds.setDatasourcetypeid(datasourcetypeid);
				ds.setDisplayname("NICS WMS Server");
				datasourceId = datalayerDao.insertDataSource(ds);
			}
		}
		return datasourceId;
	}
	
	private String getFileDatasourceId(String fileExt) {
		if(webserverURL == null) {
			return null;
		}
		String webServerURL = webserverURL.concat("/" + fileExt + "/");
		
		String datasourceId = datalayerDao.getDatasourceId(webServerURL);
		if (datasourceId == null) {
			int datasourcetypeid = datalayerDao.getDatasourceTypeId(fileExt);
			if (datasourcetypeid != -1) {
				Datasource ds = new Datasource();
				ds.setInternalurl(webServerURL);
				ds.setDatasourcetypeid(datasourcetypeid);
				datasourceId = datalayerDao.insertDataSource(ds);
			}
		}
		return datasourceId;
	}
	
	private GeoServer getGeoServer(Configuration config) {
		String geoserverUrl = config.getString(APIConfig.EXPORT_MAPSERVER_URL);
		if (geoserverUrl == null) {
			logger.error("API configuration error " + APIConfig.EXPORT_MAPSERVER_URL);
		}
		
		String geoserverUsername = config.getString(APIConfig.EXPORT_MAPSERVER_USERNAME);
		if (geoserverUsername == null) {
			logger.error("API configuration error " + APIConfig.EXPORT_MAPSERVER_USERNAME);
		}
		
		String geoserverPassword = config.getString(APIConfig.EXPORT_MAPSERVER_PASSWORD);
		if (geoserverPassword == null) {
			logger.error("API configuration error " + APIConfig.EXPORT_MAPSERVER_PASSWORD);
		}
		
		return new GeoServer(geoserverUrl.concat(APIConfig.EXPORT_REST_URL), geoserverUsername, geoserverPassword);
	}
	
	private String getFileExtension(Attachment attachment) {
		String filename = attachment.getContentDisposition().getParameter("filename");
		
		int idx = filename.lastIndexOf(".");
		if (idx != -1) {
			return filename.substring(idx + 1);
		}
		return null;
	}
	
	private void notifyNewChange(Datalayerfolder datalayerfolder, int workspaceId) throws IOException {
		if (datalayerfolder != null) {
			String topic = String.format("iweb.NICS.%s.datalayer.new", workspaceId);
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(datalayerfolder);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private void notifyDeleteChange(String dataSourceId) throws IOException {
		if (dataSourceId != null) {
			String topic = String.format("iweb.NICS.datalayer.delete");
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(dataSourceId);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private void notifyUpdateChange(Datalayer datalayer) throws IOException {
		if (datalayer != null) {
			String topic = String.format("iweb.NICS.datalayer.update");
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(datalayer);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer(
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_HOSTNAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_EXCHANGENAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERNAME_KEY),
					APIConfig.getInstance().getConfiguration().getString(APIConfig.RABBIT_USERPWD_KEY));
		}
		return rabbitProducer;
	}
	
	private Response getInvalidResponse(){
		return Response.status(Status.BAD_REQUEST).entity(
				Status.FORBIDDEN.getReasonPhrase()).build();
	}
	
}

