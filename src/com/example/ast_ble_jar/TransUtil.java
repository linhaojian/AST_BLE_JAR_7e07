package com.example.ast_ble_jar;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class TransUtil {

	//��������
	public static byte calcuCheckSum(int type,int length,byte[] data){
		int LENGTH=0;
		if(data!=null && data.length!=0){
			LENGTH=data.length;
		}
		
		byte mByte=0;
		int sum=0;
		for(int i=0;i<LENGTH;i++){
			sum+=data[i];
		}
		sum=sum+type+length;		
		mByte=(byte) (sum & 0xFF);
		return mByte;
	}
	
	public static byte[] format(int type,int length,byte[] data){
		int LENGTH=0;
		if(data!=null && data.length!=0){
			LENGTH=data.length;
		}
		byte[] array=new byte[LENGTH+3];
		byte mCheck=calcuCheckSum(type,length,data);
		
		for(int i=0;i<LENGTH+3;i++){
			if(i==0){
				array[i]=(byte) type;
			}else if(i==1){
				array[i]=(byte)length ;
			}else if(i==LENGTH+3-1){
				array[i]=mCheck;
			}else{
				array[i]=(byte) data[i-2];
			}
			
//			System.out.println(array[i]&0xFF);
		}
		
//		byte[] arr=new byte[20];
//		for(int i=0;i<20;i++){
//			if(i<LENGTH+3){
//				arr[i]=(byte) array[i];
//				System.out.print("\t"+arr[i]);
//			}else{
//				arr[i]=0x00;
//			}
//		}
		
		return array;
	}
	//*****************************************************
	//0x01:�ֻ��˷��Ͱ�ָ��
	public static byte[] getSBindCode(byte[] data){
		return format(0x01,0x03,data);
	}
	
	//0x02:�ֻ�����Ӧȷ�ϰ�ָ��
	public static byte[] getSResponseBind(){
		return format(0x02,0x00,null);
	}
	
	//0x03:�ֻ��˷��ͽ��ָ��
	public static byte[] getSUnbindCode(){
		return format(0x03,0x00,null);
	}
	
	//0x04:�ֻ��˷�������ָ��
	public static byte[] getSReconnect(byte[] data){
		return format(0x04,0x03,data);
	}
	//*****************************************************
	//������ϢЭ��
	//0x10:�ֻ��˷��;�������ָ��
	public static byte[] getSSendentary(byte[] data){
		return format(0x10,0x05,data);
	}
	//0x11:�ֻ��˷��ͺ�ˮ����ָ��
	public static byte[] getSDrinkwater(byte[] data){
		return format(0x11,0x05,data);
	}
	//0x12:�ֻ��˷�����������1ָ��
	public static byte[] getSAlarm1(byte[] data){
		return format(0x12,0x04,data);
	}
	//0x13:�ֻ��˷�����������2ָ��
	public static byte[] getSAlarm2(byte[] data){
		return format(0x13,0x04,data);
	}
	
	//0x14:�ֻ��˷������λ��ָ��
	public static byte[] getSWearPosition(byte data){
		return format(0x14,0x01,new byte[]{data});
	}
	//0x15:�ֻ��˷����û���Ϣָ��
	public static byte[] getSUserInfo(byte[] data){
		return format(0x15,0x11,data);
	}
	//0x16:�ֻ��˷������÷���ָ��
	public static byte[] getSAntiLost(byte enable){
		return format(0x16,0x01,new byte[]{enable});
	}
	//0x17:�ֻ��˷���ϵͳ����ָ��:01 IOS;02 Android
	public static byte[] getAndroid(){
		return format(0x17,0x01,new byte[]{0x02});
	}
	//����ǰ��0x19:�ֻ��˷���ʱ�Ӹ�ʽָ��  data:BIT0��0��ʾ24Сʱ�ƣ�1��ʾ12Сʱ�ƣ� BIT1��0��ʾ��-�գ�1��ʾ��-�¡�
	public static byte[] getSTimeFormat(byte data){
		return format(0x19,0x01,new byte[]{data});
	}
	
	/**
	 * ������0x19:
	 * @param data ʱ���ʽ(1byte)+����Ŀ��(2byte)+˯��Ŀ��(2byte)
	 * 		����Ŀ�꣺Э���λΪĿ��ֵ��λ��Э���λΪĿ��ֵ��λ
	 *		˯��Ŀ�꣺Э���λΪĿ��ֵ��λ��Э���λΪĿ��ֵ��λ
	 *
	 * @return
	 */
	public static byte[] getSTimeFormat(byte[] data){
		return format(0x19,0x05,data);
	}
	
	/**
	 * 7E07
	 * 0x1A:�ֻ��˷���Ŀ��ָ��
	 * @param data (����Ŀ��(2byte)+˯��Ŀ��(2byte))
	 * 		����Ŀ�꣺Э���λΪĿ��ֵ��λ��Э���λΪĿ��ֵ��λ
	 * 		˯��Ŀ�꣺Э���λΪĿ��ֵ��λ��Э���λΪĿ��ֵ��λ
	 * 
	 */
	public static byte[] getSGoal(byte[] data){
		return format(0x1A,0x04,data);
	}
	//*****************************************************
	//��ȡ������Ϣָ��
	//0x20:�ֻ��˷��Ͷ�ȡ��������ָ��
	public static byte[] getRSendentary(){
		return format(0x20,0x00,null);
	}
	//0x21:�ֻ��˷��Ͷ�ȡ��ˮ����ָ��
	public static byte[] getRDrinkwater(){
		return format(0x21,0x00,null);
	}
	//0x22:�ֻ��˷��Ͷ�ȡ��������1ָ��
	public static byte[] getRAlarm1(){
		return format(0x22,0x00,null);
	}
	//0x23:�ֻ��˷��Ͷ�ȡ��������2ָ��
	public static byte[] getRAlarm2(){
		return format(0x23,0x00,null);
	}
	//0x24:�ֻ��˷��Ͷ�ȡ���λ��ָ��
	public static byte[] getRWearPosition(){
		return format(0x24,0x00,null);
	}
	//0x25:�ֻ��˷��Ͷ�ȡ�û���Ϣָ��
	public static byte[] getRUserInfo(byte data){
		return format(0x25,0x01,new byte[]{data});
	}
	
	//0x26:��ȡ�ֻ�����ָ��
	public static byte[] getRPower(){
		return format(0x26,0x00,null);
	}
	//0x27:�ֻ��˷��Ͷ�ȡ���÷���ָ��
	public static byte[] getRAntiLost(){
		return format(0x27,0x00,null);
	}
	//*****************************************************
	//��Ϣ����Э��
	//0x30:�ֻ��˷�����Ϣ����ָ��
	public static byte[] getSPushInfo(byte[] data){
		return format(0x30,0x02,data);
	}
	
	
	//˫��Ѱ��Э��
	//0x40:�ֻ��˷���Ѱ���ֻ�ָ��
	public static byte[] getSSearch(){
		return format(0x40,0x00,null);
	}
	//0x41:�ֻ�����Ӧ����ң������ָ��
	public static byte[] getSTakePhoto(){
		return format(0x41,0x00,null);
	}
	
	
	//��ȡͬ��ʱ���ʱ���ֽ�����
	public static byte[] syncTime(){
		byte []time=new byte[6];//��	��	ʱ	��	��	��
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss");
		String[] str=sdf.format(new Date()).split("-");
		for(int i=str.length-1;i>=0;i--){
			if(i==0){
				time[5-i]=(byte) (Integer.parseInt(str[i])-2000);
			}else{
				time[5-i]=(byte) Integer.parseInt(str[i]);
			}
			
		}
		for(int i=0;i<time.length;i++){
			Log.e("", i+"---"+(time[i]&0xFF));
		}
		return time;
	}
	
	//ͬ��ʱ��Э��
	//0x50:�ֻ��˷���ͬ��ʱ��ָ��
	public static byte[] getSSynctime(){
		return format(0x50,0x06,syncTime());
	}
	//0x51:�ֻ��˻�ȡ�ֻ���ǰʱ��ָ��
	public static byte[] getReadTime(){
		return format(0x51,0x00,null);
	}
	//*****************************************************
	//�����ϴ�Э��
	//0x60:�ֻ��˷��������ϴ�ָ�����һ�����ϴ���
	public static byte[] getRUploadsingle(byte[] data){
		return format(0x60,0x06,data);
	}
	
	//0x61:�ֻ��˷��������ϴ�ָ����������ϴ���
	public static byte[] getRUploadmulti(byte[] data){
		return format(0x61,0x06,data);
	}
	
	//0x62:�ֻ��˷��������ϴ����ָ����������ϴ���
	public static byte[] getSUploadmultiFinish(){
		return format(0x62,0x00,null);
	}
	//0x63:�ֻ��˷��������ϴ�����ָ��
	public static byte[] getSUploadrequest(){
		return format(0x63,0x00,null);
	}
	//0x64:�ֻ��˷��ͻ�ȡ�ֻ������˶����ݵ�����ָ��
	public static byte[] getRSportDate(){
		return format(0x64,0x00,null);
	}
	//0x65:�ֻ��˷��ͻ�ȡ�ֻ���ǰ�������ݵķ�������ָ��
	public static byte[] getRDataMinSum(){
		return format(0x65,0x00,null);
	}
	//0x66:0x66:�ֻ��˷��͸��������ٶ�ָ��
	public static byte[] getSConnectSpeed(){
		return format(0x66,0x00,null);
	}
	
	
	//*****************************************************
	//��Ϣ��ȡЭ��
	//0x70:�ֻ��˷��Ͷ�ȡ�汾��Ϣָ��:����汾��Ӳ���汾
	public static byte[] getRVersion(){
		return format(0x70,0x00,null);
	}
	
	//0x71:�ֻ��˷��Ͷ�ȡMAC��Ϣָ��
	public static byte[] getRMac(){
		return format(0x71,0x00,null);
	}
	
	//0x72:�ֻ��˷��Ͷ�ȡ����Ϣָ��
	public static byte[] getRBindCode(){
		return format(0x72,0x00,null);
	}
	//*****************************************************
	//��������Э��
	//0x80:�ֻ��˷��ͶϿ�����ָ��
	public static byte[] getSDisconnect(){
		return format(0x80,0x00,null);
	}
	
	//*****************************************************
}
