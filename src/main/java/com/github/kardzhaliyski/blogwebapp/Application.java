package com.github.kardzhaliyski.blogwebapp;

import com.github.kardzhaliyski.boot.ContainerAutoConfigurator;
import com.github.kardzhaliyski.boot.SpringApplication;
import com.github.kardzhaliyski.boot.annotations.SpringBootApplication;


@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
}
