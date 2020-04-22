package com.jokerbee.test.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class HashMapTest {
    protected static Logger logger = LoggerFactory.getLogger("TEST");


    public static void main(String[] args) {
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("aaaaa", "1");
        map1.put("bbbbb", "1");
        map1.put("vvvvv", "1");
        map1.put("ccccc", "1");
        map1.put("ddddd", "1");
        map1.put("wwwww", "1");
        map1.put("xxxxx", "1");
        map1.put("eeeee", "1");
        logger.info("map content:{}", map1.keySet());
    }
}
