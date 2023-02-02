package com.github.kardzhaliyski.boot;

import com.github.kardzhaliyski.server.Server;

public class SpringApplication {

    public static void run(Class<?> primarySource, String... args) throws Exception {
        ContainerAutoConfigurator configurator = new ContainerAutoConfigurator();
        configurator.run(primarySource);
        Server server = new Server(configurator.getContainer());
        server.start();
    }
}
