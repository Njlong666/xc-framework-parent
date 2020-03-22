package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class LoginFilterService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /***
     * //1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
     * @param request
     * @return
     */
    public String getCookie(HttpServletRequest request) {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String uid = map.get("uid");
        if (uid == null){
            return null;
        }
        return uid;
    }

    /***
      //2、从http header查询jwt令牌是否存在，不存在则拒绝访问
     * @param request
     * @return
     */
    public String getHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)){
            return null;
        }
            if (!authorization.startsWith("Bearer ")){
            return null;
        }
        return authorization;
    }

    /***
     * //3、从Redis查询user_token令牌是否过期，过期则拒绝访问
     * @param token
     * @return
     */
    public long getRedisToken(String token) {
        String key = "user_token:"+token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }
}
