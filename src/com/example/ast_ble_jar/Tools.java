package com.example.ast_ble_jar;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Tools {
	

	public static String[] printHexString( byte[] b) {  
    	String[] sb=new String[b.length];
    	
    	StringBuilder builder=new StringBuilder();
	   for (int i = 0; i < b.length; i++) { 
	     String hex = Integer.toHexString(b[i] & 0xFF); 
	     if (hex.length() == 1) { 
	       hex = '0' + hex; 
	     } 
	  
	     sb[i]=hex.toUpperCase();
	     
	     builder.append(sb[i]+" ");
	   } 
	   Log.e("printHexString", ""+builder);
	   return sb;
    }

	public static boolean isEmpty(String str){
		if( str==null ||"".equals(str.trim())|| "".equals(str) ){
			return true;
		}
		return false;
	}
	
	public static int getMinutes(String from,String to){//09:05
		int minutes=0;
		int f=0,t=0;
		if(isTimeUrl(from) && isTimeUrl(to)){
			f=Integer.parseInt(from.substring(0, 2))*60+Integer.parseInt(from.substring(3));
			t=Integer.parseInt(to.substring(0, 2))*60+Integer.parseInt(to.substring(3));
			minutes=t-f;
		}else{
			Log.e("getMinutes","from:"+from+",to:"+to);
		}
		return minutes;
	}
	
	
	public static byte[] timeAdd1Min(byte[] data){//yyy MM dd HH mm
		int yyy=0;
		byte MM=0,dd=0,HH=0,mm=0;
		if(data.length==5){
			yyy=data[0];
			MM=data[1];
			dd=data[2];
			HH=data[3];
			mm=data[4];
			int Days=Tools.countDays(yyy+2000, MM);
			if(mm+1>=60){
				mm=0;
				if(HH+1>=24){
					HH=0;
					if(dd+1>Days){
						dd=1;
						if(MM+1>12){
							MM=1;
							yyy+=1;
						}else{
							MM+=1;
						}
					}else{
						dd+=1;
					}
				}else{
					HH+=1;
				}
			}else{
				mm+=1;
			}
		}
		return new byte[]{(byte) (yyy & 0xFF), MM,dd,HH,mm};
	}
	
	public static byte[] timeAdd7Min(byte[] data){//yyy MM dd HH mm
		int yyy=0;
		byte MM=0,dd=0,HH=0,mm=0;
		if(data.length==5){
			yyy=data[0];
			MM=data[1];
			dd=data[2];
			HH=data[3];
			mm=data[4];
			int Days=Tools.countDays(yyy+2000, MM);

			/**
			 * add 7
			 */
			if(mm+7>=60){
				mm=(byte) (mm+7-60);
				if(HH+1>=24){
					HH=0;
					if(dd+1>Days){
						dd=1;
						if(MM+1>12){
							MM=1;
							yyy+=1;
						}else{
							MM+=1;
						}
					}else{
						dd+=1;
					}
				}else{
					HH+=1;
				}
			}else{
				mm+=7;
			}
			
			
			
		}
		return new byte[]{(byte) (yyy & 0xFF), MM,dd,HH,mm};
	}
	
	public static boolean isLeap(int year){
		if((year%4==0&&year%100!=0)||(year%400==0))
			return true;
		else 
			return false;
	}
	
	public static int countDays(int year,int month){
		int tianshu=0;
		switch(month){
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			tianshu=31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			tianshu=30;
			break;
		case 2:
			if(isLeap(year)==true)
			{
				tianshu= 29;
			}
			else {
				tianshu=28;
			}
			break;
		}
		return tianshu;
	}
	/**24Сʱ��
     * Сʱ:���ӵ�������ʽ���<br>
     * <br>
     * @param pInput     Ҫ�����ַ���<br>
     * @return boolean   ���ؼ����<br>
     */
	public static boolean isTimeUrl (String pInput) {
        if(pInput == null){
            return false;
        }
        String regEx = "^([0-1]{1}\\d|2[0-3]):([0-5]\\d)$";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(pInput);
        boolean flag=matcher.matches();
        if(!flag){
        	System.out.println("ʱ���ʽ����"+pInput);
        }
        return flag;
    }
	/**
	 * ���뵽���ڵ������У�yyyy-MM-dd HH:mm:ss
	 *	^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\s+([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$
	 */
	
	/**
	 * ����yyyy-MM-dd��������ʽ���<br>
	 * @param pDate		Ҫ�����ַ���<br>
	 * @return boolean   ���ؼ����<br>
	 */
	public static boolean isDateUrl(String pDate){
		if(pDate == null){
            return false;
        }
        String regEx = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(pDate);
        boolean flag=matcher.matches();
        if(!flag){
        	System.out.println("���ڸ�ʽ(2014-09-10)����"+pDate);
        }
		return flag;
	}
	
	//************************************************
	
	public static byte[] dateFormat(){
		byte []time=new byte[7];
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss");
		String[] str=sdf.format(new Date()).split("-");
//		Log.i("current time", sdf.format(new Date()));
		for(int i=0;i<str.length;i++){
			if(i==0){
				byte[] y2Byte=Tools.get2Byte(Integer.parseInt(str[0]));
				time[0]=y2Byte[0];
				time[1]=y2Byte[1];
			}else{
				time[i+1]=(byte) Integer.parseInt(str[i]);
			}
		}
		return time;
	}
	
	public static boolean is2DaysAgo(String date){
		boolean flag=false;
		try {
			SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd");
			String sb = sdf.format(new Date());
			
			long zt=sdf.parse(sb).getTime();
			long pre=sdf.parse(date).getTime();
			Log.e("", "zt("+zt+")-pre("+pre+")="+(600000*6*24*2));
			if(zt-pre>600000*6*24*2){
				flag=true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return flag;
	}
	public static byte[] getTime2daysago(String date,String time){
		byte []dt=null;
		try {
			if(isDateUrl(date) && isTimeUrl(time)){
				dt=new byte[5];
				SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd");
				String sb = sdf.format(new Date());
				long zt=sdf.parse(sb).getTime();
				long zs=zt-600000*6*24*2;
				String _2DaysAgo=sdf.format(new Date(zs));
				
				dt=getTimeByDT(_2DaysAgo,"00:00");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dt;
	}
	
	//test
	public static byte[] getTodayTime(){
		byte []dt=null;
		try {
				dt=new byte[5];
				SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd");
				String sb = sdf.format(new Date());
				long zt=sdf.parse(sb).getTime();
				String today=sdf.format(new Date(zt));
				
				dt=getTimeByDT(today,"00:00");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dt;
	}
	
	public static byte[] getTime10MinAgo(){
		byte []time=new byte[5];//������ʱ��	
		try {
			SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd-HH-mm");
			
			String sb = sdf.format(new Date());
			long zt=sdf.parse(sb).getTime();
			long zs=zt-600000;
			String _10MinAgo=sdf.format(new Date(zs));
			Log.e("", ""+_10MinAgo);
			
			String[] str=_10MinAgo.split("-");
			for(int i=str.length-1;i>=0;i--){
				if(i==0){
					time[i]=(byte) (Integer.parseInt(str[i])-2000);
				}else{
					time[i]=(byte) Integer.parseInt(str[i]);
				}
			}
			for(int i=0;i<time.length;i++){
				Log.e("", i+"---"+(time[i]&0xFF));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return time;
	}
	
	
	public static byte[] getTimeByDT(String date,String time){
		byte []dt=null;
		if(isDateUrl(date) && isTimeUrl(time)){
			dt=new byte[5];
			dt[0]=(byte) (Integer.parseInt(date.split("-")[0])-2000);
			dt[1]=(byte) Integer.parseInt(date.split("-")[1]);
			dt[2]=(byte) Integer.parseInt(date.split("-")[2]);
			
			dt[3]=(byte) Integer.parseInt(time.split(":")[0]);
			dt[4]=(byte) Integer.parseInt(time.split(":")[1]);
		}
//		dt=timeAdd1Min(dt);
		return dt;
	}
	
	/**
	 * date+time ������ø÷���ʱ�����ٷ���
	 * @param date
	 * @param time
	 * @return
	 */
	public static int getDataCount(String date,String time){
		long minutes=0;
		try {
			SimpleDateFormat sdformat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date last=sdformat.parse(date+" "+time);
			Date current=new Date();
			Log.e("", "last="+sdformat.format(last)+",current="+sdformat.format(current));
			long betweentime=current.getTime()-last.getTime();
			minutes= Math.max((current.getTime()- last.getTime()) / 60000,0);
			
			Log.e("", "minutes="+minutes);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return (int)minutes;
	}
	
	/**
	 * 
	 * @param minutes
	 * @param version 1:7E01 3:7E07
	 * @return
	 */
	public static int getCount(int minutes,int version){
		int count=0;
		if(version==1){
			count=minutes%7!=0?minutes/7+1:minutes/7;
		}else{
			count=minutes%5!=0?minutes/5+1:minutes/5;
		}
		return count;
	}
	/**
	 * 
	 * @param bytes[4]
	 * @return
	 */
	public static int getInt(byte[] bytes)
    {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }
	/**
	 * 
	 * @param data int
	 * @return byte[4]
	 */
    public static byte[] getBytes(int data)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }
	
	public static byte[] get3Byte(int x){
		byte[] bt=new byte[3];
		int y= x & 0xFFFFFF;
		
		bt[0]=(byte) (y & 0xFF);
		bt[1]=(byte) ((y>>8) & 0xFF);
		bt[2]=(byte)((y>>16) & 0xFF);
		return bt;
	}
	/**
	 * 
	 * @param bts[3]
	 * @return
	 */
	public static int getIntFrom3Byte(byte[] bts){
		return getInt(new byte[]{bts[0],bts[1],bts[2],0x00});
	}
	
	public static byte[] get2Byte(int x){
		byte[] bt=new byte[2];
		int y= x & 0x0FFFF;
		
		bt[0]=(byte) (y & 0x0FF);
		bt[1]=(byte) ((y>>8) & 0x0FF);
		return bt;
	}
	public static int getIntFrom2Byte(byte[] bts){
		return getInt(new byte[]{bts[0],bts[1],0x00,0x00});
	}
	
	//8λ�������ַ���ת��Ϊbyte
	public static byte getByteFromString(String str){
		byte b=0;
		for(int i=0;i<8;i++){
			if(str.charAt(i)=='1'){
				b+=1<<i;
			}
		}
//		Log.e("", "* "+b);
//		Log.e("", "* "+(b&0xFF));
		return b;
	}
	//************************************************
	
	public static boolean hasSdcard(){
		String sdStatus = Environment.getExternalStorageState();  
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����  
            Log.e("hasSdcard", "SD card is not avaiable/writeable right now.");  
            return false;  
        }
        return true;
	}

	//************************************************ 
	//���㲽�� 
	public static float calcFootStep(int height,int stepNum){ 
		float pace=0; 
		pace=(float) (height*(stepNum>160?160:stepNum)*0.01*0.01*0.5*0.8); //2015-07-16
		return pace; 
	} 
	//����METS 
	public static float calcMETS(int height,int stepNum){ 
		float METS=0; 
		if(stepNum!=0){
			float pace=calcFootStep(height,stepNum); 
			METS=(float) ((pace*stepNum*0.1+3.5)/3.5); 
		}
		
	//	Log.e("METS", ""+METS);
		return METS; 
	} 
	
	//����Cal:height:cm;weight:kg 
	public static float calcCal(int height,int weight,int stepNum){ 
		float kCal=0; 
		kCal=((float)((1.05 * calcMETS(height,stepNum) * weight)*50/3.0/1000.0)); 
		return kCal; 
	} 
	
	public static String get3Dot(double xx){ 
		// BigDecimal bd = new BigDecimal(xx); 
		// bd = bd.setScale(3,BigDecimal.ROUND_HALF_UP); 

		DecimalFormat df=new DecimalFormat("0.000"); 
		String s = df.format(xx); 
		if(s.contains(",")){ 
			s=s.replace(',', '.'); 
		} 
		return s; 
	}
	//6.21 
	public static double km2mile(double km){ 
		int k = (int)(km*1000); 
		k = k*63/100; 
		BigDecimal bd = new BigDecimal(k/1000.0); 
		bd = bd.setScale(3,BigDecimal.ROUND_HALF_UP); 
		return bd.doubleValue(); 
	} 
	public static double mile2km(double mile){ 
		BigDecimal bd = new BigDecimal(mile/0.63); 
		bd = bd.setScale(3,BigDecimal.ROUND_HALF_UP); 
		return bd.doubleValue(); 
	}
	//��Ӣ�ƽ���ת�� 
	public static int cm2inch(int cm){ 
		BigDecimal bd = new BigDecimal(cm*0.4); 
		bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP); 
		return bd.shortValue(); 
	} 
	public static int inch2cm(int inch){ 
		BigDecimal bd = new BigDecimal(inch*2.5); 
		bd = bd.setScale(0,BigDecimal.ROUND_DOWN); 
		return bd.shortValue(); 
	} 

	public static int kg2pound(int kg){ 
		BigDecimal bd = new BigDecimal(kg*2.2); 
		bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP); 
		return bd.shortValue(); 
	} 
	public static int pound2kg(int pound){ 
		BigDecimal bd = new BigDecimal(pound/2.2); 
		// BigDecimal bd = new BigDecimal(((pound*5)/11)); 
		bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP); 
		return bd.shortValue(); 
	}

	//android��ȡһ�����ڴ���Ƶ�ļ���intent
    public static Intent getAudioFileIntent(File file)
    {
      Intent intent = new Intent("android.intent.action.VIEW");
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("oneshot", 0);
      intent.putExtra("configchange", 0);
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "audio/*");
      return intent;
    }
    //android��ȡһ�����ڴ���Ƶ�ļ���intent
    public static Intent getVideoFileIntent(File file)
    {
      Intent intent = new Intent("android.intent.action.VIEW");
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("oneshot", 0);
      intent.putExtra("configchange", 0);
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "video/*");
      return intent;
    }
  //android��ȡһ�����ڴ�ͼƬ�ļ���intent
    public static Intent getImageFileIntent(File file)
    {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //�������ݵ�С���� ����6λ����
    public static String disposePoint(String s){
    	if(s.length()<=7){
    		return s;
		}else if(s.length()>7){
			String s1 = s.substring(0, 7);
			char c = s1.charAt(s1.length()-1);
			if(c!='.'){
				return s1;
			}else{
				return  s.substring(0, 6);
			}
		}
    	return "";
    }
	
    /***************************************/
    public static byte getPushByInt(int push){
    	byte bt=0;
    	switch(push){
    	case 0:
    		bt=0x01;
    		break;
    	case 1:
    		bt=0x02;
    		break;
    	case 2:
    		bt=0x04;
    		break;
    	case 3:
    		bt=0x08;
    		break;
    		
		default:
			bt=0x08;
			break;
    	}
    	return bt;
    }
    
    public static byte getCombinePush(ArrayList<Integer> list){
    	byte bt=0;
    	for(int i=0;i<list.size();i++){
    		bt+=(int)Math.pow(2, list.get(i));
    	}
    	return bt;
    }
    
    //DND
	
	public static boolean isDND(String startTime,String endTime,String time){
		boolean flag=true;
		if(isTimeUrl(startTime) && isTimeUrl(endTime)&& isTimeUrl(time)){
			
			int startAlwaysMin=Integer.parseInt(startTime.split(":")[0])*60+Integer.parseInt(startTime.split(":")[1]);
			int endAlwaysMin=Integer.parseInt(endTime.split(":")[0])*60+Integer.parseInt(endTime.split(":")[1]);
			int timeAlwaysMin=Integer.parseInt(time.split(":")[0])*60+Integer.parseInt(time.split(":")[1]);
			Log.e("", "current time��"+time+"  startAlwaysMin:"+startAlwaysMin+"  endAlwaysMin:"+endAlwaysMin+"  timeAlwaysMin:"+timeAlwaysMin);
			if(startAlwaysMin>endAlwaysMin){//����
				if(startAlwaysMin<=timeAlwaysMin || timeAlwaysMin<=endAlwaysMin){//���������
					flag=true;
				}else{
					flag=false;
				}
			}else{//����
				if(startAlwaysMin<=timeAlwaysMin && timeAlwaysMin<=endAlwaysMin){//���������
					flag=true;
				}else{
					flag=false;
				}
			}
		}else{
			flag=true;
		}
		Log.e("", "isDND:"+flag);
		return flag;
	}
	
	//�ж�Ӧ����ǰ̨���Ǻ�̨
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
             if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                              Log.i("��̨", appProcess.processName);
                              return true;
                    }else{
                              Log.i("ǰ̨", appProcess.processName);
                              return false;
                    }
               }
        }
        return false;
    }
    
    /**
	 * �жϴ����ʱ���Ƿ�С�ڵ�ǰ�ֻ�ϵͳʱ��
	 * @param date
	 * @param time
	 * @return
	 */
	public static boolean lowerThanCurrent(String date,String time){
		try {
			SimpleDateFormat sdformat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date last=sdformat.parse(date+" "+time);
			Date current=new Date();
			Log.e("", "last="+sdformat.format(last)+",current="+sdformat.format(current));
			long betweentime=current.getTime()-last.getTime();
			return betweentime>0?true:false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
    
    
}


