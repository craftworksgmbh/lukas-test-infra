package at.craftworks.lukas.test.dto.frontend.error;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class ObjectErrorDTO implements Serializable {
    private final String object;
    private final String reason;
    private final String message;

    public ObjectErrorDTO() {
        this.object = null;
        this.reason = null;
        this.message = null;
    }

    public ObjectErrorDTO(String object, String reason, String message) {
        this.object = object;
        this.reason = reason;
        this.message = message;
    }
}
