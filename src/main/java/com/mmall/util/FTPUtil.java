package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Alex Cheng
 * 4/25/2020 1:57 PM
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
        //this.ftpClient = ftpClient;
    }

    //对外只暴露这个
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser, ftpPass);
        logger.info("connecting ftp server");
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("connected to ftp server, ftp result:{}",result);
        return result;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        if(connectServer(this.ip, this.port, this.user, this.pwd)){
            try {
                //设置ftp环境
                ftpClient.changeWorkingDirectory(remotePath); //切换ftp工作目录
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//二进制文件类型
                ftpClient.enterLocalPassiveMode(); //ftp本次被动模式,tomcat是ftp client端

                for(File fileItem : fileList){
                    fis = new FileInputStream(fileItem); //输入文件流
                    ftpClient.storeFile(fileItem.getName(), fis);//开始上传保存，文件名，input文件流
                }

            } catch (IOException e) {
                logger.error("upload file error", e);
                uploaded = false;
                e.printStackTrace();

            } finally {
                fis.close(); //关掉流
                ftpClient.disconnect(); //断开ftp连接
            }
        }
        return uploaded;

    }

    private boolean connectServer(String ip, int port, String user, String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("connect to FTP server failed", e);
            e.printStackTrace();
        }
        return isSuccess;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
