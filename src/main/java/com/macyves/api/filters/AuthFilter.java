package com.macyves.api.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.naming.AuthenticationNotSupportedException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthenticationException;

import com.macyves.filters.patterns.ChainedFilter;
import com.macyves.injection.guice.ServiceInjector;
import com.sun.jersey.core.util.Base64;

/**
 * BasicAuthentication filter.
 * 
 * @author yves
 * 
 */
public class AuthFilter extends ChainedFilter {

    // for the sake of the example, lets just hardcode the user details
    // this should never see the light of day in production code.
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "adminadmin";
    private static final String SESSION_KEY = "session";

    private static class SessionUser {
        private final String user;
        private final String password;

        private SessionUser(final String user, final String password) {
            this.user = user;
            this.password = password;
        }
    }

    @Override
    public void init(final FilterConfig config) throws ServletException {
        ServiceInjector.getInjector().injectMembers(this);
    }

    private SessionUser authoriseBaiseAuth(final HttpServletRequest httpRequest) throws AuthenticationNotSupportedException, AuthenticationException, UnsupportedEncodingException {
        final String authorizationHeaderValue = StringUtils.trimToEmpty(httpRequest.getHeader("Authorization"));
        if (StringUtils.isBlank(authorizationHeaderValue)) {
            throw new AuthenticationException("Please provide Authorization header.");
        }
        if (!authorizationHeaderValue.startsWith("Basic")) {
            throw new AuthenticationNotSupportedException("Only basic auth is supported.");
        }

        final int index = authorizationHeaderValue.indexOf(' ');
        if (index <= 0) {
            throw new AuthenticationException("Malformed Authorization header value.");
        }

        String encodedCredential = authorizationHeaderValue.substring(index + 1);
        String decodedCredential = new String(Base64.decode(encodedCredential.getBytes("UTF-8")), "UTF-8");

        // Basic authentication does not allow ':' in username, so we are safe
        // in the following line.
        final String[] credentialParts = decodedCredential.split(":", 2);
        if (credentialParts == null || credentialParts.length != 2
                || "".equals(credentialParts[0]) || "".equals(credentialParts[1])) {
            throw new AuthenticationException("Malformed Authorization header value.");
        }

        // check the username and passowrd here. super simplistic.
        // this should not see the light of day in production code.
        if (credentialParts[0].equals(DEFAULT_USER) && credentialParts[1].equals(DEFAULT_PASSWORD)) {
            return new SessionUser(DEFAULT_USER, DEFAULT_PASSWORD);
        } else {
            return null;
        }
    }

    @Override
    public boolean filterIncomingRequest(ServletRequest request,
            ServletResponse response) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        final StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Authetication failed. ");
        try {
            // check for sesison first
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                // has a session based on this request.
                final SessionUser user = (SessionUser) session.getAttribute(SESSION_KEY);
                if (user == null) {
                    // no session, fallback on basic authentication.
                    // uses basic authentication
                    final SessionUser user2 = authoriseBaiseAuth(httpRequest);
                    if (user2 != null) {
                        session.setAttribute(SESSION_KEY, user2);
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                final SessionUser user2 = authoriseBaiseAuth(httpRequest);
                if (user2 != null && user2.user.equals(DEFAULT_USER) && user2.password.equals(DEFAULT_PASSWORD)) {
                    return true;
                }
            }
            // httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"" +
            // realm + "\"");
        } catch (AuthenticationNotSupportedException e) {
            errorMsg.append(e.getMessage());
        } catch (AuthenticationException e) {
            errorMsg.append(e.getMessage());
        }
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMsg.toString());
        return false;
    }

    @Override
    public void filterOutgoingResponse(ServletRequest request,
            ServletResponse response) throws IOException, ServletException {
        // not needed.
    }

    @Override
    public void destroy() {
    }
}
