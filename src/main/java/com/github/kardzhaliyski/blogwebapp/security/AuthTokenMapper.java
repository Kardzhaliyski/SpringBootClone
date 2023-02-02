package com.github.kardzhaliyski.blogwebapp.security;

import com.github.kardzhaliyski.boot.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthTokenMapper {
    @Select("SELECT * FROM tokens WHERE token = #{token}")
    public AuthToken getBy(String token);

    @Insert("INSERT INTO tokens(token, username, created_date, expiration_date, role) VALUES (#{token}, #{uname}, #{createdDate}, #{expirationDate}, #{role.name})")
    public void addToken(AuthToken token);
}
