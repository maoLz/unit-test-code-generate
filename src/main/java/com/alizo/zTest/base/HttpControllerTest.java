package com.alizo.zTest.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alizo.zTest.constants.SwitchEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.alizo.zTest.constants.SwitchEnum.*;

@Slf4j
public abstract class HttpControllerTest extends ConvertControllerTest{

    public abstract String getHeadUrl();

    public byte[] requestByte(HttpRequest request, String url, Object params) {
        boolean isLog = isOpen(打印日志);
        boolean isPreProcessParams = isOpen(参数预处理);
        if(isLog) {
            log.info("[HttpControllerTest#requestByte]requestUrl:{}",url);
        }
        request.setUrl(url);
        if (params != null) {
            String paramsStr = JSON.toJSONString(params);
            if(isLog) {
                log.info("[HttpControllerTest#requestByte]reuqestParams:{}",formatJSON(paramsStr));
            }
            if (isPreProcessParams) {
                paramsStr = preProcessParam(paramsStr);
            }
            request.body(paramsStr);
        }
        InputStream res = request.execute().bodyStream();
        return IoUtil.readBytes(res);
    }

    public String request(HttpRequest request, String url, Object params) {
        boolean isLog = isOpen(打印日志);
        if(url.startsWith("/")) url = getRequestUrl(url);
        Date date = new Date();
        byte[] resByte = requestByte(request, url, params);
        Date date2 = new Date();
        if(isLog) {
            log.info("[HttpControllerTest#request]url:{},执行时间:{}ms"
                    , url, DateUtil.betweenMs(date2, date));
        }
        String res = new String(resByte, StandardCharsets.UTF_8);
        if (isOpen(结果预处理)) {
            res = processResult(res);
        }
        if (isLog) {
            log.info("[HttpControllerTest#request]response result:{}", formatJSON(res));
        }
        if(isOpen(结果断言)) {
            assertResult(res);
        }
        return res;
    }
    public String multipartPostL(String api, Object params, Map<String, File> fileMap) {
        boolean isLog = isOpen(打印日志);
        boolean isPreProcessParams = isOpen(参数预处理);
        HttpRequest request = HttpUtil.createPost("/");
        request.header("Authorization", getToken());
        request.header("content-type", "multipart/form-data");
        if(isLog) {
            log.info("[HttpControllerTest#multipartPostL]{}", JSON.toJSONString(params));
        }
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
        if(params != null) {
            form.putAll(BeanUtil.beanToMap(params));
        }
        form.putAll(fileMap);
        request.form(form);
        return request(request, getRequestUrl(api), null);
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
        byte[] i = requestByte(HttpUtil.createGet("/").header("Authorization", getToken()), requestUrl, params);
        FileUtil.writeBytes(i, localPath);
    }

    public  String preProcessParam(String params){
        return params;
    }

    public  String processResult(String result){
        return result;
    }

    public  void assertResult(String result){
        Integer code = JSONObject.parseObject(result).getInteger("code");
        assert 0 == code || 200 == code;
    }


    public String getRequestUrl(String api) {
        return address + getHeadUrl() + api;
    }
}
