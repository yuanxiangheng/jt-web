package com.jt.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.common.vo.SysResult;
import com.jt.manage.pojo.ItemDesc;
import com.jt.web.pojo.Item;
import com.jt.web.service.ItemService;

@RequestMapping("item")
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping("{itemId}")
    public ModelAndView showItem(@PathVariable("itemId") Long itemId) {
        ModelAndView mv = new ModelAndView("item");
        // 查询商品信息
        Item item = this.itemService.queryItemById(itemId);
        mv.addObject("item", item);
        
        //加载商品描述信息
        ItemDesc itemDesc = this.itemService.queryItemDescByItemId(itemId);
        mv.addObject("itemDesc", itemDesc);
        
        // 加载商品规格参数
        String itemParam = this.itemService.queryItemParamItemByItemId(itemId);
        mv.addObject("itemParam", itemParam);
        
        return mv;
    }
    
    /**
     * 通知更新缓存中数据
     * @param itemId
     * @return
     */
    @RequestMapping("updateRedis/{itemId}")
    @ResponseBody
    public SysResult updateRedis(@PathVariable("itemId") Long itemId){
        this.itemService.updateRedis(itemId);
        return SysResult.ok();
    }

}
