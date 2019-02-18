package com.jt.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitItemService.class);

    @Autowired
    private ItemService itemService;

    public void updateItem(Long itemId) {
        LOGGER.info("接受到MQ的消息，内容为：{}", itemId);
        this.itemService.updateRedis(itemId);
    }

}
