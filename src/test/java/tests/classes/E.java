package tests.classes;

import com.github.kardzhaliyski.springbootclone.context.annotations.Autowire;

public class E {
    public A aField;

    @Autowire
    public E(A aField) {
        this.aField = aField;
    }
}
