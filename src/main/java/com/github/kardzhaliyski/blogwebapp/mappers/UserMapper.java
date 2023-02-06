package com.github.kardzhaliyski.blogwebapp.mappers;

import com.github.kardzhaliyski.blogwebapp.models.User;
import com.github.kardzhaliyski.blogwebapp.models.dto.ChangeUserRoleDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> getByUsername(String username);

    @Insert("INSERT INTO users(username, first_name, last_name, email, password, role, active) VALUES (#{username}, #{firstName}, #{lastName}, #{email}, #{password}, #{role.name}, #{active})")
    void insert(User user);

    @Update("UPDATE users SET role = #{role.name} WHERE username = #{uname}")
    boolean changeRole(ChangeUserRoleDTO dto);

    @Update("UPDATE users SET active = true WHERE username = #{uname}")
    boolean activate(String uname);

    @Update("UPDATE users SET active = false WHERE username = #{uname}")
    boolean deactivate(String uname);
}
