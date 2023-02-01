package com.github.kardzhaliyski.blogwebapp.models;


import com.github.kardzhaliyski.container.annotations.Qualifier;

public class Post {
    public int id;
    public Integer userId;
    public String title;
    @Qualifier
    public String body;
}
