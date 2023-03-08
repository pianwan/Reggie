package com.example.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = {"/*"})
public class LoginCheckFilter implements Filter {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final String[] urls = new String[]{"/employee/login", "/employee/logout", "/backend/**", "/front/**", "/common/**", "/user/sendMsg", "/user/login"};

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = ((HttpServletRequest) servletRequest);
        HttpServletResponse response = ((HttpServletResponse) servletResponse);
        // 获取URI
        String uri = request.getRequestURI();
        // 判断请求是否需要处理
        if (check(uri)) {
            filterChain.doFilter(request, response);
        } else {
            if (request.getSession().getAttribute("employee") != null) {
                Long empId = Long.valueOf(request.getSession().getAttribute("employee").toString());
                BaseContext.setCurrentId(empId);
                filterChain.doFilter(request, response);
                return;
            }

            if (request.getSession().getAttribute("user") != null) {
                Long userId = Long.valueOf(request.getSession().getAttribute("user").toString());
                BaseContext.setCurrentId(userId);
                filterChain.doFilter(request, response);
                return;
            }

            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }
    }

    private boolean check(String uri) {
        for (String url : urls) {
            if (antPathMatcher.match(url, uri)) return true;
        }
        return false;
    }
}
