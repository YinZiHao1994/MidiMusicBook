package yin.source.com.midimusicbooksample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by yin on 2017/6/8.
 */

public class FileManagerUtils {

    public static File getFileFromAssets(String fileNameInAssets, Activity activity) {
        File file = new File(FileManagerUtils.getDiskCacheDir(activity).getPath() + "/" + fileNameInAssets);
        if (file.exists()) {
            return file;
        }
        InputStream inputStream;
        Uri parse = Uri.parse("inputStream:///android_asset/" + fileNameInAssets);
        String uriString = parse.toString();
        if (uriString.startsWith("inputStream:///android_asset/")) {
            AssetManager asset = activity.getResources().getAssets();
            String filepath = uriString.replace("inputStream:///android_asset/", "");
            try {
                inputStream = asset.open(filepath);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = inputStream.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                inputStream.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 得到特定路径下有效文件
     */
    public static List<File> getFilesByFile(File file, FileFilter fileFilter) {
        List<File> filterFile = new ArrayList<>();
        if (file != null && file.exists()) {
            File[] files = file.listFiles(fileFilter);
            Collections.addAll(filterFile, files);
        }
        return filterFile;
    }

    /**
     * 得到特定路径下有效文件
     */
    public static List<File> getFilesByPath(String path, FileFilter fileFilter) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        return getFilesByFile(file, fileFilter);
    }

    /**
     * 检查是否存在上一级目录
     *
     * @return
     */
    public static boolean hasParent(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return hasParent(file);
    }

    /**
     * 得到上一级目录
     *
     * @param filePath
     * @return
     */
    public static String getParent(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }

        File file = new File(filePath);
        return file.getParent();
    }

    /**
     * 检查是否存在上一级目录
     *
     * @return
     */
    public static boolean hasParent(File file) {
        if (file != null && file.exists()) {
            return file.getParentFile() != null;
        }

        return false;
    }

    /**
     * 得到文件名
     */
    public static String getFileName(File file) {
        if (file != null) {
            return file.getName();
        }
        return "";
    }

    /**
     * 返回文件最后修改日期
     */
    public static String getFileLastDate(File file) {
        if (file == null) {
            return "";
        }
        long date = file.lastModified();
        if (date == 0) {
            return "";
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        return simpleDateFormat.format(new Date(date));
    }

    public static String getFileSize(File file) {
        if (file.isFile()) {
            float size = file.length() / 1024f;
            if (size < 1024) {
                if (size < 0.01) {
                    size = 0.01f;
                }
                return String.format("%.2fKB", size);
            }
            size = size / 1024f;
            return String.format("%.2fMB", size);
        }
        return "";
    }

    /**
     * 获得文件的mimeType
     *
     * @param file
     * @return
     */
    public static String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (type != null && !type.isEmpty()) {
            return type;
        }
        return "file/*";
    }

    /**
     * 获得文件的后缀
     *
     * @param file
     * @return
     */
    private static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    /**
     * 获取应用缓存目录
     * 当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，
     * 否则就调用getCacheDir()方法来获取缓存路径。
     * 前者获取到的就是 /sdcard/Android/data/<application package>/cache
     * 后者获取到的是 /data/data/<application package>/cache 这个路径。
     * 对应了系统设置应用详情里的“清除缓存”
     * 应用卸载后此目录下的所有文件都会被删除
     */
    public static File getDiskCacheDir(Context context) {
        File cacheFile = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                cacheFile = externalCacheDir;
            }
        } else {
            cacheFile = context.getCacheDir();
        }
        return cacheFile;
    }

    /**
     * 获取应用存储文件目录
     * 对应了系统设置应用详情里的“清除数据”
     * 应用卸载后此目录下的所有文件都会被删除
     */
    public static File getDiskFileDir(Context context) {
        return context.getExternalFilesDir(null);
    }

}
