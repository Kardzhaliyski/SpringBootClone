package tests.classes;

import com.github.kardzhaliyski.container.annotations.Autowire;
import com.github.kardzhaliyski.container.annotations.Lazy;

public class L {
    @Autowire
    @Lazy
    public LA laField;
}
