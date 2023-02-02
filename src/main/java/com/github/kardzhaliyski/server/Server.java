package com.github.kardzhaliyski.server;

import com.github.kardzhaliyski.container.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

public class Server {
    private static final int DEFAULT_PORT = 8080;
    private Container container;
    private Tomcat tomcat;

    public Server(Container container) throws Exception {
        this.container = container;
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
//        DispatcherServlet servlet = new DispatcherServlet();
        DispatcherServlet servlet = container.getInstance(DispatcherServlet.class);
        String servletName = servlet.getClass().getSimpleName();
        tomcat.addServlet("", servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(null);
        server.start();
    }
}
