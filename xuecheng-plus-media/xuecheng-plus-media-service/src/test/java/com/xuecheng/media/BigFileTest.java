package com.xuecheng.media;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xuecheng.media.MinioTest.minioClient;

/**
 * @author Ian
 * @version 1.0
 * @description 大文件處理測試
 * @date 2024/6/28
 */

public class BigFileTest {

    //分塊測試
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D://develop//chunk_test//bigfile.mp4");
        String chunkPath = "d:/develop/chunk_test/chunk/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //分塊大小 megabyte MB = 1,024 Bytes * 1024 Bytes (因為上傳minio合併最小單位為5)
        long chunkSize = 1024 * 1024 * 5;
        //分塊數量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分塊總數："+chunkNum);
        //緩沖區大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile訪問文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //分塊
        for (int i = 0; i < chunkNum; i++) {
            //創建分塊文件
            File file = new File(chunkPath + i);
            if(file.exists()){
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分塊文件中寫數據
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分塊"+i);
            }

        }
        raf_read.close();

    }

    //將分塊進行合併
    @Test
    public void testMerge() throws IOException{
        //塊文件目錄
        File chunkFolder = new File("d:/develop/chunk_test/chunk/");
        //原始文件
        File originalFile = new File("d:/develop/chunk_test/bigfile.mp4");
        //合並文件
        File mergeFile = new File("d:/develop/chunk_test/bigfile01.mp4");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        //創建新的合並文件
        mergeFile.createNewFile();
        //用於寫文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指針指向文件頂端
        raf_write.seek(0);
        //緩沖區
        byte[] b = new byte[1024];
        //分塊列表 可能是無序的
        File[] fileArray = chunkFolder.listFiles();
        // 轉成集合，便於排序
        List<File> fileList = Arrays.asList(fileArray);
        // 從小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //合並文件
        for (File chunkFile : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);

            }
            raf_read.close();
        }
        raf_write.close();

        //校驗文件
        try (

                FileInputStream fileInputStream = new FileInputStream(originalFile);
                FileInputStream mergeFileStream = new FileInputStream(mergeFile);

        ) {
            //取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            //取出合並文件的md5進行比較
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合並文件成功");
            } else {
                System.out.println("合並文件失敗");
            }
        }
    }
    //將分塊文件上傳minio(講義版)
    @Test
    public void uploadChunk(){
        String chunkFolderPath = "D:\\develop\\chunk_test\\chunk\\";
        File chunkFolder = new File(chunkFolderPath);
        //分塊文件
        File[] files = chunkFolder.listFiles();
        //將分塊文件上傳至minio
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .filename(files[i].getAbsolutePath())
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上傳分塊成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //將分塊文件上傳minio(修正版)
    @Test
    public void uploadChunk2(){

        for (int i = 0; i < 15; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .filename( "D:\\develop\\chunk_test\\chunk\\"+i)
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上傳分塊成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //合並文件，要求分塊文件最小5M
    @Test
    public void test_merge() throws Exception {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(15)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder().bucket("testbucket").object("merge01.mp4").sources(sources).build();
        minioClient.composeObject(composeObjectArgs);

    }
    //清除分塊文件
    @Test
    public void test_removeObjects(){
        //合並分塊完成將分塊文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(15)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("testbucket").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
