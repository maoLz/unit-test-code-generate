package com.alizo.zTest;

import com.alibaba.fastjson2.JSONObject;
import com.alizo.zTest.base.EnvControllerTest;
import com.alizo.zTest.base.HttpControllerTest;
import com.alizo.zTest.constants.SwitchEnum;

import static com.alizo.zTest.constants.EnvEnum.*;

/**
 * 以下为使用者可以自行修改的部分
 */
public abstract class DefaultBaseControllerTest extends HttpControllerTest {


    public DefaultBaseControllerTest(){
        super();
        open(SwitchEnum.打印日志);
        setEnv(local);
    }

    /**
     * 设置运行环境
     */
    @Override
    public void local(){
        address = "http://127.0.0.1:8083";
        token = "";
    }

    @Override
    public void dev(){
        address = "";
        token = "";
    }

    @Override
    public void prod(){
        address = "";
        token = "";
    }


    /**
     * 预处理请求参数
     * @param params
     * @return
     */
    @Override
    public String preProcessParam(String params) {
        return super.preProcessParam(params);
    }

    /**
     * 处理返回结果
     * @param result
     * @return
     */
    @Override
    public String processResult(String result) {
        return super.processResult(result);
    }

    /**
     * 对每个接口返回进行判断
     * @param result
     */
    @Override
    public void assertResult(String result) {
        super.assertResult(result);
    }

    /**
     * before convert jsonStr to object
     * @param str
     * @return
     */
    @Override
    public String handleObjectResult(String str) {
        return super.handleObjectResult(str);
    }

    /**
     * before convert jsonStr to array
     * @param str
     * @return
     */
    public String handleArrayResult(String str) {
        return super.handleArrayResult(str);
    }
}
