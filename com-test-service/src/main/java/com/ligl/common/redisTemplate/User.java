package com.ligl.common.redisTemplate;

import java.io.Serializable;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/20 0020 上午 10:32
 * Version: 1.0
 */
public class User implements Serializable {

    private static final long serialVersionUID = -6011241820070393952L;

    private String id;

    private String name;

    private String password;

    /**
     * <br>------------------------------<br>
     */
    public User() {

    }

    /**
     * <br>------------------------------<br>
     */
    public User(String id, String name, String password) {
        super();
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
