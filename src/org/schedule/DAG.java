package org.schedule;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class DAG { //一个工作流中的一个TASK，以下的DAG均为工作流中的子任务TASK
	
	public int level;
	
	public int newlevel;
	
	public int slidelength;
	
	public int slidedeadline; //slidestartdeadline
	
	public int slidefinishdeadline;
	
	public int relax;
	
	public int weight;
	
	public int choosePEpre;
	
	public int ID;    //进程编号
	
	public int dagID;
	
	public int arrive_time;    //进程到达时间
	
	public int length;   //任务长度
	
	public int Ts;    //进程的执行时间
	
	public int Tr;    //进入就绪队列时间
	
	public int finish_time;    //进程执行结束时间
	
	public int finish_time_suppose;
	
	public int remain_time;    //进程剩余执行时间
	
	public int whole_time;     //周转时间
	
	public int start_time;    //进入执行队列时间
	
	public int Tw;    //等待时间
	
	public int Te;    //到现在为止，花费的执行时间
		
	public double R;
	
	public boolean done = false;
	
	public boolean ready = false;
	
	public boolean pass = false;
	
	private double upRankValue;
	
	public ArrayList<Integer> pre_task;
	
	public ArrayList<Integer> suc_task;
	
	public int Deadline;
	
	public int NewDeadline;
	
	public int PEID;
	
	public boolean islast;
	
	private boolean inserte;
	
	public double heftast;
	
	public double heftaft;
	
	public boolean inCriticalPath;
	
	public boolean iscriticalnode;
	
	public int fillbackstarttime;
	
	public int fillbackfinishtime;
	
	public int fillbackpeid;
	
	public boolean isfillback = false;
	
	public boolean fillbackdone = false;
	
	public boolean fillbackready = false;
	
	public boolean fillbackpass = false;
	
	public int prefillbackstarttime;
	
	public int prefillbackfinishtime;
	
	public int prefillbackpeid;
	
	public boolean prefillbackdone = false;
	
	public boolean prefillbackready = false;
	
	public boolean prefillbackpass = false;
	
	public DAG(){
		pre_task = new ArrayList<Integer>();
		suc_task = new ArrayList<Integer>();
		islast =false;
		inserte = false;
		inCriticalPath = false;
		iscriticalnode = false;
	}
	
	public void setnewlevel(int level){
		this.newlevel = level;
	}
	
	public int getnewlevel(){
		return newlevel;
	}
	
	public void setlevel(int level){
		this.level = level;
	}
	
	public int getlevel(){
		return level;
	}
	
	public void setslidefinishdeadline(int slidedeadline){
		this.slidefinishdeadline = slidedeadline;
	}
	
	public int getslidefinishdeadline(){
		return slidefinishdeadline;
	}
	
	public void setslidedeadline(int slidedeadline){
		this.slidedeadline = slidedeadline;
	}
	
	public int getslidedeadline(){
		return slidedeadline;
	}
	
	public void setslidelength(int slidelength){
		this.slidelength = slidelength;
	}
	
	public int getslidelength(){
		return slidelength;
	}
	
	public void setrelax(int relax){
		this.relax = relax;
	}
	
	public int getrelax(){
		return relax;
	}
	
	public void setweight(int weight){
		this.weight = weight;
	}
	
	public int getweight(){
		return weight;
	}
	
	public void setchoosePEpre(int start){
		this.choosePEpre = start;
	}
	
	public int getchoosePEpre(){
		return choosePEpre;
	}
	
	public void setfillbackstarttime(int start){
		this.fillbackstarttime = start;
	}
	
	public int getfillbackstarttime(){
		return fillbackstarttime;
	}
	
	public void setfillbackfinishtime(int finish){
		this.fillbackfinishtime = finish;
	}
	
	public int getfillbackfinishtime(){
		return fillbackfinishtime;
	}
	
	public void setfillbackpeid(int peid){
		this.fillbackpeid = peid;
	}
	
	public int getfillbackpeid(){
		return fillbackpeid;
	}
	
	public void setiscriticalnode(boolean temp){
		this.iscriticalnode = temp;
	}
	
	public boolean getiscriticalnode(){
		return iscriticalnode;
	}
	
	public void setinCriticalPath(boolean temp){
		this.inCriticalPath = temp;
	}
	
	public boolean getinCriticalPath(){
		return inCriticalPath;
	}
	
	public ArrayList<Integer> getpre(){
		return pre_task;
	}
	
	public ArrayList<Integer> getsuc(){
		return suc_task;
	}
	
	public void setpre(ArrayList<Integer> pre_){
		for(int i=0;i<pre_.size();i++)
		{
			pre_task.add(pre_.get(i));
		}
	}
	
	public void setsuc(ArrayList<Integer> suc_){
		for(int i=0;i<suc_.size();i++)
		{
			suc_task.add(suc_.get(i));
		}
	}
	
	public void addToPre(int preId){
		this.pre_task.add(preId);
	}

	public void addToSuc(int sucId){
		this.suc_task.add(sucId);
	}
	public void sette(int te){
		this.Te = te;
	}
	
	public int gette(){
		return Te;
	}
	
	public void setislast(boolean islast_){
		this.islast = islast_;
	}
	
	public boolean getislast(){
		return islast;
	}
	
	public void setready(boolean ready_){
		this.ready = true;
	}
	
	public boolean getready(){
		return ready;
	}
	
	public void setdone(boolean done_){
		this.done = true;
	}
	
	public boolean getdone(){
		return done;
	}
	
	public void settw(int tw){
		this.Tw = tw;
	}
	
	public int gettw(){
		return Tw;
	}
	
	public void setr(double r){
		this.R = r;
	}
	
	public double getr(){
		return R;
	}
	
	public void setid(int id){
		this.ID = id;
	}
	
	public int getid(){
		return ID;
	}
	
	public void setdagid(int id){
		this.dagID = id;
	}
	
	public int getdagid(){
		return dagID;
	}
	
	public void setremain(int remain){
		this.remain_time = remain;
	}
		
	public int getremain(){
		return remain_time;
	}

	public void setarrive(int arrive){
		this.arrive_time = arrive;
	}
		
	public int getarrive(){
		return arrive_time;
	}

	public void setts(int ts){
		this.Ts = ts;
	}
		
	public int getts(){
		return Ts;
	}
	
	public void settr(int tr){
		this.Tr = tr;
	}
		
	public int gettr(){
		return Tr;
	}
	
	public void setfinish(int finish){
		this.finish_time = finish;
	}
		
	public int getfinish(){
		return finish_time;
	}
	
	public void setfinish_suppose(int finish_suppose){
		this.finish_time_suppose = finish_suppose;
	}
		
	public int getfinish_suppose(){
		return finish_time_suppose;
	}
	
	public void setwhole(int whole){
		this.whole_time = whole;
	}
		
	public int getwhole(){
		return whole_time;
	}
	
	public void setstart(int start){
		this.start_time = start;
	}
		
	public int getstart(){
		return start_time;
	}
	
	public void setlength(int temp){
		this.length = temp;
	}
		
	public int getlength(){
		return length;
	}
	
	public void setdeadline(int deadline){
		this.Deadline = deadline;
	}
		
	public int getdeadline(){
		return Deadline;
	}
	
	public void setnewdeadline(int deadline){
		this.NewDeadline = deadline;
	}
		
	public int getnewdeadline(){
		return NewDeadline;
	}
	
	public void setpeid(int peid){
		this.PEID = peid;
	}
		
	public int getpeid(){
		return PEID;
	}
	
	public void setpass(boolean pass_){
		this.pass = true;
	}
	
	public boolean getpass(){
		return pass;
	}
	
	public void setUpRankValue(double upRankValue){
		this.upRankValue = upRankValue;
	}
	public double getUpRankValue(){
		return upRankValue;
	}
	
	public void setinserte(boolean temp){
		this.inserte = temp;
	}
	
	public boolean getinserte(){
		return inserte;
	}
	
	public void setheftast(double ast){
		this.heftast = ast;
	}
		
	public double getheftast(){
		return heftast;
	}
	
	public void setheftaft(double aft){
		this.heftaft = aft;
	}
		
	public double getheftaft(){
		return heftaft;
	}
	
	public void setfillbackpass(boolean pass_){
		this.fillbackpass = pass_;
	}
	
	public boolean getfillbackpass(){
		return fillbackpass;
	}
	
	public void setfillbackready(boolean ready_){
		this.fillbackready = ready_;
	}
	
	public boolean getfillbackready(){
		return fillbackready;
	}
	
	public void setfillbackdone(boolean done_){
		this.fillbackdone = done_;
	}
	
	public boolean getfillbackdone(){
		return fillbackdone;
	}
	
	public void setprefillbackstarttime(int start){
		this.prefillbackstarttime = start;
	}
	
	public int getprefillbackstarttime(){
		return prefillbackstarttime;
	}
	
	public void setprefillbackfinishtime(int finish){
		this.prefillbackfinishtime = finish;
	}
	
	public int getprefillbackfinishtime(){
		return prefillbackfinishtime;
	}
	
	public void setprefillbackpeid(int peid){
		this.prefillbackpeid = peid;
	}
	
	public int getprefillbackpeid(){
		return prefillbackpeid;
	}
	
	public void setprefillbackpass(boolean pass_){
		this.prefillbackpass = pass_;
	}
	
	public boolean getprefillbackpass(){
		return prefillbackpass;
	}
	
	public void setprefillbackready(boolean ready_){
		this.prefillbackready = ready_;
	}
	
	public boolean getprefillbackready(){
		return prefillbackready;
	}
	
	public void setprefillbackdone(boolean done_){
		this.prefillbackdone = done_;
	}
	
	public boolean getprefillbackdone(){
		return prefillbackdone;
	}
	
	public void setisfillback(boolean isfillback){
		this.isfillback = isfillback;
	}
	
	public boolean getisfillback(){
		return isfillback;
	}
}
