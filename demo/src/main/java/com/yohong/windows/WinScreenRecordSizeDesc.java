package com.yohong.windows;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yohong.constant.Constants.SCREEN_FILE_PATH;


/**
 * @author fanyufeng
 * @version 1.0
 * @description 录屏代码类，比 WinScreenRecord 文件小，60 分钟约 100M，但是分辨率较低。
 * @date 2024/5/6 16:18
 */
@Slf4j
public class WinScreenRecordSizeDesc {
	Robot robot;
	
	{
		try {
			robot = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}
	
	static DateTimeFormatter FORMATTER_YYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	
	
	ScheduledThreadPoolExecutor screenTimerPool;
	
	/*录制的频率*/
	private final int frameRate = 2;
	boolean isStop = false;
	
	public static void main(String[] args) {
	}
	
	public WinScreenRecordSizeDesc() {
		synchronized (this) {
			// 添加观察者
			screenTimerPool = ThreadUtil.createScheduledExecutor(3);
		}
		
	}
	
	
	public void start() {
		screenTimerPool = new ScheduledThreadPoolExecutor(3);
		String screenFilePath = SCREEN_FILE_PATH + "screen" + File.separator + LocalDateTime.now().format(FORMATTER_YYMMDD) + File.separator;
		File file = new File(screenFilePath);
		if (!file.exists()) {
			boolean mkdirs = file.mkdirs();
			if (mkdirs) {
				File hiddenDirectory = new File(SCREEN_FILE_PATH);
				// 获取 DosFileAttributeView
				Path directoryPathObj = hiddenDirectory.toPath();
				DosFileAttributeView dosView = Files.getFileAttributeView(directoryPathObj, DosFileAttributeView.class);
				
				// 设置隐藏属性
				try {
					dosView.setHidden(true);
					
				} catch (Exception e) {
					log.error("hidden file path error! please confirm:0", e);
				}
			}
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		String filePath = screenFilePath + LocalDateTime.now().format(formatter);
		// 是否停止录屏
		isStop = false;
		int i = 1;
		for (GraphicsDevice screen : screens) {
			DisplayMode displayMode = screen.getDisplayMode();
			Rectangle screenRect = screen.getDefaultConfiguration().getBounds();
			screenRect.height = displayMode.getHeight();
			screenRect.width = displayMode.getWidth();
			String screenRecordFilePath = i == 1 ? filePath + "-" + "-master-0" + i + ".mp4" : filePath + "-" + "-slave-0" + i + ".mp4";
			i++;
			// master 和 slave
			FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(screenRecordFilePath, displayMode.getWidth(), displayMode.getHeight(), 0);
			try {
				buildRecord(recorder);
			} catch (FFmpegFrameRecorder.Exception e) {
				log.error("buildRecord error", e);
			}
			log.info("开始进行录屏功能：{}", screenRecordFilePath);
			screenTimerPool.scheduleAtFixedRate(() -> {
				// 主动截屏屏幕偏红，需要重绘截图
				BufferedImage screenCapture = robot.createScreenCapture(screenRect);
				BufferedImage videoImg = new BufferedImage(displayMode.getWidth(), displayMode.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
				// 声明一个BufferedImage用重绘截图
				Graphics2D videoGraphics = videoImg.createGraphics();// 创建videoImg的Graphics2D
				videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
				videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_SPEED);
				videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				videoGraphics.drawImage(screenCapture, 0, 0, null); // 重绘截图
				
				Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
				Frame frame = java2dConverter.convert(videoImg);
				try {
					// 检查偏移量
					recorder.record(frame);
				} catch (Exception e) {
					log.error("录屏功能出现异常：", e);
				} finally {
					// 释放资源
					videoGraphics.dispose();
					videoGraphics = null;
					videoImg.flush();
					videoImg = null;
					java2dConverter = null;
					screenCapture.flush();
					screenCapture = null;
				}
				if (isStop) {
					try {
						log.info("录制线程池结束运行");
						recorder.stop();
						recorder.release();
						recorder.close();
						log.info("通知文件上传：{}", screenRecordFilePath);
						// 通知文件监听者上传文件
					} catch (Exception e) {
						log.error("停止录屏功能出现异常：", e);
					}
				}
			}, 0, 1000 / frameRate, TimeUnit.MILLISECONDS);
		}
	}
	
	public void stop() {
		isStop = true;
		try {
			// 睡眠 3 S 等待录屏关闭进行文件流存储
			Thread.sleep(3000);
			if(!screenTimerPool.isShutdown()){
				screenTimerPool.shutdown();
				screenTimerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			}
		} catch (InterruptedException e) {
			log.error("stop record error ", e);
		}
		
	}
	
	private void buildRecord(FFmpegFrameRecorder recorder) throws FFmpegFrameRecorder.Exception {
		
		recorder.setVideoQuality(0);
		recorder.setVideoOption("preset", "slow");
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat("mp4");
		recorder.setFrameRate(frameRate);
		// recorder.setVideoOption("crf", "23");
		// 提供视频流编码的视频比特率(以每秒位数为单位)。 x kbits
		recorder.setVideoBitrate(10 * 10 * 1024);
		recorder.setPixelFormat(0);
		
		// 下面这三个参数任意一个会触发音频编码
		// 	recorder.setSampleFormat(sampleFormat);//音频采样格式,使用avutil中的像素格式常量，例如：avutil.AV_SAMPLE_FMT_NONE
		// 	recorder.setAudioChannels(audioChannels);
		// 	recorder.setSampleRate(sampleRate)
		recorder.start();
	}
	
	
	
}
