# SpringBoot-demo

A SpringBoot Demo 项目，主要用于开发实践


## 扩展录屏功能
原理： 
1. 通过java自带的robot方法按一定频率进行截屏，先驻留内存中，存为一段一段的 Frame
2. 使用 FFmpegFrameRecorder 将图片流转为视频。
由于工作需求，需要开发录屏的功能，同时支持扩展屏
代码启动路径：`screen.VideoRecorderTest.recordTest`



### 存在不足


