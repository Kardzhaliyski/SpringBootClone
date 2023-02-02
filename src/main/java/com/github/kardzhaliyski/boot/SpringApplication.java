package com.github.kardzhaliyski.boot;

public class SpringApplication {

    public static void run(Class<?> primarySource, String... args) throws Exception {
        ContainerAutoConfigurator configurator = new ContainerAutoConfigurator();
        configurator.run(primarySource);


    }
}
