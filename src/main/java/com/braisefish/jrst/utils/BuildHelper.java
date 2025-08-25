package com.braisefish.jrst.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BuildHelper<T> {
    private T t;
    private Supplier<T> instant;

    public BuildHelper(T t) {
        this.t = t;
    }

    public BuildHelper(Supplier<T> instant) {
        this.instant = instant;
    }

    public BuildHelper<T> exec(Consumer<T> function) {
        function.accept(this.t);
        return this;
    }

    public <P1> BuildHelper<T> with(BiConsumer<T, P1> consumer, P1 p1) {
        consumer.accept(instant.get(), p1);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public T to() {
        return this.t;
    }

    public T get() {
        return this.t;
    }

    public T execGet(Consumer<T> function) {
        function.accept(this.t);
        return this.t;
    }

    public static <T> BuildHelper<T> of(T t) {
        return new BuildHelper(t);
    }

    public static <T> T ofExecGet(T t, Consumer<T> function) {
        return of(t).execGet(function);
    }

    public static <T> BuildHelper<T> ofExecGetBuild(T t, Consumer<T> function) {
        return of(t).exec(function);
    }

    public static <T> void ofExec(T t, Consumer<T> function) {
        of(t).exec(function);
    }

    public static void beautify(Runnable runnable) {
        beautify(true, runnable);
    }

    public static <T> T beautify(Supplier<T> instant, Runnable runnable) {
        beautify(true, runnable);
        return instant.get();
    }

    public static <T> T beautify(T t, Runnable runnable) {
        beautify(true, runnable);
        return t;
    }

    public static void beautify(boolean flag, Runnable okCallBack) {
        beautify(flag, okCallBack, null);
    }

    public static void beautify(boolean flag, Runnable okCallBack, Runnable noCallBack) {
        if (flag) {
            if (Objects.nonNull(okCallBack)) {
                okCallBack.run();
            }
        } else {
            if (Objects.nonNull(noCallBack)) {
                noCallBack.run();
            }
        }
    }


    public static <T> void beautify(T t, Consumer<T> consumer) {
        consumer.accept(t);
    }
}
