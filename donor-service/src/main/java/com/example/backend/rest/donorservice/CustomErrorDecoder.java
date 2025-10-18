package com.example.backend.rest.donorservice;


import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 401) {
            return new FeignException.Unauthorized("Unauthorized", response.request(), null, null);
        }
        if (response.status() == 404) {
            return new FeignException.NotFound("Not Found", response.request(), null, null);
        }
        return FeignException.errorStatus(methodKey, response);
    }
}