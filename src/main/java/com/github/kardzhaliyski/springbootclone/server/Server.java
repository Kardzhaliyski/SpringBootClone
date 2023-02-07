package com.github.kardzhaliyski.springbootclone.server;

import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import com.github.kardzhaliyski.springbootclone.filters.ExceptionHandlerFilter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

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
        addDispatcherServlet(context);
        addExceptionHandlerFilter(context);
    }

    private void addDispatcherServlet(Context context) throws Exception {
        DispatcherServlet servlet = applicationContext.getInstance(DispatcherServlet.class);
        String servletName = servlet.getClass().getSimpleName();
        tomcat.addServlet("", servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }

    private void addExceptionHandlerFilter(Context context) throws Exception {
        ExceptionHandlerFilter exceptionHandler = applicationContext.getInstance(ExceptionHandlerFilter.class);
        FilterDef filterDef = new FilterDef();
        filterDef.setFilter(exceptionHandler);
        filterDef.setFilterName("ResponseEntityExceptionHandler");
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("ResponseEntityExceptionHandler");
        filterMap.addURLPattern("/*");

        context.addFilterDef(filterDef);
        context.addFilterMap(filterMap);
    }
}
