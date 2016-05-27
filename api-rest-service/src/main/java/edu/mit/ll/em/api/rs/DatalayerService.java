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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import edu.mit.ll.nics.common.entity.datalayer.Datalayer;
import edu.mit.ll.nics.common.entity.datalayer.Datasource;

@Path("/datalayer/{workspaceId}")
public interface DatalayerService {
	
	@GET
	@Path("/{folderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatalayers(@PathParam("folderId") String folderId);
	
	@GET
	@Path("/tracking")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrackingLayers(@PathParam("workspaceId") int workspaceId);
	
	@GET
	@Path("/token/{datasourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getToken(@PathParam("datasourceId") String datasourceId);
	
	@GET
	@Path("/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getToken(
			@QueryParam("internalurl") String internalUrl,
			@QueryParam("username") String username,
			@QueryParam("password") String password);
	
	@POST
	@Path("/sources/{dataSourceId}/layer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postDataLayer(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("dataSourceId") String dataSourceId,
			Datalayer datalayer);
	
	@DELETE
	@Path("/sources/{dataSourceId}/layer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteDataLayer(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("dataSourceId") String dataSourceId);
	
	@POST
	@Path("/sources/layer/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataLayer(
			@PathParam("workspaceId") int workspaceId,
			Datalayer datalayer);
	
	@POST
	@Path("/sources/{dataSourceId}/document/{userOrgId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postDataLayerDocument(
			@PathParam("workspaceId") int workspaceId,
			@PathParam("dataSourceId") String dataSourceId,
			@PathParam("userOrgId") int userOrgId,
			@Multipart(value = "refreshRate", required = false) int refreshRate,
			MultipartBody body,
			@HeaderParam("CUSTOM-uid") String username);
	
	@POST
	@Path("/shapefile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postShapeDataLayer(
			@PathParam("workspaceId") int workspaceId,
			@Multipart("displayName") String displayName,
			MultipartBody body,
			@HeaderParam("CUSTOM-uid") String username);
	
	
	@GET
	@Path("/sources/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatasources(@PathParam("type") String type);
	
	@POST
	@Path("/sources/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postDatasource(
			@PathParam("type") String type,
			Datasource dataSource);
	
}

