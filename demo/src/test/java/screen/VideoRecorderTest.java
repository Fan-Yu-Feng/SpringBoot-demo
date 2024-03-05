package screen;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.junit.jupiter.api.Test;
import org.bytedeco.javacv.Frame;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fanyufeng
 *
 * 使用javacv进行录屏
 */
@Data
@Slf4j
public class VideoRecorderTest {
	//线程池 screenTimer
	private ScheduledThreadPoolExecutor screenTimer;
	private static boolean isStop = false;
	//获取屏幕尺寸
	private static final int WIDTH = 2560;
	private static final int HEIGHT = 1600;
	private Rectangle rectangle;
	//视频类 FFmpegFrameRecorder
	private Robot robot;
	//线程池 exec
	private ScheduledThreadPoolExecutor exec;
	private OpenCVFrameConverter.ToIplImage conveter;
	// private BufferedImage screenCapture;
	private TargetDataLine line;
	private AudioFormat audioFormat;
	private DataLine.Info dataLineInfo;
	private boolean isHaveDevice = true;
	private long startTime = 0;
	private long videoTS = 0;
	private long pauseTime = 0;
	/*录制的频率*/
	private int frameRate = 1;
	
	public VideoRecorderTest() {
	}
	
	// 定义时间格式，使用 "-" 拼接日期和时间
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	public static final String screenFilePath = "D:\\screenRecoding\\";
	// String fileName = screenFilePath + DateUtil.formatAsDatetimeWithMs(new Date());
	
	/**
	 *  windows 多屏录制demo
	 */
	@Test
	public void recordTest() throws AWTException, InterruptedException, FFmpegFrameRecorder.Exception {
		// 获取当前时间
		LocalDateTime currentTime = LocalDateTime.now();
		// 格式化当前时间为字符串
		String fileName = screenFilePath + currentTime.format(formatter);
		
		// 创建一个线程池
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();
		Robot robot = new Robot();
		screenTimer = new ScheduledThreadPoolExecutor(5);
		isStop = false;
		int i = 0;
		for (GraphicsDevice screen : screens) {
			DisplayMode displayMode = screen.getDisplayMode();
			Rectangle screenRect = screen.getDefaultConfiguration().getBounds();
			screenRect.height = displayMode.getHeight();
			screenRect.width = displayMode.getWidth();
			
			
			FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileName + i++ + ".mp4",
					displayMode.getWidth(), displayMode.getHeight(), 0);
			buildRecord(recorder);
			screenTimer.scheduleAtFixedRate(() -> {
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
					log.info("开始录制屏幕");
					recorder.record(frame);
				} catch (Exception e) {
					log.error("录屏功能出现异常：", e);
				} finally {
					// 释放资源
					// videoGraphics.dispose();
					// videoGraphics = null;
					videoImg.flush();
					videoImg = null;
					java2dConverter = null;
					// screenCapture.flush();
					// screenCapture = null;
				}
				if (isStop) {
					try {
						log.info("录制线程池结束运行");
						recorder.stop();
						recorder.release();
						recorder.close();
						Thread.sleep(1000);
						screenTimer.shutdown();
					} catch (Exception e) {
						log.error("停止录屏功能出现异常：", e);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}, 0, 1000 / frameRate, TimeUnit.MILLISECONDS);
			
		}
		
		// 线程睡眠5S 录制5S
		Thread.sleep(1000 * 30);
		log.info("执行完任务");
		isStop = true;
		Thread.sleep(1000 * 10);
		// 等待任务执行完毕
		
	}
	
	private FFmpegFrameRecorder buildRecord(FFmpegFrameRecorder recorder) throws FFmpegFrameRecorder.Exception {
		
		recorder.setVideoQuality(0);
		recorder.setVideoOption("preset", "slow");
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4); // 13
		recorder.setFormat("mp4");
		// recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2,h264,ogg,MPEG4");
		recorder.setSampleRate(44100);
		recorder.setFrameRate(frameRate);
		recorder.setVideoOption("crf", "23");
		// 2000 kb/s, 720P视频的合理比特率范围 调整低了就改变画质，降低文件的大小
		recorder.setVideoBitrate(50000); // 500 kbps
		recorder.setPixelFormat(0); // yuv420p = 0
		recorder.start();
		return recorder;
	}
	
	
}
