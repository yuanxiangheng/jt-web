package com.jt.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.common.vo.SysResult;
import com.jt.web.pojo.Cart;
import com.jt.web.pojo.User;
import com.jt.web.service.CartCookieService;
import com.jt.web.service.CartService;
import com.jt.web.threadlocal.UserThreadLocal;

@RequestMapping(value = "cart")
@Controller
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private CartCookieService cartCookieService;

    @RequestMapping("add/{itemId}")
    public String addItemToCart(@PathVariable("itemId") Long itemId,HttpServletRequest request,HttpServletResponse response) {
        // 判断用户是否登录
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录，将商品数据保存到cookie中
            try {
                this.cartCookieService.addItemToCart(itemId, request, response);
            } catch (Exception e) {
                // TODO 出现异常的跳转？
                e.printStackTrace();
            }
        } else {
            // 登录，将商品数据保存到数据库中
            this.cartService.addItemToCart(user, itemId);
        }
        return "redirect:/cart/show.html";
    }

    @RequestMapping("show")
    public ModelAndView showCart(HttpServletRequest request,HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("cart");
        User user = UserThreadLocal.get();
        List<Cart> carts = null;
        if (user == null) {
            // 未登录，从cookie中查询商品
            try {
                carts =  this.cartCookieService.queryCartByUser(request,response,true);
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        } else {
            // 登录，从api查询商品
            carts = this.cartService.queryCartByUser(user);
        }
        mv.addObject("cartList", carts);
        return mv;
    }

    @RequestMapping("delete/{itemId}")
    public String deleteCart(@PathVariable("itemId") Long itemId,HttpServletRequest request,HttpServletResponse response) {
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录，从cookie中查询商品
            try {
                this.cartCookieService.deleteCart(itemId, request, response);
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        } else {
            // 登录，从api查询商品
            this.cartService.deleteCart(user, itemId);
        }
        return "redirect:/cart/show.html";
    }

    /**
     * 更新数量
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping(value = "update/num/{itemId}/{num}")
    @ResponseBody
    public SysResult updateCart(@PathVariable("itemId") Long itemId, @PathVariable("num") Integer num,HttpServletRequest request,HttpServletResponse response) {
        User user = UserThreadLocal.get();
        if (user == null) {
            // 未登录，从cookie中查询商品
            try {
                this.cartCookieService.updateCart(request,response,itemId, num);
            } catch (Exception e) {
                e.printStackTrace();
                return SysResult.build(201, "更新数量失败！");
            }
        } else {
            // 登录，从api查询商品
            this.cartService.updateCart(user, itemId, num);
        }
        return SysResult.ok();
    }

}
