package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上傳文件
  * @param companyId 機構id
  * @param uploadFileParamsDto 上傳文件信息
  * @param localFilePath 文件磁盤路徑
  * @param objectName 對象名
  * @return 文件信息
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName);

 /**
  * @description 將文件信息添加到文件表
  * @param companyId  機構id
  * @param fileMd5  文件md5值
  * @param uploadFileParamsDto  上傳文件的信息
  * @param bucket  桶
  * @param objectName 對象名稱
  * @return com.xuecheng.media.model.po.MediaFiles
  * @author Mr.M
  * @date 2022/10/12 21:22
  */

 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

 /**
  * 將文件上傳到minio
  * @param localFilePath 文件本地路徑
  * @param mimeType 媒體類型
  * @param bucket 桶
  * @param objectName 對象名
  * @return
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

 /**
  * @description 檢查文件是否存在
  * @param fileMd5 文件的md5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  * @author Ian
  * @date 2024/7/1
  */
 public RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * @description 檢查分塊是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分塊序號
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  * @author Ian
  * @date 2024/7/1
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * @description 上傳分塊
  * @param fileMd5  文件md5
  * @param chunk  分塊序號
  * @param bytes  文件字節
  * @return com.xuecheng.base.model.RestResponse
  * @author Ian
  * @date 2024/7/1
  */
 public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

 /**
  * @description 合並分塊
  * @param companyId  機構id(標記是何機構上傳, 未來就有機會依上傳限度收費)
  * @param fileMd5  文件md5
  * @param chunkTotal 分塊總和
  * @param uploadFileParamsDto 文件信息
  * @return com.xuecheng.base.model.RestResponse
  * @author Ian
  * @date 2024/7/8
  */
 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);


 /**
  * 從minio下載文件
  * @param bucket 桶
  * @param objectName 對象名稱
  * @return 下載後的文件
  */
 public File downloadFileFromMinIO(String bucket, String objectName);

 // 根據媒資id查詢文件訊息
 public MediaFiles getFileById(String mediaId);
}
