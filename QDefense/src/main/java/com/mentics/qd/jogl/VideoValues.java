package com.mentics.qd.jogl;

import java.io.Serializable;

public class VideoValues implements Serializable {
	public int resW;
	public int resH;
	public boolean fullscr;
	
	public VideoValues(int resW, int resH, boolean fullscr){
		this.resW = resW;
		this.resH = resH;
		this.fullscr = fullscr;
	}
}
