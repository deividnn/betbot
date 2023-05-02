/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author deivid
 */
@WebFilter(urlPatterns = {"/restrito/*"})
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        try {

            HttpServletRequest httpServletRequest = (HttpServletRequest) req;
            HttpSession session = httpServletRequest.getSession();

            if (session == null || session.getAttribute("usuario") == null) {
                String contextPath2 = httpServletRequest.getContextPath();
                String url2 = contextPath2 + "/acesso.cs";
                //redireciona para a pagina menu
                ((HttpServletResponse) resp).sendRedirect(url2);
                resp.getWriter().flush();
                resp.getWriter().close();
            } else {
                chain.doFilter(req, resp);
            }
        } catch (IOException | ServletException t) {
            Logger.getGlobal().log(Level.SEVERE, ExceptionUtils.getStackTrace(t));

        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
         Logger.getGlobal().log(Level.INFO,"init");
    }

    @Override
    public void destroy() {
           Logger.getGlobal().log(Level.INFO,"destroy");
    }

}
