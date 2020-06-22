package me.ericjiang.aws.lambda.graalvm;

import java.util.Optional;
import java.util.ServiceLoader;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import me.ericjiang.aws.lambda.graalvm.serialization.RequestDeserializer;
import me.ericjiang.aws.lambda.graalvm.serialization.ResponseSerializer;

public interface LambdaRuntime {

    /**
     * Assign a {@link RequestHandler} to handle requests without any request
     * serialization or response deserialization.
     *
     * @param requestHandler object that processes requests and returns a response
     */
    void initialize(RequestHandler<String, String> requestHandler);

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
    <I, O> void initialize(RequestHandler<I, O> requestHandler, Class<I> inputClass, Class<O> outputClass);

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
    <I, O> void initialize(
            RequestHandler<I, O> requestHandler,
            RequestDeserializer<I> requestDeserializer,
            ResponseSerializer<O> responseSerializer);

    /**
     * Create an implemented instance of {@link LambdaRuntime}.
     *
     * @return an instance of {@link LambdaRuntime}
     */
    public static LambdaRuntime create() {
        final String runtimeApiDomain = Optional.ofNullable(System.getenv("AWS_LAMBDA_RUNTIME_API"))
                .orElseThrow(() -> new RuntimeException("Environment variable 'AWS_LAMBDA_RUNTIME_API' is not set."));
        return create(runtimeApiDomain);
    }

    /**
     * Create an implemented instance of {@link LambdaRuntime} using a custom endpoint for the Lambda Runtime API.
     *
     * This method exists for testing and is not intended for production use.
     *
     * @param runtimeApiHost host and port of the runtime API
     * @return an instance of {@link LambdaRuntime}
     */
    static LambdaRuntime create(String runtimeApiHost) {
        final String runtimeApiEndpoint = String.format("http://%s", runtimeApiHost);

        final LambdaRuntimeInterface lambdaRuntimeInterface = LambdaRuntimeInterfaceClient.builder()
                .runtimeApiEndpoint(runtimeApiEndpoint)
                .build();

        final LambdaInvocationPoller lambdaInvocationPoller = LambdaInvocationPoller.builder()
                .lambdaRuntimeInterface(lambdaRuntimeInterface)
                .build();

        // reference: https://immutables.github.io/json.html#type-adapter-registration
        final GsonBuilder gsonBuilder = new GsonBuilder();
        for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
            gsonBuilder.registerTypeAdapterFactory(factory);
        }
        final Gson gson = gsonBuilder.create();
        final GenericSerializationStrategy defaultSerializationStrategy = GsonSerializationStrategy.builder()
                .gson(gson)
                .build();

        final LambdaRuntime runtime = LambdaRuntimeImpl.builder()
                .lambdaRuntimeInterface(lambdaRuntimeInterface)
                .lambdaInvocationPoller(lambdaInvocationPoller)
                .defaultSerializationStrategy(defaultSerializationStrategy)
                .build();
        return runtime;
    }
}
