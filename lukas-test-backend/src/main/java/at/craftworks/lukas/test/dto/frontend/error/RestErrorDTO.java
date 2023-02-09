package at.craftworks.lukas.test.dto.frontend.error;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class RestErrorDTO implements Serializable {
    private int statusCode;
    private String reason;
    private String message;

    private List<FieldErrorDTO> fieldErrors = new ArrayList<>();
    private List<ObjectErrorDTO> globalErrors = new ArrayList<>();

    public void addFieldError(String field, String reason, String message) {
        fieldErrors.add(new FieldErrorDTO(field, reason, message));
    }

    public void addGlobalError(String objectName, String reason, String message) {
        globalErrors.add(new ObjectErrorDTO(objectName, reason, message));
    }

    public RestErrorDTO(ObjectErrorDTO errorDTO) {
        this.globalErrors.add(errorDTO);
    }

    public RestErrorDTO(FieldErrorDTO fieldErrorDTO) {
        this.fieldErrors.add(fieldErrorDTO);
    }

}
