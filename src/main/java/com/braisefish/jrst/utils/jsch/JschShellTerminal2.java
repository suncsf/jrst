package com.braisefish.jrst.utils.jsch;

import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;
import com.braisefish.jrst.lang.func.Consumer3;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;


@Slf4j
public class JschShellTerminal2 {
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

    private boolean firstOpen = true;

    private final Queue<KeyValuePair<String, String>> CMD_QUEUE = new ConcurrentLinkedDeque<>();
    private static final String HOST_LINE_REGEX = "\\[\\w+@\\w+\\s+(.+)?\\](\\s+)?(\\#)?(\\$)?";
    //
//    private final AtomicReference<String> CMD = new AtomicReference<>("");
//    private final AtomicReference<String> ID = new AtomicReference<>("");
    private Consumer<String> outputLineConsumer;
    private Consumer<Exception> errorEvent;
    private Consumer3<String, List<String>, String> outputLinesConsumer;
    private Thread checkThread;
    private final static Lock READING_LOCK = new ReentrantLock();
    private final AtomicReference<KeyValuePair<String, String>> CURRENT_CMD = new AtomicReference<>();
    //    private final Lock IN_OUT_LOCK = new ReentrantLock();
//    private final AtomicReference<KeyValuePair<String, String>> ID_CMD = new AtomicReference<>();
//    private final Queue<KeyValuePair<String, String>> IN_ID_CMD = new LinkedList<>();
//    private final Map<String, List<String>> CMD_PROPERTY = new ConcurrentHashMap<>();
//    private boolean isInit = true;
    private long timeout = 30000L;

    public JschShellTerminal2(Builder builder) throws JSchException, IOException {
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

    private void checkConnection() throws JSchException, IOException {
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

    private void init() throws JSchException, IOException {
        new Thread(() -> {
            try {
                while (true) {
                    KeyValuePair<String, String> kv = null;
                    while ((kv = CMD_QUEUE.poll()) != null) {
                        CURRENT_CMD.set(kv);
                        command(kv.getValue());
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
                while (true) {
                    boolean isFirst = true;
                    boolean eof = false;
                    List<String> lines = new ArrayList<>();
                    while (!eof) {
                        if (inputStream.available() > 0) {
                            byte[] buffer = new byte[inputStream.available()];
                            int len = inputStream.read(buffer);
                            if (len > 0) {
                                var line = new String(buffer, 0, len, StandardCharsets.UTF_8);
                                line = cleanAnsi(line);
                                line = line.replaceAll("\r", "");
                                var tempLines = StrUtil.split(line, "\n");
                                for (var tempLine : tempLines) {
                                    if(Objects.nonNull(CURRENT_CMD.get())){
                                        if(isFirst && Objects.equals(tempLine,CURRENT_CMD.get().getKey())){
                                            continue;
                                        }
                                    }

                                    lines.add(tempLine);
                                    if (firstOpen) {
                                        isFirst = false;
                                    } else {

                                        if (isFirst && pattern.matcher(tempLine).find()) {
                                            isFirst = false;
                                        } else if (!isFirst && pattern.matcher(tempLine).find() && !firstOpen) {
                                            eof = true;
//                                            READING_LOCK.unlock();
                                        } else {

                                        }
                                    }

                                }


                            }
                        }
                        if(!lines.isEmpty()){
                            if(firstOpen){
                                for (String line : lines){
                                    outputLineConsumer.accept(line);
                                }
                            }else{
                                outputLinesConsumer.accept(CURRENT_CMD.get().getValue(), lines, CURRENT_CMD.get().getKey());
                            }

                            lines.clear();
                        }

                        if (!eof && firstOpen) {
                            break;
                        }
                    }
                    if(firstOpen){
                        firstOpen = false;
                    }
                    Thread.sleep(100);
                    try {
                        if(READING_LOCK.tryLock()){
                            READING_LOCK.unlock();
                        }else{
                            READING_LOCK.unlock();
                        }
                    } catch (Exception e) {
                        log.error("error", e);
                    }
//                    READING_LOCK.unlock();
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
        CMD_QUEUE.offer(new KeyValuePair<>(id, command));
    }

    public synchronized void command(String command) throws JSchException, IOException {
        checkConnection();
        READING_LOCK.lock();
        PrintWriter printWriter = new PrintWriter(this.outputStream);
        printWriter.println(command);
        printWriter.flush();
    }

    public void pwd() {
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

        public JschShellTerminal2 build() throws JSchException, IOException {
            return new JschShellTerminal2(this);
        }
    }

    private static String cleanAnsi(String str) {
        return str.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
    }

    public static class CmdRes {
        private String id;
        private String cmd;
        private boolean isPass;
        private List<String> lines;
    }
}
