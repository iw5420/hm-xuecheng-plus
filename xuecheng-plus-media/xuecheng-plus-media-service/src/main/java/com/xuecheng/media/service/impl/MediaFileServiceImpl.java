package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Ian
 * @date 2024/6/27
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

 @Autowired
 MinioClient minioClient;

 @Autowired
 MediaFilesMapper mediaFilesMapper;

 @Autowired
 MediaProcessMapper mediaProcessMapper;
 //存儲普通文件
 @Value("${minio.bucket.files}")
 private String bucket_mediafiles;

 //存儲視頻
 @Value("${minio.bucket.videofiles}")
 private String bucket_video;

 @Autowired
 MediaFileService currentProxy;

 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 //獲取文件默認存儲目錄路徑 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/") + "/";
  //ex: 2024/06/27
  return folder;
 }

 //獲取文件的md5
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }


 private String getMimeType(String extension) {
  if (extension == null)
   extension = "";
  //根據擴展名取出mimeType
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
  //通用mimeType，字節流
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
  if (extensionMatch != null) {
   mimeType = extensionMatch.getMimeType();
  }
  return mimeType;
 }

 /**
  * @param localFilePath 文件地址
  * @param bucket        桶
  * @param objectName    對象名稱
  * @return void
  * @description 將文件寫入minIO
  * @author Ian
  * @date 2024/6/27
  */
 public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
  try {
   UploadObjectArgs testbucket = UploadObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .filename(localFilePath)
           .contentType(mimeType)
           .build();
   minioClient.uploadObject(testbucket);
   log.debug("上傳文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
   System.out.println("上傳成功");
   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上傳文件到minio出錯,bucket:{},objectName:{},錯誤原因:{}", bucket, objectName, e.getMessage(), e);
   XueChengPlusException.cast("上傳文件到文件系統失敗");
  }
  return false;
 }

 /**
  * @param companyId           機構id
  * @param fileMd5             文件md5值
  * @param uploadFileParamsDto 上傳文件的信息
  * @param bucket              桶
  * @param objectName          對象名稱
  * @return com.xuecheng.media.model.po.MediaFiles
  * @description 將文件信息添加到文件表
  * @author Ian
  * @date 2024/6/27
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
  //從數據庫查詢文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷貝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert <=0) {
    log.error("保存文件信息到數據庫失敗,{}", mediaFiles.toString());
    XueChengPlusException.cast("保存文件信息失敗");
   }
   //記錄待處理任務
   addWaitingTask(mediaFiles);
   log.debug("保存文件信息到數據庫成功,{}", mediaFiles.toString());

  }
  return mediaFiles;
 }

 /**
  * 添加待處理任務
  * @param mediaFiles 媒資文件信息
  */
 private void addWaitingTask(MediaFiles mediaFiles){
  //文件名稱
  String filename = mediaFiles.getFilename();
  //文件擴展名
  String extension = filename.substring(filename.lastIndexOf("."));
  //文件mimeType
  String mimeType = getMimeType(extension);
  //如果是avi視頻添加到視頻待處理表
  if(mimeType.equals("video/x-msvideo")){
   MediaProcess mediaProcess = new MediaProcess();
   BeanUtils.copyProperties(mediaFiles,mediaProcess);
   mediaProcess.setStatus("1");//未處理
   mediaProcess.setFailCount(0);//失敗次數默認為0
   mediaProcess.setUrl(null);
   mediaProcessMapper.insert(mediaProcess);
  }
 }


 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //查询文件信息
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles != null) {
   //桶
   String bucket = mediaFiles.getBucket();
   //存储目录
   String filePath = mediaFiles.getFilePath();
   //文件流
   InputStream stream = null;
   try {
    stream = minioClient.getObject(
            GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build());

    if (stream != null) {
     //文件已存在
     return RestResponse.success(true);
    }
   } catch (Exception e) {

   }
  }
  //文件不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

  //得到分塊文件目錄
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //得到分塊文件的路徑
  String chunkFilePath = chunkFileFolderPath + chunkIndex;

  //文件流
  InputStream fileInputStream = null;
  try {
   fileInputStream = minioClient.getObject(
           GetObjectArgs.builder()
                   .bucket(bucket_video)
                   .object(chunkFilePath)
                   .build());

   if (fileInputStream != null) {
    //分塊已存在
    return RestResponse.success(true);
   }
  } catch (Exception e) {

  }
  //分塊未存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {

//得到分塊文件的目錄路徑
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //得到分塊文件的路徑
  String chunkFilePath = chunkFileFolderPath + chunk;
  //mimeType
  String mimeType = getMimeType(null);
  //將文件存儲至minIO
  boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
  if (!b) {
   log.debug("上傳分塊文件失敗:{}", chunkFilePath);
   return RestResponse.validfail(false, "上傳分塊失敗");
  }
  log.debug("上傳分塊文件成功:{}",chunkFilePath);
  return RestResponse.success(true);

 }

 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  // 1找到分塊文件, 使用minio的sdk進行文件合並
  // 2校驗合併後和原文件是否一致
  // 3將文件信息入庫
  // 4清理分塊文件

  //=====獲取分塊文件路徑=====
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //組成將分塊文件路徑組成 List<ComposeSource>
  List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
          .limit(chunkTotal)
          .map(i -> ComposeSource.builder()
                  .bucket(bucket_video)
                  .object(chunkFileFolderPath.concat(Integer.toString(i)))
                  .build())
          .collect(Collectors.toList());
  //=====合並=====
  //文件名稱
  String fileName = uploadFileParamsDto.getFilename();
  //文件擴展名
  String extName = fileName.substring(fileName.lastIndexOf("."));
  //合並文件路徑
  String mergeFilePath = getFilePathByMd5(fileMd5, extName);
  try {
   //合並文件
   ObjectWriteResponse response = minioClient.composeObject(
           ComposeObjectArgs.builder()
                   .bucket(bucket_video)
                   .object(mergeFilePath)
                   .sources(sourceObjectList)
                   .build());
   log.debug("合並文件成功:{}",mergeFilePath);
  } catch (Exception e) {
   log.debug("合並文件失敗, bucket:{}, fileMd5:{}, 異常:{}",bucket_video, fileMd5,e.getMessage(),e);
   return RestResponse.validfail(false, "合並文件失敗。");
  }

  // ====驗證md5====
  //下載合並後的文件
  File minioFile = downloadFileFromMinIO(bucket_video,mergeFilePath);
  if(minioFile == null){
   log.debug("下載合並後文件失敗,mergeFilePath:{}",mergeFilePath);
   return RestResponse.validfail(false, "下載合並後文件失敗。");
  }

  try (InputStream newFileInputStream = new FileInputStream(minioFile)) {
   //minio上文件的md5值
   String md5Hex = DigestUtils.md5Hex(newFileInputStream);
   //比較md5值，不一致則說明文件不完整
   if(!fileMd5.equals(md5Hex)){
    log.error("校驗合併文件md5值不一致, 原始文件:{}, 合併文件:{}", fileMd5, md5Hex);
    return RestResponse.validfail(false, "文件合並校驗失敗，最終上傳失敗。");
   }
   //文件大小
   uploadFileParamsDto.setFileSize(minioFile.length());
  }catch (Exception e){
   log.debug("校驗文件失敗,fileMd5:{},異常:{}",fileMd5,e.getMessage(),e);
   return RestResponse.validfail(false, "文件合並校驗失敗，最終上傳失敗。");
  }finally {
   if(minioFile!=null){
    minioFile.delete();
   }
  }

  //文件入庫
   MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, mergeFilePath);
   if(mediaFiles == null){
     return RestResponse.validfail(false, "文件入庫失敗");
   }
  //=====清除分塊文件=====
   clearChunkFiles(chunkFileFolderPath,chunkTotal);
   return RestResponse.success(true);
  }

 /**
  * 從minio下載文件
  * @param bucket 桶
  * @param objectName 對象名稱
  * @return 下載後的文件
  */
 public File downloadFileFromMinIO(String bucket,String objectName){
  //臨時文件
  File minioFile = null;
  FileOutputStream outputStream = null;
  try{
   InputStream stream = minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .build());
   //創建臨時文件
   minioFile=File.createTempFile("minio", ".merge");
   outputStream = new FileOutputStream(minioFile);
   IOUtils.copy(stream,outputStream);
   return minioFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
   if(outputStream!=null){
    try {
     outputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return null;
 }

 @Override
 public MediaFiles getFileById(String mediaId) {
      MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
  return mediaFiles;
 }

 /**
  * 得到合並後的文件的地址
  * @param fileMd5 文件id即md5值
  * @param fileExt 文件擴展名
  * @return
  */
 private String getFilePathByMd5(String fileMd5,String fileExt){
  return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
 }

 /**
  * 清除分塊文件
  * @param chunkFileFolderPath 分塊文件路徑
  * @param chunkTotal 分塊文件總數
  */
 private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

  try {
   List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
           .limit(chunkTotal)
           .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
           .collect(Collectors.toList());

   RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
   Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
   results.forEach(r->{
    DeleteError deleteError = null;
    try {
     deleteError = r.get();
    } catch (Exception e) {
     e.printStackTrace();
     log.error("清楚分塊文件失敗,objectname:{}",deleteError.objectName(),e);
    }
   });
  } catch (Exception e) {
   e.printStackTrace();
   log.error("清楚分塊文件失敗,chunkFileFolderPath:{}",chunkFileFolderPath,e);
  }
 }



 //得到分塊文件的目錄
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
 }


 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {
  // 1將文件上傳minio
  // 2將文件信息保存到數據庫
  File file = new File(localFilePath);
  if (!file.exists()) {
   XueChengPlusException.cast("文件不存在");
  }
  //文件名稱
  String filename = uploadFileParamsDto.getFilename();
  //文件擴展名
  String extension = filename.substring(filename.lastIndexOf("."));
  //文件mimeType
  String mimeType = getMimeType(extension);
  //文件的md5值
  String fileMd5 = getFileMd5(file);
  //文件的默認目錄
  String defaultFolderPath = getDefaultFolderPath();
  //存儲到minio中的對象名(帶目錄)
  //存儲到minio中的對象名(帶目錄)
  if(StringUtils.isEmpty(objectName)){
   objectName =  defaultFolderPath + fileMd5 + extension;
  }
  //將文件上傳到minio
  boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
  if(!result)XueChengPlusException.cast("上傳文件失敗");
  //文件大小
  uploadFileParamsDto.setFileSize(file.length());
  //將文件信息存儲到數據庫
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
  if(mediaFiles==null)XueChengPlusException.cast("上傳後保存文件信息失敗");
  //準備返回數據
  UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
  BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
  return uploadFileResultDto;
 }

}
