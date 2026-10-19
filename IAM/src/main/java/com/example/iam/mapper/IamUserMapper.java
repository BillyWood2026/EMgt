package com.example.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iam.entity.user.IamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IamUserMapper extends BaseMapper<IamUser> {
    
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND deleted = 0")
    IamUser selectByUsername(@Param("username") String username);

    @Update("UPDATE iam_user SET password_hash = #{passwordHash} WHERE username = #{username} AND deleted = 0")
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordHash") String passwordHash);
}