package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.LoginFilterService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    LoginFilterService loginFilterService;

    //返回字符串代表过滤器的类型
    @Override
    public String filterType() {
        /***
         * pre：请求在被路由之前执行
         * ​	routing：在路由请求时调用
         * ​	post：在routing和errror过滤器之后调用
         * ​	error：处理请求时发生错误调用
         */
        return "pre";
    }

    //此方法返回整型数值，通过此数值来定义过滤器的执行顺序，数字越小优先级越高。
    @Override
    public int filterOrder() {
        return 0;
    }

    //返回一个Boolean值，判断该过滤器是否需要执行。返回true表示要执行此过虑器，否则不执行。
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //过滤器的业务逻辑。
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();

        //1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
        String token = loginFilterService.getCookie(request);
        if (token == null){
            //拒绝访问
            this.access_denied();
            return null;
        }
        //2、从http header查询jwt令牌是否存在，不存在则拒绝访问
        String header = loginFilterService.getHeader(request);
        if (header == null){
            //拒绝访问
            this.access_denied();
            return null;
        }

        //3、从Redis查询user_token令牌是否过期，过期则拒绝访问
        long expire = loginFilterService.getRedisToken(token);
        if (expire < 0){
            //拒绝访问
            this.access_denied();
            return null;
        }
        return null;
    }


    //拒绝访问
    private void access_denied(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应状态码
        requestContext.setResponseStatusCode(200);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String toJSONString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(toJSONString);
        requestContext.getResponse().setContentType("application/json;charset=UTF-8");
    }
}
