# SpringBoot-demo

A SpringBoot Demo 项目，主要用于开发实践


## 扩展录屏功能
原理： 
1. 通过java自带的robot方法按一定频率进行截屏，先驻留内存中，存为一段一段的 Frame
2. 使用 FFmpegFrameRecorder 将图片流转为视频。
代码启动路径：`screen.VideoRecorderTest.recordTest`


## 锁屏、开屏事件进行屏幕录制
代码启动路径：`com.yohong.windows.Win32WindowEventListener.main`
功能
1. 监听 windows 锁屏事件，在锁屏时停止录屏，屏幕解锁自动启动录屏
2. 支持更小体积的录屏操作




