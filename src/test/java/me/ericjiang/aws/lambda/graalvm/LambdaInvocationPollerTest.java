package me.ericjiang.aws.lambda.graalvm;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import me.ericjiang.aws.lambda.graalvm.exception.LambdaInvocationError;
import me.ericjiang.aws.lambda.graalvm.exception.LambdaRuntimeError;

public class LambdaInvocationPollerTest extends EasyMockSupport {
    @Test
    public void postsInvocationResponseFromHandler() throws LambdaRuntimeError {
        final LambdaRuntimeInterface runtimeInterface = mock(LambdaRuntimeInterface.class);
        final RequestHandler<String, String> requestHandler = mock(RequestHandler.class);
        final LambdaInvocationPoller lambdaInvocationPoller = LambdaInvocationPoller.builder()
                .lambdaRuntimeInterface(runtimeInterface)
                .build();

        expect(runtimeInterface.getNextInvocation()).andReturn(LambdaInvocation.builder()
                .invocationEvent("")
                .context(LambdaContext.builder()
                        .awsRequestId("")
                        .build())
                .build());
        expect(requestHandler.handleRequest(anyString(), anyObject())).andReturn("response");
        runtimeInterface.postInvocationResponse(anyString(), eq("response"));
        expectLastCall();
        replayAll();

        lambdaInvocationPoller.pollAndHandleInvocation(requestHandler, s -> s, s -> s);
        verifyAll();
    }

    @Test
    public void postsInvocationErrorOnExceptionInHandler() throws LambdaRuntimeError {
        final LambdaRuntimeInterface runtimeInterface = mock(LambdaRuntimeInterface.class);
        final RequestHandler<String, String> requestHandler = mock(RequestHandler.class);
        final LambdaInvocationPoller lambdaInvocationPoller = LambdaInvocationPoller.builder()
                .lambdaRuntimeInterface(runtimeInterface)
                .build();

        final LambdaContext context = LambdaContext.builder()
                .awsRequestId("requestId")
                .build();

        expect(runtimeInterface.getNextInvocation()).andReturn(LambdaInvocation.builder()
                .invocationEvent("input")
                .context(context)
                .build());
        expect(requestHandler.handleRequest("input", context)).andThrow(new RuntimeException(""));
        runtimeInterface.postInvocationError(anyString(), anyObject(LambdaInvocationError.class));
        expectLastCall();
        replayAll();

        lambdaInvocationPoller.pollAndHandleInvocation(requestHandler, s -> s, s -> s);
        verifyAll();
    }
}
