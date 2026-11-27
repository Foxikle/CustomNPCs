package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.internal.utils.WaitingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.mevera.lotus.api.data.DataRegistry;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class AwaitInput<T> {
    private final CompletableFuture<Result<T>> future = new CompletableFuture<>();
    private final WaitingType type;
    private final DataRegistry data;

    public CompletableFuture<Result<T>> future() {
        return future;
    }

    public void complete(T data) {
        future.complete(new Result.Ok<>(data));
    }

    public void cancel() {
        future.complete(new Result.Cancelled<>());
    }

    public void fail(String cause) {
        future.complete(new Result.Failed<>(cause));
    }
}
