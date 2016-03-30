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
package edu.mit.ll.em.api.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.entity.datalayer.Folder;

@Path("/folder/{workspaceId}")
public interface FolderService {
	@GET
	@Path(value = "/name/{folderName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFolderData(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("folderName") String folderName);
	
	@GET
	@Path(value = "/id/{folderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildFolders(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("folderId") String folderId);
	
	@POST
	@Path(value = "/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postFolder(
			@PathParam("workspaceId") int workspaceId,
			Folder folder);
	
	@POST
	@Path(value = "/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateFolder(
			@PathParam("workspaceId") int workspaceId,
			Folder folder);
	
	@DELETE
	@Path(value = "/{folderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFolder(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("folderId") String folderId);
	
	@POST
	@Path(value = "/move/{parentFolderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveFolder(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("parentFolderId") String parentFolderId,
			@QueryParam("folderId") String folderId,
			@QueryParam("datalayerfolderId") Integer datalayerfolderId,
			@QueryParam("index") int index);
}

