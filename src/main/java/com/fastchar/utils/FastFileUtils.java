package com.fastchar.utils;


import com.fastchar.core.FastChar;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * from org.apache.commons.io
 */
public class FastFileUtils {

    public static boolean isImageFile(String fileName) {
        String[] extensions = new String[]{
                ".jpeg",
                ".jpg",
                ".bmp",
                ".gif",
                ".dib",
                ".rle",
                ".jpe",
                ".jif",
                ".pcx",
                ".dcx",
                ".pic",
                ".png",
                ".tga",
                ".tif",
                ".wmf",
                ".jfif"};
        return isTargetFile(fileName, extensions);
    }


    public static boolean isMP4File(String fileName) {
        return isTargetFile(fileName, ".mp4");
    }

    public static boolean isMOVFile(String fileName) {
        return isTargetFile(fileName, ".mov");
    }

    public static boolean isAVIFile(String fileName) {
        return isTargetFile(fileName, ".avi");
    }

    public static boolean isTxtFile(String fileName) {
        return isTargetFile(fileName, ".txt");
    }

    public static boolean isExcelFile(String fileName) {
        return isTargetFile(fileName, ".xlsx", ".xls");
    }

    public static boolean isWordFile(String fileName) {
        return isTargetFile(fileName, ".docx", ".doc");
    }

    public static boolean isPPTFile(String fileName) {
        return isTargetFile(fileName, ".pptx", ".ppt");
    }

    public static boolean isPDFFile(String fileName) {
        return isTargetFile(fileName, ".pdf");
    }



    public static boolean isImageFileByMimeType(String mimeType) {
        String[] mimeTypes = new String[]{
                "image/bmp",
                "image/gif",
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/pipeg",
                "image/tiff",
                "image/x-icon",
                "image/tiff",
                "image/svg+xml"
        };

        return isTargetFileByMimeType(mimeType, mimeTypes);
    }
    public static boolean isMP4FileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "video/mp4"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }

    public static boolean isMOVFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "video/quicktime"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }


    public static boolean isAVIFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "video/x-msvideo"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }

    public static boolean isTxtFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "text/plain"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }
    public static boolean isExcelFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }
    public static boolean isWordFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }

    public static boolean isPPTFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }

    public static boolean isPDFFileByMimeType(String mimeType) {
        String[] extensions = new String[]{
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        };
        return isTargetFileByMimeType(mimeType, extensions);
    }

    public static boolean isTargetFile(String fileName, String... extensions) {
        if (FastStringUtils.isEmpty(fileName)) {
            return false;
        }
        String regex = ".+(" + FastStringUtils.join(extensions, "|") + ")$";
        return Pattern.matches(regex, fileName
                .toLowerCase());
    }

    public static boolean isTargetFileByMimeType(String mimeType, String... mimeTypes) {
        if (FastStringUtils.isEmpty(mimeType)) {
            return false;
        }
        String regex = FastStringUtils.join(mimeTypes, "|");
        return Pattern.matches(regex, mimeType
                .toLowerCase());
    }



    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            } else if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            } else {
                return new FileInputStream(file);
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
    }

    public static List<String> readLines(File file, String encoding) throws IOException {
        return readLines(file, FastCharsetsUtils.toCharset(encoding));
    }

    public static List<String> readLines(File file) throws IOException {
        return readLines(file, Charset.defaultCharset());
    }


    public static List<String> readLines(File file, Charset encoding) throws IOException {
        FileInputStream in = null;
        List var3;
        try {
            in = openInputStream(file);
            var3 = readLines(in, FastCharsetsUtils.toCharset(encoding));
        } finally {
            closeQuietly(in);
        }

        return var3;
    }

    public static List<String> readLines(InputStream input, Charset encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, FastCharsetsUtils.toCharset(encoding));
        return readLines(reader);
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList();

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            list.add(line);
        }

        return list;
    }

    public static String readFileToString(File file, String encoding) throws IOException {
        return readFileToString(file, FastCharsetsUtils.toCharset(encoding));
    }

    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, Charset.defaultCharset());
    }

    public static String readFileToString(File file, Charset encoding) throws IOException {
        FileInputStream in = null;

        String var3;
        try {
            in = openInputStream(file);
            var3 = toString(in, FastCharsetsUtils.toCharset(encoding));
        } finally {
            closeQuietly(in);
        }

        return var3;
    }

    public static String toString(InputStream input, Charset encoding) throws IOException {
        FastStringBuilderWriter sw = new FastStringBuilderWriter();
        FastIOUtils.copy((InputStream) input, (Writer) sw, (Charset) encoding);
        return sw.toString();
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }

            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }

        return new FileOutputStream(file, append);
    }


    public static void writeLines(File file, Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null, false);
    }

    public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding, boolean append) throws IOException {
        FileOutputStream out = null;

        try {
            out = openOutputStream(file, append);
            BufferedOutputStream buffer = new BufferedOutputStream(out);
            writeLines(lines, lineEnding, buffer, encoding);
            buffer.flush();
            out.close();
        } finally {
            closeQuietly(out);
        }

    }

    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, String encoding) throws IOException {
        writeLines(lines, lineEnding, output, FastCharsetsUtils.toCharset(encoding));
    }


    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, Charset encoding) throws IOException {
        if (lines != null) {
            if (lineEnding == null) {
                lineEnding = "\n";
            }

            Charset cs = FastCharsetsUtils.toCharset(encoding);

            for (Iterator i$ = lines.iterator(); i$.hasNext(); output.write(lineEnding.getBytes(cs))) {
                Object line = i$.next();
                if (line != null) {
                    output.write(line.toString().getBytes(cs));
                }
            }

        }
    }


    public static void writeStringToFile(File file, String data, Charset encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    public static void writeStringToFile(File file, String data, Charset encoding, boolean append) throws IOException {
        FileOutputStream out = null;

        try {
            out = openOutputStream(file, append);
            if (data != null) {
                out.write(data.getBytes(FastCharsetsUtils.toCharset(encoding)));
            }
            out.close();
        } finally {
            closeQuietly(out);
        }

    }

    public static void writeStringToFile(File file, String data, String encoding, boolean append) throws IOException {
        writeStringToFile(file, data, FastCharsetsUtils.toCharset(encoding), append);
    }

    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), false);
    }

    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }


    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }


    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException var2) {
        }
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        FileInputStream in = null;

        byte[] var2;
        try {
            in = openInputStream(file);
            var2 = toByteArray(in, file.length());
        } finally {
            closeQuietly(in);
        }

        return var2;
    }

    public static byte[] toByteArray(InputStream input, long size) throws IOException {
        if (size > 2147483647L) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        } else {
            return toByteArray(input, (int) size);
        }
    }

    public static byte[] toByteArray(InputStream input, int size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        } else if (size == 0) {
            return new byte[0];
        } else {
            byte[] data = new byte[size];

            int offset;
            int readed;
            for (offset = 0; offset < size && (readed = input.read(data, offset, size - offset)) != -1; offset += readed) {
            }

            if (offset != size) {
                throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
            } else {
                return data;
            }
        }
    }


    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        } else {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel input = null;
            FileChannel output = null;

            try {
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destFile);
                input = fis.getChannel();
                output = fos.getChannel();
                long size = input.size();
                long pos = 0L;

                for (long count = 0L; pos < size; pos += output.transferFrom(input, pos, count)) {
                    count = size - pos > 31457280L ? 31457280L : size - pos;
                }
            } finally {
                closeQuietly(output);
                closeQuietly(fos);
                closeQuietly(input);
                closeQuietly(fis);
            }

            if (srcFile.length() != destFile.length()) {
                throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
            } else {
                if (preserveFileDate) {
                    destFile.setLastModified(srcFile.lastModified());
                }

            }
        }
    }


    public static long copyFile(File input, OutputStream output) throws IOException {
        FileInputStream fis = new FileInputStream(input);

        long var3;
        try {
            var3 = FastIOUtils.copyLarge(fis, output);
        } finally {
            fis.close();
        }

        return var3;
    }

    public static void moveFile(File srcFile, File destFile) throws IOException {
        moveFile(srcFile, destFile, false);
    }


    public static void moveFile(File srcFile, File destFile, boolean force) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        } else if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        } else if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' is a directory");
        } else if (destFile.exists() && !force) {
            throw new IOException("Destination '" + destFile + "' already exists");
        } else if (destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' is a directory");
        } else {
            boolean rename = srcFile.renameTo(destFile);
            if (!rename) {
                copyFile(srcFile, destFile);
                if (!srcFile.delete()) {
                    deleteQuietly(destFile);
                    throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
                }
            }

        }
    }

    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        } else {
            try {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
            } catch (Exception var3) {
            }

            try {
                return file.delete();
            } catch (Exception var2) {
                return false;
            }
        }
    }


    public static void cleanDirectory(File directory) throws IOException {
        String message;
        if (!directory.exists()) {
            message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        } else if (!directory.isDirectory()) {
            message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        } else {
            File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("Failed to list contents of " + directory);
            } else {
                IOException exception = null;
                File[] arr$ = files;
                int len$ = files.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    File file = arr$[i$];

                    try {
                        forceDelete(file);
                    } catch (IOException var8) {
                        exception = var8;
                    }
                }

                if (null != exception) {
                    throw exception;
                }
            }
        }
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (file.exists()) {
                if (!file.delete()) {
                    String message = "Unable to delete file: " + file;
                    throw new IOException(message);
                }
            }
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            if (!isSymlink(directory)) {
                cleanDirectory(directory);
            }

            if (!directory.delete()) {
                String message = "Unable to delete directory " + directory + ".";
                throw new IOException(message);
            }
        }
    }


    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        } else {
            File destFile = new File(destDir, srcFile.getName());
            copyFile(srcFile, destFile, preserveFileDate);
        }
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        } else if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        } else if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        } else if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        } else {
            File parentFile = destFile.getParentFile();
            if (parentFile != null && !parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            } else if (destFile.exists() && !destFile.canWrite()) {
                throw new IOException("Destination '" + destFile + "' exists but is read-only");
            } else {
                doCopyFile(srcFile, destFile, preserveFileDate);
            }
        }
    }

    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        } else if (srcDir.exists() && !srcDir.isDirectory()) {
            throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
        } else if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        } else {
            copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
        }
    }

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, (FileFilter) null, preserveFileDate);
    }

    public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        } else if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        } else if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        } else if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        } else {
            List<String> exclusionList = null;
            if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
                File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
                if (srcFiles != null && srcFiles.length > 0) {
                    exclusionList = new ArrayList(srcFiles.length);
                    File[] arr$ = srcFiles;
                    int len$ = srcFiles.length;

                    for (int i$ = 0; i$ < len$; ++i$) {
                        File srcFile = arr$[i$];
                        File copiedFile = new File(destDir, srcFile.getName());
                        exclusionList.add(copiedFile.getCanonicalPath());
                    }
                }
            }

            doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
        }
    }

    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate, List<String> exclusionList) throws IOException {
        File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {
            throw new IOException("Failed to list contents of " + srcDir);
        } else {
            if (destDir.exists()) {
                if (!destDir.isDirectory()) {
                    throw new IOException("Destination '" + destDir + "' exists but is not a directory");
                }
            } else if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }

            if (!destDir.canWrite()) {
                throw new IOException("Destination '" + destDir + "' cannot be written to");
            } else {
                File[] arr$ = srcFiles;
                int len$ = srcFiles.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    File srcFile = arr$[i$];
                    File dstFile = new File(destDir, srcFile.getName());
                    if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                        if (srcFile.isDirectory()) {
                            doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                        } else {
                            doCopyFile(srcFile, dstFile, preserveFileDate);
                        }
                    }
                }

                if (preserveFileDate) {
                    destDir.setLastModified(srcDir.lastModified());
                }

            }
        }
    }


    public static void copyURLToFile(URL source, File destination) throws IOException {
        InputStream input = source.openStream();
        copyInputStreamToFile(input, destination);
    }

    public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout) throws IOException {
        URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        InputStream input = connection.getInputStream();
        copyInputStreamToFile(input, destination);
    }

    public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
        try {
            FileOutputStream output = openOutputStream(destination);

            try {
                FastIOUtils.copy(source, output);
                output.close();
            } finally {
                FastIOUtils.closeQuietly(output);
            }
        } finally {
            FastIOUtils.closeQuietly(source);
        }

    }


    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        } else if (File.separatorChar == '\\') {
            return false;
        } else {
            File fileInCanonicalDir = null;
            if (file.getParent() == null) {
                fileInCanonicalDir = file;
            } else {
                File canonicalDir = file.getParentFile().getCanonicalFile();
                fileInCanonicalDir = new File(canonicalDir, file.getName());
            }

            return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
        }
    }


    /**
     * 压缩文件夹或文件 zip格式
     *
     * @param resourcesPath
     * @return
     */
    public static File zipFile(String resourcesPath) {
        try {
            File resourcesFile = new File(resourcesPath);
            String targetName = resourcesFile.getName() + ".zip";   //目的压缩文件名
            File targetFile = new File(resourcesFile.getParent(), targetName);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
            createCompressedFile(out, resourcesFile, "");
            out.close();
            return targetFile;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    private static void createCompressedFile(ZipOutputStream out, File file, String dir) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                out.putNextEntry(new ZipEntry(dir + "/"));
                dir = FastStringUtils.isEmpty(dir) ? "" : dir + "/";
                for (File value : files) {
                    createCompressedFile(out, value, dir + value.getName());
                }
            }
        } else {
            FileInputStream fis = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(dir));
            int j = 0;
            byte[] buffer = new byte[1024];
            while ((j = fis.read(buffer)) > 0) {
                out.write(buffer, 0, j);
            }
            fis.close();
        }
    }


    public static String guessMimeType(String url) {
        if (FastStringUtils.isEmpty(url)) {
            return null;
        }
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String urlType = HttpURLConnection.guessContentTypeFromName(fileName);
        try {
            URL fileURL = new URL(url);
            URLConnection conn = fileURL.openConnection();
            if (conn == null) {
                return urlType;
            }
            InputStream inputStream = conn.getInputStream();
            String guessType = HttpURLConnection.guessContentTypeFromStream( new BufferedInputStream(inputStream));
            if (FastStringUtils.isEmpty(guessType)) {
                return urlType;
            }
            return guessType;
        } catch (Exception ignored) {
        }
        return urlType;
    }




    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        FileOutputStream out = null;

        try {
            out = openOutputStream(file, append);
            out.write(data);
            out.close();
        } finally {
            FastIOUtils.closeQuietly(out);
        }
    }


}
