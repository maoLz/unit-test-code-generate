package com.alizo.zTest.base;

import cn.hutool.core.util.StrUtil;
import com.alizo.zTest.constants.ContextKeyEnum;
import com.alizo.zTest.constants.EnvEnum;
import lombok.extern.slf4j.Slf4j;

import static com.alizo.zTest.constants.ContextKeyEnum.*;
import static com.alizo.zTest.constants.EnvEnum.*;

/**
 * 切换测试环境
 */
@Slf4j
public abstract class EnvControllerTest extends RefreshControllerTest{

    public static String token = "";

    public static String address = "";


    public EnvControllerTest(){
        setEnv(local);
    }

    public void setEnv(EnvEnum envEnum){
        put(ENV.name(),envEnum.name());
        switch (envEnum){
            case local:
                local();
                break;
            case dev:
                dev();
                break;
            case prod:
                prod();
                break;
        }
    }

    public EnvEnum getEnv(){
        return EnvEnum.valueOf(load(ENV.name()));
    }



    public static String getToken(){
        if(StrUtil.isNotBlank(token)){
            return token;
        }
        if(load("token") == null){
            login();
            if(load("token") == null){
                log.warn("[EnvControllerTest#getToken]token not found");

            }
        }
        String token = load("token");
        return "Bearer " + token;
    }

    public void local(){

    }

    public void dev(){

    }
    public void prod(){

    }

    public static void login(){
        //可以通过调用登录接口，来来设置
        put("token",null);
    }



}
