package tests.classes;

import com.github.kardzhaliyski.springbootclone.context.annotations.Autowire;
import com.github.kardzhaliyski.springbootclone.context.annotations.Qualifier;

public class EN {
    public A aField;

    @Autowire
    public EN(@Qualifier("aNamedField") A aNamedField) {
        this.aField = aNamedField;
    }
}
