package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.User;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (username, password, email, user_type, reference_id, active, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{userType}, #{referenceId}, #{active}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM users WHERE username = #{username}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "userType", column = "user_type"),
            @Result(property = "referenceId", column = "reference_id"),
            @Result(property = "active", column = "active"),
            @Result(property = "resetToken", column = "reset_token"),
            @Result(property = "resetTokenExpiry", column = "reset_token_expiry"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "userType", column = "user_type"),
            @Result(property = "referenceId", column = "reference_id"),
            @Result(property = "active", column = "active"),
            @Result(property = "resetToken", column = "reset_token"),
            @Result(property = "resetTokenExpiry", column = "reset_token_expiry"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByEmail(String email);

    @Update("UPDATE users SET reset_token = #{resetToken}, reset_token_expiry = #{resetTokenExpiry}, updated_at = NOW() " +
            "WHERE email = #{email}")
    int updateResetToken(String email, String resetToken, java.time.LocalDateTime resetTokenExpiry);

    @Select("SELECT * FROM users WHERE reset_token = #{resetToken}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "userType", column = "user_type"),
            @Result(property = "referenceId", column = "reference_id"),
            @Result(property = "active", column = "active"),
            @Result(property = "resetToken", column = "reset_token"),
            @Result(property = "resetTokenExpiry", column = "reset_token_expiry"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByResetToken(String resetToken);

    @Update("UPDATE users SET password = #{password}, reset_token = NULL, reset_token_expiry = NULL, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updatePassword(Long id, String password);
}
