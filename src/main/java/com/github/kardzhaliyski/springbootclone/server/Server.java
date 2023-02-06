package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class Server {
    private static final int DEFAULT_PORT = 8080;
    private ApplicationContext applicationContext;
    private Tomcat tomcat;

    public Server(ApplicationContext applicationContext) throws Exception {
        this.applicationContext = applicationContext;
        init();
    }

    public void start() throws LifecycleException {
     tomcat.start();
     tomcat.getServer().await();
    }

    private void init() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(DEFAULT_PORT);
        tomcat.getConnector();

        Context context = tomcat.addContext("", null);
        DispatcherServlet servlet = applicationContext.getInstance(DispatcherServlet.class);
        String servletName = servlet.getClass().getSimpleName();
        tomcat.addServlet("", servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }
}