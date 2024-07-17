# Interface test code generation tool

## introduce

This tool is used to generate interface unit test code to facilitate self-testing by back-end personnel.

## Steps for usage

1. Introduce the modules that need to generate test code into the unit-test-code-generate/pom file.
2. Configure the module file path and package path in unit-test-code-generate/src/main/java/com/alizo/zTest/codeGen/TestCode.java
3. Execute the first method of TestCode to initialize (the initial process will read the target/class file, so the module file needs to be processed first🧬)
4. Configure relevant environment parameters in zTest/DefaultBaseControllerTest.java
