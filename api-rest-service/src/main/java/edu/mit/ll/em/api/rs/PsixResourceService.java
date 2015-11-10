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
package edu.mit.ll.em.api.rs;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.em.api.rs.PsixResource;

@Path("/psixresources")
public interface PsixResourceService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPsixResources();

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePsixResources();

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putPsixResources(Collection<PsixResource> psixResources);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postPsixResources(PsixResource psixResource);

	@GET
	@Path(value = "/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPsixResourceCount();

	@GET
	@Path(value = "/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchPsixResourceResources();	
	
	@GET
	@Path(value = "/{psixResourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPsixResource(@PathParam("psixResourceId") int psixResourceId);

	@DELETE
	@Path(value = "/{psixResourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePsixResource(@PathParam("psixResourceId") int psixResourceId);

	@PUT
	@Path(value = "/{psixResourceId}")
    @Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response putPsixResource(@PathParam("psixResourceId") int psixResourceId, PsixResource psixResource);

	@POST
	@Path(value = "/{psixResourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postPsixResource(@PathParam("psixResourceId") int psixResourceId);	
}

