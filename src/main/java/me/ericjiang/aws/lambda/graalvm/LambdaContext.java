package me.ericjiang.aws.lambda.graalvm;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
class LambdaContext implements Context {

    @NonNull
    private final String awsRequestId;

    private final String logGroupName;

    private final String logStreamName;

    private final String functionName;

    private final String functionVersion;

    private final String invokedFunctionArn;

    private final CognitoIdentity identity;

    private final ClientContext clientContext;

    private final int remainingTimeInMillis;

    private final int memoryLimitInMB;

    private final LambdaLogger logger;

}
