package com.alizo.zTest.base;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.util.List;


/**
 * 用于格式化输出/输入，以及转化
 */
public abstract class ConvertControllerTest extends EnvControllerTest{

    public String formatJSON(String params) {
        try {
            return formatJSON(JSON.parse(params));
        }catch (Exception e){
            return params;
        }
    }

    public String formatJSON(Object params) {
            return JSON.toJSONString(params
                    , JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.WriteMapNullValue);
    }

    public <T> T parse(String res, Class<T> clazz) {
        res = handleObjectResult(res);
        return JSON.parseObject(res,clazz);
    }

    public  <T> T parse(String res,Class<T> T,String key){
        return JSON.parseObject(res).getObject(key,T);

    }


    public  <T> List<T> parseArr(String res, Class<T> clazz) {
        res = handleArrayResult(res);
        return parseArr(res,clazz);
    }

    public  <T> List<T> parseArr(String res, Class<T> clazz,String key) {
        JSONArray data = JSON.parseObject(res).getJSONArray(key);
        return data.toList(clazz);
    }


    public  JSONObject parse(String res) {
        return  JSON.parseObject(res);
    }



    public   String handleObjectResult(String str){
        return str;
    }

    public   String handleArrayResult(String str){
        return str;
    }
}
