#!/usr/bin/perl
#
# Copyright (c) 2008-2015, Massachusetts Institute of Technology (MIT)
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# 3. Neither the name of the copyright holder nor the names of its contributors
# may be used to endorse or promote products derived from this software without
# specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

# Script for generating JAX-RS boiler plate code for REST resources.
# Version: 1.0


use FileHandle;

# Set by hand.
my $resourceClass = "Report";
my $resourceInstance = "report";
my $packagePath = "edu.mit.ll.hps.phinics.papi.rs";
my $baseUriPath = lc($resourceClass)."s";

#
# Do not change anything below this point.
#

#
# Build the Service Interface file.
#
my $uripath = "/${resourceInstance}s";
my $fh = FileHandle->new($resourceClass.'Service.java', "w");
die "$!" unless defined $fh;

print $fh <<__END1__

package ${packagePath};

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

\@Path("/${baseUriPath}")
public interface ${resourceClass}Service {
	\@GET
	\@Produces(MediaType.APPLICATION_JSON)
	public Response get${resourceClass}s();

	\@DELETE
	\@Produces(MediaType.APPLICATION_JSON)
	public Response delete${resourceClass}s();

	\@PUT
	\@Consumes(MediaType.APPLICATION_JSON)
	\@Produces(MediaType.APPLICATION_JSON)
	public Response put${resourceClass}s(Collection<${resourceClass}> ${resourceInstance}s);

	\@POST
	\@Consumes(MediaType.APPLICATION_JSON)
	\@Produces(MediaType.APPLICATION_JSON)
	public Response post${resourceClass}s(${resourceClass} ${resourceInstance});

	\@GET
	\@Path(value = "/count")
	\@Produces(MediaType.APPLICATION_JSON)
	public Response get${resourceClass}Count();

	\@GET
	\@Path(value = "/search")
	\@Produces(MediaType.APPLICATION_JSON)
	public Response search${resourceClass}Resources();	
	
	\@GET
	\@Path(value = "/{${resourceInstance}Id}")
	\@Produces(MediaType.APPLICATION_JSON)
	public Response get${resourceClass}(\@PathParam("${resourceInstance}Id") int ${resourceInstance}Id);

	\@DELETE
	\@Path(value = "/{${resourceInstance}Id}")
	\@Produces(MediaType.APPLICATION_JSON)
	public Response delete${resourceClass}(\@PathParam("${resourceInstance}Id") int ${resourceInstance}Id);

	\@PUT
	\@Path(value = "/{${resourceInstance}Id}")
    \@Consumes(MediaType.APPLICATION_JSON)	
	\@Produces(MediaType.APPLICATION_JSON)
	public Response put${resourceClass}(\@PathParam("${resourceInstance}Id") int ${resourceInstance}Id, ${resourceClass} ${resourceInstance});

	\@POST
	\@Path(value = "/{${resourceInstance}Id}")
	\@Produces(MediaType.APPLICATION_JSON)
	public Response post${resourceClass}(\@PathParam("${resourceInstance}Id") int ${resourceInstance}Id);	
}

__END1__
;

$fh->close();

#
# Build the Service Implementation file.
#
$fh = FileHandle->new($resourceClass.'ServiceImpl.java', "w");
die "$!" unless defined $fh;

print $fh <<__END2__

package ${packagePath}.impl;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.mit.ll.hps.phinics.papi.dataaccess.${resourceClass}DAO;
import edu.mit.ll.hps.phinics.papi.dataaccess.ICSDatastoreException;
import edu.mit.ll.hps.phinics.papi.rs.${resourceClass};
import edu.mit.ll.hps.phinics.papi.rs.${resourceClass}ServiceResponse;
import edu.mit.ll.hps.phinics.papi.rs.${resourceClass}Service;
import edu.mit.ll.hps.phinics.papi.util.PAPILogger;

/**
 * 
 * \@AUTHOR sa23148
 *
 */
public class ${resourceClass}ServiceImpl implements ${resourceClass}Service {

	private static final String CNAME = ${resourceClass}ServiceImpl.class.getName();
	
	/**
	 * Read and return all ${resourceClass} items.
	 * \@return Response
	 * \@see ${resourceClass}Response
	 */
	public Response get${resourceClass}s() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/* 
		Response response = null;
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();

        Set<Integer> ${resourceInstance}Ids = ${resourceClass}DAO.getInstance().getAll${resourceClass}Ids();
		for (Integer ${resourceInstance}Id : ${resourceInstance}Ids) {
			${resourceInstance}Response.get${resourceClass}s().add(${resourceClass}DAO.getInstance().get${resourceClass}ById(${resourceInstance}Id));
		}
		${resourceInstance}Response.setMessage("ok");
		${resourceInstance}Response.setCount(${resourceInstance}Ids.size());
		response = Response.ok(${resourceInstance}Response).status(Status.OK).build();

		return response;
		*/
	}

	/**
	 * Delete all ${resourceClass} items.
	 * This is an unsupported operation.
	 * @return Response
	 * @see ${resourceClass}Response
	 */
	public Response delete${resourceClass}s() {
		return makeUnsupportedOpRequestResponse();
	}

	/**
	 * Bulk creation of ${resourceClass} items.
	 * @param A collection of ${resourceClass} items to be created.
	 * @return Response
	 * @see ${resourceClass}Response
	 */
	public Response put${resourceClass}s(Collection<${resourceClass}> ${resourceInstance}s) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();
		Response response = null;
		int errorCount = 0;
		for (${resourceClass} ${resourceInstance} : ${resourceInstance}s) {
			try {
				${resourceClass} new${resourceClass} = ${resourceClass}DAO.getInstance().create${resourceClass}(${resourceInstance});
				${resourceInstance}Response.get${resourceClass}s().add(new${resourceClass});
			} catch (ICSDatastoreException e) {
				PAPILogger.getInstance().e(CNAME, e.getMessage());
				++errorCount;
			}			
		}
		
		if (errorCount == 0) {
			${resourceInstance}Response.setMessage("ok");
			${resourceInstance}Response.setCount(${resourceInstance}Ids.size());
			response = Response.ok(${resourceInstance}Response).status(Status.OK).build();			
		} else {
			${resourceInstance}Response.setMessage("Failures. " + errorCount + " out of " + ${resourceInstance}s.size() + " were not created.");
			response = Response.ok(${resourceInstance}Response).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;
		*/
	}

	/**
	 *  Creation of a single ${resourceClass} item.
	 * @param A collection of ${resourceClass} items to be created.
	 * @return Response
	 * @see ${resourceClass}Response
	 */	
	public Response post${resourceClass}s(${resourceClass} ${resourceInstance}) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();
		Response response = null;

		${resourceClass} new${resourceClass} = null;
		try {
			new${resourceClass} = ${resourceClass}DAO.getInstance().create${resourceClass}(${resourceInstance});
			${resourceInstance}Response.get${resourceClass}s().add(new${resourceClass});
			${resourceInstance}Response.setMessage("ok");
			${resourceInstance}Response.setCount(${resourceInstance}Ids.size());
			response = Response.ok(${resourceInstance}Response).status(Status.OK).build();
		} catch (ICSDatastoreException e) {
			${resourceInstance}Response.setMessage("failed to create ${resourceInstance}.");
			response = Response.ok(${resourceInstance}Response).status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return response;
		*/
	}


	/**
	 *  Read a single ${resourceClass} item.
	 * @param ID of ${resourceClass} item to be read.
	 * @return Response
	 * @see ${resourceClass}Response
	 */	
	public Response get${resourceClass}(int ${resourceInstance}Id) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();

		if (${resourceInstance}Id < 1) {
			${resourceInstance}Response.setMessage("Invalid ${resourceInstance}Id value: " + ${resourceInstance}Id) ;
			response = Response.ok(${resourceInstance}Response).status(Status.BAD_REQUEST).build();
			return response;
		}

		${resourceClass} u = ${resourceClass}DAO.getInstance().get${resourceClass}ById(${resourceInstance}Id);
		if (u == null) {
			${resourceInstance}Response.setMessage("No ${resourceInstance} found for ${resourceInstance}Id value: " + ${resourceInstance}Id) ;
			response = Response.ok(${resourceInstance}Response).status(Status.NOT_FOUND).build();
			return response;			
		}

		${resourceInstance}Response.get${resourceClass}s().add(u);
		${resourceInstance}Response.setMessage("ok");
		${resourceInstance}Response.setCount(1);
		response = Response.ok(${resourceInstance}Response).status(Status.OK).build();

		return response;
		*/
	}

	/**
	 *  Delete a single ${resourceClass} item.
	 * @param ID of ${resourceClass} item to be read.
	 * @return Response
	 * @see ${resourceClass}Response
	 */	
	public Response delete${resourceClass}(int ${resourceInstance}Id) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();

		if (${resourceInstance}Id < 1) {
			${resourceInstance}Response.setMessage("Invalid ${resourceInstance}Id value: " + ${resourceInstance}Id) ;
			response = Response.ok(${resourceInstance}Response).status(Status.BAD_REQUEST).build();
			return response;
		}

		try {
			${resourceClass}DAO.getInstance().remove${resourceClass}(${resourceInstance}Id);
			${resourceInstance}Response.setMessage("ok");
			response = Response.ok(${resourceInstance}Response).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			${resourceInstance}Response.setMessage(e.getMessage()) ;
			response = Response.ok(${resourceInstance}Response).status(Status.NOT_FOUND).build();			
		}

		return response;
		*/
	}

	/**
	 *  Update a single ${resourceClass} item.
	 * @param ID of ${resourceClass} item to be read.
	 * @return Response
	 * @see ${resourceClass}Response
	 */	
	public Response put${resourceClass}(int ${resourceInstance}Id, ${resourceClass} ${resourceInstance}) {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		Response response = null;
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();

		if (${resourceInstance}Id < 1) {
			${resourceInstance}Response.setMessage("Invalid ${resourceInstance}Id value: " + ${resourceInstance}Id) ;
			response = Response.ok(${resourceInstance}Response).status(Status.BAD_REQUEST).build();
			return response;
		}

		if (${resourceInstance} == null) {
			${resourceInstance}Response.setMessage("Invalid null ${resourceClass} object.") ;
			response = Response.ok(${resourceInstance}Response).status(Status.BAD_REQUEST).build();
			return response;
		}		

		try {
			${resourceClass}DAO.getInstance().update${resourceClass}(${resourceInstance}Id, ${resourceInstance});
			${resourceClass} u = ${resourceClass}DAO.getInstance().get${resourceClass}ById(${resourceInstance}Id);
			${resourceInstance}Response.get${resourceClass}s().add(u);
			${resourceInstance}Response.setMessage("ok");
			response = Response.ok(${resourceInstance}Response).status(Status.OK).build();			
		} catch (ICSDatastoreException e) {
			${resourceInstance}Response.setMessage(e.getMessage()) ;
			response = Response.ok(${resourceInstance}Response).status(Status.NOT_FOUND).build();	
		}

		return response;
		*/
	}

	/**
	 *  Post a single ${resourceClass} item.
	 *  This is an illegal operation. 
	 * @param ID of ${resourceClass} item to be read.
	 * @return Response
	 * @see ${resourceClass}Response
	 */	
	public Response post${resourceClass}(int ${resourceInstance}Id) {
		// Illegal as per RESTful guidelines.
		return makeIllegalOpRequestResponse();
	}

	/**
	 *  Return the number of ${resourceClass} items stored. 
	 * @return Response
	 * @see ${resourceClass}Response
	 */		
	public Response get${resourceClass}Count() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
		/*	
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();
		${resourceInstance}Response.setMessage("ok");
		${resourceInstance}Response.setCount(${resourceClass}DAO.getInstance().get${resourceClass}Count());
		${resourceInstance}Response.set${resourceClass}s(null);
		Response response = Response.ok(${resourceInstance}Response).status(Status.OK).build();		
		return response;
		*/
	}


	/**
	 *  Search the ${resourceClass} items stored. 
	 * @return Response
	 * @see ${resourceClass}Response
	 */		
	public Response search${resourceClass}Resources() {
		// TODO: Needs implementation.
		return makeUnsupportedOpRequestResponse();
	}
	
	private Response makeIllegalOpRequestResponse() {
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();
		${resourceInstance}Response.setMessage("Request ignored.") ;
		Response response = Response.notModified("Illegal operation requested").
			status(Status.FORBIDDEN).build();
		return response;
	}
	
	private Response makeUnsupportedOpRequestResponse() {
		${resourceClass}ServiceResponse ${resourceInstance}Response = new ${resourceClass}ServiceResponse();
		${resourceInstance}Response.setMessage("Request ignored.") ;
		Response response = Response.notModified("Unsupported operation requested").
			status(Status.NOT_IMPLEMENTED).build();
		return response;
	}		
}

__END2__
;

$fh->close();

#
# Build the Service Response file.
#
$fh = FileHandle->new($resourceClass.'ServiceResponse.java', "w");
die "$!" unless defined $fh;

print $fh <<__END3__

package edu.mit.ll.hps.phinics.papi.rs;

import java.util.ArrayList;
import java.util.Collection;

public class ${resourceClass}ServiceResponse {

	private String message;
	
	private Collection<${resourceClass}> ${resourceClass}s = new ArrayList<${resourceClass}>();
	
	// TODO: Really used for returning a count REST request; i.e., do not get
	// the list of ${resourceClass}s just the count.
	private int count;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<${resourceClass}> get${resourceClass}s() {
		return ${resourceClass}s;
	}

	public void set${resourceClass}s(Collection<${resourceClass}> ${resourceClass}s) {
		this.${resourceClass}s = ${resourceClass}s;
		if (${resourceClass}s != null) {
			// Goofy, I know, but needed because this attribute is also used for returning
			// the count of ${resourceClass}s, without necessarily reading the ${resourceClass} objects.
			// So, let's keep this attribute meaningful.
			count = ${resourceClass}s.size();
		}
	}

	@Override
	public String toString() {
		return "${resourceClass}ServiceResponse [${resourceClass}s=" + ${resourceClass}s + ", message="
				+ message + "]";
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}	
}

__END3__
;

#
# Build the Data Access Object file.
#
$fh = FileHandle->new($resourceClass.'DAO.java', "w");
die "$!" unless defined $fh;

print $fh <<__END4__

package edu.mit.ll.hps.phinics.papi.dataaccess;

import java.util.Set;

import edu.mit.ll.hps.phinics.papi.rs.${resourceClass};


public class ${resourceClass}DAO extends BaseDAO {
	
	private static final String CNAME = ${resourceClass}DAO.class.getName();

	// Lazy-initialization Holder class idiom.
	private static class Holder {
		public static ${resourceClass}DAO instance = new ${resourceClass}DAO();
	}

	public static ${resourceClass}DAO getInstance() {
		return Holder.instance;
	}
		
	public ${resourceClass} create${resourceClass}(${resourceClass} ${resourceInstance}) throws ICSDatastoreException {
		if (${resourceInstance} == null) {
			throw new NullPointerException(CNAME + ":create${resourceClass} called with null ${resourceInstance} argument");
		}
		// TODO: Insert implementation here.
		return ${resourceInstance};
	}	
	
	public Set<Integer> getAll${resourceClass}Ids() {
		Set<Integer> ret = null;
		// TODO: Insert implementation here. 
		return ret;
	}

	public ${resourceClass} get${resourceClass}ById(int id) {
		${resourceClass} ret = null;
		// TODO: Insert implementation here. 
		return ret;
	}

	public void remove${resourceClass}(int id) throws ICSDatastoreException {
		// TODO: Insert implementation here. 
	}
	
	public ${resourceClass} update${resourceClass}(int ${resourceInstance}Id, ${resourceClass} other) throws ICSDatastoreException {
		${resourceClass} ret = null;
		if (other == null) {
			throw new NullPointerException(CNAME + ":update${resourceClass} called with null ${resourceInstance} argument");
		}
		// TODO: Insert implementation here.
		return ret;
	}

	public int get${resourceClass}Count() {
		int count = -1;
		// TODO: Insert implementation here.
		return count;
	}
}


__END4__
;

#
# Build the client/server shared resource bean.
#
$fh = FileHandle->new($resourceClass.'.java', "w");
die "$!" unless defined $fh;

print $fh <<__END5__

package edu.mit.ll.hps.phinics.papi.rs;

public class ${resourceClass} extends PAPIBean {

}

__END5__
;
