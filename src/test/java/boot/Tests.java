package boot;

import com.github.kardzhaliyski.blogwebapp.Application;
import com.github.kardzhaliyski.blogwebapp.controllers.CommentsControllerImpl;
import com.github.kardzhaliyski.boot.SpringApplication;
import com.github.kardzhaliyski.boot.annotations.Component;
import com.github.kardzhaliyski.container.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.annotation.Annotation;

public class Tests {
    Container r;

    @BeforeEach
    public void init() throws Exception {
        r = new Container();
    }

    @Test
    public void test() throws Exception {
        SpringApplication.run(Application.class);

    }

    @Test
    public void test2() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    }
}
