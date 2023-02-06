package com.github.kardzhaliyski.blogwebapp.controllers;

import com.github.kardzhaliyski.blogwebapp.mappers.CommentMapper;
import com.github.kardzhaliyski.blogwebapp.mappers.PostMapper;
import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.blogwebapp.models.Post;
import com.github.kardzhaliyski.blogwebapp.models.UserRole;
import com.github.kardzhaliyski.blogwebapp.security.Role;
import com.github.kardzhaliyski.springbootclone.annotations.*;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.classes.ResponseEntity;

import static com.github.kardzhaliyski.springbootclone.utils.HttpStatus.*;


@RestController
@RequestMapping("/posts")
public class PostsControllerImpl implements PostsController {

    private PostMapper postMapper;
    private CommentMapper commentMapper;

    public PostsControllerImpl(PostMapper postMapper, CommentMapper commentMapper) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping(value = {"/", ""})
    @Role(UserRole.USER)
    public Post[] getPosts() {
        return postMapper.getAllPosts();
    }

    @GetMapping("/{postId}")
    @Role(UserRole.USER)
    public Post getPost(@PathVariable int postId) {
        Post post = postMapper.getPostById(postId);
        if (post == null) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found");
        }

        return post;
    }

    @GetMapping("/{postId}/comments")
    @Role(UserRole.USER)
    public Comment[] getCommentsForPost(@PathVariable int postId) {
        return commentMapper.getAllCommentsForPost(postId);
    }

    @DeleteMapping("/{postId}")
    @Role(UserRole.USER)
    public void deletePost(@PathVariable int postId) {
        postMapper.deleteById(postId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping({"/", ""})
    @Role(UserRole.USER)
    public ResponseEntity addPost(@RequestBody Post post) {
        if (post.title == null || post.body == null || post.userId == 0) {
            return new ResponseEntity<>("Invalid data", BAD_REQUEST);
//            throw new ResponseStatusException(BAD_REQUEST, "Invalid data!");
        }

        postMapper.addPost(post);
        return new ResponseEntity(post, CREATED);
    }

    @PutMapping("/{postId}")
    @Role(UserRole.USER)
    public Post updatePost(@PathVariable int postId, @RequestBody Post post) {
        if (!postMapper.contains(postId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid userId");
        }

        post.id = postId;
        postMapper.updatePost(post);
        return post;
    }
}
