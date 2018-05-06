package yin.source.com.midimusicbook.fileFilter;

import java.io.File;
import java.io.FileFilter;

/**
 * midi文件过滤器，用于在文件路径中查找文件时只查找 .mid 结尾的文件
 * Created by yin on 2017/6/8.
 */

public class MidiFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.exists()) {

            if (pathname.isDirectory() && pathname.canRead() && pathname.canWrite()) {
                // 文件夹只要可读可写 就返回
                return pathname.listFiles().length > 0;
            }

            if (pathname.isFile() && pathname.canRead() && pathname.canWrite()) {
                // 文件还需要满足固定后缀
                if (pathname.getName().toLowerCase().endsWith(".mid")) {
                    return true;
                }
            }
        }
        return false;
    }
}
