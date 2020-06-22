package me.ericjiang.aws.lambda.graalvm.serialization;

/**
 * Object that deserializes incoming requests.
 * @param <T> Type of request objects
 */
public interface RequestDeserializer<T> {

    /**
     * Deserializes a request object.
     * @param request serialized request to deserialize
     * @return deserialized request object
     */
    T deserialize(String request);

}