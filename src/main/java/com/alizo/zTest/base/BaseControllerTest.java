/*
package com.alizo.zTest.base;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.zlz.Test.TestCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@Slf4j
public abstract class BaseControllerTest extends TestContext {

    protected String address = "http://127.0.0.1:8080";

    public boolean isLog = true;

    public boolean isEncrypt() {
        return false;
    }




    public abstract String getHeadUrl();

    public static String formatJSON(String params) {
        return formatJSON(JSON.parse(params));
    }

    public static String formatJSON(Object params) {
        return JSON.toJSONString(params
                , JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.WriteMapNullValue);
    }

    public static <T> T parse(String res, Class<T> T) {
        return JSON.parseObject(res).getObject("content",T);
    }

    public static <T> T parseData(String res, Class<T> T) {
        JSONObject object = JSON.parseObject(res);
        String data = object.getString("content");
        return JSON.parseObject(data, T);
    }

    public static <T> List<T> parseArr(String res, Class<T> clazz) {
        return parseArr(JSON.parseObject(res).getString("content"),clazz,"content");
    }

    public static <T> List<T> parseArr(String res, Class<T> clazz,String name) {
        JSONArray data = JSON.parseObject(res).getJSONArray(name);
        return data.toList(clazz);
    }


    public JSONObject parse(String res) {
        JSONObject object = JSON.parseObject(res);
        String data = object.getString("data");
        return JSON.parseObject(data);
    }

    public byte[] requestByte(HttpRequest request, String url, Object params) {
        if(isLog) {
            log.info("request url:{}", url);
        }
        request.setUrl(url);
        if (params != null) {
            String paramsStr = JSON.toJSONString(params);
            if(isLog) {
                log.info("request params:{}", formatJSON(paramsStr));
            }
            if (isEncrypt()) {
                paramsStr = ApiEncryptUtils.encryptApi(paramsStr);
                if (isLog) {
                    log.debug("encrypt params:{}", paramsStr);
                }
                JSONObject object = new JSONObject();
                object.put("arg0", paramsStr);
                paramsStr = object.toString();
            }
            request.body(paramsStr);
        }
        InputStream res = request.execute().bodyStream();
        return IoUtil.readBytes(res);
    }

    public String request(HttpRequest request, String url, Object params) {

        if(url.startsWith("/")) url = getRequestUrl(url);
        Date date = new Date();
        byte[] resByte = requestByte(request, url, params);
        Date date2 = new Date();
        log.info("[BaseControllerTest#request]url:{},执行时间:{}ms"
                ,url,DateUtil.betweenMs(date2,date));
        String res = new String(resByte, StandardCharsets.UTF_8);
        if (isLog) {
        //log.info("response resultEn:{}", res);
        }
        if (isEncrypt()) {
            res = ApiEncryptUtils.decryptApi((res));
        }
        if (isLog) {
            log.info("response resultDe:{}", formatJSON(res));
        }
        //assert  0 == JSONObject.parseObject(res).getInteger("code");
        return res;
    }

    public String request(HttpRequest request, String url) {
        return request(request, url, null);
    }

    public String requestWithToken(HttpRequest request, String url, Object params) {
        request.header("Authorization", getToken());
        return request(request, url, params);
    }

    public String post(String api, Object params) {
        HttpRequest request = HttpUtil.createPost("/");
        request.header("content-type", "application/json");
        return request(request, getRequestUrl(api), params);
    }

    public String postL(String api, Object params) {
        HttpRequest request = HttpUtil.createPost("/");
        request.header("Authorization", getToken());
        request.header("content-type", "application/json");
        return requestWithToken(request, getRequestUrl(api), params);
    }

    public String multipartPostL(String api, Object params, Map<String, File> fileMap) {
        HttpRequest request = HttpUtil.createPost("/");
        request.header("Authorization", getToken());
        request.header("content-type", "multipart/form-data");
        log.info("[BaseControllerTest#multipartPostL]{}",JSON.toJSONString(params));

        Map<String, Object> form = new HashMap<>();
        String urlParams = "";
        if (params != null) {
            Map<String, Object> paramMap = BeanUtil.beanToMap(params);
            urlParams = HttpUtil.toParams(paramMap);
        } else {
            String[] arr = api.split("\\?");
            if (arr.length == 2) {
                api = arr[0];
                urlParams = arr[1];
            }
        }
        if (isEncrypt() && StrUtil.isNotBlank(urlParams)) {
            form.put("arg0", URLEncoder.encode(
                    URLEncoder.encode(
                            ApiEncryptUtils.encryptApi(urlParams), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }else{
            if(params != null) {
                form.putAll(BeanUtil.beanToMap(params));
            }
        }
        form.putAll(fileMap);
        request.form(form);
        return request(request, getRequestUrl(api), null);
    }

    public String submitForm(String api, Object params) {
        Map map = null;
        if (params instanceof Map) {
            map = (Map) params;
        } else {
            map = BeanUtil.beanToMap(params);
        }
        HttpRequest request = HttpUtil.createPost("/");
        request.form(map);
        return requestWithToken(request, getRequestUrl(api), null);
    }

    public String delete(String api, Object params) {
        HttpRequest request = HttpRequest.delete("/");
        return request(request, getRequestUrl(api), params);
    }

    public String get(String api, Object params) {
        HttpRequest httpRequest = HttpUtil.createGet("/");
        return get(httpRequest, api, params);
    }


    public String get(HttpRequest request, String api, Object params) {
        String requestUrl = getRequestUrl(api);
        if (params != null) {
            Map<String, Object> paramMap = BeanUtil.beanToMap(params);
            String urlParams = HttpUtil.toParams(paramMap);
            requestUrl = requestUrl + "?" + urlParams;
        }
        if (isEncrypt()) {
            if (isLog) {
                log.info("origin url:{}", requestUrl);
            }
            String[] u = requestUrl.split("\\?");
            if (u.length == 2) {
                requestUrl = u[0] + "?arg0=" + URLEncoder.encode(
                        URLEncoder.encode(
                                ApiEncryptUtils.encryptApi(u[1]), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
        }
        return request(request, requestUrl, null);
    }

    public String get(String api) {
        HttpRequest httpRequest = HttpUtil.createGet("/");
        return get(httpRequest, api, null);
    }

    public String getL(String api, Object params) {
        HttpRequest httpRequest = HttpUtil.createGet("/");
        httpRequest.header("Authorization", getToken());
        return get(httpRequest, api, params);
    }

    public String getL(String api) {
        HttpRequest httpRequest = HttpUtil.createGet("/");
        httpRequest.header("Authorization", getToken());
        return get(httpRequest, api, null);
    }

    public void download(String api, Object params, String localPath) {
        String requestUrl = getRequestUrl(api);
        if (isEncrypt()) {
            if (isLog) {
                log.info("origin url:{}", requestUrl);
            }
            String[] u = requestUrl.split("\\?");
            if (u.length == 2) {
                requestUrl = u[0] + "?arg0=" + URLEncoder.encode(
                        URLEncoder.encode(
                                ApiEncryptUtils.encryptApi(u[1]), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
        }
        byte[] i = requestByte(HttpUtil.createGet("/").header("Authorization", getToken()), requestUrl, params);
        FileUtil.writeBytes(i, localPath);
    }

    public void downloadP(String api, Object params, String localPath) {

        byte[] i = requestByte(HttpUtil.createPost("/").header("Authorization", getToken()), getRequestUrl(api), params);
        FileUtil.writeBytes(i, localPath);
    }


    public String getRequestUrl(String api) {
        return address + getHeadUrl() + api;
    }


}
*/
