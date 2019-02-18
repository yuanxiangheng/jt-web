package com.jt.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jt.common.service.HttpClientService;
import com.jt.common.service.RedisService;
import com.jt.common.spring.exetend.PropertyConfig;
import com.jt.common.vo.SysResult;
import com.jt.manage.pojo.ItemDesc;
import com.jt.manage.pojo.ItemParamItem;
import com.jt.web.pojo.Item;

@Service
public class ItemService {

    @Autowired
    private HttpClientService httpClientService;

    @PropertyConfig
    private String MANAGE_TAOTAO;

    @Autowired
    private RedisService redisService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String REDIS_ITEM_KEY = "TAOTAO_WEB_ITEM_";

    private static final String REDIS_ITEM_DESC_KEY = "TAOTAO_WEB_ITEM_DESC_";
    private static final String REDIS_ITEM_PARAM_ITEM_KEY = "TAOTAO_WEB_ITEM_PARAM_ITEM_";

    public Item queryItemById(Long itemId) {
        String key = REDIS_ITEM_KEY + itemId;
        // 从缓存中命中
        try {
            String redisData = this.redisService.get(key);
            if (StringUtils.isNoneEmpty(redisData)) {
                return MAPPER.readValue(redisData, Item.class);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // 数据从后台管理系统中获取，通过Httpclient获取
        String url = MANAGE_TAOTAO + "/item/query/" + itemId;
        Item item = null;
        try {
            String jsonData = this.httpClientService.doGet(url);
            SysResult taotaoResult = SysResult.formatToPojo(jsonData, Item.class);
            item = (Item) taotaoResult.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != item) {
            // 写入到缓存中
            try {
                this.redisService.set(key, MAPPER.writeValueAsString(item),
                        60 * 60 * 24 * 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return item;
    }

    public ItemDesc queryItemDescByItemId(Long itemId) {
        String key = REDIS_ITEM_DESC_KEY + itemId;
        // 从缓存中命中
        try {
            String redisData = this.redisService.get(key);
            if (StringUtils.isNoneEmpty(redisData)) {
                return MAPPER.readValue(redisData, ItemDesc.class);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String url = MANAGE_TAOTAO + "/item/query/item/desc/" + itemId;
        ItemDesc itemDesc = null;
        try {
            String jsonData = this.httpClientService.doGet(url);
            SysResult taotaoResult = SysResult.formatToPojo(jsonData, ItemDesc.class);
            itemDesc = (ItemDesc) taotaoResult.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != itemDesc) {
            // 写入到缓存中
            try {
                this.redisService.set(key, MAPPER.writeValueAsString(itemDesc),
                        60 * 60 * 24 * 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return itemDesc;
    }

    /**
     * 加载商品规格参数
     * 
     * @param itemId
     * @return
     */
    public String queryItemParamItemByItemId(Long itemId) {
        String key = REDIS_ITEM_PARAM_ITEM_KEY + itemId;
        // 从缓存中命中
        try {
            String redisData = this.redisService.get(key);
            if (StringUtils.isNoneEmpty(redisData)) {
                return redisData;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String url = MANAGE_TAOTAO + "/item/param/item/query/" + itemId;
        String strResult = null;
        try {
            String jsonData = this.httpClientService.doGet(url);
            SysResult taotaoResult = SysResult.formatToPojo(jsonData, ItemParamItem.class);
            ItemParamItem itemParamItem = (ItemParamItem) taotaoResult.getData();
            String paramData = itemParamItem.getParamData();
            ArrayNode arrayNode = (ArrayNode) MAPPER.readTree(paramData);
            StringBuilder sb = new StringBuilder();
            sb.append("<table cellpadding=\"0\" cellspacing=\"1\" width=\"100%\" border=\"0\" class=\"Ptable\"><tbody>");

            for (JsonNode jsonNode : arrayNode) {
                sb.append("<tr><th class=\"tdTitle\" colspan=\"2\">" + jsonNode.get("group").asText()
                        + "</th></tr><tr>");
                ArrayNode params = (ArrayNode) jsonNode.get("params");
                for (JsonNode param : params) {
                    sb.append("<tr><td class=\"tdTitle\">" + param.get("k").asText() + "</td><td>"
                            + param.get("v").asText() + "</td></tr>");
                } 
            }
            sb.append("</tbody></table>");
            strResult = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != strResult) {
            // 写入到缓存中
            try {
                this.redisService.set(key, strResult, 60 * 60 * 24 * 30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }

    public void updateRedis(Long itemId) {
        this.redisService.del(REDIS_ITEM_KEY + itemId);
        this.redisService.del(REDIS_ITEM_DESC_KEY + itemId);
        this.redisService.del(REDIS_ITEM_PARAM_ITEM_KEY + itemId);
    }

}
