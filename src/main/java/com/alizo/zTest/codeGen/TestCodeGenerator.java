package com.alizo.zTest.codeGen;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alizo.zTest.DefaultBaseControllerTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class TestCodeGenerator {

    private Class<?> controllClass = null;

    public StringBuilder code = new StringBuilder();

    public StringBuilder importCode = new StringBuilder();

    public List<Class> existClass = new ArrayList<>();

    public Map<String,Integer> methodName = new HashMap<>();
    public String location = "/Users/zhanglizhong/code/company/XinYunTiao-Mediate/xyt-test/src/test/java/";

    public boolean overwrite = false;

    public String packageName = "";

    public Class<?> fatherClass = DefaultBaseControllerTest.class;

    private List<String> existMethod = new ArrayList<>();

    public static List<String> defaultMethods = new ArrayList<>();
    static {
        defaultMethods.add("getControllerClass");
        defaultMethods.add("refresh");
        defaultMethods.add("getHeadUrl");

    }


    public TestCodeGenerator(Class<?> controllClass){
        this.controllClass = controllClass;
    }


    public void doOverwrite(File existFile,Class<?> controllClass){
        loadExistMethod();
        String temp =  new String(FileUtil.readBytes(existFile),StandardCharsets.UTF_8);
        temp = temp.replaceAll("package (.*)","package $1\n\n\\$IMPORT_CODE\\$");
        temp = temp.substring(0,temp.lastIndexOf("}"));
        code = new StringBuilder(temp);
        Method[] methods = controllClass.getDeclaredMethods();
        for(Method method:methods){
            if(existMethod.contains(method.getName())) continue;
            initMethod(method);
            log.warn("新增接口方法{}",method.getName());
        }
        code.append("}");
    }

    public void loadExistMethod(){
        try {
            Class<?> clazz = Class.forName(packageName+"Test");
            Arrays.stream(clazz.getDeclaredMethods()).filter(e-> !defaultMethods.contains(e.getName())).forEach(method -> {
                existMethod.add(method.getName());
                methodName.put(method.getName(),0);
            });
            log.warn("已存在的接口方法{}",JSON.toJSONString(existMethod));
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    public void codeGenerate(){
        packageName = controllClass.getName();
        log.info("[TestCodeGenerator#codeGenerate]packageName:{}",packageName);
        Annotation a = controllClass.getAnnotation(Controller.class);
        Annotation rs = controllClass.getAnnotation(RestController.class);
        if(a == null && rs == null) return;
        String path = location+
                controllClass.getPackageName().replaceAll("\\.","/")
                +"/"+controllClass.getSimpleName()+"Test.java";
        log.info("[TestCodeGenerator#codeGenerate]path:{}",path);

        File file = new File(path);
        String codeStr = "";
        if(file.exists()){
            log.info("[TestCodeGenerator#codeGenerate]文件存在,进行方法追加");
            doOverwrite(file,controllClass);
            codeStr = code.toString().replaceAll("\\$IMPORT_CODE\\$",importCode.toString());
        }else {
            initPackage();
            code.append("$IMPORT_CODE$\n");
            addImport(fatherClass);
            addImport(cn.hutool.http.Method.class);
            addImport(HttpRequest.class);
            addImport(HttpUtil.class);
            addImport(Long.class);
            addImport(Test.class);
            addImport(controllClass);
            initClass();
            codeStr = code.toString().replaceAll("\\$IMPORT_CODE\\$", importCode.toString());
            if (!file.getParentFile().exists()) {
                FileUtil.mkdir(file.getParentFile());
            }
        }
        FileUtil.writeBytes(codeStr.getBytes(StandardCharsets.UTF_8),file);
    }

    public void initPackage(){
        code.append("package ").append(controllClass.getPackageName()).append(";");
        code.append("\n");
    }

    public void addImport(Class<?> controllClass){
        if(controllClass.isArray() || controllClass.isPrimitive()) return;
        if(existClass.contains(controllClass)){
            return;
        }
        existClass.add(controllClass);
        importCode.append("import "+ controllClass.getName());
        importCode.append(";\n");
    }

    public void initClass(){
        code.append("/**\n");
        code.append("* {@link "+packageName+"}\n");
        code.append("*/\n");
        code.append("public class ").append(controllClass.getSimpleName()).append("Test extends "+fatherClass.getSimpleName()+" {\n");
        RequestMapping annotation =controllClass.getAnnotation(RequestMapping.class);
        String value = annotation == null ? "" : annotation.value()[0];
        if(StrUtil.isBlank(value)) value = "/";
        if (!"/".equals(value) && '/' == value.charAt(value.length()-1)) value = value.substring(0,value.length()-1);
        code.append("    @Override\n" +
                "    public String getHeadUrl() {\n" +
                "        return \""+value+"\";\n" +
                "    }\n");
        code.append("\n");
        code.append("    @Override\n" +
                    "    public Class<?>  getControllerClass() {\n" +
                    "        return "+controllClass.getSimpleName()+".class;\n" +
                    "    }\n");
        code.append("\n");
        code.append("    @Override\n" +
                    "    @Test\n"+
                    "    public void refreshCode() {\n" +
                    "        super.refreshCode();\n" +
                    "    }\n");
        Method[] methods = controllClass.getDeclaredMethods();
        for(Method method:methods){
            initMethod(method);
        }
        code.append("}");

    }

    public void initMethod(Method method) {
        RequestMapping requestMapping = null;
        Annotation[] annotations = method.getAnnotations();
        Annotation temp = null;
        for (Annotation annotation : annotations) {
            temp = annotation;
            if (annotation instanceof RequestMapping) {
                requestMapping = (RequestMapping) annotation;
            } else {
                requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
            }
            if(requestMapping != null) break;
        }
        if (requestMapping == null) return;

        if(requestMapping.method().length == 0) {
            initAnnotation(method,temp);
            initGetRequest(method,requestMapping);
            return;
        }
        RequestMethod requestMethod = requestMapping.method()[0];
        if (RequestMethod.GET.equals(requestMethod)) {
            initAnnotation(method,temp);
            initGetRequest(method, (GetMapping) temp);
        }else if(RequestMethod.POST.equals(requestMethod)){
            initAnnotation(method,temp);
            initPostRequest(method,(PostMapping) temp);
        }else{
            initAnnotation(method,temp);
            initOther(method,temp,requestMapping.method()[0].name());
        }
        code.append("\n");
    }

    /**
     *
     * @param method
     */
    public void initAnnotation(Method method,Annotation annotation){

        code.append("\t/**").append("\n");
        code.append("\t* {@link "+packageName+"#"+method.getName()+"(");
        for(Parameter parameter:method.getParameters()){
            code.append(parameter.getType().getSimpleName()).append(",");
        }
        if(method.getParameters().length > 0){
            code = new StringBuilder(code.substring(0,code.length()-1));
        }
        code.append(")}\n");
        String[] vals = (String[]) DynaBean.create(annotation).invoke("value");
        if(vals.length > 0) {
            code.append("\t* ").append(vals[0]).append("\n");
        }
        code.append("\t*/").append("\n");
    }

    public String getMethodName(Method method){
        Integer m = 0;
        String mn = method.getName();
        if(methodName.containsKey(method.getName())){
            m = methodName.get(method.getName());
            mn = mn +(m+1);
        }
        methodName.put(method.getName(),m+1);
        return mn;

    }

    public void initGetRequest(Method method, GetMapping annotation){
        code.append("\t@Test\n");
        code.append("\t").append("public void ").append(getMethodName(method)).append("(){\n");
        String param = "";
        String[] vals = annotation.value();
        if(vals.length > 0) {
            param =vals[0];
        }
        Parameter[] parameter = method.getParameters();
        StringBuilder t2 = new StringBuilder();
        for (Parameter temp:parameter){
            if(param.indexOf("{"+temp.getName()+"}") > 0){
                param = param.replace("{"+temp.getName()+"}","\"+"+temp.getName()+"+\"");
            }else{
                if(temp.getType() == String.class) {
                    t2.append(temp.getName()).append("=").append("\"+").append(temp.getName()).append("+\"&");
                }
            }
            initParam(temp);
        }

        if(!t2.isEmpty()){
            param = param + "?" +t2.substring(0,t2.length()-1);
        }
        code.append("\t\tgetL(\"").append(param).append("\");\n");
        code.append("\t}\n");
    }

    public void initGetRequest(Method method, RequestMapping annotation){
        code.append("\t@Test\n");
        code.append("\t").append("public void ").append(getMethodName(method)).append("(){\n");
        String param = "";
        String[] vals = annotation.value();
        if(vals.length > 0) {
            param =vals[0].startsWith("/")?vals[0]:"/"+vals[0];
        }
        Parameter[] parameter = method.getParameters();
        StringBuilder t2 = new StringBuilder();
        for (Parameter temp:parameter){
            if(param.indexOf("{"+temp.getName()+"}") > 0){
                param = param.replace("{"+temp.getName()+"}","\"+"+temp.getName()+"+\"");
            }else{
                if(temp.getType() == String.class) {
                    t2.append(temp.getName()).append("=").append("\"+").append(temp.getName()).append("+\"&");
                }
            }
            initParam(temp);
        }

        if(!t2.isEmpty()){
            param = param + "?" +t2.substring(0,t2.length()-1);
        }
        code.append("\t\tgetL(\"").append(param).append("\");\n");
        code.append("\t}\n");
    }

    public void initPostRequest(Method method, PostMapping annotation){
        code.append("\t@Test\n");
        code.append("\t").append("public void ").append(getMethodName(method)).append("(){\n");
        Parameter[] parameter = method.getParameters();
        String methodParam = "";
        for (Parameter param:parameter){
            initParam(param);
            methodParam = StrUtil.isEmpty(methodParam) ?  param.getName():methodParam;
        }

        String param = "";
        String[] vals = annotation.value();
        if(vals.length > 0) {
            param =vals[0];
        }
        code.append("\t\tpostL(\"").append(param).append("\",").append(methodParam).append(");\n");
        code.append("\t}\n");
    }

    public void initOther(Method method, Annotation annotation,String methodStr){
        code.append("\t@Test\n");
        code.append("\t").append("public void ").append(getMethodName(method)).append("(){\n");
        Parameter[] parameter = method.getParameters();
        String methodParam = "";
        String param = "";
        String[] vals = (String[]) DynaBean.create(annotation).invoke("value");
        if(vals.length > 0) {
            param =vals[0];
        }
        for (Parameter temp:parameter){
            if(param.indexOf("{"+temp.getName()+"}") > 0){
                param = param.replace("{"+temp.getName()+"}","\"+"+temp.getName()+"+\"");
            }
            initParam(temp);
            methodParam = StrUtil.isEmpty(methodParam) ?  temp.getName():methodParam;

        }
        methodParam = StrUtil.isEmpty(methodParam)?"null":methodParam;

        code.append("\t\tHttpRequest req = HttpUtil.createRequest(Method.")
                .append(methodStr.toUpperCase()).append(",\"/\");\n");
        code.append("\t\t//request.header(\"Authorization\", getToken());\n");
        code.append("\t\t").append("request(req,\"").append(param).append("\",").append(methodParam).append(");\n");
        code.append("\t}\n");
    }



    public static boolean hasPublicNoArgConstructor(Class<?> clazz)  {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (isPublic(constructor) && constructor.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
    }

    public void initParam(Parameter param){
        addImport(param.getType());
        String temp = " = \"\"";
        if(param.getType() == String.class){
        }else if(param.getType().isArray()){
            temp = " = new "+ param.getType().getSimpleName() +"{}";
        }else if(param.getType().isInterface()){
            temp = " = null";
        }else if(hasPublicNoArgConstructor(param.getType())){
            temp = " = new " + param.getType().getSimpleName() +"()";
        }else if(param.getType().isPrimitive()){
            temp = "";
        } else{
            temp = " = null";
        }
        code.append("\t\t").append(param.getType().getSimpleName())
                .append(" ").append(param.getName()).append(temp).append(";\n");
    }

    public static boolean isPublic(Constructor<?> constructor) {
        return Modifier.PUBLIC == constructor.getModifiers();
    }
}
