package me.ericjiang.aws.lambda.graalvm;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import me.ericjiang.aws.lambda.graalvm.exception.LambdaRuntimeError;

public class LambdaRuntimeTest {
    @Test
    public void succeeds() throws LambdaRuntimeError {
        final LambdaRuntime runtime = LambdaRuntime.create("localhost:1234");
        assertNotNull(runtime);
    }
}
