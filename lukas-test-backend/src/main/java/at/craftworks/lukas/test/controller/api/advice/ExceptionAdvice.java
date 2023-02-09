/*
 * Copyright (c) 2014-2018 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2018-10-25
 */
package at.craftworks.lukas.test.controller.api.advice;

import com.fasterxml.jackson.databind.JsonMappingException;
import  at.craftworks.lukas.test.dto.frontend.error.ErrorReason;
import  at.craftworks.lukas.test.dto.frontend.error.RestErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controller Advice that takes care of translating exceptions into Error DTOs
 * <p>
 *     It also logs the errors - depending on the project the logging should probably be reduced or increased
 * </p>
 */
@Slf4j
@ControllerAdvice
public class ExceptionAdvice {
    private final MessageSource messageSource;

    public ExceptionAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    /*
     * Generic unhandled exception resulting in HTTP status 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("squid:S1872")
    public ResponseEntity<RestErrorDTO> handler(Exception ex, WebRequest request) {
        // avoids compile/runtime dependency by using class name
        if (ex.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
            // ignore when client aborts request
            return null;
        }
        return unhandledRestError(ex, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestErrorDTO> handler(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return validationRestError(ex, request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RestErrorDTO> handler(MissingServletRequestPartException ex, WebRequest request) {
        return validationRestError(ex, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RestErrorDTO> handler(MissingServletRequestParameterException ex, WebRequest request) {
        return validationRestError(ex, request);
    }

    /*
     * Generic validation exception resulting in HTTP status 400 Bad Request. This is the result of a failing validation
     * of a request body object annotated with @Valid or @Validated.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorDTO> handler(MethodArgumentNotValidException ex, WebRequest request) {
        return validationRestError(ex, request);
    }


    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<RestErrorDTO> handler(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.CONFLICT.value());
        error.setReason(ErrorReason.CONFLICT.getCode());
        error.setMessage(ErrorReason.CONFLICT.getMessage());

        log.info("Returning from REST API " + request.toString() + " with error and HTTP status 409", ex);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /*
     * Generic exception indicating that a request could not be processed because of syntax errors. In case of a
     * JsonMappingException this is returned as validation error with HTTP status 400 Bad Request and
     * detailed field and object errors
     * In all other cases a generic validation error is sent back
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestErrorDTO> handler(HttpMessageNotReadableException ex, WebRequest request) {
        if (ex.getCause() instanceof JsonMappingException cause) {
            return validationRestError(cause, request);
        } else {
            return validationRestError(ex, request);
        }
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RestErrorDTO> handler(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());

        log.debug("[HttpMediaTypeNotSupportedException] Returning from REST API " + request.toString() +
                " with error and HTTP status 409", ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RestErrorDTO> handler(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return httpMethodNotSupported(ex, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestErrorDTO> handler(AccessDeniedException ex, WebRequest request) {
        return accessDenied();
    }

    // TODO add exceptionhandlers for your custom exceptions here




    // HELPERS

    private ResponseEntity<RestErrorDTO> unhandledRestError(Throwable ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setReason(ErrorReason.UNHANDLED_ERROR.getCode());
        error.setMessage(ErrorReason.UNHANDLED_ERROR.getMessage());
        log.info("Unhandled exception in REST API " + request.toString() + ", returning with HTTP status 500", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<RestErrorDTO> httpMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(MessageFormat.format("The HTTP {0} method is not supported", ex.getMethod()));
        error.setReason(ErrorReason.HTTP_METHOD_NOT_SUPPORTED.getCode());
        log.info("HttpRequestMethodNotSupportedException for request {} - HTTP status 405", request.toString());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(MethodArgumentTypeMismatchException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        error.addFieldError(ex.getName(), "TYPE_MISSMATCH", ex.getMessage());

        log.info("Returning from REST API " + request.toString() + " with error and HTTP status 400", ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(MissingServletRequestPartException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        error.addFieldError(ex.getRequestPartName(), "PARAMETER_MISSING", ex.getMessage());

        log.info("MissingServletRequestPartException: Returning from REST API {} with error and HTTP status 400", request.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(HttpMessageNotReadableException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());

        log.info("HttpMessageNotReadableException: Returning from REST API {} with error and HTTP status 400", request.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(MissingServletRequestParameterException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        error.addFieldError(ex.getParameterName(), "PARAMETER_MISSING", ex.getMessage());

        log.info("MissingServletRequestParameterException: Returning from REST API {} with error and HTTP status 400", request.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(JsonMappingException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        if (!ex.getPath().isEmpty()) {
            JsonMappingException.Reference reference = ex.getPath().get(ex.getPath().size() - 1);
            String modelName = reference.getFrom().getClass().getSimpleName();
            String field = reference.getFieldName();
            String reason = MessageFormat.format("Invalid.{0}.{1}", modelName, field);
            String message = "Unknown field or invalid value for field provided";
            error.addFieldError(field, reason.toUpperCase(), message);
        }
        log.info("JsonMappingException: Returning from REST API {} with error and HTTP status 400", request.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> validationRestError(MethodArgumentNotValidException ex, WebRequest request) {
        RestErrorDTO error = new RestErrorDTO();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> objectErrors = ex.getBindingResult().getGlobalErrors();

        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        error.setReason(ErrorReason.VALIDATION_FAILED.getCode());
        error.setMessage(ErrorReason.VALIDATION_FAILED.getMessage());
        error.setMessage(MessageFormat.format(
                "The request did not pass validation " +
                        "because of {0} field error(s) and {1} object error(s).",
                fieldErrors.size(),
                objectErrors.size()
        ));

        Locale currentLocale = LocaleContextHolder.getLocale();

        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String[] fieldErrorCodes = fieldError.getCodes();
            String reason = fieldErrorCodes != null ? fieldErrorCodes[0] : "null";
            String message = messageSource.getMessage(fieldError, currentLocale);
            error.addFieldError(field, reason.toUpperCase(), message);
        }

        for (ObjectError objectError : objectErrors) {
            String[] objectErrorCodes = objectError.getCodes();
            String reason = objectErrorCodes != null ? objectErrorCodes[0] : "null";
            String message = messageSource.getMessage(objectError, currentLocale);
            error.addGlobalError(objectError.getCode(), reason.toUpperCase(), message);
        }

        log.info("MethodArgumentNotValidException: Returning from REST API {} with error and HTTP status 400", request.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<RestErrorDTO> accessDenied() {
        RestErrorDTO error = new RestErrorDTO();
        error.setStatusCode(HttpStatus.FORBIDDEN.value());
        error.setReason(ErrorReason.ACCESS_DENIED.getCode());
        error.setMessage(ErrorReason.ACCESS_DENIED.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}
