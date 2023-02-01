package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.boot.annotations.GetMapping;
import com.github.kardzhaliyski.boot.annotations.RequestMapping;
import com.github.kardzhaliyski.boot.annotations.RequestParam;
import com.github.kardzhaliyski.boot.annotations.RestController;

@RestController
@RequestMapping("/comments")
public interface CommentsController {

    @GetMapping({"/", ""})
    @Role(UserRole.USER)
    public Comment[] getComments();

    @GetMapping(value = {"/", ""}, params = {"postId"})
    @Role(UserRole.USER)
    public Comment[] getComment(@RequestParam("postId") int postId);
}
