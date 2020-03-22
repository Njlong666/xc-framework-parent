package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    //Redis过期时间
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //请求SpringSecurity申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        String authTokenString = JSON.toJSONString(authToken);
        //将令牌存储到Redis中
        boolean saveToken = this.saveToken(access_token, authTokenString, tokenValiditySeconds);
        if (!saveToken){
            //保存令牌失败
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }

    /****
     * 用户申请令牌
     * @param username 账号
     * @param password 密码
     * @param clientId 客户端ID
     * @param clientSecret 客户端密码
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //从eureka中获取服务地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH); //微服务的实例地址
        // http://ip:端口
        URI uri = serviceInstance.getUri();
        //http://localhost:40400/auth/oauth/token
        String authUrl = uri + "/auth/oauth/token";

        //定义headers
        LinkedMultiValueMap<String, String> headrs = new LinkedMultiValueMap<>();
        String basic = this.getBasic(clientId, clientSecret);
        headrs.add("Authorization",basic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(body, headrs);
        //设置 restTemplate 远程调用,对 400, 401不报错 并且正常返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401){
                    super.handleError(response);
                }
            }
        });
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map bodyResult = exchange.getBody();
        if (bodyResult == null || bodyResult.get("access_token") == null || bodyResult.get("refresh_token") == null || bodyResult.get("jti") == null){
            //获取spring security返回的错误信息
            String error_description = (String) bodyResult.get("error_description");
            if (error_description.equals("坏的凭证")){
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }else if (error_description.indexOf("UserDetailsService returned null") >= 0){
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }
            //申请令牌失败
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setJwt_token((String)bodyResult.get("access_token")); //访问令牌(jwt)
        authToken.setRefresh_token((String) bodyResult.get("refresh_token"));//刷新令牌(jwt)
        authToken.setAccess_token((String)bodyResult.get("jti"));//jti，作为用户的身份标识
        return authToken;
    }

    /****
     *  将token存储到 Redis中
     * @param access_token 用户令牌
     * @param content AuthToken对象内容
     * @param ttl 过期时间
     * @return boolean
     */
    private boolean saveToken(String access_token,String content , long ttl){
        String key = "user_token:"+access_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }

    //Base64编码
    private String getBasic(String clientId,String clientSecret){
        String string = clientId + ":" +clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    /**
     * 从Redis中获取JWT
     * @param token
     * @return
     */
    public AuthToken getJwt(String token) {
        String key = "user_token:"+token;
        String user_token = stringRedisTemplate.opsForValue().get(key);
        AuthToken authToken = null;
        try {
            authToken = JSON.parseObject(user_token, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 从redis删除token
     * @param token
     * @return
     */
    public boolean delToken(String token) {
        String key = "user_token:"+token;
        stringRedisTemplate.delete(key);
        return true;
    }
}
