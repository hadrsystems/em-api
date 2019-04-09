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
package edu.mit.ll.em.api.openam;

import edu.mit.ll.em.api.rs.RegisterUser;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.sso.util.SSOUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("FieldCanBeLocal")
public class OpenAmGatewayTest {

    private SSOUtil ssoUtil;
    private OpenAmGateway gateway;
    private User user;
    private RegisterUser registerUser;

    @Before
    public void setUp() throws Exception {
        ssoUtil = mock(SSOUtil.class);
        gateway = new OpenAmGateway(ssoUtil);
        user = new User();
        user.setUsername("tester@tabordasolutions.net");
        user.setFirstname("Test");
        user.setLastname("Testerson");
        registerUser = new RegisterUser();
        registerUser.setPassword("password1");
    }

    @Test
    public void testCreateIdentityUser() throws Exception {
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.createUser(
                "tester@tabordasolutions.net",
                "password1",
                "Test",
                "Testerson",
                false)
        ).thenReturn("{\"status\":\"success\", \"message\":\" successfully created identity for tester@tabordasolutions.net\"}");

        JSONObject response = gateway.createIdentityUser(user, registerUser);
        assertEquals("success", response.getString("status"));
    }

    @Test
    public void testCreateIdentityUserFailure() throws Exception {
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.createUser("tester@tabordasolutions.net", "password1", "Test", "Testerson", false))
                .thenReturn("{\"status\":\"fail\", \"message\":\"badness\"}");

        JSONObject response = gateway.createIdentityUser(user, registerUser);
        assertEquals("fail", response.getString("status"));
        assertEquals("badness", response.getString("message"));
    }

    @Test
    public void testCreateIdentityUserFailsWithOpenAmLoginFailure() throws Exception {
        when(ssoUtil.loginAsAdmin()).thenReturn(false);
        JSONObject response = gateway.createIdentityUser(user, registerUser);
        assertEquals("fail", response.getString("status"));
        assertEquals("Failed to log in as Administrator with SSOUtil. Cannot create Identity.", response.getString("message"));
    }

    @Test
    public void testCreateUserFailsWithInvalidResponseFromOpenAm() throws Exception {
        String uid = "tester@tabordasolutions.net";
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.createUser("tester@tabordasolutions.net", "password1", "Test", "Testerson", false))
                    .thenReturn("badness");
        JSONObject response = gateway.createIdentityUser(user, registerUser);
        assertEquals("fail", response.getString("status"));
        assertTrue(response.getString("message").startsWith("JSON exception reading createUser response"));
    }

    @Test
    public void testDeleteIdentityUserFailure() throws Exception {
        String uid = "tester@tabordasolutions.net";
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.deleteUser(uid))
                .thenReturn("{\"status\":\"fail\", \"message\":\"Can't find user by uid\"}");

        JSONObject response = gateway.deleteIdentityUser(uid);
        assertEquals("fail", response.getString("status"));
        assertEquals("Can't find user by uid", response.getString("message"));
    }

    @Test
    public void testDeleteIdentityUser() throws Exception {
        String uid = "tester@tabordasolutions.net";
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.deleteUser(uid))
                .thenReturn("{\"status\":\"success\", \"message\":\"successfully deleted identity with uid : " + uid + "\"}");
        JSONObject response = gateway.deleteIdentityUser(uid);
        assertEquals("success", response.getString("status"));
        assertEquals("successfully deleted identity with uid : " + uid, response.getString("message"));
    }

    @Test
    public void testDeleteUserFailsWithOpenAmLoginFailure() throws Exception {
        String uid = "tester@tabordasolutions.net";
        when(ssoUtil.loginAsAdmin()).thenReturn(false);
        JSONObject response = gateway.deleteIdentityUser(uid);
        assertEquals("fail", response.getString("status"));
        assertEquals("Failed to log in as Administrator with SSOUtil. Cannot delete Identity with uid : " + uid, response.getString("message"));
    }

    @Test
    public void testDeleteUserFailsWithInvalidResponseFromOpenAm() throws Exception {
        String uid = "tester@tabordasolutions.net";
        when(ssoUtil.loginAsAdmin()).thenReturn(true);
        when(ssoUtil.deleteUser(uid)).thenReturn("Error");
        JSONObject response = gateway.deleteIdentityUser(uid);
        assertEquals("fail", response.getString("status"));
        assertTrue(response.getString("message").startsWith("JSON exception reading delete identity response"));
    }
}
