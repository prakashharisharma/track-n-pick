package com.example.web.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration("customAccessDeniedHandler")
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            LOGGER.warn(
                    "User: "
                            + auth.getName()
                            + " attempted to access the protected URL: "
                            + request.getRequestURI());
        } else {
            LOGGER.warn(
                    "An Anonymous User from IP "
                            + request.getRemoteAddr()
                            + " attempted to access the protected URL: "
                            + request.getRequestURI());
        }

        response.sendRedirect(request.getContextPath() + "/validate/accessDenied.html");
    }
}
