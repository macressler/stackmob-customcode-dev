package com.stackmob.customcode.dev.test.server;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockJettyRequest extends Request {
    private final String methodString;
    private final String uriString;
    private final Map<String, String> headers;
    private final String bodyString;

    private final AtomicBoolean handled = new AtomicBoolean(false);

    public MockJettyRequest(String methodString, String uriString, Map<String, String> headers, String bodyString) {
        this.methodString = methodString;
        this.uriString = uriString;
        this.headers = headers;
        this.bodyString = bodyString;
    }

    @Override
    public String getMethod() {
        return this.methodString;
    }

    @Override
    public HttpURI getUri() {
        return new HttpURI(uriString);
    }

    @Override
    public String getPathInfo() {
        try {
            return new URL(uriString).getPath();
        } catch (MalformedURLException t) {
            return "/";
        }
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(this.uriString);
    }

    @Override
    public String getHeader(String headerName) {
        return this.headers.get(headerName);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new StringReader(this.bodyString));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public void setHandled(boolean h) {
        handled.set(h);
    }

    public boolean getHandled() {
        return handled.get();
    }
}
