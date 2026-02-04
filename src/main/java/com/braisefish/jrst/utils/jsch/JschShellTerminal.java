package com.braisefish.jrst.utils.jsch;

import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;
import com.braisefish.jrst.lang.func.Consumer2;
import com.braisefish.jrst.lang.func.Consumer3;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;


@Slf4j
public class JschShellTerminal {
    public final static String PWD_CMD_ID = "PWD_CMD_ID";
    @Getter
    private Session session;
    @Getter
    private ChannelShell channel;

    @Getter
    private InputStream inputStream;
    @Getter
    private OutputStream outputStream;
    @Getter
    private final int len;

    private final AtomicReference<String> CMD = new AtomicReference<>("");
    private final AtomicReference<String> ID = new AtomicReference<>("");
    private Consumer<String> outputLineConsumer;
    private Consumer<Exception> errorEvent;
    private Consumer3<String, List<String>, String> outputLinesConsumer;
    private Thread checkThread;
    private static final String HOST_LINE_REGEX = "\\[\\w+@\\w+\\s+(.+)?\\](\\s+)?(\\#)?(\\$)?";
    private final Lock READING_LOCK = new ReentrantLock();
    private final Lock IN_OUT_LOCK = new ReentrantLock();
    private final AtomicReference<KeyValuePair<String, String>> ID_CMD = new AtomicReference<>();
    private final Queue<KeyValuePair<String, String>> IN_ID_CMD = new LinkedList<>();
    private final Map<String, List<String>> CMD_PROPERTY = new ConcurrentHashMap<>();
    private boolean isInit = true;
    private long timeout = 30000L;

    public JschShellTerminal(Builder builder) throws JSchException, IOException {
        this.len = Objects.nonNull(builder.len) ? (builder.len > 0 ? builder.len : 4096) : 4096;
        this.outputLineConsumer = builder.outputLineConsumer;
        this.outputLinesConsumer = builder.outputLinesConsumer;
        this.errorEvent = builder.errorEvent;
        this.session = builder.session;
        if (Objects.nonNull(builder.timeout)) {
            this.timeout = builder.timeout;
        }
        if (Objects.isNull(this.outputLineConsumer)) {
            this.outputLineConsumer = (line) -> {
                log.info("{}", line);
            };
        }
        if (Objects.isNull(this.outputLinesConsumer)) {
            this.outputLinesConsumer = (id, lines, cmd) -> {
                log.info("{}", lines);
            };
        }
        if (Objects.isNull(this.errorEvent)) {
            this.errorEvent = (ex) -> {
                log.error("error", ex);
            };
        }
        checkConnection();
        init();
    }

    void checkConnection() throws JSchException, IOException {
        if (!this.session.isConnected()) {
            this.session.connect();
        }
        if (Objects.isNull(this.channel) || this.channel.isClosed()) {
            this.channel = (ChannelShell) this.session.openChannel("shell");
            this.channel.setPty(true);
            this.channel.setPtyType("dumb");
            this.channel.setPtySize(1200, Integer.MAX_VALUE, 0, 0);
            this.inputStream = this.channel.getInputStream();
            this.outputStream = this.channel.getOutputStream();
        }
        if (!this.channel.isConnected()) {
            this.channel.connect();
        }
    }

    void init() throws JSchException, IOException {
        new Thread(() -> {
            try {
                while (true) {
                    KeyValuePair<String, String> kv = null;
                    while (Objects.nonNull(kv = IN_ID_CMD.poll())) {
                        READING_LOCK.lock();
                        command(kv.getValue());
                        ID_CMD.set(kv);
                        READING_LOCK.unlock();
                        int time = 0;
                        while (!CMD_PROPERTY.containsKey(kv.getKey())) {
                            Thread.sleep(100);
                            time += 100;
                            if (time > timeout) {
                                this.errorEvent.accept(new TimeoutException("result waiting timeout!"));
                                break;
                            }
                        }
                        if (Objects.nonNull(this.outputLinesConsumer)) {
                            this.outputLinesConsumer.accept(kv.getValue(), CMD_PROPERTY.get(kv.getKey()), kv.getKey());
                        }
                        CMD_PROPERTY.remove(kv.getKey());
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                log.error("error", e);
                this.errorEvent.accept(e);
            }
        }).start();
        checkThread = new Thread(() -> {
            try {
                Pattern pattern = Pattern.compile(HOST_LINE_REGEX);
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
                String line;
                boolean isFirst = true;

                List<String> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {

                    log.info("{}", line);
                    line = cleanAnsi(line);
                    if (isFirst && pattern.matcher(line).find() && Objects.isNull(ID_CMD.get()) && StrUtil.isBlank(ID.get())) {
                        this.outputLineConsumer.accept(line);
                        continue;
                    }
                    if (isFirst && pattern.matcher(line).find() && Objects.nonNull(ID_CMD.get())) {
                        READING_LOCK.lock();
                        lines = new ArrayList<>();
                        isFirst = false;
                        IN_OUT_LOCK.lock();
                        var kv = ID_CMD.get();
                        ID.set(kv.getKey());
                        CMD.set(kv.getValue());
                        IN_OUT_LOCK.unlock();
                        lines.add(line);
                    } else if (!isFirst && pattern.matcher(line).find()) {
                        lines.add(line);
                        isFirst = true;
                        READING_LOCK.unlock();
//                        if (Objects.nonNull(this.outputLinesConsumer)) {
//                            this.outputLinesConsumer.accept(CMD.get(), lines, ID.get());
//                            CMD_PROPERTY.put(ID.get(), lines);
//                        }
                        CMD_PROPERTY.put(ID.get(), lines);
                    } else if (StrUtil.isBlank(CMD.get())) {
                        if (Objects.nonNull(this.outputLineConsumer)) {
                            this.outputLineConsumer.accept(line);
                        }
                    } else {
                        lines.add(line);
                    }
                }
            } catch (Exception e) {
                log.error("error", e);
                this.errorEvent.accept(e);
            }
        });
        checkThread.start();
    }


    public synchronized void command(String command, String id) throws JSchException, IOException {
//        if (READING_LOCK.tryLock(10, TimeUnit.SECONDS)) {
//            CMD.set(command);
//            PrintWriter printWriter = new PrintWriter(this.outputStream);
//            printWriter.println(command);
//            printWriter.flush();
//            READING_LOCK.unlock();
//        }
        IN_OUT_LOCK.lock();
        var kv = new KeyValuePair<>(id, command);
        IN_ID_CMD.offer(kv);
        IN_OUT_LOCK.unlock();
    }

    public synchronized void command(String command) throws JSchException, IOException {
        checkConnection();
        PrintWriter printWriter = new PrintWriter(this.outputStream);
        printWriter.println(command);
        printWriter.flush();
    }

    public void pwd()  {
        try {
            command("pwd", PWD_CMD_ID);
        } catch (JSchException | IOException e) {
           log.error("pwd error", e);
        }
    }

    public static class Builder {
        private Session session;
        private Consumer<String> outputLineConsumer;
        private Consumer3<String, List<String>, String> outputLinesConsumer;
        private Integer len;
        private Long timeout;
        private Consumer<Exception> errorEvent;


        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        public Builder setOutputLineConsumer(Consumer<String> outputLineConsumer) {
            this.outputLineConsumer = outputLineConsumer;
            return this;
        }

        public Builder setOutputLinesConsumer(Consumer3<String, List<String>, String> outputLinesConsumer) {
            this.outputLinesConsumer = outputLinesConsumer;
            return this;
        }

        public Builder setLen(Integer len) {
            this.len = len;
            return this;
        }

        public Builder setTimeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setErrorEvent(Consumer<Exception> errorEvent) {
            this.errorEvent = errorEvent;
            return this;
        }

        public JschShellTerminal build() throws JSchException, IOException {
            return new JschShellTerminal(this);
        }
    }

    private static String cleanAnsi(String str) {
        return str.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
    }

}
