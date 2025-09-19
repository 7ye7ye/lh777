package org.jeecg.modules.hospital;

import org.jeecg.config.shiro.IgnoreAuth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @IgnoreAuth
    @GetMapping("/test")
    public int test() {
        return 777;
    }
}
