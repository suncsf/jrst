package com.braisefish.jrst.utils.thread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author sunchao
 */
public class JrstThread extends Thread {

    private final AtomicBoolean isStop;
    private final Consumer<AtomicBoolean> consumer;

    public JrstThread(Consumer<AtomicBoolean> consumer) {
        this.consumer = consumer;
        this.isStop = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        this.consumer.accept(isStop);
    }

    public void stopThread() {
        this.isStop.set(true);
    }
}
