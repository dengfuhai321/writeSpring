package com.xiaoden.demo.service;

import com.xiaoden.spring.annotation.Service;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/26 0026
 */
@Service
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is"+name;
    }
}
