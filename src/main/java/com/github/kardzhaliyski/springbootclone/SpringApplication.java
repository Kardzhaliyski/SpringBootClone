package com.github.kardzhaliyski.springbootclone;

import com.github.kardzhaliyski.springbootclone.server.Server;

public class SpringApplication {

    public static void run(Class<?> primarySource, String... args) throws Exception {
        ContainerAutoConfigurator configurator = new ContainerAutoConfigurator();
        configurator.scanPackage(primarySource);
        Server server = new Server(configurator.getApplicationContext());
        server.start();
    }
}
