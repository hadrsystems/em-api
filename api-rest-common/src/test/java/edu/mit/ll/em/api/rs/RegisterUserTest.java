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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class RegisterUserTest {
    private static Validator validator;
    private static final String PASSWORD_ERROR_MESSAGE = "Please provide Password of 8-20 characters with at least one digit, one upper case letter, one lower case letter and one special symbol @#$%-_!.";
    private static final String FIRST_NAME_ERROR_MESSAGE = "Please provide First Name.";
    private static final String LAST_NAME_ERROR_MESSAGE = "Please provide Last Name.";
    private static final String EMAIL_ERROR_MESSAGE = "Please provide valid Email Address.";
    private static final String PHONE_NUMBER_ERROR_MESSAGE = "Please provide valid Phone number.";
    private static final String ORGANIZATION_TYPE_ID_ERROR_MESSAGE = "Please provide Organization Type Id.";
    private static final String ORGANIZATION_ID_ERROR_MESSAGE = "Please provide Organization Id.";
    private RegisterUser registerUser;

    @BeforeClass
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Before
    public void setup() {
        registerUser = new RegisterUser(8, 1, "firstName", "lastName", "email@hehe.com", "(543) 211-9264", "Ia@hhrom7", Arrays.asList(1, 5));
    }

    @Test
    public void passwordIsRequired() {
        registerUser.setPassword(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());

        registerUser.setPassword("");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustHaveLowercaseLetter() {
        registerUser.setPassword("PASSWORD#1");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());

        registerUser.setPassword("PaSSWORD#1");
        violations = validator.validate(registerUser);
        assertEquals(0, violations.size());
    }

    @Test
    public void passwordMustHaveAtleastEightCharacters() {
        registerUser.setPassword("PaS@");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustHaveMaximumTenCharacters() {
        registerUser.setPassword("Password#1Password#11");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustHaveANumeric() {
        registerUser.setPassword("Password#");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustHaveUppercaseLetter() {
        registerUser.setPassword("password#1");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustHaveOneOfSpecialCharacters() {
        registerUser.setPassword("Password1");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void passwordMustNotHaveAnyCharactersApartFromSpecified() { //apart from alphabet(upper,lower), numeric, special chars (@,#,$,%,_,!,-)
        registerUser.setPassword("Password1# ");
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals(PASSWORD_ERROR_MESSAGE, violation.getMessage());

        registerUser.setPassword("Password#1");
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void firstNameIsRequired() {
        registerUser.setFirstName(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("firstName", violation.getPropertyPath().toString());
        assertEquals(FIRST_NAME_ERROR_MESSAGE, violation.getMessage());

        registerUser.setFirstName("");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("firstName", violation.getPropertyPath().toString());
        assertEquals(FIRST_NAME_ERROR_MESSAGE, violation.getMessage());

        registerUser.setFirstName("     ");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("firstName", violation.getPropertyPath().toString());
        assertEquals(FIRST_NAME_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void lastNameIsRequired() {
        registerUser.setLastName(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("lastName", violation.getPropertyPath().toString());
        assertEquals(LAST_NAME_ERROR_MESSAGE, violation.getMessage());

        registerUser.setLastName("");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("lastName", violation.getPropertyPath().toString());
        assertEquals(LAST_NAME_ERROR_MESSAGE, violation.getMessage());

        registerUser.setLastName("     ");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("lastName", violation.getPropertyPath().toString());
        assertEquals(LAST_NAME_ERROR_MESSAGE, violation.getMessage());
    }

    @Test
    public void testEmailAddressValidation() {
        registerUser.setEmail(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals(EMAIL_ERROR_MESSAGE, violation.getMessage());

        registerUser.setEmail("");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals(EMAIL_ERROR_MESSAGE, violation.getMessage());

        registerUser.setEmail("     ");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals(EMAIL_ERROR_MESSAGE, violation.getMessage());

        registerUser.setEmail("HLL");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals(EMAIL_ERROR_MESSAGE, violation.getMessage());

        registerUser.setEmail("HEAH@GH");
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());

        registerUser.setEmail("HEAH@help.org");
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testPhoneValidation() {
        registerUser.setPhone(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());

        registerUser.setPhone("");
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());

        registerUser.setPhone("    ");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("phone", violation.getPropertyPath().toString());
        assertEquals(PHONE_NUMBER_ERROR_MESSAGE, violation.getMessage());

        registerUser.setPhone("953 200-1212");
        violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        violation = violations.iterator().next();
        assertEquals("phone", violation.getPropertyPath().toString());
        assertEquals(PHONE_NUMBER_ERROR_MESSAGE, violation.getMessage());

        registerUser.setPhone("(953) 821-8319");
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void organizationTypeIdIsRequired() {
        registerUser.setOrganizationTypeId(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("organizationTypeId", violation.getPropertyPath().toString());
        assertEquals(ORGANIZATION_TYPE_ID_ERROR_MESSAGE, violation.getMessage());

        registerUser.setOrganizationTypeId(1);
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void organizationIdIsRequired() {
        registerUser.setOrganizationId(null);
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(registerUser);
        assertEquals(1, violations.size());
        ConstraintViolation<RegisterUser> violation = violations.iterator().next();
        assertEquals("organizationId", violation.getPropertyPath().toString());
        assertEquals(ORGANIZATION_ID_ERROR_MESSAGE, violation.getMessage());

        registerUser.setOrganizationId(1);
        violations = validator.validate(registerUser);
        assertTrue(violations.isEmpty());
    }
}