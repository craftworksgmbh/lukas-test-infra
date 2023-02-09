/*
 * Copyright (c) 2014-2020 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2020-01-10
 */
package at.craftworks.__companyLower__.__projectLower__.dto.frontend;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TestDTO {
    @Min(5)
    @Max(100)
    @NotNull
    private Integer number;

    @NotEmpty
    @NotNull
    private String str;
}
