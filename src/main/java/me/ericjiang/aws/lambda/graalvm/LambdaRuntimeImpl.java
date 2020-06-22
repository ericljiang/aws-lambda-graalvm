package me.ericjiang.aws.lambda.graalvm;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import me.ericjiang.aws.lambda.graalvm.exception.LambdaRuntimeError;
import me.ericjiang.aws.lambda.graalvm.serialization.RequestDeserializer;
import me.ericjiang.aws.lambda.graalvm.serialization.ResponseSerializer;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * The {@link LambdaRuntime} polls for incoming requests and invokes the
 * provided {@link RequestHandler}, optionally parsing incoming requests and
 * outgoing responses if configured.
 */
@Slf4j
@Builder
class LambdaRuntimeImpl implements LambdaRuntime {

    @NonNull
    private final LambdaRuntimeInterface lambdaRuntimeInterface;

    @NonNull
    private final LambdaInvocationPoller lambdaInvocationPoller;

    @NonNull
    private final GenericSerializationStrategy defaultSerializationStrategy;

    /**
     * Assign a {@link RequestHandler} to handle requests without any request
     * serialization or response deserialization.
     *
     * @param requestHandler object that processes requests and returns a response
     */
    public void initialize(RequestHandler<String, String> requestHandler) {
        initialize(requestHandler, s -> s, s -> s);
    }

    /**
     * Assign a {@link RequestHandler} to handle requests using default serializtion
     * for requests and responses.
     *
     * @param <I>            Type of requests
     * @param <O>            Type of responses
     * @param requestHandler Object that processes requests and returns a response
     * @param inputClass     Class of requests
     * @param outputClass    Class of responses
     */
    public <I, O> void initialize(RequestHandler<I, O> requestHandler, Class<I> inputClass, Class<O> outputClass) {
        initialize(
                requestHandler,
                i -> defaultSerializationStrategy.deserialize(i, inputClass),
                o -> defaultSerializationStrategy.serialize(o, outputClass));
    }

    /**
     * Assign a {@link RequestHandler} to process incoming requests using the
     * specified {@link RequestDeserializer} to parse requests and
     * {@link ResponseSerializer} to package responses.
     *
     * @param <I>                 Type of requests
     * @param <O>                 Type of responses
     * @param requestHandler      Object that processes requests and returns a
     *                            response
     * @param requestDeserializer Object that parses incoming requests to type
     *                            {@link I}
     * @param responseSerializer  Object that serializes outgoing requests from type
     *                            {@link O}
     */
    public <I, O> void initialize(
            RequestHandler<I, O> requestHandler,
            RequestDeserializer<I> requestDeserializer,
            ResponseSerializer<O> responseSerializer) {
        try {
            while (true) {
                this.lambdaInvocationPoller.pollAndHandleInvocation(
                        requestHandler,
                        requestDeserializer,
                        responseSerializer);
            }
        } catch (LambdaRuntimeError | RuntimeException e) {
            log.error("Error occured during runtime initialization. Attempting to post to runtime interface.", e);
            attemptPostInitializationError(e);
        } finally {
            log.info("Exiting Lambda runtime.");
        }
    }

    /**
     * Posts an initialization error to the Lambda Runtime API. If the error
     * reporting fails, logs an error and fails silently.
     * @param initializationError
     */
    private void attemptPostInitializationError(Throwable t) {
        try {
            this.lambdaRuntimeInterface.postInitializationError(t);
            log.info("Successfully posted initialization error to runtime interface.");
        } catch (LambdaRuntimeError | RuntimeException e) {
            log.error("Failed while posting initialization error to Lambda Runtime API", e);
        }
    }
}
