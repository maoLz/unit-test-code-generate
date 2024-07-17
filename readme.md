# 接口测试代码生成工具

## 介绍

该工具用于生成接口单元测试代码，方便后端人员自测使用

## 使用步骤

1. 在unit-test-code-generate/pom文件中引入需要生成测试代码的模块
2. 在unit-test-code-generate/src/main/java/com/alizo/zTest/codeGen/TestCode.java 中配置module文件路径以及包路径
3. 执行TestCode第一个方法，进行初始化（初始过程，会读取target/class文件，所以需先将module文件进行🧬）
4. 在zTest/DefaultBaseControllerTest.java 配置相关的环境参数

