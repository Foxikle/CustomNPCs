package dev.foxikle.customnpcs.internal.utils.await;

public sealed interface Result<T> {
    record Ok<T>(T value) implements Result<T> {
    }

    record Cancelled<T>() implements Result<T> {
    }

    record Failed<T>(String cause) implements Result<T> {
    }
}
