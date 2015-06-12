package com.macyves.filters.patterns;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 
 * Base pattern for filter implementations.
 * 
 * @author yves
 * 
 */
public abstract class ChainedFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // first we process the incoming request, then pass it onwards.
        if (filterIncomingRequest(request, response)) {
            chain.doFilter(request, response);
            // then we process the outgoing response
            filterOutgoingResponse(request, response);
        } else {
            return;
        }
    }

    /**
     * Return false to break out of the request processing. else return true.
     */
    public abstract boolean filterIncomingRequest(ServletRequest request, ServletResponse response) throws IOException, ServletException;

    public abstract void filterOutgoingResponse(ServletRequest request, ServletResponse response) throws IOException, ServletException;

}
