package com.android.cim.ftpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.android.cim.Constants;
import com.android.cim.util.Log;

public class FtpClient implements Runnable{

    private String mFtpServer = null;
    private String mPort;
    private String mUserName;
    private String mPassword;
    private String mRemotePath;
    
    private int mSuccessCount;
    private int mFailureCount;

    public FtpClient(String server, String port, String username, String password, String remotePath) {
        mFtpServer = server;
        mPort = port;
        mUserName = username;
        mPassword = password;
        mRemotePath = remotePath;
        
        mSuccessCount = 0;
        mFailureCount = 0;
    }
    /**
     * 通过ftp上传文件
     * 
     * @param url
     *            ftp服务器地址 如： 192.168.1.110
     * @param port
     *            端口如 ： 21
     * @param username
     *            登录名
     * @param password
     *            密码
     * @param remotePath
     *            上到ftp服务器的磁盘路径
     * @param fileNamePath
     *            要上传的文件路径
     * @param fileName
     *            要上传的文件名
     * @return
     */
    public String ftpUpload(String url, String port, String username,
            String password, String remotePath, String fileNamePath,
            String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(url, Integer.parseInt(port));
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                fis = new FileInputStream(fileNamePath + fileName);
                ftpClient.storeFile(fileName, fis);

                returnMessage = "1"; // 上传成功
            } else {// 如果登录失败
                returnMessage = "0";
            }
        } catch (IOException e) {
            e.printStackTrace();
            returnMessage = "0";
        } finally {
            // IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return returnMessage;
    }

    @Override
    public void run() {
        Log.d(Log.TAG, "Constants.RECORD_PATH = " + Constants.RECORD_PATH);
        File dir = new File(Constants.RECORD_PATH);
        String files[] = dir.list();
        String result = null;
        for (String file : files) {
            if (".nomedia".equals(file)) {
                continue;
            }
            Log.d(Log.TAG, "Uploading file = " + file);
            result = ftpUpload(mFtpServer, mPort, mUserName, mPassword, mRemotePath, Constants.RECORD_PATH, file);
            Log.d(Log.TAG, "Uploading file = " + file + " , result = " + result);
            if ("1".equals(result)) {
                mSuccessCount++;
                File deleteFile = new File(Constants.RECORD_PATH + file);
                Log.d(Log.TAG, "delete file : " + deleteFile.getAbsolutePath());
                deleteFile.delete();
            } else {
                mFailureCount++;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(Log.TAG, "Upload Completely ------- Success : " + mSuccessCount + " , Failure : " + mFailureCount);
    }

    public void execUpload() {
        new Thread(this).start();
    }
}
