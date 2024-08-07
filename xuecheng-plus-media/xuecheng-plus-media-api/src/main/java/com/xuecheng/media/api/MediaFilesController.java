package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒資文件管理接口
 * @author Ian
 * @date 2024/6/27
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {

  @Autowired
  MediaFileService mediaFileService;

 @ApiOperation("媒资列表查询接口")
 @PostMapping("/files")
 public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
  Long companyId = 1232141425L;
  return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);

 }

 @ApiOperation("上传文件")
 @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
 public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata, @RequestParam(value = "folder",required=false) String folder, @RequestParam(value = "objectName",required=false) String objectName) throws IOException {

     Long companyId = 1232141425L;
     UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
     //文件大小
     uploadFileParamsDto.setFileSize(filedata.getSize());
     //圖片
     uploadFileParamsDto.setFileType("001001");
     //文件名稱
     uploadFileParamsDto.setFilename(filedata.getOriginalFilename());//文件名稱
     //文件大小
     long fileSize = filedata.getSize();
     uploadFileParamsDto.setFileSize(fileSize);
     //創建臨時文件
     File tempFile = File.createTempFile("minio", "temp");
     //上傳的文件拷貝到臨時文件
     filedata.transferTo(tempFile);
     //文件路徑
     String absolutePath = tempFile.getAbsolutePath();
     //上傳文件
     UploadFileResultDto uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, absolutePath, objectName);

     return uploadFileResultDto;
 }

}
