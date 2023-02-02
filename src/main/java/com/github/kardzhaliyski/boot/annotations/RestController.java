package com.github.kardzhaliyski.boot.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Controller
@ResponseBody
@Component
public @interface RestController {
}
