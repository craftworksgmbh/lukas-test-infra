/*
 * Copyright (c) 2014-2018 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2018-10-25
 */
package at.craftworks.lukas.test.dto.frontend.error;


public enum ErrorReason {
    // TODO add your own error reasons
    ACCESS_DENIED("The access to this resource was denied"),
    NOT_FOUND("The resource was not found"),
    UNHANDLED_ERROR("An error occured"),
    VALIDATION_FAILED("The submitted data is invalid"),
    HTTP_METHOD_NOT_SUPPORTED("This HTTP Method is not supported"),
    CONFLICT("The request was not successful, because a resource was updated from another request. Try again");

    private String message;

    ErrorReason(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return this.name();
    }
}
