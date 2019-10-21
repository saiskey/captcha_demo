package com.selfimpr.captcha.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selfimpr.captcha.click.ClickPoint;
import com.selfimpr.captcha.click.ClickTextCaptcha;
import com.selfimpr.captcha.click.GraphicsEngine;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/click")
public class ClickController {

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ClickTextCaptcha captcha = GraphicsEngine.genClickTextCaptcha();
        request.getSession().setAttribute("clickTextCaptcha", captcha);
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        Map resultMap = new HashMap();
        resultMap.put("status", "OK");
        resultMap.put("data", captcha.getWords());
        PrintWriter writer = response.getWriter();
        ObjectMapper om = new ObjectMapper();
        writer.write(om.writeValueAsString(resultMap));
        writer.close();
    }

    @RequestMapping("/checkCaptcha")
    public void checkCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //验证验证码
        String clickPointStr = request.getParameter("a");
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = decoder.decodeBuffer(clickPointStr);
        String decodedStr = new String(b);
        ObjectMapper om = new ObjectMapper();
        List list = om.readValue(decodedStr, new TypeReference<List<ClickPoint>>() {
        });
        ClickTextCaptcha captcha = (ClickTextCaptcha) request.getSession().getAttribute("clickTextCaptcha");
        boolean result = GraphicsEngine.checkClickTextCaptcha(list, captcha.getRectangles());
        Map resultMap = new HashMap();
        if (result) {
            resultMap.put("status", "OK");
        } else {
            resultMap.put("status", "FAIL");
        }
        String resultStr = om.writeValueAsString(resultMap);
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(resultStr);
        writer.close();
    }

    @RequestMapping("/captchaImg")
    public void captchaImg(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ClickTextCaptcha captcha = (ClickTextCaptcha) request.getSession().getAttribute("clickTextCaptcha");
        if (null == captcha) {
            return;
        }
        response.setHeader("Content-Type", "image/jpeg");
        OutputStream os = response.getOutputStream();
        ImageIO.write(captcha.getImage(), "jpg", os);
        os.close();
    }
}
