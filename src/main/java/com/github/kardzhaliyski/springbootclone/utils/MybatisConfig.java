package com.github.kardzhaliyski.springbootclone.utils;

import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

public class MybatisConfig {
    private static final String DATASOURCE_DRIVER_CLASSNAME_PROPERTY_KEY = "spring.datasource.driverClassName";
    private static final String DATASOURCE_URL_PROPERTY_KEY = "spring.datasource.url";
    private static final String DATASOURCE_USERNAME_PROPERTY_KEY = "spring.datasource.username";
    private static final String DATASOURCE_PASSWORD_PROPERTY_KEY = "spring.datasource.password";

    private Configuration mybatisConfig;
    private ApplicationContext applicationContext;
    private SqlSessionFactory sqlSessionFactory;

    public MybatisConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        String url = (String) applicationContext.getInstance(DATASOURCE_URL_PROPERTY_KEY);
        String driver = (String) applicationContext.getInstance(DATASOURCE_DRIVER_CLASSNAME_PROPERTY_KEY); //todo try to resolve from url if not present
        String username = (String) applicationContext.getInstance(DATASOURCE_USERNAME_PROPERTY_KEY);
        String password = (String) applicationContext.getInstance(DATASOURCE_PASSWORD_PROPERTY_KEY);

        if (url == null || username == null || password == null || driver == null) {
            throw new IllegalStateException("Missing datasource properties"); //todo
        }

        PooledDataSource dataSource = new PooledDataSource(driver, url, username, password);
        Environment env = new Environment("env", new JdbcTransactionFactory(), dataSource);
        mybatisConfig = new Configuration();
        mybatisConfig.setEnvironment(env);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig);
    }

    public void initMapper(Class<?> clazz){
        initMappers(clazz);
    }
    public void initMappers(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (!clazz.isInterface() || !clazz.isAnnotationPresent(Mapper.class)) {
                continue;
            }

            mybatisConfig.addMapper(clazz);
            Object mapper = Proxy.newProxyInstance(
                    MybatisConfig.class.getClassLoader(),
                    new Class[]{clazz},
                    new MapperInvocationHandler(sqlSessionFactory, clazz));
            applicationContext.registerInstance(clazz, mapper);
        }

    }

    private static class MapperInvocationHandler implements InvocationHandler {
        private final SqlSessionFactory sqlSessionFactory;
        private final Class<?> mapperClass;

        public MapperInvocationHandler(SqlSessionFactory sqlSessionFactory, Class<?> mapperClass) {
            this.sqlSessionFactory = sqlSessionFactory;
            this.mapperClass = mapperClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try (SqlSession session = sqlSessionFactory.openSession(true)) {
                Object mapper = session.getMapper(mapperClass);
                return method.invoke(mapper, args);
            }
        }
    }
}
