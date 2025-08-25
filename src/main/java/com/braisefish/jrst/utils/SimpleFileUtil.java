package com.braisefish.jrst.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.lang.JrstCommonException;
import com.braisefish.jrst.lang.JwtAuthorazationException;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * @author sunchao
 * @date 2025/08/25 11:05:00
 */
public class SimpleFileUtil {
    private static String baseDir;
    private Builder builder;
    private String basePath;
    private String relativeDirPath;
    private String relativeFilePath;
    private String physicsDirPath;
    private String physicsFilePath;

    public String getPhysicsFilePath() {
        return physicsFilePath;
    }

    public String getPhysicsDirPath() {
        return physicsDirPath;
    }

    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    public String getRelativeDirPath() {
        return relativeDirPath;
    }

    public String getBasePath() {
        return basePath;
    }

    public static void registerBaseDir(String baseDir) throws IOException, JrstCommonException {
        if(StrUtil.isNotBlank(SimpleFileUtil.baseDir)){
            throw new JrstCommonException("已存在baseDir");
        }
        if (StrUtil.isBlank(baseDir)) {
            throw new IOException("请设置baseDir");
        }
        SimpleFileUtil.baseDir = File.separator + baseDir;
    }

    public static String getBaseDir() {
        if (StrUtil.isBlank(baseDir)) {
            return File.separator + "default_dir";
        }
        return baseDir;
    }

    public static String getBasePhysicsDir() {
        return System.getProperty("user.dir") + getBaseDir();
    }



    private SimpleFileUtil() {
    }

    public SimpleFileUtil(Builder builder) throws IOException {
        this.builder = builder;
        this.basePath = System.getProperty("user.dir");
        this.relativeDirPath = getBaseDir() + builder.getDir();
        this.physicsDirPath = this.basePath + this.relativeDirPath;
        if (builder.getCreateDir()) {
            var dir = new File(this.physicsDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        final String ext = StrUtil.isNotBlank(builder.getExt()) ? builder.getExt() : "";
        final String fileName = StrUtil.replace(builder.getFileName(), ext, "");
        this.relativeFilePath = this.relativeDirPath + File.separator + fileName + ext;
        this.physicsFilePath = this.physicsDirPath + File.separator + fileName + ext;
        if (builder.getCreateFile()) {
            var file = new File(this.physicsFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        }
    }

    public String getFileName() {
        return builder.getFileName();
    }

    public String getExt() {
        return builder.getExt();
    }

    public void writeString(String content) {
        FileUtil.writeString(content, this.physicsFilePath, CharsetUtil.UTF_8);
    }

    public void writeFromStream(InputStream inputStream) {
        FileUtil.writeFromStream(inputStream, this.physicsFilePath);
    }

    public BufferedInputStream getInputStream() {
        return FileUtil.getInputStream(this.physicsFilePath);
    }

    public BufferedOutputStream getOutputStream() {
        return FileUtil.getOutputStream(this.physicsFilePath);
    }

    public String readString() {
        return FileUtil.readString(this.getPhysicsFilePath(), CharsetUtil.UTF_8);
    }

    public List<String> readLines() {
        return FileUtil.readLines(this.getPhysicsFilePath(), CharsetUtil.UTF_8);
    }

    public void readLines(byte[] bytes) {
        FileUtil.writeBytes(bytes, CharsetUtil.UTF_8);
    }

    public void readLines(List<String> list) {
        FileUtil.writeLines(list, this.getPhysicsFilePath(), CharsetUtil.UTF_8);
    }

    public boolean del() {
        return FileUtil.del(this.getPhysicsFilePath());
    }

    public boolean exist() {
        return FileUtil.exist(this.getPhysicsFilePath());
    }

    public boolean createNewFile() throws IOException {
        return toFile().createNewFile();
    }

    public boolean mkdirs() {
        return FileUtil.file(this.getPhysicsFilePath()).mkdirs();
    }

    public boolean existsDir() {
        return FileUtil.file(this.getPhysicsDirPath()).exists();
    }

    public File toFile() {
        return FileUtil.file(this.getPhysicsFilePath());
    }

    public String getCreateUserId() {
        return builder.getCreateUserId();
    }

    public boolean hasBuilderInputStreamData() {
        return Objects.nonNull(this.builder.getInputStream());
    }

    public void writeBuilderInputStreamData() throws IOException {
        if (!builder.getCreateDir()) {
            var dir = new File(this.physicsDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        if (!builder.getCreateFile()) {
            var dir = new File(this.physicsFilePath);
            if (!dir.exists()) {
                dir.createNewFile();
            }
        }
        FileUtil.writeFromStream(this.builder.getInputStream(), FileUtil.file(this.getPhysicsFilePath()));
    }


    public static void openFolderByDesktop(String folderPath) {

        // 创建文件夹对象
        File folder = new File(folderPath);
        // 检查Desktop是否支持打开URL
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            System.out.println("Desktop 不支持打开文件夹");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据相对路径地址反推物理路径及加载数据
     *
     * @param relativeFilePath
     * @return
     * @throws IOException
     */
    public static SimpleFileUtil loadRelativeFilePath(String relativeFilePath) throws IOException {
        if (StrUtil.isBlank(relativeFilePath)) {
            return null;
        }
        if (relativeFilePath.contains("/") && !Objects.equals("/", File.separator)) {
            relativeFilePath = StrUtil.replace(relativeFilePath, "/", File.separator);
        }
        if (relativeFilePath.contains("\\") && !Objects.equals("\\", File.separator)) {
            relativeFilePath = StrUtil.replace(relativeFilePath, "\\", File.separator);
        }
        String relativeDirPath = StrUtil.replace(
                relativeFilePath,
                File.separator + FileUtil.getName(relativeFilePath),
                ""
        );
        final String ext = "." + FileUtil.getSuffix(relativeFilePath);
        final String fileName = StrUtil.replace(FileUtil.getName(relativeFilePath), ext, "");
        return new Builder()
                .setDir(
                        StrUtil.replace(relativeDirPath, getBaseDir(), "")
                )
                .setCreateDir(false)
                .setFileName(fileName)
                .setExt(ext)
                .build();
    }

    public static String initPhysicsDir(String dir) {
        if (StrUtil.isBlank(dir)) {
            return getBasePhysicsDir();
        }
        if (StrUtil.startWith(dir, getBasePhysicsDir())) {
            return dir;
        }
        if (StrUtil.startWith(dir, getBaseDir())) {
            dir = StrUtil.replace(dir, getBaseDir(), "");
        }
        return getBasePhysicsDir() + (StrUtil.startWith(dir, File.separator) ? dir : (File.separator + dir));
    }

    public static String initDir(String dir) {
        if (StrUtil.isBlank(dir)) {
            return getBaseDir();
        }
        if (StrUtil.startWith(dir, getBaseDir())) {
            return dir;
        }
        return getBaseDir() + (StrUtil.startWith(dir, File.separator) ? dir : (File.separator + dir));
    }

    public static String appendDir(String dir, String appendDir) {
        if (StrUtil.isBlank(dir)) {
            return getBaseDir() + (StrUtil.startWith(appendDir, File.separator) ? appendDir : (File.separator + appendDir));
        }
        if (StrUtil.startWith(dir, getBaseDir())) {
            return dir + (StrUtil.startWith(appendDir, File.separator) ? appendDir : (File.separator + appendDir));
        }
        return getBaseDir() + (StrUtil.startWith(dir, File.separator) ? dir : (File.separator + dir)) + (StrUtil.startWith(appendDir, File.separator) ? appendDir : (File.separator + appendDir));
    }

    public static String loadRelativeDirPathToPhysicsDirPath(String relativeDirPath) {
        if (StrUtil.isBlank(relativeDirPath)) {
            return null;
        }

        if (relativeDirPath.contains("/") && !Objects.equals("/", File.separator)) {
            relativeDirPath = StrUtil.replace(relativeDirPath, "/", File.separator);
        }
        if (relativeDirPath.contains("\\") && !Objects.equals("\\", File.separator)) {
            relativeDirPath = StrUtil.replace(relativeDirPath, "\\", File.separator);
        }
        if (!StrUtil.startWith(relativeDirPath, File.separator)) {
            relativeDirPath = File.separator + relativeDirPath;
        }
        if (StrUtil.startWith(relativeDirPath, getBaseDir())) {
            relativeDirPath = StrUtil.replace(relativeDirPath, getBaseDir(), "");
        }
        return StrUtil.join(getBasePhysicsDir(), relativeDirPath);
    }


    public static class Builder {
        private String dir;
        private String fileName;
        private String ext;
        private String createUserId;
        private Boolean createDir = false;
        private Boolean createFile = false;
        private InputStream inputStream;

        public Builder setDir(String dir) {
            if (StrUtil.isBlank(dir)) {
                return this;
            }
            this.dir = (StrUtil.startWith(dir, File.separator) ? "" : File.separator) + dir;
            return this;
        }

        public Builder setExt(String ext) {
            if (StrUtil.isBlank(ext)) {
                return this;
            }
            if (!StrUtil.startWith(ext, ".")) {
                ext = "." + ext;
            }
            this.ext = ext;
            return this;
        }

        public Builder setCreateFile(Boolean createFile) {
            if (StrUtil.isBlank(fileName)) {
                this.setCreateFile(createFile, UUID.randomUUID().toString(true));
            }
            this.createFile = createFile;
            return this;
        }

        public Builder setCreateFile(Boolean createFile, String fileName) {
            this.setCreateDir(true);
            this.createFile = createFile;
            this.setFileName(fileName);
            return this;
        }

        public Builder appendDir(String dir) {
            if (StrUtil.isBlank(dir)) {
                return this;
            }
            if (Objects.isNull(this.dir)) {
                this.dir = "";
            }
            this.dir += File.separator + dir;
            return this;
        }

        public SimpleFileUtil build() throws IOException {
            return new SimpleFileUtil(this);
        }

        public String getDir() {
            return dir;
        }

        public String getFileName() {
            return fileName;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public String getExt() {
            return ext;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public Builder setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
            return this;
        }

        public Boolean getCreateDir() {
            return createDir;
        }

        public Builder setCreateDir(Boolean createDir) {
            this.createDir = createDir;
            return this;
        }

        public Boolean getCreateFile() {
            return createFile;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public Builder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }
    }
}
