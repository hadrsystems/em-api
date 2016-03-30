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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;

import edu.mit.ll.em.api.rs.FeatureServiceResponse;
import edu.mit.ll.em.api.rs.FolderDataServiceResponse;
import edu.mit.ll.em.api.rs.FolderService;
import edu.mit.ll.em.api.util.APIConfig;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.nics.common.rabbitmq.RabbitFactory;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.common.entity.Feature;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.datalayer.Datalayerfolder;
import edu.mit.ll.nics.common.entity.datalayer.Folder;
import edu.mit.ll.nics.common.entity.datalayer.Rootfolder;
import edu.mit.ll.nics.nicsdao.DatalayerDAO;
import edu.mit.ll.nics.nicsdao.FolderDAO;
import edu.mit.ll.nics.nicsdao.impl.DatalayerDAOImpl;
import edu.mit.ll.nics.nicsdao.impl.FolderDAOImpl;

/**
 * 
 * @AUTHOR st23420
 *
 */
public class FolderServiceImpl implements FolderService {
	
	private static final String ERROR_MESSAGE = "An unexpected error occurred while attempting to update the folder.";

	/** CNAME - the name of this class for referencing in loggers */
	private static final String CNAME = FolderServiceImpl.class.getName();
	
	/** Folder DAO */
	private static final FolderDAO folderDao = new FolderDAOImpl();
	
	/** Datalayer DAO */
	private static final DatalayerDAO datalayerDao = new DatalayerDAOImpl();

	private RabbitPubSubProducer rabbitProducer;
	
	/**
	 * Return Folder items
	 * 
	 * @return Response
	 * @see FolderResponse
	 */
	@Override
	public Response getChildFolders(int workspaceId, String folderId) {
		try{
			return getFolderData(folderId, workspaceId);
		}catch(Exception e){
			e.printStackTrace();
		}
		//Send error message back
		return Response.ok(new FolderDataServiceResponse()).status(Status.OK).build();
	}
	
	/**
	 * Return Folder items
	 * 
	 * @return Response
	 * @see FolderResponse
	 */
	@Override
	public Response getFolderData(int workspaceId, String folderName) {
		try{
			Rootfolder root = folderDao.getRootFolder(folderName, workspaceId);
			if(root != null){
				return getFolderData(root.getFolderid(), workspaceId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//Send error message back
		return Response.ok(new FolderDataServiceResponse()).status(Status.OK).build();
	}
	
	/**
	 * Create new folder
	 * 
	 * @return Response
	 * @see FolderResponse
	 */
	@Override
	public Response postFolder(int workspaceId, Folder folder) {

		Response response;
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		Folder newFolder = null;
		
		try {
			folder.setWorkspaceid(workspaceId);
			folder.setIndex(folderDao.getNextFolderIndex(folder.getParentfolderid()));
			newFolder = folderDao.createFolder(folder);
			
			if(newFolder != null){
				
				folderResponse.getFolders().add(newFolder);
				folderResponse.setMessage(Status.OK.getReasonPhrase());
				folderResponse.setCount(folderResponse.getFolders().size());
				response = Response.ok(folderResponse).status(Status.OK).build();
				
				try {
					
					String topic = String.format("iweb.NICS.%s.folder.new", workspaceId);
					notifyFolder(newFolder, topic);
				
				} catch (Exception e) {
					APILogger.getInstance().e(CNAME,"Failed to publish updating a folder message event");
				}
				
			}else{
				folderResponse.setMessage(Status.EXPECTATION_FAILED.getReasonPhrase());
				folderResponse.setCount(0);
				response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			folderResponse.setMessage(ERROR_MESSAGE);
			response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return response;
	}

	/**
	 * Update existing folder
	 * 
	 * @return Response
	 * @see FolderResponse
	 */
	@Override
	public Response updateFolder(int workspaceId, Folder folder) {
		Response response;
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		Folder updatedFolder = null;
		
		try {
			folder.setWorkspaceid(workspaceId);
			updatedFolder = folderDao.updateFolder(folder); 
			
			if (updatedFolder != null) {

				folderResponse.getFolders().add(updatedFolder);
				folderResponse.setMessage(Status.OK.getReasonPhrase());
				folderResponse.setCount(folderResponse.getFolders().size());
				response = Response.ok(folderResponse).status(Status.OK).build();
				
				try {
					
					String topic = String.format("iweb.NICS.%s.folder.update", workspaceId);
					notifyFolder(folder, topic);
					
				} catch (Exception e) {
					APILogger.getInstance().e(CNAME,"Failed to publish updating a folder message event");
				}
			}
			else{
				folderResponse.setMessage(ERROR_MESSAGE);
				response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();
				return response;
			}
						
		} catch (Exception e) {
			APILogger.getInstance().e(CNAME, "Data access exception while updating Folder"
					+ folder.getFoldername() +  ": " + e.getMessage());
			folderResponse.setMessage(ERROR_MESSAGE);
			folderResponse.setCount(folderResponse.getFolders().size());
			response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();			
		}
		
		return response;
	}

	/**
	 * Delete folder
	 * 
	 * @return Response
	 * @see FolderResponse
	 */
	@Override
	public Response deleteFolder(int workspaceId, String folderId) {
		Response response;
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		boolean deletedFolder = false;
		
		if (folderId == null) {
			folderResponse.setMessage("Invalid folderId value: " + folderId);
			response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();
			return response;
		}
		
		try {
			deletedFolder = folderDao.removeFolder(folderId);
			
			if (deletedFolder) {
				
				folderResponse.setMessage(Status.OK.getReasonPhrase());
				folderResponse.setCount(folderResponse.getFolders().size());
				response = Response.ok(folderResponse).status(Status.OK).build();
				
				try {
					
					String topic = String.format("iweb.NICS.%s.folder.delete", workspaceId);
					notifyFolder(folderId, topic);
					
				} catch (Exception e) {
					APILogger.getInstance().e(CNAME,"Failed to publish updating a folder message event");
				}
				
			}
			else{
				folderResponse.setMessage(ERROR_MESSAGE);
				response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();
				return response;
			}
					
		} catch (Exception e) {
			APILogger.getInstance().e(CNAME, "Data access exception while deleting Folder with folderId: "
					+ folderId +  ": " + e.getMessage());
			folderResponse.setMessage(ERROR_MESSAGE);
			folderResponse.setCount(folderResponse.getFolders().size());
			response = Response.ok(folderResponse).status(Status.INTERNAL_SERVER_ERROR).build();				
		}

		return response;
	}
	
	@Override
	public Response moveFolder(int workspaceId, String parentFolderId, String folderId, Integer datalayerfolderId, int index) {
		Response response;
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		
		if (folderId != null && !folderId.isEmpty()) {
			Folder folder = folderDao.getFolder(folderId);
			//decrement all higher indexes from previous parent OR REORDER
			folderDao.decrementIndexes(folder.getParentfolderid(), folder.getIndex());
			
			//increment all higher/equal indexes in new parent 
			folderDao.incrementIndexes(parentFolderId, index);
			
			//update parent folder id
			folder.setParentfolderid(parentFolderId);
			//update index
			folder.setIndex(index);
			folder = folderDao.updateFolder(folder);

			try {
				
				String topic = String.format("iweb.NICS.%s.folder.update", workspaceId);
				notifyFolder(folder, topic);
				
			} catch (Exception e) {
				APILogger.getInstance().e(CNAME,"Failed to publish updating a folder message event");
			}
			
			folderResponse.setCount(1);
			folderResponse.setFolders(Arrays.asList(folder));
			response = Response.ok(folderResponse).build();
		} else if(datalayerfolderId != null) {
			Datalayerfolder dlFolder = datalayerDao.getDatalayerfolder(datalayerfolderId);
			
			//decrement all higher indexes from previous parent
			datalayerDao.decrementIndexes(dlFolder.getFolderid(), dlFolder.getIndex());
			
			//make space for destination. increment all higher/equal indexes in new parent 
			datalayerDao.incrementIndexes(parentFolderId, index);
			
			//make the move. update parent folder id, update index
			dlFolder.setFolderid(parentFolderId);
			dlFolder.setIndex(index);
			dlFolder = datalayerDao.updateDatalayerfolder(dlFolder);
			dlFolder = datalayerDao.getDatalayerfolder(
					dlFolder.getDatalayerid(), dlFolder.getFolderid());
			
			try {
				String topic = String.format("iweb.NICS.%s.datalayer.update", workspaceId);
				notifyFolder(dlFolder, topic);
				
			} catch (Exception e) {
				APILogger.getInstance().e(CNAME,"Failed to publish updating a folder message event");
			}
			
			folderResponse.setCount(1);
			folderResponse.setDatalayerfolders(Arrays.asList(dlFolder));
			response = Response.ok(folderResponse).build();
		} else {
			folderResponse.setMessage(ERROR_MESSAGE);
			response = Response.ok(folderResponse).status(Status.BAD_REQUEST).build();
		}
		
		return response;
	}
	
	
	private Response getFolderData(String folderId, int workspaceId){
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		
		folderResponse.setRootId(folderId);
		folderResponse.setDatalayerfolders(datalayerDao.getDatalayerFolders(folderId));
		folderResponse.setFolders(folderDao.getOrderedFolders(folderId, workspaceId));
		
		return Response.ok(folderResponse).status(Status.OK).build();
	}
	
	private void notifyFolder(Object folder, String topic) throws IOException {
		if (folder != null) {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(folder);
			getRabbitProducer().produce(topic, message);
		}
	}
	
	/**
	 * Get Rabbit producer to send message
	 * @return
	 * @throws IOException
	 */
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

}

