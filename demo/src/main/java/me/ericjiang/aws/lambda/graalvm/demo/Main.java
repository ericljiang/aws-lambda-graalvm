package me.ericjiang.aws.lambda.graalvm.demo;

import me.ericjiang.aws.lambda.graalvm.LambdaRuntime;

public class Main {
    public static void main(String[] args) {
        final LambdaRuntime runtime = LambdaRuntime.create();
        runtime.initialize(new Handler(), Request.class, Response.class);
    }
}
