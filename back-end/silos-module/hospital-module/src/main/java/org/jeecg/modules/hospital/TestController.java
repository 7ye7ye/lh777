package org.jeecg.modules.hospital;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @PostMapping("/test")
    public int test() {
        return 777;
    }
}
