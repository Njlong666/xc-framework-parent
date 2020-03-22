package com.xuecheng.framework.exception;


import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.Immutable;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/*****
 * 异常捕获类
 */
@ControllerAdvice
public class ExceptionCatch {

    private static final Logger  LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);;

    //配置异常所对应的错误代码
    private static ImmutableMap <Class<? extends Throwable>,ResultCode>EXCEPTION;

    //定义map.builder对象 去构建ImmutableMap
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    @ExceptionHandler({CustomException.class})
    @ResponseBody
    public ResponseResult customException(CustomException e){
        LOGGER.error("catch exception {}",e.getMessage());

        ResultCode resultCode = e.getResultCode();
        return new ResponseResult(resultCode);
    }



    //捕获Exception异常
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ResponseResult exception(Exception e){
        LOGGER.error("catch exception {}",e.getMessage());
        if (EXCEPTION == null){
            EXCEPTION = builder.build();
        }
        //map中找对应的错误代码

        ResultCode resultCode1 = EXCEPTION.get(e.getClass());
        if (resultCode1 != null){
            return new ResponseResult(resultCode1);
        }else {
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }
    }

    static {
        //定义异常类型所对应的错误码
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PAPAM);
    }
}
