package com.github.kardzhaliyski.blogwebapp.mappers;

import com.github.kardzhaliyski.blogwebapp.models.Comment;
import com.github.kardzhaliyski.boot.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface CommentMapper {

    @Select("SELECT * FROM comments WHERE post_id = #{id}")
    Comment[] getAllCommentsForPost(int id);

    @Select("SELECT * FROM comments")
    Comment[] getAllComments();

    @Insert("INSERT INTO comments(post_id, name, email, body) VALUES (#{postId}, #{name}, #{email}, #{body})")
    void addComment(Comment comment);
}
