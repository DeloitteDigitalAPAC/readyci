package com.squarepolka.readyci.util.appcenter;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpHeaders.TIMEOUT;

public class AppCenterHelper {

    private final RestTemplate restTemplate;
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

    private String token;
    private String appOwner;
    private String appName;

    public AppCenterHelper(String token, String appOwner, String appName) {

        this.token = token;
        this.appOwner = appOwner;
        this.appName = appName;

        interceptors.add(new HeaderRequestInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE));
        interceptors.add(new HeaderRequestInterceptor("X-API-Token", token));

        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(interceptors);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

    }

    public ACUploadConfiguration getUploadConfiguration() {
        String url = String.format("https://api.appcenter.ms/v0.1/apps/%s/%s/release_uploads", appOwner, appName);
        return restTemplate.postForObject(url, null, ACUploadConfiguration.class);
    }

    public ACUploadConfirmationResponse markAsComplete(String uploadId) {
        String url = String.format("https://api.appcenter.ms/v0.1/apps/%s/%s/release_uploads/%s", appOwner, appName, uploadId);
        ACUploadConfirmationRequest request = new ACUploadConfirmationRequest("committed");
        return restTemplate.patchForObject(url, request, ACUploadConfirmationResponse.class);
    }

    public ACReleaseResponse releaseBuild(String releaseId, String releaseNotes, String distributionGroup) {
        ACReleaseRequest request = new ACReleaseRequest(distributionGroup, releaseNotes);
        String url = String.format("https://api.appcenter.ms/v0.1/apps/%s/%s/releases/%s", appOwner, appName, releaseId);
        return restTemplate.patchForObject(url, request, ACReleaseResponse.class);
    }


    static class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

        private final String headerName;
        private final String headerValue;

        HeaderRequestInterceptor(String headerName, String headerValue) {
            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().set(headerName, headerValue);
            return execution.execute(request, body);
        }
    }
}
