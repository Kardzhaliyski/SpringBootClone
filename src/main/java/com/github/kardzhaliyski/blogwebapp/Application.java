package com.github.kardzhaliyski.blogwebapp;

import com.github.kardzhaliyski.boot.SpringApplication;
import com.github.kardzhaliyski.boot.annotations.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootApplication
public class Application {
	public static void main(String[] args) throws URISyntaxException, IOException {
		SpringApplication.run(Application.class, args);
	}
}
