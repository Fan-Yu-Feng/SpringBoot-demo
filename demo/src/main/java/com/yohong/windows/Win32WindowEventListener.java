package com.yohong.windows;


import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.DBT.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser.HDEVNOTIFY;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fanyufeng
 * @version 1.0
 * @description 监听Windows的系统事件：锁屏、开屏、设备等
 * @date 2024/5/6 15:50
 */
@Slf4j
public class Win32WindowEventListener implements WindowProc {
	
	
	WinScreenRecord winScreenRecord;
	
	WinScreenRecordSizeDesc winScreenRecordSizeDesc;
	
	/**
	 * Instantiates a new win32 window test.
	 */
	public Win32WindowEventListener() {
		// winScreenRecord = new WinScreenRecord();
		winScreenRecordSizeDesc = new WinScreenRecordSizeDesc();
		// 开启录屏
		startScreenRecord();
		// define new window class
		String windowClass = "MyWindowClass";
		HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");
		
		WNDCLASSEX wClass = new WNDCLASSEX();
		wClass.hInstance = hInst;
		wClass.lpfnWndProc = Win32WindowEventListener.this;
		wClass.lpszClassName = windowClass;
		
		// register window class
		User32.INSTANCE.RegisterClassEx(wClass);
		getLastError();
		
		// create new window 创建了一个隐藏的窗口 hWnd，用于接收系统事件。
		HWND hWnd = User32.INSTANCE
				            .CreateWindowEx(
						            User32.WS_EX_TOPMOST,
						            windowClass,
						            "My hidden helper window, used only to catch the windows events",
						            0, 0, 0, 0, 0,
						            null, // WM_DEVICECHANGE contradicts parent=WinUser.HWND_MESSAGE
						            null, hInst, null);
		
		getLastError();
		log.info("window sucessfully created! window hwnd: "
				         + hWnd.getPointer().toString());
		// 注册监听Windows会话状态变化的通知，包括屏幕锁定和解锁事件。
		Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hWnd,
				Wtsapi32.NOTIFY_FOR_THIS_SESSION);
		
		/* this filters for all device classes */
		// DEV_BROADCAST_HDR notificationFilter = new DEV_BROADCAST_HDR();
		// notificationFilter.dbch_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;
		
		/* this filters for all usb device classes */
		DEV_BROADCAST_DEVICEINTERFACE notificationFilter = new DEV_BROADCAST_DEVICEINTERFACE();
		notificationFilter.dbcc_size = notificationFilter.size();
		notificationFilter.dbcc_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;
		notificationFilter.dbcc_classguid = DBT.GUID_DEVINTERFACE_USB_DEVICE;
		
		/*
		 * use User32.DEVICE_NOTIFY_ALL_INTERFACE_CLASSES instead of
		 * DEVICE_NOTIFY_WINDOW_HANDLE to ignore the dbcc_classguid value
		 * 注册监听设备变化的通知，包括USB设备的插拔事件。
		 */
		HDEVNOTIFY hDevNotify = User32.INSTANCE.RegisterDeviceNotification(
				hWnd, notificationFilter, User32.DEVICE_NOTIFY_WINDOW_HANDLE);
		
		getLastError();
		if (hDevNotify != null) {
			log.info("RegisterDeviceNotification was sucessfully!");
		}
		
		MSG msg = new MSG();
		while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0) {
			User32.INSTANCE.TranslateMessage(msg);
			User32.INSTANCE.DispatchMessage(msg);
		}
		
		User32.INSTANCE.UnregisterDeviceNotification(hDevNotify);
		Wtsapi32.INSTANCE.WTSUnRegisterSessionNotification(hWnd);
		User32.INSTANCE.UnregisterClass(windowClass, hInst);
		User32.INSTANCE.DestroyWindow(hWnd);
		
		log.info("program exit!");
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.sun.jna.platform.win32.User32.WindowProc#callback(com.sun.jna.platform
	 * .win32.WinDef.HWND, int, com.sun.jna.platform.win32.WinDef.WPARAM,
	 * com.sun.jna.platform.win32.WinDef.LPARAM)
	 */
	@Override
	public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
		switch (uMsg) {
			case WinUser.WM_CREATE: {
				onCreate(wParam, lParam);
				return new LRESULT(0);
			}
			case WinUser.WM_DESTROY: {
				User32.INSTANCE.PostQuitMessage(0);
				return new LRESULT(0);
			}
			case WinUser.WM_SESSION_CHANGE: {
				this.onSessionChange(wParam, lParam);
				return new LRESULT(0);
			}
			case WinUser.WM_DEVICECHANGE: {
				LRESULT lResult = this.onDeviceChange(wParam, lParam);
				return lResult != null ? lResult :
						       User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
			}
			default:
				return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
		}
	}
	
	/**
	 * Gets the last error.
	 *
	 * @return the last error
	 */
	public final int getLastError() {
		int rc = Kernel32.INSTANCE.GetLastError();
		
		if (rc != 0) {
			log.info("error: " + rc);
		}
		
		return rc;
	}
	
	/**
	 * On session change.
	 *
	 * @param wParam the w param
	 * @param lParam the l param
	 */
	protected void onSessionChange(WPARAM wParam, LPARAM lParam) {
		switch (wParam.intValue()) {
			case Wtsapi32.WTS_SESSION_LOCK: {
				log.info(" WTS_SESSION_LOCK ");
				this.onMachineLocked(lParam.intValue());
				this.stopScreenRecord();
				break;
			}
			case Wtsapi32.WTS_SESSION_UNLOCK: {
				this.startScreenRecord();
				log.info(" WTS_SESSION_UNLOCK ");
				this.onMachineUnlocked(lParam.intValue());
				break;
			}
			case Wtsapi32.WTS_CONSOLE_CONNECT: {
				this.onConsoleConnect(lParam.intValue());
				break;
			}
			case Wtsapi32.WTS_CONSOLE_DISCONNECT: {
				this.onConsoleDisconnect(lParam.intValue());
				break;
			}
			case Wtsapi32.WTS_SESSION_LOGON: {
				this.onMachineLogon(lParam.intValue());
				break;
			}
			case Wtsapi32.WTS_SESSION_LOGOFF: {
				this.onMachineLogoff(lParam.intValue());
				break;
			}
			
			default:
				break;
			
		}
	}
	
	/**
	 * On console connect.
	 *
	 * @param sessionId the session id
	 */
	protected void onConsoleConnect(int sessionId) {
		log.info("onConsoleConnect: " + sessionId);
	}
	
	/**
	 * On console disconnect.
	 *
	 * @param sessionId the session id
	 */
	protected void onConsoleDisconnect(int sessionId) {
		log.info("onConsoleDisconnect: " + sessionId);
	}
	
	/**
	 * On machine locked.
	 *
	 * @param sessionId the session id
	 */
	protected void onMachineLocked(int sessionId) {
		log.info("onMachineLocked: " + sessionId);
	}
	
	/**
	 * On machine unlocked.
	 *
	 * @param sessionId the session id
	 */
	protected void onMachineUnlocked(int sessionId) {
		log.info("onMachineUnlocked: " + sessionId);
	}
	
	/**
	 * On machine logon.
	 *
	 * @param sessionId the session id
	 */
	protected void onMachineLogon(int sessionId) {
		log.info("onMachineLogon: " + sessionId);
	}
	
	/**
	 * On machine logoff.
	 *
	 * @param sessionId the session id
	 */
	protected void onMachineLogoff(int sessionId) {
		log.info("onMachineLogoff: " + sessionId);
	}
	
	/**
	 * On device change.
	 *
	 * @param wParam the w param
	 * @param lParam the l param
	 *
	 * @return the result. Null if the message is not processed.
	 */
	protected LRESULT onDeviceChange(WPARAM wParam, LPARAM lParam) {
		switch (wParam.intValue()) {
			case DBT.DBT_DEVICEARRIVAL: {
				return onDeviceChangeArrival(lParam);
			}
			case DBT.DBT_DEVICEREMOVECOMPLETE: {
				return onDeviceChangeRemoveComplete(lParam);
			}
			case DBT.DBT_DEVNODES_CHANGED: {
				//lParam is 0 for this wParam
				return onDeviceChangeNodesChanged();
			}
			default:
				log.info(
						"Message WM_DEVICECHANGE message received, value unhandled.");
		}
		return null;
	}
	
	protected LRESULT onDeviceChangeArrivalOrRemoveComplete(LPARAM lParam, String action) {
		DEV_BROADCAST_HDR bhdr = new DEV_BROADCAST_HDR(lParam.longValue());
		switch (bhdr.dbch_devicetype) {
			case DBT.DBT_DEVTYP_DEVICEINTERFACE: {
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363244.aspx
				DEV_BROADCAST_DEVICEINTERFACE bdif = new DEV_BROADCAST_DEVICEINTERFACE(bhdr.getPointer());
				log.info("BROADCAST_DEVICEINTERFACE: " + action);
				log.info("dbcc_devicetype: " + bdif.dbcc_devicetype);
				log.info("dbcc_name:       " + bdif.getDbcc_name());
				log.info("dbcc_classguid:  " + bdif.dbcc_classguid.toGuidString());
				break;
			}
			case DBT.DBT_DEVTYP_HANDLE: {
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363245.aspx
				DEV_BROADCAST_HANDLE bhd = new DEV_BROADCAST_HANDLE(bhdr.getPointer());
				log.info("BROADCAST_HANDLE: " + action);
				break;
			}
			case DBT.DBT_DEVTYP_OEM: {
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363247.aspx
				DEV_BROADCAST_OEM boem = new DEV_BROADCAST_OEM(bhdr.getPointer());
				log.info("BROADCAST_OEM: " + action);
				break;
			}
			case DBT.DBT_DEVTYP_PORT: {
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363248.aspx
				DEV_BROADCAST_PORT bpt = new DEV_BROADCAST_PORT(bhdr.getPointer());
				log.info("BROADCAST_PORT:  " + action);
				log.info("dbcp_devicetype: " + bpt.dbcp_devicetype);
				log.info("dbcp_name:       " + bpt.getDbcpName());
				break;
			}
			case DBT.DBT_DEVTYP_VOLUME: {
				// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363249.aspx
				DEV_BROADCAST_VOLUME bvl = new DEV_BROADCAST_VOLUME(bhdr.getPointer());
				int logicalDriveAffected = bvl.dbcv_unitmask;
				short flag = bvl.dbcv_flags;
				boolean isMediaNotPhysical = 0 != (flag & DBT.DBTF_MEDIA/*value is 1*/);
				boolean isNet = 0 != (flag & DBT.DBTF_NET/*value is 2*/);
				log.info(action);
				int driveLetterIndex = 0;
				while (logicalDriveAffected != 0) {
					if (0 != (logicalDriveAffected & 1)) {
						log.info("Logical Drive Letter: " +
								         ((char) ('A' + driveLetterIndex)));
					}
					logicalDriveAffected >>>= 1;
					driveLetterIndex++;
				}
				log.info("isMediaNotPhysical:" + isMediaNotPhysical);
				log.info("isNet:" + isNet);
				break;
			}
			default:
				return null;
		}
		// return TRUE means processed message for this wParam.
		// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363205.aspx
		// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363208.aspx
		return new LRESULT(1);
	}
	
	protected LRESULT onDeviceChangeArrival(LPARAM lParam) {
		return onDeviceChangeArrivalOrRemoveComplete(lParam, "Arrival");
	}
	
	protected LRESULT onDeviceChangeRemoveComplete(LPARAM lParam) {
		return onDeviceChangeArrivalOrRemoveComplete(lParam, "Remove Complete");
	}
	
	protected LRESULT onDeviceChangeNodesChanged() {
		log.info("Message DBT_DEVNODES_CHANGED");
		// return TRUE means processed message for this wParam.
		// see http://msdn.microsoft.com/en-us/library/windows/desktop/aa363211.aspx
		return new LRESULT(1);
	}
	
	/**
	 * On create.
	 *
	 * @param wParam the w param
	 * @param lParam the l param
	 */
	protected void onCreate(WPARAM wParam, LPARAM lParam) {
		log.info("onCreate: WM_CREATE");
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void main(String[] args) {
		new Win32WindowEventListener();
	}
	
	
	public void startScreenRecord() {
		if (this.winScreenRecordSizeDesc != null) {
			this.winScreenRecordSizeDesc.start();
			return;
		}
		this.winScreenRecordSizeDesc = new WinScreenRecordSizeDesc();
		this.winScreenRecordSizeDesc.start();
	}
	
	public void stopScreenRecord() {
		if(this.winScreenRecordSizeDesc != null){
			this.winScreenRecordSizeDesc.stop();
			return;
		}
		this.winScreenRecordSizeDesc = new WinScreenRecordSizeDesc();
		this.winScreenRecordSizeDesc.stop();
	}
	
	
}