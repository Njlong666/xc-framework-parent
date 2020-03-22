package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/***
 * Feign拦截器 解决微服务之间Feign调用导致JWT令牌无法传递问题
 *
 */
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        try {
            //使用RequestContextHolder工具获取request相关变量
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null){
                //获取request
                HttpServletRequest request = attributes.getRequest();
                Enumeration<String> requestHeaderNames = request.getHeaderNames();
                if (requestHeaderNames != null){
                    while (requestHeaderNames.hasMoreElements()){
                        String name = requestHeaderNames.nextElement();
                        String values = request.getHeader(name);
                        if (name.equals("authorization")){
                            requestTemplate.header(name,values);
                        }

                    }
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
