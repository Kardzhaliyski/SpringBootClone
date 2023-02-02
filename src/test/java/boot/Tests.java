package boot;

import com.github.kardzhaliyski.blogwebapp.Application;
import com.github.kardzhaliyski.boot.ContainerAutoConfigurator;
import com.github.kardzhaliyski.boot.SpringApplication;
import com.github.kardzhaliyski.container.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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

    }

    @Test
    public void test3() throws IOException {


    }
}
