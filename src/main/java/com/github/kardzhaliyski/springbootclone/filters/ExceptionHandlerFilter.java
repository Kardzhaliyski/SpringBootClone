package com.github.kardzhaliyski.springbootclone.filters;

import com.github.kardzhaliyski.springbootclone.exceptions.ResponseStatusException;
import com.github.kardzhaliyski.springbootclone.utils.HttpStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExceptionHandlerFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (ResponseStatusException e) {
            res.reset();
            res.setStatus(e.httpStatus.getCode());
        }
        catch (Exception e) {
            res.reset();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        }
    }
}
