/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
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
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.mit.ll.em.api.rs.MDTrack;

@Path("/mdtracks")
public interface MDTrackService {
	/*
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMDTracks(@DefaultValue("-1") @QueryParam("userId") int userId);
	*/

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMDTracks(@QueryParam("") MDTOptionalParms optionalParms);
	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMDTracks();

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putMDTracks(Collection<MDTrack> mdtracks);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMDTrack(@CookieParam("iPlanetDirectoryPro") Cookie cookie, MDTrack mdtrack);

	@GET
	@Path(value = "/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMDTrackCount();

	@GET
	@Path(value = "/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchMDTrackResources();	
	
	@GET
	@Path(value = "/{mdtrackId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMDTrack(@PathParam("mdtrackId") int mdtrackId);

	@DELETE
	@Path(value = "/{mdtrackId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMDTrack(@PathParam("mdtrackId") int mdtrackId);

	@PUT
	@Path(value = "/{mdtrackId}")
    @Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response putMDTrack(@PathParam("mdtrackId") int mdtrackId, MDTrack mdtrack);

	@POST
	@Path(value = "/{mdtrackId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMDTrack(@PathParam("mdtrackId") int mdtrackId);	
}

