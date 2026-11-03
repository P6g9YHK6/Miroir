package com.miroir;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MiroirServiceAuthTest {

    private final MiroirService service = new MiroirService();

    @Test
    public void noneModeAlwaysAuthorized() {
        MiroirService.setAuthMode(MiroirService.AuthMode.NONE);
        assertTrue(service.isAuthorized("", Collections.emptyMap()));
        assertTrue(service.isAuthorized(null, Collections.emptyMap()));
    }

    @Test
    public void pinModeRequiresMatchingPin() {
        MiroirService.setAuthMode(MiroirService.AuthMode.PIN);
        MiroirService.setRequiredPin("123456");

        assertTrue(service.isAuthorized("pin=123456", Collections.emptyMap()));
        assertFalse(service.isAuthorized("pin=000000", Collections.emptyMap()));
        assertFalse(service.isAuthorized("", Collections.emptyMap()));
        assertFalse(service.isAuthorized(null, Collections.emptyMap()));
    }

    @Test
    public void pinModeWithEmptyConfiguredPinDeniesEverything() {
        MiroirService.setAuthMode(MiroirService.AuthMode.PIN);
        MiroirService.setRequiredPin("");
        assertFalse(service.isAuthorized("pin=", Collections.emptyMap()));
        assertFalse(service.isAuthorized("pin=anything", Collections.emptyMap()));
    }

    @Test
    public void basicModeRequiresMatchingCredentials() {
        MiroirService.setAuthMode(MiroirService.AuthMode.BASIC);
        MiroirService.setBasicCredentials("viewer", "s3cret");

        Map<String, String> okHeaders = new HashMap<>();
        okHeaders.put("authorization", "Basic " + basicAuthValue("viewer", "s3cret"));
        assertTrue(service.isAuthorized("", okHeaders));

        Map<String, String> wrongHeaders = new HashMap<>();
        wrongHeaders.put("authorization", "Basic " + basicAuthValue("viewer", "wrong"));
        assertFalse(service.isAuthorized("", wrongHeaders));

        assertFalse(service.isAuthorized("", Collections.emptyMap()));
    }

    @Test
    public void basicModeRejectsMalformedAuthorizationHeader() {
        MiroirService.setAuthMode(MiroirService.AuthMode.BASIC);
        MiroirService.setBasicCredentials("viewer", "s3cret");

        Map<String, String> notBasic = new HashMap<>();
        notBasic.put("authorization", "Bearer abcdef");
        assertFalse(service.isAuthorized("", notBasic));

        Map<String, String> notBase64 = new HashMap<>();
        notBase64.put("authorization", "Basic %%%not-base64%%%");
        assertFalse(service.isAuthorized("", notBase64));

        Map<String, String> noColon = new HashMap<>();
        noColon.put("authorization", "Basic " + android.util.Base64.encodeToString(
            "novalidseparator".getBytes(), android.util.Base64.NO_WRAP));
        assertFalse(service.isAuthorized("", noColon));
    }

    @Test
    public void basicModeWithNoConfiguredCredentialsDeniesEverything() {
        MiroirService.setAuthMode(MiroirService.AuthMode.BASIC);
        MiroirService.setBasicCredentials("", "");

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Basic " + basicAuthValue("", ""));
        assertFalse(service.isAuthorized("", headers));
    }

    private static String basicAuthValue(String user, String pass) {
        return android.util.Base64.encodeToString(
            (user + ":" + pass).getBytes(), android.util.Base64.NO_WRAP);
    }
}
