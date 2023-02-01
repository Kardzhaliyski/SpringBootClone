package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.blogwebapp.models.Post;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.boot.annotations.*;
import com.github.kardzhaliyski.boot.classes.ResponseEntity;
import com.github.kardzhaliyski.boot.utils.HttpStatus;

@RestController
@RequestMapping("/posts")
public interface PostsController {
    @GetMapping(value = {"/", ""})
    @Role(UserRole.USER)
    public Post[] getPosts();

    @GetMapping("/{postId}")
    @Role(UserRole.USER)
    public Post getPost(@PathVariable int postId);

    @GetMapping("/{postId}/comments")
    @Role(UserRole.USER)
    public Comment[] getCommentsForPost(@PathVariable int postId);

    @DeleteMapping("/{postId}")
    @Role(UserRole.USER)
    public void deletePost(int postId);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping({"/", ""})
    @Role(UserRole.USER)
    public ResponseEntity addPost(@RequestBody Post post);

    @PutMapping("/{postId}")
    @Role(UserRole.USER)
    public Post updatePost(@PathVariable int postId, @RequestBody Post post);
}
