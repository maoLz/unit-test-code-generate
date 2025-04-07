package com.alizo.zTest.codeGen;


import com.alizo.zTest.ConfigConstant;
import com.alizo.zTest.DefaultBaseControllerTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestCode {




    public static  Class<?> server = DefaultBaseControllerTest.class;




    @Test
    public void 生成模块下所有代码(){
        List<Class<?>> list= getClasses(ConfigConstant.packageName);
        list.forEach(e->{
            TestCodeGenerator testCodeGenerator = new TestCodeGenerator(e);
            String property = System.getProperty("user.dir");
            testCodeGenerator.location = property + ConfigConstant.location;
            testCodeGenerator.overwrite =true;
            testCodeGenerator.fatherClass = server;
            testCodeGenerator.codeGenerate();
        });
    }


    @Test
    public void 生成单个文件代码(){
        Class<?> controllerClazz = null;
        TestCodeGenerator testCodeGenerator = new TestCodeGenerator(controllerClazz);
        String property = System.getProperty("user.dir");
        testCodeGenerator.location = property + ConfigConstant.location;
        testCodeGenerator.overwrite =true;
        testCodeGenerator.fatherClass = server;
        testCodeGenerator.codeGenerate();
    }


    public void refresh(Class<?> clazz){
        TestCodeGenerator testCodeGenerator = new TestCodeGenerator(clazz);
        String property = System.getProperty("user.dir");
        testCodeGenerator.location = property + ConfigConstant.location;
        testCodeGenerator.overwrite =true;
        testCodeGenerator.fatherClass = server;
        testCodeGenerator.codeGenerate();
    }



    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace(".", "/");
        File dir = new File(ConfigConstant.modulePath+"/target/classes/"+path);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    List<Class<?>> subClasses = getClasses(packageName + "." + fileName);
                    classes.addAll(subClasses);
                } else if (fileName.endsWith(".class")) {
                    String className = fileName.substring(0, fileName.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + "." + className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classes;
    }

}
