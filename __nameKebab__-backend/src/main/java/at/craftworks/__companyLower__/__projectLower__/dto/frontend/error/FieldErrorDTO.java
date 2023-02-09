package at.craftworks.__companyLower__.__projectLower__.dto.frontend.error;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class FieldErrorDTO implements Serializable {
    private final String field;
    private final String reason;
    private final String message;

    public FieldErrorDTO() {
        this.field = null;
        this.reason = null;
        this.message = null;
    }

    public FieldErrorDTO(String field, String reason, String message) {
        this.field = field;
        this.reason = reason;
        this.message = message;
    }
}
