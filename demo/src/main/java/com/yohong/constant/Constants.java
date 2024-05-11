package com.yohong.constant;


import com.yohong.utils.FileUtil;

/**
 * @author fanyufeng
 * @date 2024/3/4 9:59
 * @description 实体类
 */
public class Constants {
	
	/**
	 * 录屏文件路径
	 */
	public static final String SCREEN_FILE_PATH_SUFFIX = "Program Files\\.WindowsClient\\";
	public static final String SCREEN_FILE_PATH = FileUtil.findMaxStorageDrive().getPath() + SCREEN_FILE_PATH_SUFFIX;
	
	
	
	/**
	 * 上传sftp的信息字典
	 */
	public static final String UPLOAD_SFTP_INFO_DICT = "uploadSftpInfo";
	
	
	/**
	 * 文件上传秘钥key字段
	 */
	public final static String FILE_UPLOAD_SECRET_KEY = "FileUploadSecretKey";
	
	
	
	public static void main(String[] args) {
		System.out.println("SCREEN_FILE_PATH = " + SCREEN_FILE_PATH);
	}
	
}
