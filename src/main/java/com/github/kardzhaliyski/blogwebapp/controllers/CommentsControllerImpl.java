package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.mappers.CommentMapper;
import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.boot.annotations.GetMapping;
import com.github.kardzhaliyski.boot.annotations.RequestMapping;
import com.github.kardzhaliyski.boot.annotations.RequestParam;
import com.github.kardzhaliyski.boot.annotations.RestController;

@RestController
@RequestMapping("/comments")
public class CommentsControllerImpl {


    private CommentMapper commentMapper;

    public CommentsControllerImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @GetMapping({"/", ""})
    @Role(UserRole.USER)
    public Comment[] getComments() {
        return commentMapper.getAllComments();
    }

    @GetMapping(value = {"/", ""}, params = {"postId"})
    @Role(UserRole.USER)
    public Comment[] getComment(@RequestParam("postId") int postId) {
        return commentMapper.getAllCommentsForPost(postId);
    }


}
