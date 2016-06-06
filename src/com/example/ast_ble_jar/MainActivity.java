package com.example.ast_ble_jar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartteam.wisdat.ble.BlueToothController;
import com.smartteam.wisdat.ble.BlueToothController.DeviceMsgCallBack;
import com.smartteam.wisdat.ble.BlueToothController.ScanTimerDevicesCallback;
import com.smartteam.wisdat.ble.entity.ScanEntity;

public class MainActivity extends Activity {
	private BlueToothController mBlueToothController;
	public static final String SER_UUID="0000fff0-0000-1000-8000-00805f9b34fb";
	public static final String RW_CHARAC_UUID="0000fff1-0000-1000-8000-00805f9b34fb";
	public static final String NOTI_CHARAC_UUID="0000fff2-0000-1000-8000-00805f9b34fb";
	public static final String NOTI_DESC_UUID="00002902-0000-1000-8000-00805f9b34fb";
	
	public static final String INDICATE_CHARAC_UUID="0000fff3-0000-1000-8000-00805f9b34fb";
	public static final String INDICATE_DESC_UUID="00002902-0000-1000-8000-00805f9b34fb";
	private ScanEntity mEntity;
	private String macLast="";
	private boolean reConnect=false;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
	private int randomInt=0;
	
	private LinearLayout linearLayout4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initBlue();
	
		initView();
	}
	
	private void initView(){
		linearLayout4=(LinearLayout) findViewById(R.id.linearLayout4);
	}
	
	private void addTextView(final String text,final String color){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time=sf.format(new Date());
				TextView tv=new TextView(MainActivity.this);
				tv.setText(text +"  "+time);
				tv.setTextColor(Color.parseColor(color));
				tv.setTextSize(12);
				linearLayout4.addView(tv);
			}
		});
	}

	/**
	 *  初始化蓝牙
	 */
	private void initBlue() {
		// 初始化蓝牙操作类
		mBlueToothController=new BlueToothController();	
		mBlueToothController.initBluetooth(this);
		//设置搜索过滤的条件
		mBlueToothController.setFilterType(BlueToothController.TYPE_MAC, "84:EB:18:0D:A8:54");
		//添加搜索过滤结果的回调
		mBlueToothController.setMsScanTimerDevicesCallback(new ScanTimerDevicesCallback() {
			
			@Override
			public void getDevices(final ArrayList<ScanEntity> scans) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(scans!=null&&scans.size()>0){
							mBlueToothController.stopScan();
							mEntity=scans.get(0);
							int passcode=0;
							if((mEntity.getScanRecoder()[31]&0xFF)==(0x04) && (mEntity.getScanRecoder()[32])==0x16){
								passcode=Tools.getIntFrom3Byte(new byte[]{mEntity.getScanRecoder()[33],mEntity.getScanRecoder()[34],mEntity.getScanRecoder()[35]});
							}
							int bndbyte=mEntity.getScanRecoder()[30] & 0xFF;
							mEntity.setBindbyte(bndbyte);
							mEntity.setPassCode(passcode);
							Log.e("linhaojian", mEntity.toString());
							addTextView(mEntity.toString(),"#000000");
							judgeConnect(mEntity);
						}
					}
				});
			}
		});
		//添加连接上设备的通信回调
		mBlueToothController.setmDeviceMsgCallback(new DeviceMsgCallBack() {
			
			@Override
			public void notiCallBack(BluetoothGatt gatt, BluetoothGattCharacteristic c_msg,byte[] data) {
//				mBluetoothGattCharacteristic=gatt.getService(UUID.fromString(SER_UUID)).getCharacteristic(UUID.fromString(RW_CHARAC_UUID));
				dataCallBack(data);
			}
			
			@Override
			public void getRssi(BluetoothGatt gatt, int status, int rssi) {}
			
			@Override
			public void Disconnect(final BluetoothGatt gatt,final int status) {
				mBluetoothGattCharacteristic=null;
				disConnect();
				addTextView("断开连接，状态为："+status,"#ff0000");
			}
			
			@Override
			public void Connected(final BluetoothGatt gatt,
					final BluetoothGattCharacteristic c_msg, int status) {
				mBluetoothGattCharacteristic=c_msg;
				Log.e("linhaojian", "连接成功");
				addTextView("连接成功","#ff0000");
				connectedFlow();
			}

			@Override
			public void onCharacteristicWrite() {}
		});
		//设置连接搜索时间(循环)
		mBlueToothController.startScan(3000);
	}
	
	/**
	 *   判断是使用什么样的连接方式
	 */
	private void judgeConnect(ScanEntity entity){
		if(macLast.equals(entity.getMac())&&entity.getBindbyte()==0x1A){
			ReConnectFlow(entity);
		}else{
			ConnectFlow(entity);
		}
	}
	
	/**
	 *   第一次连接流程
	 */
	private void ConnectFlow(ScanEntity entity){
		reConnect=false;
		mBlueToothController.connect(entity.getDevice(), SER_UUID, NOTI_CHARAC_UUID, RW_CHARAC_UUID, false);
		Log.e("linhaojian", "连接中....");
		addTextView("连接中....","#ff0000");
	}
	
	/**
	 *   重连流程
	 */
	private void ReConnectFlow(ScanEntity entity){
		reConnect=true;
		mBlueToothController.connect(entity.getDevice(), SER_UUID, NOTI_CHARAC_UUID, RW_CHARAC_UUID, false);
		Log.e("linhaojian", "重连中...");
		addTextView("重连中...","#ff0000");
	}

	/**
	 *   连接成功之后的流程：发送指令
	 */
	private void connectedFlow(){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(reConnect){
//					new Handler().postDelayed(new Runnable() {
//									
//									@Override
//									public void run() {
								sendReConnect();
//									}
//					}, 5000);
				}else{
//					new Handler().postDelayed(new Runnable() {
//						
//						@Override
//						public void run() {
							sendBindCode();
//						}
//					}, 8000);
				}
			}
		});
		
	}
	
	/**
	 *   断开处理
	 */
	private void disConnect(){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//设置连接搜索时间(循环)
				mBlueToothController.startScan(3000);
			}
		});
	}
	
	/**
	 *   数据回调处理
	 * @param datas
	 */
	private void dataCallBack(final byte[] datas){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<datas.length;i++){
					sb.append(Integer.toHexString(datas[i] & 0xFF)+" , ");
				}
				Log.w("linhaojian","回调的命令："+sb.toString());
				if(datas[0]==0x01){
					Log.e("linhaojian", "0x01:手环端响应绑定指令成功");
					addTextView("0x01:手环端响应绑定指令成功","#ff0000");
				}else if(datas[0]==0x17){
					Log.e("linhaojian", "0x17:手环端响应系统指令成功");
					addTextView("0x17:手环端响应系统指令成功","#ff0000");
					sendTimeFormat();
				}else if(datas[0]==0x19){
					Log.e("linhaojian", "0x19:手环端响应时间格式指令成功");
					addTextView("0x19:手环端响应时间格式指令成功","#ff0000");
					sendTime1();
				}else if(datas[0]==0x50){
					Log.e("linhaojian", "0x50:手环端响应时间指令成功");
					addTextView("0x50:手环端响应时间指令成功","#ff0000");
					macLast=mEntity.getMac();
				}else if(datas[0]==0x04){
					Log.e("linhaojian", "0x04:手环端响应重连指令成功");
					addTextView("0x04:手环端响应重连指令成功","#ff0000");
					sendTime1();
				}else if(datas[0]==0x02){
					if(responseBind()){
						sendAndroid();
					}
				}
			}
		});
	}
	
	////////////////////////////////////  第一次连接流程  //////////////////////////////////  
	
	/**
	 *   发送绑定码
	 */
	private void sendBindCode(){
		Random random =new Random(); 
		randomInt=random.nextInt(10000)+1; 
		byte[] bt=TransUtil.getSBindCode(Tools.get3Byte(randomInt));
		boolean is=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, bt);
		Log.e("linhaojian","发送绑定码："+is);
		
		addTextView("发送绑定码："+is,"#ff0000");
	}
	
	private boolean isAndroid=false;
	/**
	 *  发送系统
	 */
	private void sendAndroid(){
		isAndroid=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getAndroid());
		Log.e("linhaojian","发送系统："+isAndroid);
		addTextView("发送系统："+isAndroid,"#ff0000");
		while(!isAndroid){
			isAndroid=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getAndroid());
			Log.e("linhaojian","发送系统："+isAndroid);
			addTextView("发送系统："+isAndroid,"#ff0000");
		}
	}
	
	/**
	 *   发送时间格式
	 */
	private void sendTimeFormat(){
		int time12or24 = 24;
		byte data=0x00;//BIT0：0表示24小时制；1表示12小时制； BIT1：0表示月-日；1表示日-月。
		
		byte[] dataArray=new byte[5];

		byte[] stepArray=Tools.get2Byte(10000);
		byte[] sleepArray=Tools.get2Byte((int)(7.5*60));
		dataArray[1]=stepArray[0];
		dataArray[2]=stepArray[1];
		dataArray[3]=sleepArray[0];
		dataArray[4]=sleepArray[1];
		dataArray[0]=data;
		boolean is=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getSTimeFormat(dataArray));
		
		Log.e("linhaojian","发送时间格式："+is);
		
		addTextView("发送时间格式："+is,"#ff0000");
	}
	
	/**
	 *  发送系统时间
	 */
	private void sendTime(){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Log.e("linhaojian","发送系统时间："+mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getSSynctime()));
			}
		}, 6000);
	}
	private void sendTime1(){
		boolean is=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getSSynctime());
		Log.e("linhaojian","发送系统时间："+is);
		addTextView("发送系统时间："+is,"#ff0000");
	}
	
	
    ///////////////////////////////////  重连连接流程  //////////////////////////////////  
	
	/**
	 *   发送重新连接
	 */
	private void sendReConnect(){
		byte[] bt=Tools.get3Byte(randomInt);
		boolean is=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getSReconnect(bt));
		Log.e("linhaojian","发送重连："+is);
		addTextView("发送重连："+is,"#ff0000");
	}
	
	/**
	 *   响应下位机的0x02
	 */
	private boolean responseBind(){
		boolean isRes=mBlueToothController.wirteValue(mBluetoothGattCharacteristic, TransUtil.getSResponseBind());
		Log.e("linhaojian","发送响应下位机的0x02："+isRes);
		addTextView("发送响应下位机的0x02："+isRes,"#ff0000");
		return isRes;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("fuck", "onDestroy");
	}
}

