import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.SshjSftp;
import com.braisefish.jrst.lang.JrstCommonException;
import com.braisefish.jrst.utils.BuildHelper;
import com.braisefish.jrst.utils.JsonUtils;
import com.braisefish.jrst.utils.SimpleFileUtil;
import com.braisefish.jrst.utils.html.HtmlTagUtil;
import com.braisefish.jrst.utils.html.HtmlUtil;
import com.braisefish.jrst.utils.jsch.JschShellUtil;
import com.braisefish.jrst.utils.str.DynamicStringBuilder;
import com.braisefish.jrst.utils.thread.JrstThread;
import com.braisefish.jrst.utils.verify.code.IVerifyCodeEntry;
import com.braisefish.jrst.utils.verify.code.VerifyCodeInput;
import com.braisefish.jrst.utils.verify.code.VerifyCodeOutput;
import com.braisefish.jrst.utils.verify.code.VerifyCodeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AppTest {
    private final static Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test() throws IOException {
        HtmlTagUtil htmlTagUtil = new HtmlTagUtil.Builder()
                .setTagName("div")
                .setTagStyle("color: red;")
                .addTagAttr("id", "test")
                .addTagAttr("class", "test")
                .setAfterText("这是外层开始文本")
                .setBeforeText("这是外层结束文本")
                .build();
        htmlTagUtil.addChildTag(
                new HtmlTagUtil.Builder()
                        .setTagName("p")
                        .setTagStyle("color: green;")
                        .addTagAttr("id", "test2")
                        .addTagAttr("class", "test2")
                        .setAfterText("这是内层开始文本")
                        .setBeforeText("这是内层结束文本")
                        .build()
        );
        System.out.println(htmlTagUtil.toStr());
        var htmlUtil = new HtmlUtil.Builder()
                .setTitle("测试")
                .addStyle("body {color: red;}")
                .addScript("alert('hello world');")
                .setContent(htmlTagUtil.toStr())
                .build();
        htmlUtil.toStr(htmlTagUtil.toStr());
        System.out.println(htmlUtil.toStr());
        var htmlPath = SimpleFileUtil.initPhysicsDir("temp" + File.separator + UUID.randomUUID().toString(true) + "index.html");
        FileUtil.writeUtf8String(htmlUtil.toStr(), htmlPath);
        Desktop.getDesktop().open(new File(htmlPath));
    }


    @Test
    public void stringBuilderTest() throws IOException, JrstCommonException {
        SimpleFileUtil.registerBaseDir("mgnt");
        DynamicStringBuilder dynamicStringBuilder = new DynamicStringBuilder();
        dynamicStringBuilder.appendLine("<html>")
                .appendLine("<head>")
                .appendLine("<title>")
                .appendLine("测试")
                .appendLine("</title>")
                .appendLine("</head>")
                .appendLine("<body>")
                .appendLine("<div>")
                .appendLine("<p style=\"color: red;\">")
                .append("这是内层开始文本")
                .lineBreak()
                .appendLine("这是内层结束文本")
                .appendLine("</p>")
                .appendLine("</div>")
                .appendLine("</body>")
                .appendLine("</html>");
        System.out.println(dynamicStringBuilder);

        var htmlPath = SimpleFileUtil.initPhysicsDir("temp" + File.separator + UUID.randomUUID().toString(true) + "index.html");
        FileUtil.writeUtf8String(dynamicStringBuilder.toString(), htmlPath);
        Desktop.getDesktop().open(new File(htmlPath));
    }


    @Test
    public void threadTest() {
        BuildHelper.ofExecGet(new JrstThread(isStop -> {
            while (!isStop.get()) {
                try {
                    System.out.println("线程执行");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("线程被中断");
                }
            }
        }), (jrstThread) -> {
            try {
                jrstThread.start();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("线程被中断");
            } finally {
                if (jrstThread.isAlive()) {
                    jrstThread.stopThread();
                }
            }
        });

    }

    @Test
    public void jschTest() {

    }

    @Test
    public void verifyCodeTest() throws JrstCommonException, JsonProcessingException {
        VerifyCodeUtil.registerClearExpireVerifyCodeTimer();
        VerifyCodeInput verifyCodeInput;
        VerifyCodeOutput verifyCodeOutput;
        for (int i = 0; i < 10; i++) {
            verifyCodeInput = new VerifyCodeInput();
            verifyCodeInput.setVerifyCodeKey(UUID.randomUUID().toString(true));
            verifyCodeInput.setCodeCount(4);
            verifyCodeInput.setHeight(40);
            verifyCodeInput.setWidth(120);
            verifyCodeInput.setCircleCount(4);

            verifyCodeOutput = VerifyCodeUtil.createVerifyCode(verifyCodeInput);
        }
        verifyCodeInput = new VerifyCodeInput();
        verifyCodeInput.setVerifyCodeKey(UUID.randomUUID().toString(true));
        verifyCodeInput.setCodeCount(4);
        verifyCodeInput.setHeight(40);
        verifyCodeInput.setWidth(120);
        verifyCodeInput.setCircleCount(4);

        verifyCodeOutput = VerifyCodeUtil.createVerifyCode(verifyCodeInput);
        log.info("验证码json:{}", JsonUtils.toJson(verifyCodeOutput));
        VerifyCodeOutput finalVerifyCodeOutput = verifyCodeOutput;
        VerifyCodeUtil.verifyCode(new IVerifyCodeEntry() {
            @Override
            public String getVerifyCode() {
                return finalVerifyCodeOutput.getCode();
            }

            @Override
            public String getVerifyCodeKey() {
                return finalVerifyCodeOutput.getUid();
            }
        });
        ObjectMapper objectMapper = JsonUtils.getObjectMapper();
        log.info("验证码json:{}", objectMapper.writeValueAsString(verifyCodeOutput));
    }

    @Test
    public void sshTest() throws Exception {
//        var session = JschUtil.createSession("192.168.56.104", 22, "sysadm", "goldwind@32365");
//        JschShellUtil jschShellUtil = new JschShellUtil.Builder()
//                .setSession(session)
//                .build();
//        var text = jschShellUtil.executeAndReadText("cd /");
//        log.info("text:{}", text);
//        text = jschShellUtil.executeAndReadText("ls");
//        log.info("text:{}", text);
//        text = jschShellUtil.executeAndReadText("cd /etc");
//        text = jschShellUtil.executeAndReadText("pwd");
//        log.info("text:{}", text);


        JschShellUtil jschShellUtil = new JschShellUtil.Builder()
                .setSession(JschUtil.createSession("192.168.56.101", 22, "sysadm", "goldwind@32365"))
                .build();
        var lines =    jschShellUtil.readOriginalOutput();
        log.info("lines:{}", lines);
         lines =  jschShellUtil.executeAndRead("cd /tmp");
        log.info("lines:{}", lines);
         lines = jschShellUtil.executeAndRead("find /tmp -maxdepth 1 -type f -size 0 -delete");
        log.info("lines:{}", lines);
    }
}
