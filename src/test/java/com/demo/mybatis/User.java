package com.demo.mybatis;

import java.util.Date;

/**
 * @author: yinchao
 * @ClassName: User
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 23:16
 */
public class User {

    private Long id;
    private Integer userId;          // 用户ID
    private String name;        // 姓名
    private String email;        // 年龄
    private Date create_time;        // 创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
