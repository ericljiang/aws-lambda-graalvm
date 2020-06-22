package me.ericjiang.aws.lambda.graalvm;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
class LambdaInvocation {

    @NonNull
    private final LambdaContext context;

    @NonNull
    private final String invocationEvent;
}
