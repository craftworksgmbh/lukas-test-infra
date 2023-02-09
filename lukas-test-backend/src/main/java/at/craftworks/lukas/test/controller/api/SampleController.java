/*
 * Copyright (c) 2014-2020 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2020-01-10
 */
package at.craftworks.lukas.test.controller.api;

import at.craftworks.lukas.test.dto.frontend.TestDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SampleController {

    @Operation(operationId = "helloWorld", summary = "hello to the world")
    @GetMapping("/hello-world")
    public TestDTO helloWorld(Pageable pageable) {
        return new TestDTO();
    }
}
