package com.alizo.zTest.base;

import cn.hutool.core.util.StrUtil;
import com.alizo.zTest.constants.SwitchEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试上下文
 */
@Slf4j
public class TestContext {


    /**
     * 用于存储各类临时数据，于各个@test方法之前共享
     */
    private final  static ThreadLocal<Map<String,Object>> map = new ThreadLocal<Map<String, Object>>();

    static {
        map.set(new HashMap<>());
    }

    public static void put(String key,Object value){
        log.info("context set key :{}",key);
        map.get().put(key,value);
    }

    public static void clear(String key){
        map.get().put(key,null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(String key){
        T val = (T) map.get().get(key);
        if(val == null && !"token".equals(key)) {
            if(!SwitchEnum.names.contains(key)) {
                log.error("key :{} not found", key);
                return null;
            }
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder stringBuilder = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                if(!element.getClassName().startsWith("data.center")){
                    continue;
                }
                stringBuilder.append(element.getClassName()).append(".").append(element.getMethodName())
                        .append(":").append(element.getLineNumber()).append("\n");

            }

        }
        return val;
    }

    public static <T> List<T> loadList(String key,Class<T> clazz){
        return (List<T>) map.get().get(key);
    }

    public static void removeAll(){
        map.remove();
    }


    public static boolean isOpen(SwitchEnum i){
        return load(i.name()) != null && (boolean) load(i.name());
    }

    public static void  open(SwitchEnum switchEnum){
        put(switchEnum.name(),true);
    }
    public static void  close(SwitchEnum switchEnum){
        put(switchEnum.name(),false);
    }
}
