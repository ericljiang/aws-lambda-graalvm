package me.ericjiang.aws.lambda.graalvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Test;

import me.ericjiang.aws.lambda.graalvm.exception.LambdaRuntimeError;

public class LambdaRuntimeInterfaceClientTest extends LocalServerTestBase {

    @Test
    public void getsNextInvocation() throws Exception {
        this.serverBootstrap.registerHandler("/2018-06-01/runtime/invocation/next", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                    throws HttpException, IOException {
                response.addHeader("Lambda-Runtime-Aws-Request-Id", "123");
                response.setEntity(new StringEntity("abc"));
            }
        });
        final HttpHost localTestServer = start();
        final String url = localTestServer.toURI();
        final LambdaRuntimeInterfaceClient client = LambdaRuntimeInterfaceClient.builder()
                .runtimeApiEndpoint(url)
                .build();

        final LambdaInvocation invocation = client.getNextInvocation();
        assertEquals("123", invocation.getContext().getAwsRequestId());
        assertEquals("abc", invocation.getInvocationEvent());
    }

    @Test(expected = LambdaRuntimeError.class)
    public void getNextInvocationThrowsLambdaRuntimeError() throws Exception {
        this.serverBootstrap.registerHandler("/2018-06-01/runtime/invocation/next", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                    throws HttpException, IOException {
                throw new IOException();
            }
        });
        final HttpHost localTestServer = start();
        final String url = localTestServer.toURI();
        final LambdaRuntimeInterfaceClient client = LambdaRuntimeInterfaceClient.builder()
                .runtimeApiEndpoint(url)
                .build();

        client.getNextInvocation();
    }

    @Test
    public void postsInvocationResponse() throws Exception {
        final AtomicBoolean postReceived = new AtomicBoolean(false);
        this.serverBootstrap.registerHandler("/2018-06-01/runtime/invocation/123/response", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                    throws HttpException, IOException {
                postReceived.set(true);
            }
        });
        final HttpHost localTestServer = start();
        final String url = localTestServer.toURI();
        final LambdaRuntimeInterfaceClient client = LambdaRuntimeInterfaceClient.builder()
                .runtimeApiEndpoint(url)
                .build();

        client.postInvocationResponse("123", "");
        assertTrue(postReceived.get());
    }

    @Test(expected = LambdaRuntimeError.class)
    public void postInvocationResponseThrowsLambdaRuntimeError() throws Exception {
        this.serverBootstrap.registerHandler("/2018-06-01/runtime/invocation/123/response", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                    throws HttpException, IOException {
                throw new IOException();
            }
        });
        final HttpHost localTestServer = start();
        final String url = localTestServer.toURI();
        final LambdaRuntimeInterfaceClient client = LambdaRuntimeInterfaceClient.builder()
                .runtimeApiEndpoint(url)
                .build();

        client.postInvocationResponse("123", "");
    }
}
