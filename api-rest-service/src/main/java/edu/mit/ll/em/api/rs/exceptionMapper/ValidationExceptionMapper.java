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
package edu.mit.ll.em.api.rs.exceptionMapper;

import edu.mit.ll.em.api.rs.ValidationErrorResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    protected static final String VALIDATION_ERROR_MESSAGE = "Invalid Registration data";

    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> validationErrors = this.getValidationErrors(exception.getConstraintViolations());
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), VALIDATION_ERROR_MESSAGE, validationErrors);
        return Response.ok().status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

    private Map<String, String> getValidationErrors(Set<ConstraintViolation<?>> violations) {
        Map<String, String> validationErrors = new HashMap<String, String>();
        Iterator<ConstraintViolation<?>> iterator = violations.iterator();
        String arg1String = "arg1.";
        while(iterator.hasNext()) {
            ConstraintViolation violation = iterator.next();
            String attributeName = violation.getPropertyPath().toString();
            int arg1Index = attributeName.indexOf("arg1.");
            if(arg1Index > -1) {
                attributeName = attributeName.substring(arg1Index + arg1String.length());
            }
            validationErrors.put(attributeName, violation.getMessage());
        }
        return validationErrors;
    }
}