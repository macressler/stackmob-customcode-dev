package com.stackmob.customcode.dev.test.server;

import org.eclipse.jetty.server.Request;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class MockJettyRequest extends Request {
    private final String methodString;
    private final String uriString;
    private final Map<String, String> headers;
    private final String bodyString;

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
    public Enumeration getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }
}
