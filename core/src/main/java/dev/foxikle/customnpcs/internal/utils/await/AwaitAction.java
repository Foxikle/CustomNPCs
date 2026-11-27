package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.actions.Action;

import java.util.concurrent.CompletableFuture;

public final class AwaitAction {
    private final CompletableFuture<Result<Action>> future = new CompletableFuture<>();

    public CompletableFuture<Result<Action>> future() {
        return future;
    }

    public void complete(Action action) {
        future.complete(new Result.Ok<>(action));
    }

    public void cancel() {
        future.complete(new Result.Cancelled<>());
    }
}
