package com.github.kardzhaliyski.springbootclone.utils;

import com.github.kardzhaliyski.springbootclone.context.ApplicationContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MybatisConfig {
    private static final String DATASOURCE_DRIVER_CLASSNAME_PROPERTY_KEY = "spring.datasource.driverClassName";
    private static final String DATASOURCE_URL_PROPERTY_KEY = "spring.datasource.url";
    private static final String DATASOURCE_USERNAME_PROPERTY_KEY = "spring.datasource.username";
    private static final String DATASOURCE_PASSWORD_PROPERTY_KEY = "spring.datasource.password";

    public static void init(ApplicationContext applicationContext, Set<Class<?>> classes) {
        String url = (String) applicationContext.getInstance(DATASOURCE_URL_PROPERTY_KEY);
        String driver = (String) applicationContext.getInstance(DATASOURCE_DRIVER_CLASSNAME_PROPERTY_KEY); //todo try to resolve from url if not present
        String username = (String) applicationContext.getInstance(DATASOURCE_USERNAME_PROPERTY_KEY);
        String password = (String) applicationContext.getInstance(DATASOURCE_PASSWORD_PROPERTY_KEY);

        //todo missing validation checks

        UnpooledDataSource dataSource = new UnpooledDataSource(driver, url, username, password); //todo change to pooled after making it use more than 1 session
        Environment env = new Environment("env", new JdbcTransactionFactory(), dataSource);
        Configuration mybatisConfig = new Configuration();
        mybatisConfig.setEnvironment(env);

        List<Class<?>> mapperClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (!clazz.isInterface() || !clazz.isAnnotationPresent(Mapper.class)) {
                continue;
            }

            mybatisConfig.addMapper(clazz);
            mapperClasses.add(clazz);
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig);
        SqlSession session = sqlSessionFactory.openSession(true); //todo close and refresh ???
        for (Class<?> clazz : mapperClasses) {
            Object mapper = session.getMapper(clazz);
            applicationContext.registerInstance(clazz, mapper);
        }
    }
}
