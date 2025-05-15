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
    private String user_id;          // 用户ID
    private String name;        // 姓名
    private Date create_time;        // 创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
