package com.braisefish.jrst.utils.jsch;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.ChannelType;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.experimental.Accessors;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class JschShellUtil {
    public static final String ECHO_EOF_MAKE = "P_EOF";
    public static final String ECHO_STSP_EOF_CMD_APPEND = ";echo \"" + ECHO_EOF_MAKE + "\"";
    public static final String ECHO_STSP_EOF_CMD = "echo \"" + ECHO_EOF_MAKE + "\"";
    private Session session;
    private ChannelShell channel;
    private InputStream inputStream;
    private OutputStream outputStream;

    public void setSession(Session session) {
        this.session = session;
    }

    public void setChannel(ChannelShell channel) {
        this.channel = channel;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private JschShellUtil(Builder builder) throws JSchException, IOException {
        this.session = builder.session;
        if (!session.isConnected()) {
            session.connect();
        }
        this.channel = (ChannelShell) session.openChannel(ChannelType.SHELL.getValue());
        this.inputStream = channel.getInputStream();
        this.outputStream = channel.getOutputStream();
        channel.connect();
    }

    /**
     * 执行命令
     *
     * @param command 命令
     * @throws IOException 命令执行异常
     */
    public void execute(String command) throws IOException {
        var cmd = command + ECHO_STSP_EOF_CMD_APPEND + "\n";
        outputStream.write((cmd).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * 执行命令
     *
     * @param command 命令
     * @throws IOException 命令执行异常
     */
    public void executeOriginal(String command) throws IOException {
        var cmd = command + "\n";
        outputStream.write((cmd).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * 读取并返回结果
     *
     * @param command 命令
     * @return 读取结果
     * @throws IOException 读取异常
     */
    public List<String> executeAndRead(String command) throws IOException {
        return executeAndRead(command, true, true);
    }

    /**
     * 读取并返回结果
     *
     * @param command    命令
     * @param clearFirst 清理首行
     * @return 读取结果
     * @throws IOException 读取异常
     */
    public List<String> executeAndRead(String command, boolean clearFirst) throws IOException {
        return executeAndRead(command, clearFirst, false);
    }

    /**
     * 读取并返回结果
     *
     * @param command       命令
     * @param clearFirst    清理首行
     * @param clearHostLine 清理主机行
     * @return 读取结果
     * @throws IOException 读取异常
     */
    public List<String> executeAndRead(String command, boolean clearFirst, boolean clearHostLine) throws IOException {
        execute(command);
        return readLines(clearFirst, clearHostLine);
    }

    /**
     * 读取并返回结果
     *
     * @param command 命令
     * @return 返回输出内容
     * @throws IOException 读取异常
     */
    public String executeAndReadText(String command) throws IOException {
        return executeAndReadText(command, true, true);
    }

    /**
     * 读取并返回结果
     *
     * @param command    命令
     * @param clearFirst 清理首行
     * @return 返回输出内容
     * @throws IOException 读取异常
     */
    public String executeAndReadText(String command, boolean clearFirst) throws IOException {
        return executeAndReadText(command, clearFirst, false);
    }

    /**
     * 读取并返回结果
     *
     * @param command       命令
     * @param clearFirst    清理首行
     * @param clearHostLine 清理主机行
     * @return 返回输出内容
     * @throws IOException 读取异常
     */
    public String executeAndReadText(String command, boolean clearFirst, boolean clearHostLine) throws IOException {
        var lines = executeAndRead(command, clearFirst, clearHostLine);
        return String.join("\n", lines);
    }

    /**
     * 读取并返回结果
     *
     * @param clearFirst    清理首行
     * @param clearHostLine 清理主机行
     * @return 读取结果
     * @throws IOException 读取异常
     */
    public List<String> readLines(boolean clearFirst, boolean clearHostLine) throws IOException {
        AtomicBoolean first = new AtomicBoolean(true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> theLines = new ArrayList<>();
        String theLine;

        while ((theLine = reader.readLine()) != null) {
            theLine = cleanAnsi(theLine);
            theLine = StrUtil.replace(theLine, ECHO_STSP_EOF_CMD_APPEND, "");
            if (StrUtil.endWith(theLine, ECHO_EOF_MAKE)) {
                theLine = theLine.replace(ECHO_EOF_MAKE, "");
                theLines.add(theLine);
                break;
            }
            theLines.add(theLine);
        }
        Pattern pattern = Pattern.compile(";(\\n+)?(\\s+)?e(\\n+)?(\\s+)?c(\\n+)?(\\s+)?h(\\n+)?(\\s+)?o(\\n+)?(\\s+)?\"(\\n+)?(\\s+)?P(\\n+)?(\\s+)?_(\\n+)?(\\s+)?E(\\n+)?(\\s+)?O(\\n+)?(\\s+)?F(\\n+)?(\\s+)?\"");
        String content = StrUtil.join("\n", theLines);
        var matcher = pattern.matcher(content);
        if (matcher.find()) {
            var text = matcher.group();
            content = StrUtil.replace(content, text, "");
        }
        theLines = StrUtil.split(content, "\n");
        var lines = new ArrayList<String>();
        for (var line : theLines) {
            if (first.get() && clearFirst) {
                first.set(false);
                continue;
            }
            if (clearHostLine && StrUtil.contains(line, "[") && StrUtil.contains(line, "@")
                    && StrUtil.contains(line, "]")
                    && (StrUtil.contains(line, "$") || StrUtil.contains(line, "#") || StrUtil.contains(line, ">"))) {
                continue;
            }
            line = cleanAnsi(line);
            lines.add(line);
        }

        return lines;
    }



    public static List<String> clearLines(boolean clearFirst, boolean clearHostLine, List<String> theLines) {
        AtomicBoolean first = new AtomicBoolean(true);
        Pattern pattern = Pattern.compile(";(\\n+)?(\\s+)?e(\\n+)?(\\s+)?c(\\n+)?(\\s+)?h(\\n+)?(\\s+)?o(\\n+)?(\\s+)?\"(\\n+)?(\\s+)?P(\\n+)?(\\s+)?_(\\n+)?(\\s+)?E(\\n+)?(\\s+)?O(\\n+)?(\\s+)?F(\\n+)?(\\s+)?\"");
        String content = StrUtil.join("\n", theLines);
        var matcher = pattern.matcher(content);
        if (matcher.find()) {
            var text = matcher.group();
            content = StrUtil.replace(content, text, "");
        }
        theLines = StrUtil.split(content, "\n");
        var lines = new ArrayList<String>();
        for (var line : theLines) {
            if (first.get() && clearFirst) {
                first.set(false);
                continue;
            }
            if (clearHostLine && StrUtil.contains(line, "[") && StrUtil.contains(line, "@")
                    && StrUtil.contains(line, "]")
                    && (StrUtil.contains(line, "$") || StrUtil.contains(line, "#") || StrUtil.contains(line, ">"))) {
                continue;
            }
            line = cleanAnsi(line);
            lines.add(line);
        }

        return lines;
    }

    /**
     * 读取原始输出,无结束标识
     *
     * @return 输出内容
     * @throws Exception 读取异常
     */
    public List<String> readOriginalOutput() throws Exception {
        byte[] buffer = new byte[4096];
        int bytesRead;
        List<String> output = new ArrayList<>();
        while (inputStream.available() > 0 && (bytesRead = inputStream.read(buffer)) > 0) {
            var str = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            output.add(str);
        }
        return output;
    }


    private static String cleanAnsi(String str) {
        return str.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
    }

    public static class Builder {

        private Session session;

        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        public JschShellUtil build() throws JSchException, IOException {
            return new JschShellUtil(this);
        }
    }
}
