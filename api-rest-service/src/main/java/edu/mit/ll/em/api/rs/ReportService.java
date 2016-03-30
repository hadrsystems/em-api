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

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import edu.mit.ll.em.api.rs.Report;
import edu.mit.ll.nics.common.entity.Form;

//@Path("/reports/{incidentId}/{reportType}")
@Path("/reports")
public interface ReportService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response getReports(@PathParam("incidentId") int incidentId,
			@PathParam("reportType") String reportType,
			//@QueryParam("username") String userName, // WHY?			
			@QueryParam("") ReportOptParms optParms);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response deleteReports(@PathParam("reportType") int reportType);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response putReports(@PathParam("reportType") int reportType,
			Collection<Report> reports);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response postReport(@PathParam("incidentId") int incidentId,
			@PathParam("reportType") String reportType, Form form);
	
	/*@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response postReports(@PathParam("incidentId") int incidentId,
			@PathParam("reportType") int reportType, Report report);*/

	@POST
	@Consumes("multipart/form-data")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{incidentId}/{reportType}")
	public Response postReports(@PathParam("incidentId") int incidentId,
			@PathParam("reportType") String reportType, MultipartBody body);
	
	@GET
	@Path(value = "/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportCount(@PathParam("reportType") int reportType);
	
	@GET
	@Path(value = "/types")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportTypes();

	@GET
	@Path(value = "/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchReportResources(@PathParam("reportType") int reportType,
			@QueryParam("") ReportOptParms optParms);	
	
		
	@GET
	@Path(value = "/{reportId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReport(@PathParam("reportType") int reportType,
			@PathParam("reportId") int reportId,
			@QueryParam("fields") String fields);

	@DELETE
	@Path(value = "/{reportId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteReport(@PathParam("reportType") int reportType,
			@PathParam("reportId") int reportId);

	@PUT
	@Path(value = "/{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public Response putReport(@PathParam("reportType") int reportType,
			@PathParam("reportId") int reportId, Report report);

	@POST
	@Path(value = "/{reportId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postReport(@PathParam("reportType") int reportType,
			@PathParam("reportId") int reportId);	
}

