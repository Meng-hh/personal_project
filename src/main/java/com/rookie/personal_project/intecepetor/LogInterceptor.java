package com.rookie.personal_project.intecepetor;

import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 生成唯一的 TraceID
        String tid = UUID.randomUUID().toString().replace("-", "");

        // 2. 放入 MDC (Logback 实际上是把这个值存到了 ThreadLocal 中)
        MDC.put(TRACE_ID, tid);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        // 3. 请求处理完后，必须移除，防止线程池复用导致 TraceID 混乱
        MDC.remove(TRACE_ID);
    }
}