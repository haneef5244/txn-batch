package com.process.txnbatch.util;

import com.common.txnintegration.resp.AuthResp;
import com.common.txnintegration.util.ResourceConstantUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Service
public class AuthService {

    Logger log = LoggerFactory.getLogger(AuthService.class);

    @Value("${oauth.client.id}")
    private String clientId;

    @Value("${oauth.client.secret}")
    private String clientSecret;

    @Value("${oauth.admin.username}")
    private String adminUsername;

    @Value("${oauth.admin.password}")
    private String adminPassword;

    @Value("${oauth.grant.type}")
    private String grantType;

    private String token;

    public String getTokenInstance() {
        if (token.isEmpty()) {
            getNewToken();
            return token;
        }
        return token;
    }

    public void getNewToken() {
        RestTemplate restTemplate = new RestTemplate();
        /** Authentication */
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username",adminUsername);
        map.add("password",adminPassword);
        map.add("grant_type",grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, createHeaders(clientId, clientSecret));

        ResponseEntity<AuthResp> response = restTemplate.exchange(ResourceConstantUtil.TXN_CUSTOMER_GET_TOKEN,
                HttpMethod.POST,
                entity,
                AuthResp.class);
        token = response.getBody().getAccess_token();
    }

    private HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }


}
