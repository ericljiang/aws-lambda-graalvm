package me.ericjiang.aws.lambda.graalvm.demo;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface Request {
    String getMessage();
}