import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.extra.ssh.JschUtil;
import com.braisefish.jrst.lang.JrstCommonException;
import com.braisefish.jrst.utils.BuildHelper;
import com.braisefish.jrst.utils.SimpleFileUtil;
import com.braisefish.jrst.utils.html.HtmlTagUtil;
import com.braisefish.jrst.utils.html.HtmlUtil;
import com.braisefish.jrst.utils.str.DynamicStringBuilder;
import com.braisefish.jrst.utils.thread.JrstThread;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AppTest {

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
    public void threadTest() throws IOException, JrstCommonException, InterruptedException {
        BuildHelper.ofExecGet(new JrstThread(isStop -> {
            while (!isStop.get()) {
                try {
                    System.out.println("线程执行");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("线程被中断");
                }
            }
        }),(jrstThread)->{
            try {
                jrstThread.start();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("线程被中断");
            }finally {
                if(jrstThread.isAlive()){
                    jrstThread.stopThread();
                }
            }
        });

    }

    @Test
    public void jschTest() throws IOException, JrstCommonException {

    }
}
