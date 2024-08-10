package com.tim.vpnprotocols.xrayNeko.fmt.gson;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import com.tim.vpnprotocols.xrayNeko.util.JavaUtil;

public class GsonConverters {
    
    public static String toJson(Object value) {
        if (value instanceof Collection) {
            if (((Collection<?>) value).isEmpty()) return "";
        }
        return JavaUtil.gson.toJson(value);
    }

    
    public static List toList(String value) {
        if (JavaUtil.isNullOrBlank(value)) return CollectionsKt.listOf();
        return JavaUtil.gson.fromJson(value, List.class);
    }

    
    public static Set toSet(String value) {
        if (JavaUtil.isNullOrBlank(value)) return SetsKt.setOf();
        return JavaUtil.gson.fromJson(value, Set.class);
    }

}
