package com.smartteam.wisdat.ble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.smartteam.wisdat.ble.BlueToothControllerBackCall.BleStatuscallback;
import com.smartteam.wisdat.ble.entity.ScanEntity;
import com.smartteam.wisdat.ble.until.Tools;
/**
 *    所有的方法最好都在主线程调用，以免出现线程不同步的问题
 * @author Administrator
 *
 */
public class BlueToothController {
	private final static String TAG="BlueToothController";
	//蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter;
	//蓝牙管理器
	private BluetoothManager mBluetoothManager;
	//蓝牙设备回调接口
	private LeScanCallback mLeScanCallback;
	//设备信息数据集合
	private ArrayList<ScanEntity> scanEntitys;
	//上下文都系
	private Context context;
	//搜索时间控制器
	private Timer scanTimer;
	//数据回调的标示
	private boolean isScanCallback;
	//过滤方式 1 设备名字过滤
	public static final int TYPE_NAME=1;
	//过滤方式 2 设备广播包UUID过滤
	public static final int TYPE_UUID=2;
	//过滤方式 3 设备MAC过滤
	public static final int TYPE_MAC=3;
	//过滤方式
	private int filterType=1;
	//过滤条件
	private String filterCondition="";
	//蓝牙操作对象
	private BlueToothControllerBackCall backcall;
	//重连方式 1 直接连接对象
	public static final int CONNECT_OBJECT=1;
	//重连方式 2 限定搜索时间之内再连接
	public static final int CONNECT_SCAN_TIME=2;
	//重连方式 3 一直搜索到有设备再连接
	public static final int CONNECT_SCAN_UTIL=3;
	//重连添加
	private int connectType=1;
	//重连表示
	private boolean isreconnect=false;
	//重连的设备对象
	private BluetoothDevice reconnectDevice;
	//重连的时候，搜索指定时间设备列表
	private ArrayList<BluetoothDevice> devices;
	//连接的gatt对象
	private BluetoothGatt gatt;
	
	/**
	 *  初始化蓝牙相关的类
	 */
	public void initBluetooth(Context context){
		this.context=context;
		backcall=new BlueToothControllerBackCall();
		//初始化蓝牙回调
		initBlueToothCallBack();
		mBluetoothManager=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
		   //检测---是否有蓝牙4.0设备
	       if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	    	   Toast.makeText(context, "没有蓝牙4.0设备！",Toast.LENGTH_SHORT).show();
	    	   return;
	       }else{
	    	   //初始化蓝牙适配器
	    	   mBluetoothAdapter=mBluetoothManager.getAdapter();
	    	   //判断蓝牙适配器是否可用
	    	   if(mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled()){
		    	   Toast.makeText(context, "没有开启蓝牙功能！",Toast.LENGTH_SHORT).show();
	    		   return;
	    	   }else{
	    		   Log.i(TAG, "蓝牙初始化成功！");
	    		   initLeScanCallback();
	    	   }
	       }
	       
	       Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	       // If there are paired devices
	       if(pairedDevices.size()>0){
	       // Loop through paired devices
	       for(BluetoothDevice device : pairedDevices){
	       // Add the name and address to an array adapter to show in a ListView
	       		Log.e("linhao", device.getAddress()+"   "+device.getName()+"   "+device.getBondState());
//	       		BluetoothGatt connectGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
//	       			@Override
//	       			public void onConnectionStateChange(BluetoothGatt gatt,
//	       					int status, int newState) {
//	       				Log.i("linhao", status+"");
//	       			}
//				});;
	       }
	       }
	       
	}
	
	/**
	 *   系统蓝牙开关打开了，继续执行扫描
	 */
	public void reInitBluetooth(){
		if(mLeScanCallback==null){
			initLeScanCallback();
		}
			startScan(3000);
	}
		
	/**
	 *   蓝牙回调
	 */
	private void initBlueToothCallBack(){
		backcall.setBleStauscallback(new BleStatuscallback() {

			@Override
			public void Connected(BluetoothGatt gatt, BluetoothGattCharacteristic c_msg,int status) {
				// TODO Auto-generated method stub
				if(isreconnect){
					isreconnect=false;
				}
				mDeviceMsgCallback.Connected(gatt,c_msg, status);
			}

			@Override
			public void Disconnect(BluetoothGatt gatt, int status) {
				// TODO Auto-generated method stub
				if(isreconnect){
					isreconnect=false;
				}
				mDeviceMsgCallback.Disconnect(gatt, status);
			}

			@Override
			public void getRssi(BluetoothGatt gatt, int status, int rssi) {
				// TODO Auto-generated method stub
				mDeviceMsgCallback.getRssi(gatt, status, rssi);
			}

			@Override
			public void notiCallBack(BluetoothGatt gatt,BluetoothGattCharacteristic c_msg, byte[] data) {
				// TODO Auto-generated method stub
				mDeviceMsgCallback.notiCallBack(gatt,c_msg,data);
			}

			@Override
			public void onCharacteristicWrite() {
				// TODO Auto-generated method stub
				mDeviceMsgCallback.onCharacteristicWrite();
			}
			
		});
	}
	
	/**
	 *  初始化蓝牙设备回调接口
	 */
	private void initLeScanCallback(){
		if(scanEntitys!=null){
			scanEntitys.clear();
		}else{
			scanEntitys=new ArrayList<ScanEntity>();
		}
		mLeScanCallback=new LeScanCallback() {
			
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				if(device!=null){
					Log.i("LHJ",device.getName()==null?"null":device.getName() +"    "+device.getAddress());
					
					if(isreconnect){//重连
						if(connectType==CONNECT_SCAN_TIME){
							if(devices!=null){
								devices.add(device);
							}
						}else if(connectType==CONNECT_SCAN_UTIL){
							scanReconnect(device);						
						}
					}else{
						if(filterType==TYPE_UUID){
							UUIDFilter(device, rssi, scanRecord);
						}else if(filterType==TYPE_NAME){
							NAMEFilter(device, rssi, scanRecord);
						}else if(filterType==TYPE_MAC){
							MACFilter(device, rssi, scanRecord);
						}else{//过滤方式错误
							scanEntitys=null;
						}
						
						if(isScanCallback){//回调数据
							isScanCallback=false;
							if(scanEntitys!=null&&scanEntitys.size()>0){
								ArrayList<ScanEntity> scanEntityss=dealDatas(scanEntitys);
								msScanTimerDevicesCallback.getDevices(scanEntityss);							
								scanEntitys.clear();
							}else{//当搜索不到或者过滤条件不正确  不回调
								msScanTimerDevicesCallback.getDevices(null);			
							}
						}
					}
				}
			}
		};
	}
	
	/**
	 *   UUID过滤--128bit
	 * @param device  设备
	 * @param rssi  设备信号
	 * @param scanRecord  广播包
	 */
	private void UUIDFilter(BluetoothDevice device, int rssi, byte[] scanRecord){
		String[] msg=Tools.splitType(Tools.splitScanRecode(Tools.bytesToHexString(scanRecord)));
		//0:128bit  1:16bit  2:localname  3:manufacturer
		if(filterCondition.equals(msg[0])){//判断设备过滤条件
			//记录数据
			ScanEntity mScanEntity=new ScanEntity();
			mScanEntity.setMac(device.getAddress());
			mScanEntity.setRssi(rssi);
			mScanEntity.setScanRecoder(scanRecord);
			mScanEntity.setName(msg[2]);	
			mScanEntity.setDevice(device);
			mScanEntity.setScanRecodestr(Tools.splitType(Tools.splitScanRecode(Tools.bytesToHexString(scanRecord))));
			
			scanEntitys.add(mScanEntity);
		}
	}
	
	/**
	 *   设备NAME过滤
	 * @param device
	 * @param rssi
	 * @param scanRecord
	 */
	private void NAMEFilter(BluetoothDevice device, int rssi, byte[] scanRecord){
		if(filterCondition.equals(device.getName())){
			ScanEntity mScanEntity=new ScanEntity();
			mScanEntity.setDevice(device);
			mScanEntity.setMac(device.getAddress());
			mScanEntity.setName(device.getName());
			mScanEntity.setRssi(rssi);
			mScanEntity.setScanRecoder(scanRecord);
			mScanEntity.setScanRecodestr(Tools.splitType(Tools.splitScanRecode(Tools.bytesToHexString(scanRecord))));
			
			scanEntitys.add(mScanEntity);
		}
	}
	
	/**
	 *   设备MAC过滤
	 * @param device
	 * @param rssi
	 * @param scanRecord
	 */
	private void MACFilter(BluetoothDevice device, int rssi, byte[] scanRecord){
		if(filterCondition.equals(device.getAddress())){
			ScanEntity mScanEntity=new ScanEntity();
			mScanEntity.setDevice(device);
			mScanEntity.setMac(device.getAddress());
			mScanEntity.setName(device.getName());
			mScanEntity.setRssi(rssi);
			mScanEntity.setScanRecoder(scanRecord);
			mScanEntity.setScanRecodestr(Tools.splitType(Tools.splitScanRecode(Tools.bytesToHexString(scanRecord))));
			
			scanEntitys.add(mScanEntity);
		}
	}
	
	/**
	 *   设置蓝牙设备搜索设备的过滤条件	
	 * @param type   过滤方式 3 种（UUID,NAME,MAC）
	 * @param condition  过滤条件 3 种，对应于过滤方式（UUID,NAME,MAC）
	 */
	public void setFilterType(int type,String condition){
		this.filterType=type;
		this.filterCondition=condition;
	}
	
	/**
	 *   按照指定的搜索时间进行循环的搜索  
	 *   scan Second：毫秒值
	 */
	public void startScan(int scanSecond){
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			stopScan();
			scanTimer=new Timer();
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			scanTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					isScanCallback=true;
					//根据设置的间隔秒数进行数据处理
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mBluetoothAdapter.startLeScan(mLeScanCallback);
				}
			}, scanSecond, scanSecond);
		}
	}
	
	/**
	 *  停止搜索设备
	 */
	public void stopScan(){
		if(scanTimer!=null){
			scanTimer.cancel();
			scanTimer=null;
		}
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
	
	/**
	 *  释放蓝牙相关的类
	 */
	public void release(){
		//停止搜索
		stopScan();
		mBluetoothAdapter=null;
		mBluetoothManager=null;
		mLeScanCallback=null;
		scanEntitys=null;
		isScanCallback=false;
		backcall=null;
		Log.i(TAG,"释放蓝牙相关类");
		//垃圾回收
		System.gc();
	}
	
	/**
	 *  创建设备信息回调，根据设置的间隔时间，回调时间段里面的设备信息
	 */
	public interface ScanTimerDevicesCallback{
		void getDevices(ArrayList<ScanEntity> scans);
	}
	
	private ScanTimerDevicesCallback msScanTimerDevicesCallback;

	/**
	 *   搜索结果的回调
	 * @param msScanTimerDevicesCallback 搜索时间回调的设备列表
	 */
	public void setMsScanTimerDevicesCallback(
			ScanTimerDevicesCallback msScanTimerDevicesCallback) {
		this.msScanTimerDevicesCallback = msScanTimerDevicesCallback;
	}
	
	/**
	 *   3种重连方式  重连的时候停止搜索
	 * @param connectType  重连的方式 ： 直接连接CONNECT_OBJECT ； 指定时间搜索再连接CONNECT_SCAN_TIME  ； 一直搜到就连接CONNECT_SCAN_UTIL
	 * @param device  要重连的对象 
	 * @param scanTime  指定搜索时间  ms单位 只对CONNECT_SCAN_TIME有用
	 */
	public void reconnect(final int connectType,final BluetoothDevice device,final int scanTime){
		((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				isreconnect=true;
				BlueToothController.this.connectType=connectType;
				BlueToothController.this.reconnectDevice=device;
				stopScan();
				switch (connectType) {
				case CONNECT_OBJECT:
					if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
						gatt=device.connectGatt(context, false, backcall);
					}
					break;

				case CONNECT_SCAN_TIME:
					reStartScanTime(scanTime);
					break;
					
				case CONNECT_SCAN_UTIL:
					reStartScanUtil();
					break;
				}
			}
		});
	
	}
	
	/**
	 *   一直搜索：搜索到的设备继续进行与重连的操作
	 * @param device 设备
	 */
	private void scanReconnect(final BluetoothDevice device){
		((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(reconnectDevice.getAddress().equals(device.getAddress())){
					stopScan();
					gatt=device.connectGatt(context, false, backcall);
				}
			}
		});
	}

	/**
	 *   指定时间搜索：搜索到的所有设备进行过滤与重连的操作
	 * @param devices  设备列表  
	 */
	private void scanTimeReconnect(final ArrayList<BluetoothDevice> devices){
	
		((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				stopScan();
				for(BluetoothDevice device:devices){
					if(reconnectDevice.getAddress().equals(device.getAddress())){
						gatt=device.connectGatt(context, false, backcall);
						break;
					}
				}
			}
		});
	}
		
	/**
	 *   指定时间搜索要重连的设备
	 * @param scanSecond
	 */
	private void reStartScanTime(int scanSecond){
		if(devices!=null){
			devices.clear();
		}
		devices=new ArrayList<BluetoothDevice>();
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			scanTimer=new Timer();
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			scanTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					scanTimeReconnect(devices);
				}
			}, scanSecond);
		}
	}
	
	/**
	 *   一直搜索，搜索到就连接
	 */
	private void reStartScanUtil(){
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			stopScan();//停止搜索
			scanTimer=new Timer();
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			scanTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					//根据设置的间隔秒数进行数据处理
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mBluetoothAdapter.startLeScan(mLeScanCallback);
				}
			}, 3000, 3000);
		}
	}
	
	/**
	 *   连接指定设备,如果不需要noti,把Char_NOTI_UUID设为null或者空。
	 * @param device
	 * @param Service_UUID
	 * @param Char_NOTI_UUID
	 * @param Char_MSG_UUID
	 */
	public void connect(BluetoothDevice device,String Service_UUID,String Char_NOTI_UUID,String Char_MSG_UUID,boolean autoConnect){
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			backcall.setUUID(Service_UUID, Char_NOTI_UUID, Char_MSG_UUID);
			gatt=device.connectGatt(context, autoConnect, backcall);
			
		 }
	}

	/**
	 *   向下位机写入数据
	 * @param characteristic  写数据的特征项
	 * @param bytes  数据或者命令
	 * @return  成功返回true
	 */
	public boolean wirteValue(BluetoothGattCharacteristic characteristic,byte[] bytes){
		if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()){
			Log.i("命令", Arrays.toString(bytes));
			return backcall.wirteValue(characteristic, bytes);
		}else{
			return false;
		}
	}
	
	/**
	 *   设备连接状态的回调与交换信息的回调
	 */
	public interface DeviceMsgCallBack{
		void Connected(BluetoothGatt gatt,BluetoothGattCharacteristic c_msg, int status);
		void Disconnect(BluetoothGatt gatt, int status);
		void getRssi(BluetoothGatt gatt, int status,int rssi);
		void notiCallBack(BluetoothGatt gatt,BluetoothGattCharacteristic c_msg,byte[] data);
		void onCharacteristicWrite();
	}

	//设备信息回调对象
	private DeviceMsgCallBack mDeviceMsgCallback;
	
	/**
	 *   初始化回调，监听连上设备的连接状态或者通信
	 * @param mDeviceMsgCallback
	 */
	public void setmDeviceMsgCallback(DeviceMsgCallBack mDeviceMsgCallback) {
		this.mDeviceMsgCallback = mDeviceMsgCallback;
	}
	
	/**
	 *   过滤相同的设备
	 * @param scans
	 */
	private ArrayList<ScanEntity> dealDatas(ArrayList<ScanEntity> scans){
		ArrayList<ScanEntity> s=null;
		if(scans!=null&&scans.size()>0){
			//倒序列表
			Collections.reverse(scans);
			//去除相同mac的对象
			HashSet<ScanEntity> hs=new HashSet<ScanEntity>(scans);
			s=new ArrayList<ScanEntity>();
			for(ScanEntity scan:hs){
				s.add(scan);
			}
		}else{
			return s;
		}
		return s;
	}

	/**
	 *   断开设备
	 */
	public void accordDisConnect(){
		Log.e("主动断开设备gatt", ""+(gatt==null));
		if(gatt!=null){
			gatt.disconnect();
		}
	}
}

