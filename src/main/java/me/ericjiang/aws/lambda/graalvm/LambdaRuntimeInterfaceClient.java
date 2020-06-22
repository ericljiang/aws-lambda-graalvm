package me.ericjiang.aws.lambda.graalvm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import me.ericjiang.aws.lambda.graalvm.exception.LambdaRuntimeError;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class LambdaRuntimeInterfaceClient implements LambdaRuntimeInterface {

    @NonNull
    private final String runtimeApiEndpoint;

    @Override
    public LambdaInvocation getNextInvocation() throws LambdaRuntimeError {
        try {
            final URL url = new URL(String.format("%s/2018-06-01/runtime/invocation/next", runtimeApiEndpoint));
            final HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestMethod("GET");

            final int responseCode = httpClient.getResponseCode();
            log.debug("Sending 'GET' request to URL: {}", url.toString());
            log.debug("Response Code: {}", responseCode);
            log.debug("Headers: {}", httpClient.getHeaderFields().toString());

            final LambdaContext context = LambdaContext.builder()
                    .awsRequestId(httpClient.getHeaderField("Lambda-Runtime-Aws-Request-Id"))
                    .remainingTimeInMillis(0)
                    .invokedFunctionArn("Lambda-Runtime-Invoked-Function-Arn")
                    .build();

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {
                final String response = in.lines().collect(Collectors.joining("\n"));
                log.debug("Response: {}", response);
                return LambdaInvocation.builder()
                        .context(context)
                        .invocationEvent(response)
                        .build();
            }
        } catch (IOException e) {
            throw new LambdaRuntimeError("Error while getting invocation from Lambda runtime API.", e);
        }
    }

    @Override
    public void postInvocationResponse(String awsRequestId, String invocationResponse) throws LambdaRuntimeError {
        try {
            final URL url = new URL(String.format("%s/2018-06-01/runtime/invocation/%s/response",
                    runtimeApiEndpoint, awsRequestId));
            sendPost(url, invocationResponse);
        } catch (IOException | RuntimeException e) {
            throw new LambdaRuntimeError("Error while posting invocation response to Lambda runtime API.", e);
        }
    }

    @Override
    public void postInvocationError(String awsRequestId, Throwable error) throws LambdaRuntimeError {
        try {
            throw new UnsupportedOperationException();
        } catch (RuntimeException e) {
            throw new LambdaRuntimeError("Error while posting invocation error to Lambda runtime API.", e);
        }
    }

    @Override
    public void postInitializationError(Throwable error) throws LambdaRuntimeError {
        try {
            throw new UnsupportedOperationException();
        } catch (RuntimeException e) {
            throw new LambdaRuntimeError("Error while posting invocation error to Lambda runtime API.", e);
        }
    }

    private void sendPost(URL url, String data) throws IOException {
        final HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();

        //add reuqest header
        httpClient.setRequestMethod("POST");

        // Send post request
        httpClient.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream())) {
            wr.writeBytes(data);
            wr.flush();
        }

        int responseCode = httpClient.getResponseCode();
        log.debug("Sending 'POST' request to URL: {}", url);
        log.debug("Post data: {}", data);
        log.debug("Response Code: {}", responseCode);

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {
            final String response = in.lines().collect(Collectors.joining("\n"));
            log.debug("Response: {}", response);
        }

    }
}
