package com.stylefeng.guns.rest.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FTPUtil {

    private String hostName;
    private Integer port;
    private String userName;
    private String password;
    private String uploadPath;

    private FTPClient ftpClient = null;

    private void initFTPClient(){
        try{
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("utf-8");
            ftpClient.connect(hostName,port);
            ftpClient.login(userName,password);
        }catch (Exception e){
            log.error("初始化FTP失效",e);
        }
    }

    //输入路径，返回路径里的文件转换后的字符串
    public String getFileStrByAddress(String fileAddress){
        BufferedReader  bufferedReader = null;
        try{
            initFTPClient();
            bufferedReader = new BufferedReader(
                    new InputStreamReader(ftpClient.retrieveFileStream(fileAddress))
            );
            StringBuffer stringBuffer = new StringBuffer();
            while(true){
                String lenStr = bufferedReader.readLine();
                if(lenStr == null){
                    break;
                }
                stringBuffer.append(lenStr);
            }
            ftpClient.logout();
            return stringBuffer.toString();
        }catch (Exception e){
            log.error("获取文件信息失败",e);
        }finally {
            try{
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    //上传文件
    public boolean uploadFile(String fileName, File file){

        FileInputStream fileInputStream = null;


        try{

            fileInputStream = new FileInputStream(file);

            //FTP相关内容
            initFTPClient();

            //设置FTP的关键参数

            ftpClient.setControlEncoding("utf-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            //将ftpClient的工作空间修改
            ftpClient.changeWorkingDirectory(this.getUploadPath());

            //上传文件
            ftpClient.storeFile(fileName,fileInputStream);

            return true;

        }catch (Exception e){
            log.error("上传失败",e);

            return false;
        }finally {
            try{
                fileInputStream.close();
                ftpClient.logout();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }



    public static void main(String[] args) {
        FTPUtil ftpUtil = new FTPUtil();
        String fileStrByAddress = ftpUtil.getFileStrByAddress("seats/cgs.json");
        System.out.println(fileStrByAddress);
    }

}
