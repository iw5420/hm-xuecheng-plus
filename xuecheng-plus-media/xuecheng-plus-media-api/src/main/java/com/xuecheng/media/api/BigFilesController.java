package com.xuecheng.media.api;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


/**
 * @author Ian
 * @version 1.0
 * @description 大文件上傳接口
 * @date 2024/7/1
 */
@Api(value = "大文件上傳接口", tags = "大文件上傳接口")
@RestController
public class BigFilesController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "文件上傳前檢查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkFile(fileMd5);
        return booleanRestResponse;
    }


    @ApiOperation(value = "分塊文件上傳前的檢測")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkChunk(fileMd5, chunk);

        return booleanRestResponse;
    }

    @ApiOperation(value = "上傳分塊文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        //創建一個臨時文件
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        //文件路徑
        String localFilePath = tempFile.getAbsolutePath();
        RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);
        return restResponse;
    }

    @ApiOperation(value = "合並文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("視頻文件");
        uploadFileParamsDto.setFileType("001002");
        RestResponse restResponse = mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
        return restResponse;

    }


}
