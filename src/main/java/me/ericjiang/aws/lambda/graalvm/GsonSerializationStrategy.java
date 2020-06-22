package me.ericjiang.aws.lambda.graalvm;

import com.google.gson.Gson;

import lombok.Builder;
import lombok.NonNull;

/**
 * {@link GenericSerializationStrategy} implementation that uses Gson to serialize and deserialize.
 */
@Builder
class GsonSerializationStrategy implements GenericSerializationStrategy {

    @NonNull
    private final Gson gson;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> String serialize(T value, Class<T> classOfT) {
        return this.gson.toJson(value, classOfT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(String string, Class<T> classOfT) {
        return this.gson.fromJson(string, classOfT);
    }

}