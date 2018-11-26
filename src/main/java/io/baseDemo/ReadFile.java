package io.baseDemo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 读取文件
 */
public class ReadFile {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception {
        lineRead();
    }

    /**
     * 按行读取数据
     */
    private static void lineRead() throws Exception {
        FileReader reader = new FileReader("src/main/resources/test1.txt");
        BufferedReader bufferedReader = new BufferedReader(reader);
        //按行读取
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        bufferedReader.close();
    }


    /**
     * 单个的读取数据
     * @throws Exception 异常
     */
    private static void read() throws Exception {
        FileReader reader = new FileReader("");
        char[] chars = new char[BUFFER_SIZE];
        int len = 0;
        while ((len = reader.read(chars)) != -1) {
            System.out.print(new String(chars, 0, len));
        }
        reader.close();
    }


}



