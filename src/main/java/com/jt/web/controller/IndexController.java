package com.jt.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.jt.web.service.IndexService;

@Controller
public class IndexController {
	
	@Autowired
	private IndexService indexService;

	
	@RequestMapping("index")
	public ModelAndView index(){
        ModelAndView mv = new ModelAndView("index");

        // 获取大广告位数据
        mv.addObject("indexAD1", this.indexService.queryIndexAD1());
        
        // 京淘快报
        mv.addObject("news", this.indexService.queryIndexNews());

        return mv;
	}
}
