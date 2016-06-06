package com.smartteam.wisdat.ble.entity;

public class CommandEntity implements Comparable<CommandEntity>{
	private int serialmun;
	private byte[] command;
	public int getSerialmun() {
		return serialmun;
	}
	/**
	 *   设置命令发送的优先级
	 * @param serialmun 50：用户信息  51：时间格式  52：提醒  53：闹钟  54：（心率）
	 *                                       55：目标  56：亮度  57：以上都是历史数据
	 */
	public void setSerialmun(int serialmun) {
		this.serialmun = serialmun;
	}
	public byte[] getCommand() {
		return command;
	}
	public void setCommand(byte[] command) {
		this.command = command;
	}
	@Override
	public int compareTo(CommandEntity another) {
		if(another!=null){
			if(this.serialmun>another.serialmun){
				return 1;
			}else if(this.serialmun==another.serialmun){
				return 0;
			}
		}
		return -1;
	}
	
	
}
