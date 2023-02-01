package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.mappers.CommentMapper;
import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.boot.annotations.Component;

@Component
public class CommentsControllerImpl implements CommentsController {


    private CommentMapper commentMapper;

    public CommentsControllerImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public Comment[] getComments() {
        return commentMapper.getAllComments();
    }

    @Override
    public Comment[] getComment( int postId) {
        return commentMapper.getAllCommentsForPost(postId);
    }


}
