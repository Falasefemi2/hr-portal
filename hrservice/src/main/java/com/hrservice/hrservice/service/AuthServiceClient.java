package com.hrservice.hrservice.service;

import com.hrservice.hrservice.config.AuthServiceConfig;
import com.hrservice.hrservice.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final AuthServiceConfig authServiceConfig;
    private final RestTemplate restTemplate;

    public UserInfo validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ValidateTokenResponse> response = restTemplate.exchange(
                    authServiceConfig.getUrl() + "/auth/validate",
                    HttpMethod.GET,
                    entity,
                    ValidateTokenResponse.class
            );
            if (response.getBody() != null && response.getBody().getValid()) {
                return response.getBody().getUser();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserInfo> getUsersByDepartment(Long departmentId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<UserInfo>> response = restTemplate.exchange(
                    authServiceConfig.getUrl() + "/auth/users/department/" + departmentId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UserInfo>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            return List.of();
        }
    }

    public UserInfo getUserByEmployeeId(String employeeId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserInfo> response = restTemplate.exchange(
                    authServiceConfig.getUrl() + "/auth/users/employee/" + employeeId,
                    HttpMethod.GET,
                    entity,
                    UserInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    @lombok.Data
    private static class ValidateTokenResponse {
        private Boolean valid;
        private UserInfo user;
    }
}

