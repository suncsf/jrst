package com.braisefish.jrst.utils;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class WkhtmltoUtil {
    private static String getWkhtmltoPdfToolPath() {
        String path = SimpleFileUtil.initPhysicsDir("plugin" + File.separator + "wkhtmltopdf.exe");
        if (!FileUtil.exist(path)) {
            var inputStream = ResourceUtil.getStream("wkhtmltopdf.exe");
            FileUtil.writeFromStream(inputStream, path);
        }
        return path;
    }

    /**
     * 纵向打印
     *
     * @param htmlStr html内容
     * @return pdf文件
     * @throws IOException 异常
     */
    public static SimpleFileUtil quickHtmlToPdfConvertPortrait(String htmlStr) throws IOException {
        return WkhtmltoUtil.quickHtmlToPdfConvert(htmlStr, null);
    }

    /**
     * 横向打印
     *
     * @param htmlStr html内容
     * @return pdf文件
     * @throws IOException 异常
     */
    public static SimpleFileUtil quickHtmlToPdfConvertLandscape(String htmlStr) throws IOException {
        return WkhtmltoUtil.quickHtmlToPdfConvert(htmlStr, List.of("--orientation Landscape"));
    }

    /**
     * html转pdf
     *
     * @param htmlStr    html内容
     * @param appendArgs 追加参数
     * @return pdf文件
     * @throws IOException 异常
     */
    public static SimpleFileUtil quickHtmlToPdfConvert(String htmlStr, List<String> appendArgs) throws IOException {
        String id = UUID.randomUUID().toString(true);
        SimpleFileUtil sourceHtml = new SimpleFileUtil.Builder()
                .setDir("temp" + File.separator + "wkhtmltopdf" + File.separator + "html")
                .setCreateDir(true)
                .setCreateFile(true, id)
                .setExt(".html").build();
        sourceHtml.writeString(htmlStr);
        SimpleFileUtil toPdf = new SimpleFileUtil.Builder()
                .setDir("temp" + File.separator + "wkhtmltopdf" + File.separator + "pdf")
                .setCreateDir(true)
                .setCreateFile(true, id)
                .setExt(".pdf").build();
        if (WkhtmltoUtil.convert(sourceHtml.getPhysicsFilePath(), toPdf.getPhysicsFilePath(), appendArgs)) {
            return toPdf;
        }
        return null;
    }

    /**
     * html转pdf
     *
     * @param srcPath  html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath pdf保存路径
     * @return 转换成功返回true
     */
    public static boolean convert(String srcPath, String destPath) throws IOException {
        SimpleFileUtil simpleFileUtil = new SimpleFileUtil.Builder()
                .setDir("plugin")
                .setFileName("wkhtmltopdf")
                .setExt(".exe")
                .setCreateFile(false)
                .setCreateDir(false)
                .build();
        if (simpleFileUtil.exist()) {
            return convert(srcPath, destPath, simpleFileUtil.getPhysicsFilePath(), null);
        }
        return false;

    }

    /**
     * html转pdf
     *
     * @param srcPath    html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath   pdf保存路径
     * @param appendArgs
     * @return 转换成功返回true
     */
    public static boolean convert(String srcPath, String destPath, List<String> appendArgs) throws IOException {
        String wkhtmltoPdfToolPath = getWkhtmltoPdfToolPath();
        if (FileUtil.exist(wkhtmltoPdfToolPath)) {
            return convert(srcPath, destPath, wkhtmltoPdfToolPath, appendArgs);
        }
        return false;

    }

    /**
     * html转pdf
     *
     * @param srcPath   html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath  pdf保存路径
     * @param toPdfTool 工具地址
     * @return 转换成功返回true
     */
    public static boolean convert(String srcPath, String destPath, String toPdfTool) {
        return convert(srcPath, destPath, toPdfTool, null);
    }

    /**
     * @param srcPath    html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath   pdf保存路径
     * @param toPdfTool  工具地址
     * @param appendArgs 追加参数
     * @return 转换成功返回true
     */
    public static boolean convert(String srcPath, String destPath, String toPdfTool, List<String> appendArgs) {
        File file = new File(destPath);
        File parent = file.getParentFile();
        //如果pdf保存路径不存在，则创建路径
        if (!parent.exists()) {
            parent.mkdirs();
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append(toPdfTool);
        cmd.append(" ");
        //设置页面上边距 (default 10mm)
        cmd.append(" --margin-top 5mm ");
        // (设置页眉和内容的距离,默认0)
        cmd.append(" --header-spacing 5 ");
        cmd.append(" --footer-font-name 宋体 ");
        cmd.append(" --footer-font-size 8 ");
        //设置在中心位置的页脚内容
        cmd.append(" --footer-center 通信诊断工具V2.0");
        cmd.append(" --disable-smart-shrinking ");
        // (设置页脚和内容的距离)
        cmd.append(" --footer-spacing 5 ");
        cmd.append(" --no-stop-slow-scripts ");
        if (Objects.nonNull(appendArgs) && !appendArgs.isEmpty()) {
            for (String appendArg : appendArgs) {
                cmd.append(" ").append(appendArg).append(" ");
            }
        }
        cmd.append("\"").append(srcPath).append("\"");
        cmd.append(" ");
        cmd.append("\"").append(destPath).append("\"");
        boolean result = true;
        try {
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "Error");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "Output");
            errorGobbler.start();
            outputGobbler.start();

            proc.waitFor();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 类名：StreamGobbler
     *
     * @author sunchao
     * 创建时间：2023/8/30 10:47
     * 最后修改时间：2023/8/30 10:47
     * 版本：1.0.0
     * 描述：
     */
    private static class StreamGobbler extends Thread {
        private final Logger log = LoggerFactory.getLogger(StreamGobbler.class);
        InputStream is;
        String type;

        public StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if ("Error".equals(type)) {
                        log.error(line);
                    } else {
                        log.debug(line);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
