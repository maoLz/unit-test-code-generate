package com.alizo.zTest.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用于测试某些特殊流程分支
 */
public enum SwitchEnum {

    打印日志,

    参数预处理,

    结果预处理,

    结果断言;

    public static List<String> names = new ArrayList<>();
    static {
        names.addAll(Arrays.asList(SwitchEnum.values()).stream().map(SwitchEnum::name).toList());
    }
}
