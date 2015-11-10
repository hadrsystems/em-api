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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.em.api.rs.FolderDataServiceResponse;
import edu.mit.ll.em.api.rs.FolderService;
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

	/** Folder DAO */
	private static final FolderDAO folderDao = new FolderDAOImpl();
	
	/** Datalayer DAO */
	private static final DatalayerDAO datalayerDao = new DatalayerDAOImpl();

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
	
	private Response getFolderData(String folderId, int workspaceId){
		FolderDataServiceResponse folderResponse = new FolderDataServiceResponse();
		
		folderResponse.setRootId(folderId);
		folderResponse.setDatalayers(datalayerDao.getDatalayerFolders(folderId));
		folderResponse.setFolders(folderDao.getOrderedFolders(folderId, workspaceId));
		
		return Response.ok(folderResponse).status(Status.OK).build();
	}
}

