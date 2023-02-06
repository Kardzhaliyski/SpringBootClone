package tests.classes;

import com.github.kardzhaliyski.springbootclone.context.annotations.Autowire;
import com.github.kardzhaliyski.springbootclone.context.annotations.Lazy;

public class L {
    @Autowire
    @Lazy
    public LA laField;
}
