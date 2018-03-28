package edu.mit.ll.em.api.rs;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ActiveSessionResponseTest {

    @Test
    public void equalsReturnsTrueGivenEqualObjects() {
        Object differentObject = new Object();
        ActiveSessionResponse a1 = new ActiveSessionResponse(200, "ok", true);
        ActiveSessionResponse a2EqualsToa1 = new ActiveSessionResponse(200, "ok", true);
        ActiveSessionResponse a3NotEqualToa1 = new ActiveSessionResponse(400, "ok", true);
        ActiveSessionResponse a4NotEqualToa1 = new ActiveSessionResponse(200, "Not ok", true);
        ActiveSessionResponse a5NotEqualToa1 = new ActiveSessionResponse(200, "ok", false);
        ActiveSessionResponse a6NotEqualToa1 = new ActiveSessionResponse(400, "Not ok", false);

        assertFalse(a1.equals(null));
        assertTrue(a1.equals(a1));
        assertTrue(a1.equals(a2EqualsToa1));
        assertFalse(a1.equals(a3NotEqualToa1));
        assertFalse(a1.equals(a4NotEqualToa1));
        assertFalse(a1.equals(a5NotEqualToa1));
        assertFalse(a1.equals(a6NotEqualToa1));
        assertFalse(a1.equals(differentObject));
    }

}
