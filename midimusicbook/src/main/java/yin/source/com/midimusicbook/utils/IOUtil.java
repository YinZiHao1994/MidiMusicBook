package yin.source.com.midimusicbook.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yin.source.com.midimusicbook.exception.MidiFileException;

/**
 * Created by yin on 2017/5/15.
 */

public class IOUtil {

    /**
     * 将Bitmap转换成文件
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    private static File saveBitmapToFile(Bitmap bm, String path, String fileName) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File myCaptureFile = new File(path, fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
    }

    public static Bitmap getBitmapFromFile(File file) {
        if (!file.exists()) {
            return null;
        } else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return BitmapFactory.decodeStream(fis);
        }
    }

    /**
     * Return the file contents as a byte array. 将文件内容以一个byte数组的方式返回
     * If any IO error_avator occurs, return null. 如果IO异常出现，返回null
     */
    public static byte[] getByteDataByUri(Uri uri, Activity activity) {
        try {
            InputStream inputStream = handleFileInputStreamFromUri(uri, activity);
            return getBytesData(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the file contents as a byte array. 将文件内容以一个byte数组的方式返回
     * If any IO error_avator occurs, return null. 如果IO异常出现，返回null
     */
    public static byte[] getByteDataByFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return getBytesData(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] getBytesData(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new MidiFileException("inputStream is null");
        }
        byte[] data;
        int totalLen, len, offset;
        totalLen = inputStream.available();
        offset = 0;
        data = new byte[totalLen];
        while (offset < totalLen) {
            len = inputStream.read(data, offset, totalLen - offset);
            if (len <= 0) {
                throw new MidiFileException("Error reading midi file");
            }
            offset += len;
        }
        return data;
    }

    private static InputStream handleFileInputStreamFromUri(Uri uri, Activity activity) throws IOException {
        InputStream file;

        String uriString = uri.toString();

        if (uriString.startsWith("file:///android_asset/")) {
            AssetManager asset = activity.getResources().getAssets();
            String filepath = uriString.replace("file:///android_asset/", "");
            file = asset.open(filepath);
        } else if (uriString.startsWith("content://")) {
            ContentResolver resolver = activity.getContentResolver();
            file = resolver.openInputStream(uri);
        } else {
            file = new FileInputStream(uri.getPath());
        }
        return file;
    }


    /**
     * Assets拷贝文件以及文件夹到指定目录
     *
     * @param context
     * @param assetsFile
     * @param savePath
     */
    public static void copyFilesFromAssets(Context context, String assetsFile, String savePath) throws IOException {
        String fileNames[] = context.getAssets().list(assetsFile);// 获取assets目录下的所有文件及目录名
        if (fileNames.length > 0) {// 如果是目录
            File file = new File(savePath);
            file.mkdirs();
            // 递归
            for (String fileName : fileNames) {
                copyFilesFromAssets(context, assetsFile + "/" + fileName,
                        savePath + "/" + fileName);
            }
        } else {// 如果是文件
            InputStream is = context.getAssets().open(assetsFile);
            FileOutputStream fos = new FileOutputStream(new File(savePath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
        }
    }


    /**
     * 微信分享用的 bitmap 转字节
     *
     * @param bmp
     * @param needRecycle
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
