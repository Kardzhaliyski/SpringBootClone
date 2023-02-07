package com.github.kardzhaliyski.springbootclone.filters;

import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExceptionHandlerFilter extends HttpFilter {

    public static Logger logger = LoggerFactory.getLogger(ExceptionHandlerFilter.class);

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (ResponseStatusException e) {
            handle(res, e.httpStatus);
        } catch (ServletException e) {
            Throwable cause = e.getCause();
            switch (cause) {
                case ResponseStatusException ex -> handle(res, ex.httpStatus);
                default -> handle(res, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch (Exception e) {
            logger.warn("Internal Server Error: ", e);
            handle(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void handle(HttpServletResponse res, HttpStatus e) {
        res.reset();
        res.setStatus(e.getCode());
    }

}
