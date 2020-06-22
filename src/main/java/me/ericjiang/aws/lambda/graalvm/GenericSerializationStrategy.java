package me.ericjiang.aws.lambda.graalvm;

/**
 * Object that serializes and deserializes values of any type.
 */
interface GenericSerializationStrategy {

    /**
     * Serialize a value of type T to a String.
     * @param <T> the value's type
     * @param value the value to serialize
     * @param classOfT Class object for type T
     * @return the value serialized as a String
     */
    <T> String serialize(T value, Class<T> classOfT);

    /**
     * Deserialize a value of type T from a String.
     * @param <T> the value's type
     * @param string the value serialized as a String
     * @param classOfT Class object for type T
     * @return the deserialized value
     */
    <T> T deserialize(String string, Class<T> classOfT);

}