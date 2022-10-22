package com.epam.microservices.gateway.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.cloud.gateway.support.MvcFoundOnClasspathFailureAnalyzer.MESSAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

  private static final Logger logger = LoggerFactory.getLogger(GlobalErrorAttributes.class);

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
    Throwable error = this.getError(request);
    MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
      .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
    HttpStatus errorStatus = findHttpStatus(error, responseStatusAnnotation);
    logger.info("errorStatus: {}", errorStatus);
    Map<String, Object> map = super.getErrorAttributes(request, options);
    String errorCode = getErrorCode(map, errorStatus);
    map.remove("timestamp");
    map.remove("path");
    map.remove("error");
    map.remove("requestId");
    map.put("errorCode", errorCode);
    return map;
  }

  private HttpStatus findHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
    if (error instanceof ResponseStatusException) {
      return ((ResponseStatusException) error).getStatus();
    }
    return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(INTERNAL_SERVER_ERROR);
  }

  private String getErrorCode(Map<String, Object> map, HttpStatus errorStatus) {
    String errorCode;
    switch (errorStatus) {
      case UNAUTHORIZED:
        errorCode = "401 UnAuthorized";
        break;
      case NOT_FOUND:
        logger.warn("The url: {} is not found", map.get("path"));
        errorCode = "404 Not Found";
        map.put(MESSAGE, "NOT FOUND");
        break;
      case METHOD_NOT_ALLOWED:
        errorCode = "405 Method Not Allowed";
        break;
      default:
        errorCode = "500 Internal Server Error";
        map.put(MESSAGE, "Unexpected Error");
    }
    return errorCode;
  }
}