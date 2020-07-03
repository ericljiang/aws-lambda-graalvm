package me.ericjiang.aws.lambda.graalvm.demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request input, Context context) {
        return ImmutableResponse.builder()
                .message("Received message: " + input.getMessage())
                .build();
    }

}