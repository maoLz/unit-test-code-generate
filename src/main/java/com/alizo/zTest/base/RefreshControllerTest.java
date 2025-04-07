package com.alizo.zTest.base;

import com.alizo.zTest.ConfigConstant;
import com.alizo.zTest.DefaultBaseControllerTest;
import com.alizo.zTest.codeGen.TestCodeGenerator;
import com.sun.mail.imap.AppendUID;
import org.apache.poi.ss.formula.functions.T;

public abstract class RefreshControllerTest extends TestContext{

    public abstract Class<?>  getControllerClass();

    public void refreshCode(){
        Class<?> controllerClazz = getControllerClass();
        TestCodeGenerator testCodeGenerator = new TestCodeGenerator(controllerClazz);
        String property = System.getProperty("user.dir");
        testCodeGenerator.location = property + ConfigConstant.location;
        testCodeGenerator.overwrite =true;
        testCodeGenerator.fatherClass = DefaultBaseControllerTest.class;;
        testCodeGenerator.codeGenerate();
    }
}
