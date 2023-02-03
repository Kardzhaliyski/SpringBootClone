package boot;

import com.github.kardzhaliyski.blogwebapp.Application;
import com.github.kardzhaliyski.boot.ContainerAutoConfigurator;
import com.github.kardzhaliyski.boot.SpringApplication;
import com.github.kardzhaliyski.container.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

public class Tests {
    Container r;

    @BeforeEach
    public void init() throws Exception {
        r = new Container();
    }

    @Test
    public void test() throws Exception {
        Pattern compile = Pattern.compile("\\{\\w+}");


    }

    @Test
    public void test2() {
        String newPath = "get:/posts/{arg1}/comments";
        newPath = newPath.replaceAll("\\{\\w+}", "[\\\\w-\\\\.]+");
        System.out.println(newPath);
        Pattern compile = Pattern.compile(newPath);
        System.out.println(compile.matcher("get:/posts/13/comments").find());
    }

    @Test
    public void test3() throws IOException {
        String path = "get:/posts/{arg1}/comm.ents/*";
        path = path.replaceAll("\\.", "\\\\.");
        path = path.replaceAll("\\*", "\\.+");
        path = path.replaceAll("\\{\\w+}", "[\\\\w-\\\\.]+");
        System.out.println(Pattern.compile(path).matcher("get:/posts/13/comm.ents/asd/something").matches());

    }
}
