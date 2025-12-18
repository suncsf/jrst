package com.braisefish.jrst.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.lang.JrstCommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author sunchao
 */
public class SimpleFileUtil {
    private static final Logger log = LoggerFactory.getLogger(SimpleFileUtil.class);
    /**
     * Base directory
     */
    private static String baseDir;
    /**
     * Base make
     */
    private static String baseMake;
    /**
     * Builder
     */
    private Builder builder;

    private String userDir;
    private String relativeDirPath;
    private String relativeFilePath;
    private String physicsDirPath;
    private String physicsFilePath;

    /**
     * Get physics file path
     */
    public String getPhysicsFilePath() {
        return physicsFilePath;
    }

    /**
     * Get physics directory path
     */
    public String getPhysicsDirPath() {
        return physicsDirPath;
    }

    /**
     * Get relative file path
     */
    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    /**
     * Get relative directory path
     */
    public String getRelativeDirPath() {
        return relativeDirPath;
    }

    /**
     * Get user directory
     */
    public String getUserDir() {
        return userDir;
    }

    /**
     * Register base directory
     */
    public static void registerBaseDir(String baseDir) throws IOException, JrstCommonException {
        if (StrUtil.isNotBlank(SimpleFileUtil.baseDir)) {
            throw new JrstCommonException("已存在baseDir");
        }
        if (StrUtil.isBlank(baseDir)) {
            throw new IOException("请设置baseDir");
        }
        SimpleFileUtil.baseDir = baseDir;
        SimpleFileUtil.baseDir = File.separator + baseDir;
    }

    /**
     * Get base directory
     */
    public static String getBaseDir() {
        if (StrUtil.isBlank(baseDir)) {
            return File.separator + "default_dir";
        }
        return baseDir;
    }

    /**
     * Get base physics directory
     */
    public static String getBasePhysicsDir() {
        return System.getProperty("user.dir") + getBaseDir();
    }


    /**
     * Builder
     */
    private SimpleFileUtil() {
    }

    /**
     * Builder
     */
    public SimpleFileUtil(Builder builder) throws IOException {
        this.builder = builder;
        this.userDir = System.getProperty("user.dir");
        this.relativeDirPath = getBaseDir() + builder.getDir();
        this.physicsDirPath = this.userDir + this.relativeDirPath;
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

    /**
     * Get file name
     *
     * @return File name
     */
    public String getFileName() {
        return builder.getFileName();
    }

    /**
     * Get file extension
     *
     * @return File extension
     */
    public String getExt() {
        return builder.getExt();
    }

    /**
     * Write string
     *
     * @param content String
     */
    public void writeString(String content) {
        FileUtil.writeString(content, this.physicsFilePath, CharsetUtil.UTF_8);
    }

    /**
     * Write from stream
     *
     * @param inputStream Input stream
     */
    public void writeFromStream(InputStream inputStream) {
        FileUtil.writeFromStream(inputStream, this.physicsFilePath);
    }

    /**
     * Get input stream
     *
     * @return Input stream
     */
    public BufferedInputStream getInputStream() {
        return FileUtil.getInputStream(this.physicsFilePath);
    }

    /**
     * Get output stream
     *
     * @return Output stream
     */
    public BufferedOutputStream getOutputStream() {
        return FileUtil.getOutputStream(this.physicsFilePath);
    }

    /**
     * Read string
     *
     * @return String
     */
    public String readString() {
        return FileUtil.readUtf8String(this.getPhysicsFilePath());
    }

    /**
     * Read lines
     *
     * @return List of lines
     */
    public List<String> readLines() {
        return FileUtil.readLines(this.getPhysicsFilePath(), CharsetUtil.UTF_8);
    }

    /**
     * Read lines
     *
     * @param bytes Bytes
     */
    public void readLines(byte[] bytes) {
        FileUtil.writeBytes(bytes, CharsetUtil.UTF_8);
    }

    /**
     * Read lines
     *
     * @param list List of lines
     */
    public void readLines(List<String> list) {
        FileUtil.writeLines(list, this.getPhysicsFilePath(), CharsetUtil.UTF_8);
    }

    /**
     * Delete file
     *
     * @return true if the file is deleted successfully, false otherwise
     */
    public boolean del() {
        return FileUtil.del(this.getPhysicsFilePath());
    }

    /**
     * Whether the file exists
     *
     * @return true if the file exists, false otherwise
     */
    public boolean exist() {
        return FileUtil.exist(this.getPhysicsFilePath());
    }

    /**
     * Create a new file
     *
     * @return true if the file is created successfully, false otherwise
     */
    public boolean createNewFile() throws IOException {
        return toFile().createNewFile();
    }

    /**
     * Create directory
     *
     * @return true if the directory is created successfully, false otherwise
     */
    public boolean mkdirs() {
        return FileUtil.file(this.getPhysicsFilePath()).mkdirs();
    }

    /**
     * Whether the directory exists
     *
     * @return true if the directory exists, false otherwise
     */
    public boolean existsDir() {
        return FileUtil.file(this.getPhysicsDirPath()).exists();
    }

    /**
     * Convert to File
     */
    public File toFile() {
        return FileUtil.file(this.getPhysicsFilePath());
    }

    /**
     * Get the user ID of the file creator
     */
    public String getCreateUserId() {
        return builder.getCreateUserId();
    }

    /**
     * Whether there is builder input stream data
     */
    public boolean hasBuilderInputStreamData() {
        return Objects.nonNull(this.builder.getInputStream());
    }

    /**
     * Write builder input stream data
     *
     * @throws IOException
     */
    public void writeBuilderInputStreamData() throws IOException {
        if (!builder.getCreateDir()) {
            var dir = new File(this.physicsDirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Directory creation failed");
                }
                log.info("Directory created successfully");
            }
        }
        if (!builder.getCreateFile()) {
            var dir = new File(this.physicsFilePath);
            if (!dir.exists()) {
                if (!dir.createNewFile()) {
                    throw new IOException("File creation failed");
                }
                log.info("File created successfully");
            }
        }
        FileUtil.writeFromStream(this.builder.getInputStream(), FileUtil.file(this.getPhysicsFilePath()));
    }

    /**
     * Open folder by desktop
     *
     * @param folderPath Path of the folder to open
     */
    public static void openFolderByDesktop(String folderPath) {
        File folder = new File(folderPath);
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.info("Desktop Cannot open folder");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(folder);
        } catch (IOException e) {
            log.error("open folder error", e);
        }
    }

    /**
     * Derive physical paths and load data based on relative path addresses
     *
     * @param relativeFilePath relative path
     * @return SimpleFileUtil  instance
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

    public static String loadPhysicsToRelative(String physicsPath) throws IOException {
        if (!FileUtil.exist(physicsPath)) {
            return physicsPath;
        }
        File file = new File(physicsPath);
        var filePath = file.getCanonicalPath();
        return StrUtil.replace(filePath, getBasePhysicsDir(), "");
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

    public static File createTempFile() {
        return createTempFile(".temp", true);
    }

    public static File createTempFile(String suffix, boolean isDeleteOnExit) {
        return createTempFile(suffix, null, isDeleteOnExit);
    }

    public static File createTempFile(String suffix, String dir, boolean isDeleteOnExit) {
        return createTempFile(SimpleFileUtil.baseDir + "_", suffix, dir, isDeleteOnExit);
    }

    public static File createTempFile(String prefix, String suffix, String dir, boolean isDeleteOnExit) {
        return FileUtil.createTempFile(prefix, suffix, new File(createTempDir(dir)), isDeleteOnExit);
    }

    public static String createTempDir() {
        return initPhysicsDir("temp");
    }

    public static String createTempDir(String dir) {
        return initPhysicsDir("temp" + (StrUtil.isNotBlank(dir) ? (File.separator + dir) : ""));
    }
    public static String combine(String... paths) {
        if (paths == null) {
            throw new IllegalArgumentException("Paths array cannot be null");
        }

        String[] components = Arrays.stream(paths)
                .filter(p -> p != null && !p.isEmpty())
                .toArray(String[]::new);

        if (components.length == 0) {
            return "";
        } else if (components.length == 1) {
            return components[0];
        }

        return String.join(File.separator, components);
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
