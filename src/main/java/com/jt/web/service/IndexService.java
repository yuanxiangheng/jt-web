package com.jt.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.common.service.HttpClientService;
import com.jt.common.service.RedisService;
import com.jt.common.spring.exetend.PropertyConfig;
import com.jt.common.vo.EasyUIResult;
import com.jt.manage.pojo.Content;

@Service
public class IndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private HttpClientService httpClientService;

    @PropertyConfig
    private String MANAGE_TAOTAO;

    @PropertyConfig
    private String INDEX_AD1;

    @PropertyConfig
    private String INDEX_NEWS;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisService redisService;

    private static final String REDIS_INDEX_AD1 = "TAOTAO_WEB_INDEX_AD1";

    /**
     * 从后台管理系统中获取数据
     * 
     * @param url
     * @return
     */
    private String getDataFromManage(String url) {
        String data = null;
        try {
            data = this.httpClientService.doGet(url);
        } catch (Exception e) {
            LOGGER.error("请求失败! url = " + url, e);
            return null;
        }
        return data;
    }

    /**
     * 获取大广告位数据
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public String queryIndexAD1() {
        // 从缓存中命中
        try {
            String data = this.redisService.get(REDIS_INDEX_AD1);
            if (StringUtils.isNoneEmpty(data)) {
                return data;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // 获取数据
        String url = MANAGE_TAOTAO + INDEX_AD1;

        EasyUIResult easyUIResult = EasyUIResult.formatToList(getDataFromManage(url), Content.class);

        // 封装前端的数据结构
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<Content> contents = (List<Content>) easyUIResult.getRows();
        for (Content content : contents) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("width", 670);
            map.put("height", 240);
            map.put("href", content.getUrl());
            map.put("alt", "");
            map.put("src", content.getPic());
            map.put("widthB", 550);
            map.put("heightB", 240);
            map.put("srcB", content.getPic());

            result.add(map);
        }

        String resultStr = null;
        // 将result转化为json字符串
        try {
            resultStr = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            LOGGER.error("转化json错误! result = " + result, e);
            return null;
        }
        try {
            // 将结果数据写入缓存
            this.redisService.set(REDIS_INDEX_AD1, resultStr, 60 * 60 * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }

    @SuppressWarnings("unchecked")
    public List<Content> queryIndexNews() {
        // 获取数据
        String url = MANAGE_TAOTAO + INDEX_NEWS;
        EasyUIResult easyUIResult = EasyUIResult.formatToList(getDataFromManage(url), Content.class);
        return (List<Content> )easyUIResult.getRows();
    }

}
