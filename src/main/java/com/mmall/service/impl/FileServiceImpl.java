package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Alex Cheng
 * 4/25/2020 1:34 PM
 */

//使用mvn的MultipartFile文件系统，文件上传到tomcat,然后ftp到ftp server,最后在tomcat上删除原文件
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String upload(MultipartFile file, String path){
        //避免重名所以重命名
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() +  "." + fileExtensionName; //UUID避免后面文件覆盖前面同名的

        logger.info("Begin to upload file, the filename is:{}, upload path is:{}, new file name:{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true); //可能没有权限就事先设定一下
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName); //tomcat上路径和
        try {
            file.transferTo(targetFile); //文件经过新命名放到指定路径(Tomcat服务器上的)

            // toto将targetFile上传到FTP服务器上,如果异常会被log并且抛出
            FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));

            // todo上传后，删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("upload file failed", e);
            return null;
        }
        return targetFile.getName();
    }
}
