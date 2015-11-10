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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.codehaus.jackson.map.ObjectMapper;

import edu.mit.ll.em.api.rs.DatalayerService;
import edu.mit.ll.em.api.rs.DatalayerServiceResponse;
import edu.mit.ll.em.api.rs.DatalayerDocumentServiceResponse;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.rabbitmq.RabbitFactory;
import edu.mit.ll.em.api.util.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.Chat;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.entity.UserOrg;
import edu.mit.ll.nics.common.entity.datalayer.Datalayer;
import edu.mit.ll.nics.common.entity.datalayer.Datalayerfolder;
import edu.mit.ll.nics.common.entity.datalayer.Datalayersource;
import edu.mit.ll.nics.common.entity.datalayer.Datasource;
import edu.mit.ll.nics.common.entity.datalayer.Document;
import edu.mit.ll.nics.common.entity.datalayer.Folder;
import edu.mit.ll.nics.common.entity.datalayer.Rootfolder;
import edu.mit.ll.nics.nicsdao.DatalayerDAO;
import edu.mit.ll.nics.nicsdao.DocumentDAO;
import edu.mit.ll.nics.nicsdao.FolderDAO;
import edu.mit.ll.nics.nicsdao.UserDAO;
import edu.mit.ll.nics.nicsdao.UserOrgDAO;
import edu.mit.ll.nics.nicsdao.impl.DocumentDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.FolderDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.DatalayerDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.UserOrgDAOImpl;

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
	private static final UserOrgDAO userOrgDao = new UserOrgDAOImpl();
	
	private RabbitPubSubProducer rabbitProducer;

	@Override
	public Response getDatalayers(String folderId) {
		DatalayerServiceResponse datalayerResponse = new DatalayerServiceResponse();
		try{
			datalayerResponse.setDatalayers(datalayerDao.getDatalayerFolders(folderId));
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
		Datalayer newDatalayer = null;
		
		try{
			datalayer.setCreated(new Date());
			datalayer.getDatalayersource().setCreated(new Date());
			datalayer.getDatalayersource().setDatasourceid(dataSourceId);
			
			String datalayerId = datalayerDao.insertDataLayer(dataSourceId, datalayer);
			newDatalayer = datalayerDao.reloadDatalayer(datalayerId);

			//Currently always uploads to Data
			Rootfolder folder = folderDao.getRootFolder("Data", workspaceId);
			int nextFolderIndex = folderDao.getNextFolderIndex(folder.getFolderid());
				
			datalayerDao.insertDataLayerFolder(folder.getFolderid(), datalayerId, nextFolderIndex);
			Datalayerfolder dlf = datalayerDao.getDatalayerfolder(datalayerId, folder.getFolderid());
			newDatalayer.setDatalayerfolders(new HashSet<Datalayerfolder>(Arrays.asList(dlf)));
			
			datalayerResponse.setDatalayers(Arrays.asList(newDatalayer));
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
				notifyChange(newDatalayer, "Data");
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		return response;
	}
	
	public Response postDataLayerDocument(int workspaceId, String dataSourceId, int userOrgId, MultipartBody body, String username){
		
		DatalayerDocumentServiceResponse datalayerResponse = new DatalayerDocumentServiceResponse();
		Response response = null;
		Datalayer newDatalayer = null;
		Datalayer datalayer = new Datalayer();
		Document doc = null;
		ZipInputStream zipStream;
		ZipEntry entry;
		Boolean uploadedDataLayer = false;
		String kmlFileName = null;
		datalayer.setDatalayersource(new Datalayersource());
		String filePath = null;
		Boolean valid = false;
		User user = null;
		
		try{
			
			user = userDao.getUser(username);
			Set<UserOrg> userOrgs = user.getUserorgs();
			Iterator iter = userOrgs.iterator();
			
			while(iter.hasNext()){
				
				UserOrg userOrg = (UserOrg)iter.next();
				
				if(userOrg.getUserorgid() == userOrgId && (userOrg.getSystemroleid() == 0 || userOrg.getSystemroleid() == 1 ||
						userOrg.getSystemroleid() == 4	)){
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
					
					if(attachment.getContentDisposition().getParameter("filename").endsWith("kmz")){
						filePath = APIConfig.getInstance().getConfiguration().getString(APIConfig.KMZ_UPLOAD_PATH,"/opt/data/nics/upload/kmz");
					}else if(attachment.getContentDisposition().getParameter("filename").endsWith("kml")){
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

				datalayer.setCreated(new Date());
				datalayer.getDatalayersource().setCreated(new Date());
				datalayer.getDatalayersource().setDatasourceid(dataSourceId);
				
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
				            		kmlFileName = entry.getName();
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
				
				}else {
					uploadedDataLayer = doc.getFilename().endsWith(".kml");
					kmlFileName = doc.getFilename();
				}
				
			}
			
			if(uploadedDataLayer){
				
				datalayer.getDatalayersource().setLayername(kmlFileName);
			
				String datalayerId = datalayerDao.insertDataLayer(dataSourceId, datalayer);
				newDatalayer = datalayerDao.reloadDatalayer(datalayerId);
				
				Rootfolder folder = folderDao.getRootFolder("Data", workspaceId);
				int nextFolderIndex = folderDao.getNextFolderIndex(folder.getFolderid());
					
				datalayerDao.insertDataLayerFolder(folder.getFolderid(), datalayerId, nextFolderIndex);
				Datalayerfolder dlf = datalayerDao.getDatalayerfolder(datalayerId, folder.getFolderid());
				
				newDatalayer.setDatalayerfolders(new HashSet<Datalayerfolder>(Arrays.asList(dlf)));

				datalayerResponse.setDatalayers(Arrays.asList(newDatalayer));
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
				notifyChange(newDatalayer, "Data");
			} catch (IOException e) {
				logger.error("Failed to publish DatalayerService message event", e);
			}
		}
		
		return response;
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
	
	private String getFileExtension(Attachment attachment) {
		String filename = attachment.getContentDisposition().getParameter("filename");
		
		int idx = filename.lastIndexOf(".");
		if (idx != -1) {
			return filename.substring(idx + 1);
		}
		return null;
	}
	
	private void notifyChange(Datalayer datalayer, String rootName) throws IOException {
		if (datalayer != null) {
			String topic = String.format("iweb.nics.data.newdatalayer.%s", rootName);
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(datalayer);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	private RabbitPubSubProducer getRabbitProducer() throws IOException {
		if (rabbitProducer == null) {
			rabbitProducer = RabbitFactory.makeRabbitPubSubProducer();
		}
		return rabbitProducer;
	}
	
	private Response getInvalidResponse(){
		return Response.status(Status.BAD_REQUEST).entity(
				Status.FORBIDDEN.getReasonPhrase()).build();
	}
	
}

