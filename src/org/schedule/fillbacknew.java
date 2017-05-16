package org.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import org.jdom.xpath.XPath;
import org.jdom.Attribute;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.generate.BuildParameters;
import org.generate.DagBuilder;

public class fillbacknew {
	
	private static ArrayList<PE> PEList;
	private static ArrayList<DAGMap> DAGMapList;
	
	private static ArrayList<DAG> DAG_queue;
	private static ArrayList<DAG> readyqueue;

	private static HashMap<Integer,Integer> DAGDependMap;
	private static HashMap<String,Double> DAGDependValueMap;

	private static ArrayList<DAG> DAG_queue_personal;
	private static HashMap<Integer,Integer> DAGDependMap_personal;
	private static HashMap<String,Double> DAGDependValueMap_personal;
	private static Map<Integer,int[]> ComputeCostMap;
	private static Map<Integer,Integer> AveComputeCostMap;
	
	public static Map<Integer,double[]> DAGExeTimeMap;
	private static Map<Integer,DAG> DAGIdToDAGMap;
	private static Map<Integer,Double> upRankValueMap;
	private static Map<Integer,double[]> vmComputeCostMap;
	private static Map<Integer,Double> vmAveComputeCostMap;	
	private static Map<Integer,Integer[]> cloudletInVm;
	private static Map<Integer,Integer> cloudletInVmId;
	
	private static int islastnum = 0;
	private static double deadLineTimes = 1.3;//deadline的倍数值 （1.1，1.3，1.6，2.0）
	private static int pe_number = 8;
	
	public static String[][] rate = new String[5][2];
	
	public static int current_time;
	public static int proceesorEndTime = BuildParameters.timeWindow;//时间窗
	public static int timeWindow;
	public static int T = 1;

	public static int fillbacktasknum = 10;
	public static int[][] message;
	public static int dagnummax = 10000;
	public static int timewindowmax = 9000000;
	public static int mesnum = 5;
	private static HashMap<Integer,ArrayList> SlotListInPes;
	private static HashMap<Integer,HashMap> TASKListInPes;
	
	public fillbacknew(){
		readyqueue = new ArrayList<DAG>();
		DAG_queue = new ArrayList<DAG>();
		DAG_queue_personal = new ArrayList<DAG>();
		PEList = new ArrayList<PE>();
		DAGMapList = new ArrayList<DAGMap>();
		DAGDependMap = new HashMap<Integer,Integer>();
		DAGDependValueMap = new HashMap<String,Double>();
		deadLineTimes = BuildParameters.deadLineTimes;
		pe_number = BuildParameters.processorNumber;
		current_time = 0;
		timeWindow = proceesorEndTime/pe_number;

		message = new int[dagnummax][mesnum];
		SlotListInPes = new HashMap<Integer,ArrayList>();
		TASKListInPes = new HashMap<Integer,HashMap>();
		for(int i=0;i<pe_number;i++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKListInPes.put(i, TASKInPe);
			//数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
		}
	}
	
	/**
	 * 开始fillback算法
	 */
	//public static void main(String[] args) throws Throwable{
	public void runMakespan() throws Throwable{
		// TODO Auto-generated method stub
		
		//init dagmap
		fillbacknew fb = new fillbacknew();
		DAGdepend dagdepend = new DAGdepend();
		computerability vcc = new computerability();
		initPE();
		initdagmap(dagdepend,vcc);
		
		//start scheduling
		scheduleFirstDAG();
		current_time = DAGMapList.get(0).getsubmittime();
		
		for(int i=1;i<DAGMapList.size();i++)
		{
			HashMap<Integer,ArrayList> SlotListInPestemp = new HashMap<Integer,ArrayList>();
			HashMap<Integer,HashMap> TASKListInPestemp = new HashMap<Integer,HashMap>();
			
			computeSlot(DAGMapList.get(i).getsubmittime(),DAGMapList.get(i).getDAGdeadline());
			SlotListInPestemp = copySlot();
			TASKListInPestemp = copyTASK();

			scheduleOtherDAG(i,SlotListInPestemp,TASKListInPestemp);
			
		}
	
		//output the result
		outputresult();
		storeresult();
		
	}
	
	/**
     * 输出处理器资源利用率和任务完成率
     */
	public static void outputresult()
	{
		int suc = 0;
		int effective = 0;
		int tempp = timeWindow;

		for(int j=0;j<DAGMapList.size();j++)
		{
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for(int i=0;i<DAGMapList.get(j).gettasklist().size();i++)
			{
				DAG dag_temp = (DAG) DAGMapList.get(j).gettasklist().get(i);
				DAGTaskList.add(dag_temp);
			}

			if(DAGMapList.get(j).getfillbackdone())
			{
				suc++;
				for(int i=0;i<DAGMapList.get(j).gettasklist().size();i++)
				{
					effective = effective + DAGTaskList.get(i).getts();
				}
			}
		}
		
		DecimalFormat df = new DecimalFormat("0.0000");
		System.out.println("fillbackFIFO:");
		System.out.println("PE's use ratio is "+df.format((float)effective/(pe_number*tempp)));
		System.out.println("effective PE's use ratio is "+df.format((float)effective/(tempp*pe_number)));
		System.out.println("Task Completion Rates is "+df.format((float)suc/DAGMapList.size()));
		System.out.println();
	}
	
	/**
     * 保存本算法的各个任务的开始结束时间
     */
	public static void storeresult()
	{
		int dagcount = 0;
		for(DAGMap dagmap:DAGMapList)
		{
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for(int i=0;i<dagmap.gettasklist().size();i++)
			{
				DAG dag = (DAG) dagmap.gettasklist().get(i);
				DAGTaskList.add(dag);
				message[dagcount][0] = dag.getdagid();
				message[dagcount][1] = dag.getid();
				message[dagcount][2] = dag.getfillbackpeid();
				message[dagcount][3] = dag.getfillbackstarttime();
				message[dagcount][4] = dag.getfillbackfinishtime();
				dagcount++;
			}
		}
	}
	
	/**
     * 根据relax后结果重新计算空闲时间段SlotListInPes
     * @param submit，DAG提交时间
     * @param deadline，DAG截止时间
     */
	public static void computeSlot(int submit,int deadline){
		
		SlotListInPes.clear();

		for(int i=0;i<pe_number;i++)
		{
			int Slotcount = 0;
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			ArrayList<slot> slotListinpe_ori = new ArrayList<slot>();
			
			if(TASKInPe.size()==0)
			{
				slot tem = new slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			}
			else if(TASKInPe.size()==1)
			{
				if(TASKInPe.get(0)[0] > submit)
				{
					slot tem = new slot();
					ArrayList<String> below_ = new ArrayList<String>();
					below_.add(TASKInPe.get(0)[2]+" "+TASKInPe.get(0)[3]+" "+0);
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(TASKInPe.get(0)[0]);
					tem.setbelow(below_);
					slotListinpe.add(tem);
					Slotcount++;
				}
				if(TASKInPe.get(0)[1] < deadline)
				{
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			}
			else
			{
				for(int j=1;j<TASKInPe.size();j++)
				{
					if(TASKInPe.get(j-1)[1] < TASKInPe.get(j)[0])
					{
						slot tem = new slot();
						ArrayList<String> below_ = new ArrayList<String>();
						for(int k=j;k<TASKInPe.size();k++)
						{
							below_.add(TASKInPe.get(k)[2]+" "+TASKInPe.get(k)[3]+" "+j);
						}
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(TASKInPe.get(j-1)[1]);
						tem.setslotfinishtime(TASKInPe.get(j)[0]);
						tem.setbelow(below_);
						slotListinpe_ori.add(tem);
						Slotcount++;
					}
				}
				
				int startslot = 0;
				for(int j=0;j<slotListinpe_ori.size();j++)
				{
					slot tem = new slot();
					tem = slotListinpe_ori.get(j);
					if(j==0 && tem.getslotstarttime() > submit)
					{
						startslot = 0;
						break;
					}
					
					if(tem.getslotstarttime() <= submit && tem.getslotfinishtime() > submit)
					{
						tem.setslotstarttime(submit);
						startslot = j;
						break;
					}
					else if(tem.getslotstarttime() > submit && slotListinpe_ori.get(j-1).getslotfinishtime() <= submit)
					{
						startslot = j;
						break;
					}
					
					if(j==(slotListinpe_ori.size()-1))
						startslot = slotListinpe_ori.size();
				}	
				
				int count = 0;
				for(int j=startslot;j<slotListinpe_ori.size();j++)
				{
					slot tem = new slot();
					tem = slotListinpe_ori.get(j);
					
					if(tem.getslotfinishtime()<deadline)
					{
						tem.setslotId(count);
						slotListinpe.add(tem);
						count++;
					}
					else if(tem.getslotfinishtime() >= deadline && tem.getslotstarttime()<deadline)
					{
						tem.setslotId(count);
						tem.setslotfinishtime(deadline);
						slotListinpe.add(tem);
						break;
					}
				}
				
				if(TASKInPe.get(TASKInPe.size()-1)[1]<deadline && TASKInPe.get(TASKInPe.size()-1)[1]>submit)
				{
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(TASKInPe.get(TASKInPe.size()-1)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
				else if(TASKInPe.get(TASKInPe.size()-1)[1]<=submit)
				{
					slot tem = new slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			}
			
			SlotListInPes.put(i, slotListinpe);
		}
		
	}
	
	/**
     * 根据relax后结果修改slotlistinpe
     */
	public static void changeinpe(ArrayList<slot> slotlistinpe,int inpe)
	{
		ArrayList<String> below = new ArrayList<String>();
		
		for(int i=0;i<slotlistinpe.size();i++)
		{
			ArrayList<String> belowte = new ArrayList<String>();
			
			slot slottem = slotlistinpe.get(i);

			for(int j=0;j<slottem.getbelow().size();j++)
			{
				below.add(slottem.getbelow().get(j));
			}
			
			String belowbuf[] = below.get(0).split(" ");
			int buffer = Integer.valueOf(belowbuf[2] ).intValue();
			if(buffer >= inpe)
			{
				buffer += 1;
				for(int j=0;j<below.size();j++)
				{
					String belowbuff = belowbuf[0]+" "+belowbuf[1]+" "+buffer;
					belowte.add(belowbuff);
				}			
				slottem.getbelow().clear();
				slottem.setbelow(belowte);
			}
		}
		
	}
	
	/**
     * 根据relax后结果修改TASKListInPes
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     */
	private static void changetasklistinpe(DAGMap dagmap)
	{
		for(int i=0;i<pe_number;i++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			for(int j=0;j<TASKInPe.size();j++)
			{
				if(TASKInPe.get(j)[2]==dagmap.getDAGId())
				{
					DAG temp = new DAG();
					temp = getDAGById(TASKInPe.get(j)[2],TASKInPe.get(j)[3]);
					TASKInPe.get(j)[0] = temp.getfillbackstarttime();
					TASKInPe.get(j)[1] = temp.getfillbackfinishtime();
				}
			}
			TASKListInPes.put(i, TASKInPe);
		}
	}
	
	/**
     * 计算每层中的各个子任务可以后移的距离
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param DAGTaskList，DAG中各个子任务
     * @param canrelaxDAGTaskList，可以后移的子任务列表
     * @param DAGTaskDependValue，依赖关系
     * @param levelnumber，层数
     * @param totalrelax，总冗余值
     */
	public static void calculateweight(DAGMap dagmap,ArrayList<DAG> DAGTaskList,ArrayList<DAG> canrelaxDAGTaskList,Map<String,Double> DAGTaskDependValue,int levelnumber,int totalrelax)
	{
		int startlevelnumber = canrelaxDAGTaskList.get(0).getnewlevel();
		int[] weight = new int[levelnumber];
		int[] relax = new int[DAGTaskList.size()];
		int[] maxlength = new int[levelnumber+1];
		int weightsum = 0;
		
		for(int i=startlevelnumber;i<=levelnumber;i++)
		{
			int max = 0,maxid = 0;
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				
				if(canrelaxDAGTaskList.contains(dagtem))
				{
					if(i==levelnumber)
					{
						max = dagtem.getts();
						maxid = i;
					}
					else
					{
						
						int value = dagtem.getts();
						for(int k=0;k<dagmap.taskinlevel.get(i+1).size();k++)
						{
							DAG dagsuc = new DAG();
							dagsuc = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i+1).get(k));
							if(dagmap.isDepend(String.valueOf(dagtem.getid()), String.valueOf(dagsuc.getid())))
							{
								if(dagtem.getfillbackpeid()!=dagsuc.getfillbackpeid())
								{
									int tempp = dagtem.getts()+(int)(double)DAGTaskDependValue.get(dagtem.getid()+" "+dagsuc.getid());
									if(value<tempp)
									{
										value = tempp;
										maxid = dagtem.getid();
									}
								}
							}
						}
						if(max<value)
						{
							max = value;
							maxid = dagtem.getid();
						}
					}
				}
			}
			weight[i-1] = max;
			maxlength[i-1] = maxid;
		}
		
		for(int i=startlevelnumber-1;i<levelnumber;i++)
		{
			weightsum = weight[i]+weightsum;
		}
		
		for(int i=startlevelnumber;i<=levelnumber;i++)
		{
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				
				if(canrelaxDAGTaskList.contains(dagtem))
				{
					int tem = weight[i-1]*totalrelax/weightsum;
					dagtem.setrelax(tem);
				}
			}
		}
	}
	
	/**
     * 将相同层数的子任务放在一个列表里
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param DAGTaskList，DAG中各个子任务
     * @param deadline，DAG的截止时间
     * @return levelnumber,现有层数
     */
	public static int putsameleveltogether(DAGMap dagmap,ArrayList<DAG> DAGTaskList,int deadline)
	{
		int levelnumber = DAGTaskList.get(DAGTaskList.size()-1).getnewlevel();
		int finishtime = DAGTaskList.get(DAGTaskList.size()-1).getfillbackfinishtime();
		int totalrelax = deadline-finishtime;
		for(int j=1;j<=levelnumber;j++)
		{
			ArrayList<Integer> samelevel = new ArrayList<Integer>();
			for(int i=0;i<dagmap.gettasklist().size();i++)
			{
				if(DAGTaskList.get(i).getnewlevel()==j)
					samelevel.add(i);
			}
			dagmap.taskinlevel.put(j, samelevel);
		}
		return levelnumber;
	}
	
	/**
     * 根据调度结果，重新计算DAG中各个子任务的层数
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param DAGTaskList，DAG中各个子任务
     */
	public static void calculatenewlevel(DAGMap dagmap,ArrayList<DAG> DAGTaskList,ArrayList<DAG> DAGTaskListtemp,ArrayList<DAG> setorderbystarttime)
	{
		DAG min = new DAG();
		DAG temp = new DAG();
		for(int k = 0 ; k < DAGTaskListtemp.size() ; k++)
		{
			int tag = k;
			min = DAGTaskListtemp.get(k);
			temp = DAGTaskListtemp.get(k);
			for(int p = k+1 ; p < DAGTaskListtemp.size() ; p++ )
			{
				if(DAGTaskListtemp.get(p).getfillbackstarttime() < min.getfillbackstarttime() )
				{
					min = DAGTaskListtemp.get(p);
					tag = p;
				}
			}
			if(tag != k)
			{
				DAGTaskListtemp.set(k, min);
				DAGTaskListtemp.set(tag, temp);
			}
		}
		dagmap.setorderbystarttime(DAGTaskListtemp);
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			setorderbystarttime.add((DAG) dagmap.getorderbystarttime().get(i));
		}
		for(int i=0;i<setorderbystarttime.size();i++)
		{
			DAG dag = new DAG();
			dag = setorderbystarttime.get(i);
			
			if(i==0)
			{
				dag.setnewlevel(1);
			}
			else
			{
				int max = 0;
				for(int j=i-1;j>=0;j--)
				{
					if(setorderbystarttime.get(j).getfillbackpeid()==dag.getfillbackpeid())
					{
						max = setorderbystarttime.get(j).getnewlevel()+1;
						break;
					}
				}
				Iterator <Integer>it = DAGTaskList.get(i).getpre().iterator();
				while(it.hasNext())
				{
					int pretempid = it.next();	
					int leveltemp = DAGTaskList.get(pretempid).getnewlevel()+1;
					if(leveltemp>max)
						max = leveltemp;
				}
				dag.setnewlevel(max);
			}
		}
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			for(DAG dag:setorderbystarttime)
				if(dag.getid() == DAGTaskList.get(i).getid())
					DAGTaskList.get(i).setnewlevel(dag.getnewlevel());
		}
	}
	
	/**
     * 计算DAG中原始各个子任务的层数
     * @param DAGTaskList，DAG中各个子任务
     */
	public static void calculateoriginallevel(ArrayList<DAG> DAGTaskList)
	{
		for(int i=0;i<DAGTaskList.size();i++)
		{
			if(i==0)
				DAGTaskList.get(i).setlevel(1);
			else
			{
				int max = 0;
				Iterator <Integer>it = DAGTaskList.get(i).getpre().iterator();
				while(it.hasNext())
				{
					int pretempid = it.next();	
					int leveltemp = DAGTaskList.get(pretempid).getlevel()+1;
					if(leveltemp>max)
						max = leveltemp;
				}
				DAGTaskList.get(i).setlevel(max);
			}
		}
	}
	
	/**
     * 根据调度结果进行levelrelaxing操作
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     */
	public static void wholerelax(DAGMap dagmap)
	{
		int Criticalnum = CriticalPath(dagmap);
		int submit = dagmap.getsubmittime();
		int deadline = dagmap.getDAGdeadline();
		
		ArrayList<DAG> canrelaxDAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskListtemp = new ArrayList<DAG>();
		ArrayList<DAG> setorderbystarttime = new ArrayList<DAG>();
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
			DAGTaskListtemp.add((DAG) dagmap.gettasklist().get(i));
		}
		
		calculateoriginallevel(DAGTaskList);
		calculatenewlevel(dagmap,DAGTaskList,DAGTaskListtemp,setorderbystarttime);
		int levelnumber = putsameleveltogether(dagmap,DAGTaskList,deadline);
		
		int finishtime = DAGTaskList.get(DAGTaskList.size()-1).getfillbackfinishtime();
		int totalrelax = deadline-finishtime;
		
		//find the is not fillback task
		boolean finishsearch = true;
		for(int i=levelnumber;i>=1;i--)
		{
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				
				if(dagtem.getisfillback()==false)
					canrelaxDAGTaskList.add(dagtem);
				else
				{
					finishsearch = false;
					break;
				}
			}
			if(finishsearch==false)
				break;
		}

		
		if(canrelaxDAGTaskList.size()>0)
		{
			
			DAG mindag = new DAG();
			DAG tempdag = new DAG();
			for(int k = 0 ; k < canrelaxDAGTaskList.size() ; k++)
			{
				int tag = k;
				mindag = canrelaxDAGTaskList.get(k);
				tempdag = canrelaxDAGTaskList.get(k);
				for(int p = k+1 ; p < canrelaxDAGTaskList.size() ; p++ )
				{
					if(canrelaxDAGTaskList.get(p).getid() < mindag.getid() )
					{
						mindag = canrelaxDAGTaskList.get(p);
						tag = p;
					}
				}
				if(tag != k)
				{
					canrelaxDAGTaskList.set(k, mindag);
					canrelaxDAGTaskList.set(tag, tempdag);
				}
			}
			
			int startlevelnumber = canrelaxDAGTaskList.get(0).getnewlevel();
			calculateweight(dagmap,DAGTaskList,canrelaxDAGTaskList,DAGTaskDependValue,levelnumber,totalrelax);			
			
			int startinpe[] = new int[pe_number];
			int finishinpe[] = new int[pe_number];
			int length = -1;
			int maxpeid = -1;
			for(int k=0;k<pe_number;k++)
				startinpe[k] = timewindowmax;
			for(int k=0;k<canrelaxDAGTaskList.size();k++)
			{
				DAG dagtem = new DAG();
				dagtem = canrelaxDAGTaskList.get(k);
				if(startinpe[dagtem.getfillbackpeid()] > dagtem.getfillbackstarttime())
					startinpe[dagtem.getfillbackpeid()] = dagtem.getfillbackstarttime();
				if(finishinpe[dagtem.getfillbackpeid()] < dagtem.getfillbackfinishtime())
					finishinpe[dagtem.getfillbackpeid()] = dagtem.getfillbackfinishtime();
			}
			for(int k=0;k<pe_number;k++)
			{
				if(length < (finishinpe[k]-startinpe[k]))
				{
					length = finishinpe[k]-startinpe[k];
					maxpeid = k;
				}
			}
			for(int k=0;k<canrelaxDAGTaskList.size();k++)
			{
				DAG dagtem = new DAG();
				dagtem = canrelaxDAGTaskList.get(k);
				if(dagtem.getfillbackpeid() == maxpeid)
					dagtem.setiscriticalnode(true);
			}
			
			
			for(int j=0;j<dagmap.taskinlevel.get(startlevelnumber).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(startlevelnumber).get(j));
				
				if(canrelaxDAGTaskList.contains(dagtem))
				{
					dagtem.setslidefinishdeadline(dagtem.getfillbackfinishtime()+dagtem.getrelax());
					dagtem.setslidedeadline(dagtem.getrelax()+dagtem.getfillbackstarttime());
					dagtem.setslidelength(dagtem.getrelax());
				}
			}
			
			for(int i=startlevelnumber+1;i<=levelnumber;i++)
			{
				DAG dagtem1 = new DAG();
				dagtem1 = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i-1).get(0));
				int starttime = dagtem1.getslidefinishdeadline();
				int finishdeadline = -1;

				for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
				{
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
					
					dagtem.setfillbackstarttime(starttime);
					dagtem.setfillbackfinishtime(dagtem.getfillbackstarttime()+dagtem.getts());
				}
				
				for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
				{
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
					if(dagtem.getiscriticalnode())
					{
						finishdeadline = dagtem.getfillbackfinishtime() + dagtem.getrelax();
						break;
					}
				}
				
				if(finishdeadline == -1)
				{
					for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
					{
						DAG dagtem = new DAG();
						dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
						if(finishdeadline < dagtem.getfillbackfinishtime())
							finishdeadline = dagtem.getfillbackfinishtime();
					}
					
				}
				
				for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
				{
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
		
					dagtem.setslidefinishdeadline(finishdeadline);
					dagtem.setslidedeadline(finishdeadline-dagtem.getts());
					dagtem.setslidelength(dagtem.getslidedeadline()-dagtem.getfillbackstarttime());
				}
				
			}
			
		}
		else
		{
			for(int i=0;i<dagmap.gettasklist().size();i++)
			{
				DAGTaskList.get(i).setslidedeadline(DAGTaskList.get(i).getfillbackstarttime());
				DAGTaskList.get(i).setslidelength(0);
			}
		}
	}
	
	/**
     * 判断后移空闲块后的负载能否使子任务成功放入空闲块
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param readylist，readylist就绪队列
     * @return isslide，能否放入
     */
	public static boolean scheduling(DAGMap dagmap,ArrayList<DAG> readylist)
	{
		boolean findsuc = true;
		
		do
		{
			int finimin = timewindowmax;
			int mindag = -1;
			int message[][] = new int[readylist.size()][6];
			int[] finish = new int[readylist.size()];
			//0 is if success 1 means success 0 means fail, 1 is earliest start time,
			//2 is peid, 3 is slotid, 4 is if need slide, 5 is slide length
			
			for(int i=0;i<readylist.size();i++)
			{
				DAG dag = new DAG();
				dag = readylist.get(i);
				message[i] = findslot(dagmap,dag);
				finish[i] = message[i][1]+dag.getts();
			}
		
			for(int i=0;i<readylist.size();i++)
			{
				if(message[i][0]==0)
				{
					findsuc = false;
					return findsuc;
				}
			}
		
			for(int i=0;i<readylist.size();i++)
			{
				if(finimin>finish[i])
				{
					finimin = finish[i];
					mindag = i;
				}
			}
			
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			DAG dagtemp = new DAG();
			for(int i=0;i<dagmap.gettasklist().size();i++)
			{
				DAG dag = new DAG();
				DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
				dag = (DAG) dagmap.gettasklist().get(i);
				if(dag.getid()==readylist.get(mindag).getid())
					dagtemp = (DAG) dagmap.gettasklist().get(i);
			}
			
			
			int startmin = finimin-readylist.get(mindag).getts();
			int pemin = message[mindag][2];
			int slotid = message[mindag][3];
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);
			dagtemp.setprefillbackdone(true);
			dagtemp.setprefillbackdone(true);
			//System.out.println("fillback task " + dagtemp.getid() +" in DAG "+dagtemp.getdagid()+" : start at "+ dagtemp.getfillbackstarttime() +" finish at "+ (dagtemp.getfillbackstarttime()+dagtemp.getts()) + " at PE"+ dagtemp.getfillbackpeid());
			
			
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);
		
			if(message[mindag][4]==1)
			{
				int slide = message[mindag][5];
				ArrayList<String> below = new ArrayList<String>();
				ArrayList<slot> slotafter = new ArrayList<slot>();
				
				slot slottemp = new slot();
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				for(int j=0;j<SlotListInPes.get(pemin).size();j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				for(int j=0;j<slotlistinpe.size();j++)
				{
					if(slotlistinpe.get(j).getslotId() == slotid)
					{
						slottemp = slotlistinpe.get(j);
						break;
					}
				}
				
				for(int i=0;i<slottemp.getbelow().size();i++)
				{
					below.add(slottemp.getbelow().get(i));
				}
				
				for(int j=0;j<slotlistinpe.size();j++)
				{
					if(slotlistinpe.get(j).getslotstarttime() > slottemp.getslotstarttime())
					{
						slotafter.add(slotlistinpe.get(j));
					}
				}
				
				
				if(below.size() <= fillbacktasknum)
				{
					int count = 0;
					for(int i=0;i<below.size();i++)
					{
						boolean flag = false;
						String buf[] = below.get(i).split(" ");
						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();
						DAG dag_temp = new DAG();
						dag_temp = getDAGById(DAGId,TASKId);
						
						int temp = slide;
						if(count<slotafter.size())
						{
							if((dag_temp.getfillbackstarttime()+dag_temp.getts()) == slotafter.get(count).getslotstarttime())
							{
								if((slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime())<slide)
								{
									slide = slide - (slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime());
								}
								else
								{
									flag = true;
								}
								count++;
							}
						}
						
						getDAGById(DAGId,TASKId).setfillbackstarttime(getDAGById(DAGId,TASKId).getfillbackstarttime()+temp);
						if(getDAGById(DAGId,TASKId).getfillbackfinishtime()!=0)
							getDAGById(DAGId,TASKId).setfillbackfinishtime(getDAGById(DAGId,TASKId).getfillbackfinishtime()+temp);
						
						TASKInPe.get(inpe+i)[0] = getDAGById(DAGId,TASKId).getfillbackstarttime();
						TASKInPe.get(inpe+i)[1] = getDAGById(DAGId,TASKId).getfillbackstarttime()+getDAGById(DAGId,TASKId).getts();
						TASKInPe.get(inpe+i)[2] = DAGId;
						TASKInPe.get(inpe+i)[3] = TASKId;
						
						if(flag)
							break;
					}
						
				}
				else
				{
					int count = 0;
					for(int i=0;i<fillbacktasknum;i++)
					{
						boolean flag = false;
						String buf[] = below.get(i).split(" ");
						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();
						DAG dag_temp = new DAG();
						dag_temp = getDAGById(DAGId,TASKId);
						
						int temp = slide;
						
						if(count<slotafter.size())
						{
							if((dag_temp.getfillbackstarttime()+dag_temp.getts()) == slotafter.get(count).getslotstarttime())
							{
								if((slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime())<slide)
								{
									slide = slide - (slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime());
								}
								else
								{
									slide = 0;
									flag = true;
								}
								count++;
							}
						}
						
						getDAGById(DAGId,TASKId).setfillbackstarttime(getDAGById(DAGId,TASKId).getfillbackstarttime()+temp);
						if(getDAGById(DAGId,TASKId).getfillbackfinishtime()!=0)
							getDAGById(DAGId,TASKId).setfillbackfinishtime(getDAGById(DAGId,TASKId).getfillbackfinishtime()+temp);
						
						TASKInPe.get(inpe+i)[0] = getDAGById(DAGId,TASKId).getfillbackstarttime();
						TASKInPe.get(inpe+i)[1] = getDAGById(DAGId,TASKId).getfillbackstarttime()+getDAGById(DAGId,TASKId).getts();
						TASKInPe.get(inpe+i)[2] = DAGId;
						TASKInPe.get(inpe+i)[3] = TASKId;
						
						if(flag)
							break;
					}
				}
			}
			
			
			if(TASKInPe.size()>0)
			{
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				for(int j=0;j<SlotListInPes.get(pemin).size();j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				ArrayList<String> below = new ArrayList<String>();
				
				slot slottem = new slot();
				for(int i=0;i<slotlistinpe.size();i++)
				{
					if(slotlistinpe.get(i).getslotId()==slotid)
					{
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				for(int i=0;i<slottem.getbelow().size();i++)
				{
					below.add(slottem.getbelow().get(i));
				}

				if(below.size()>0)
				{
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();		
			
					for(int i=TASKInPe.size();i>inpe;i--)
					{
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i-1)[0];
						st_fitemp[1] = TASKInPe.get(i-1)[1];
						st_fitemp[2] = TASKInPe.get(i-1)[2];
						st_fitemp[3] = TASKInPe.get(i-1)[3];
						TASKInPe.put(i, st_fitemp);
					}

					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);
					
					dagtemp.setisfillback(true);
					
					changeinpe(slotlistinpe,inpe);
					
				}
				else
				{
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}
				
			}
			else
			{
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finimin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}
			
			computeSlot(dagmap.getsubmittime(),dagmap.getDAGdeadline());
			
			readylist.remove(mindag);
		
		}while(readylist.size()>0);
		
		return findsuc;
	}
	
	/**
     * 判断DAG中其余节点能否找到空闲时间段放入
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param dagtemp，DAG中其余TASK中的一个
     * @return message，0 is if success(1 means success 0 means fail), 1 is earliest start time, 2 is peid, 3 is slotid
     */
	public static int[] findslot(DAGMap dagmap,DAG dagtemp)
	{	
		int message[] = new int[6];
		
		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = timewindowmax;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];
		int[] slotid = new int[pe_number];
		int[] isneedslide = new int[pe_number]; //0 means don't need 1 means need slide
		int[] slidelength = new int[pe_number];
		
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		ArrayList<DAG> pre_queue = new ArrayList<DAG>();
		ArrayList<Integer> pre = new ArrayList<Integer>(); 
		pre = dagtemp.getpre();
		if(pre.size()>=0)
		{
			for(int j = 0;j<pre.size();j++)
			{
				DAG buf = new DAG();
				buf = getDAGById(dagtemp.getdagid(),pre.get(j));
				pre_queue.add(buf);
			}
		}
		
		for(int i=0;i<pe_number;i++)
		{
			int predone = 0;
			
			if(pre_queue.size() == 1)
			{
				if(pre_queue.get(0).getfillbackpeid() == i)
				{
					predone = pre_queue.get(0).getfillbackfinishtime();
				}
				else
				{
					int value = (int)(double)DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+" "+String.valueOf(dagtemp.getid()));
					predone = pre_queue.get(0).getfillbackfinishtime() + value;
				}
			}
			else if(pre_queue.size() >= 1)
			{
				for(int j=0;j<pre_queue.size();j++)
				{
					if(pre_queue.get(j).getfillbackpeid() == i)
					{
						if(predone < pre_queue.get(j).getfillbackfinishtime())
						{
							predone = pre_queue.get(j).getfillbackfinishtime();
						}
					}
					else
					{
						int valu = (int)(double)DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+" "+String.valueOf(dagtemp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+valu;
						if(predone < value)
							predone = value;
					}	
				}
			}
				

			startinpe[i] = -1;
			ArrayList<slot> slotlistinpe = new ArrayList<slot>();
			for(int j=0;j<SlotListInPes.get(i).size();j++)
				slotlistinpe.add((slot) SlotListInPes.get(i).get(j));
			for(int j=0;j<SlotListInPes.get(i).size();j++)
			{
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if(predone <= slst)
				{
					if((slst+dagtemp.getts()) <= slfi && (slst+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					}
					else if((slst+dagtemp.getts()) > slfi && (slst+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						slide = slst + dagtemp.getts() - slfi;
						
						if(checkslide(i,slotlistinpe.get(j).getslotId(),slide))
						{
							startinpe[i] = slst;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 1;
							slidelength[i] = slide;
							break;
						}
					}
				}
				else if(predone > slst && predone<slfi)
				{
					if((predone+dagtemp.getts()) <= slfi && (predone+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						startinpe[i] = predone;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					}
					else if((predone+dagtemp.getts()) > slfi && (predone+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						slide = predone + dagtemp.getts() - slfi;
						
						if(checkslide(i,slotlistinpe.get(j).getslotId(),slide))
						{
							startinpe[i] = predone;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 1;
							slidelength[i] = slide;
							break;
						}
					}
				}
			}
		}
		
		for(int i=0;i<pe_number;i++)
		{
			if(startinpe[i] != -1)
			{
				findsuc = true;
				if(startinpe[i]<startmin)
				{
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}
		
		//0 is if success 1 means success 0 means fail, 1 is earliest start time, 2 is peid, 3 is slotid
		if(findsuc)
		{
			message[0] = 1;
			message[1] = startmin;
			message[2] = pemin;
			message[3] = slotid[pemin];
			message[4] = isneedslide[pemin];
			if(isneedslide[pemin]==1)
				message[5] = slidelength[pemin];
			else
				message[5] = -1;
		}
		else
		{
			message[0] = 0;
		}

		return message;
	}

	/**
     * 判断后移空闲块后的负载能否使子任务成功放入空闲块
     * @param peid，空闲块在的PE的ID
     * @param slotid，空闲块在该PE上的ID
     * @param slide，需要后移的距离
     * @return isslide，能否放入
     */
	public static boolean checkslide(int peid,int slotid,int slide){
		
		boolean isslide = true;
		int slidetry = slide;
		ArrayList<String> below = new ArrayList<String>();
		ArrayList<slot> slotafter = new ArrayList<slot>();
		
		slot slottemp = new slot();
		ArrayList<slot> slotlistinpe = new ArrayList<slot>();
		
		for(int j=0;j<SlotListInPes.get(peid).size();j++)
			slotlistinpe.add((slot) SlotListInPes.get(peid).get(j));
		for(int j=0;j<slotlistinpe.size();j++)
		{
			if(slotlistinpe.get(j).getslotId() == slotid)
			{
				slottemp = slotlistinpe.get(j);
				break;
			}
		}
		
		for(int i=0;i<slottemp.getbelow().size();i++)
		{
			below.add(slottemp.getbelow().get(i));
		}
		
		for(int j=0;j<slotlistinpe.size();j++)
		{
			if(slotlistinpe.get(j).getslotstarttime() > slottemp.getslotstarttime())
			{
				slotafter.add(slotlistinpe.get(j));
			}
		}
		
		if(below.size() <= fillbacktasknum)
		{	
			int count = 0;
			for(int i=0;i<below.size();i++)
			{
				boolean flag = false;
				String buf[] = below.get(i).split(" ");
				int DAGId = Integer.valueOf(buf[0]).intValue();
				int TASKId = Integer.valueOf(buf[1]).intValue();
				int inpe = Integer.valueOf(buf[2]).intValue();
				DAG dagtemp = new DAG();
				dagtemp = getDAGById(DAGId,TASKId);
				
				if((dagtemp.getfillbackstarttime()+slidetry) > dagtemp.getslidedeadline())
				{
					isslide = false;
					return isslide;
				}
				else
				{
					if(count<slotafter.size())
					{				
						if(dagtemp.getfillbackfinishtime() == slotafter.get(count).getslotstarttime())
						{
							if((slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime())<slidetry)
							{
								slidetry = slidetry - (slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime());
							}
							else
							{
								flag = true;
								break;
							}
							count++;
						}
					}
				}
				if(flag)
					break;

			}
		}
		else
		{
			int count = 0;
			for(int i=0;i<fillbacktasknum;i++)
			{
				boolean flag = false;
				String buf[] = below.get(i).split(" ");
				int DAGId = Integer.valueOf(buf[0]).intValue();
				int TASKId = Integer.valueOf(buf[1]).intValue();
				int inpe = Integer.valueOf(buf[2]).intValue();
				DAG dagtemp = new DAG();
				dagtemp = getDAGById(DAGId,TASKId);
				
				if((dagtemp.getfillbackstarttime()+slidetry) > dagtemp.getslidedeadline())
				{
					isslide = false;
					return isslide;
				}
				else
				{
					if(count<slotafter.size())
					{
						if(dagtemp.getfillbackfinishtime() == slotafter.get(count).getslotstarttime())
						{
							if((slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime())<slidetry)
							{
								slidetry = slidetry - (slotafter.get(count).getslotfinishtime()-slotafter.get(count).getslotstarttime());
							}
							else
							{
								slidetry = 0;
								flag = true;
								break;
							}
							count++;
						}
					}
				}
				if(flag)
					break;

			}
			if(slidetry > 0)
				isslide = false;
		}
		
		return isslide;
		
	}
	
	/**
     * 判断DAG中起始节点能否找到空闲时间段放入
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param dagtemp，起始节点
     * @return findsuc，能否放入
     */
	public static boolean findfirsttaskslot(DAGMap dagmap,DAG dagtemp){
		//perfinish is the earliest finish time minus task'ts time, the earliest start time
		
		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = 0;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];
		int[] slotid = new int[pe_number];
		
		for(int i=0;i<pe_number;i++)
		{
			startinpe[i] = -1;
			ArrayList<slot> slotlistinpe = new ArrayList<slot>();
			for(int j=0;j<SlotListInPes.get(i).size();j++)
				slotlistinpe.add((slot) SlotListInPes.get(i).get(j));
			for(int j=0;j<SlotListInPes.get(i).size();j++)
			{
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if(dagtemp.getarrive() <= slst)
				{
					if((slst+dagtemp.getts()) <= slfi && (slst+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					}
					else if((slst+dagtemp.getts()) > slfi && (slst+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						slide = slst + dagtemp.getts() - slfi;
						
						if(checkslide(i,slotlistinpe.get(j).getslotId(),slide))
						{
							startinpe[i] = slst;
							slotid[i] = slotlistinpe.get(j).getslotId();
							break;
						}
					}
				}
				else
				{
					if((dagtemp.getarrive()+dagtemp.getts()) <= slfi && (dagtemp.getarrive()+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						startinpe[i] = dagtemp.getarrive();
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					}
					else if((dagtemp.getarrive()+dagtemp.getts()) > slfi && (dagtemp.getarrive()+dagtemp.getts()) <= dagtemp.getdeadline())
					{
						slide = dagtemp.getarrive() + dagtemp.getts() - slfi;
						
						if(checkslide(i,slotlistinpe.get(j).getslotId(),slide))
						{
							startinpe[i] = dagtemp.getarrive();
							slotid[i] = slotlistinpe.get(j).getslotId();
							break;
						}
					}
				}
			}
		}
		
		for(int i=0;i<pe_number;i++)
		{
			if(startinpe[i] != -1)
			{
				findsuc = true;
				if(startinpe[i]<startmin)
				{
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}
		
		if(findsuc)
		{
			finishmin = startmin+dagtemp.getts();
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);
			//System.out.println("fillback task " + dagtemp.getid() +" in DAG "+dagtemp.getdagid()+" : start at "+ dagtemp.getfillbackstarttime() +" finish at "+ (dagtemp.getfillbackstarttime()+dagtemp.getts()) + " at PE"+ dagtemp.getfillbackpeid());

			
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);
			if(TASKInPe.size()>0)
			{
				ArrayList<slot> slotlistinpe = new ArrayList<slot>();
				for(int j=0;j<SlotListInPes.get(pemin).size();j++)
					slotlistinpe.add((slot) SlotListInPes.get(pemin).get(j));
				ArrayList<String> below = new ArrayList<String>();
				
				slot slottem = new slot();
				for(int i=0;i<slotlistinpe.size();i++)
				{
					if(slotlistinpe.get(i).getslotId()==slotid[pemin])
					{
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				for(int i=0;i<slottem.getbelow().size();i++)
				{
					below.add(slottem.getbelow().get(i));
				}

				if(below.size()>0)
				{
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();		
			
					for(int i=TASKInPe.size();i>inpe;i--)
					{
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i-1)[0];
						st_fitemp[1] = TASKInPe.get(i-1)[1];
						st_fitemp[2] = TASKInPe.get(i-1)[2];
						st_fitemp[3] = TASKInPe.get(i-1)[3];
						TASKInPe.put(i, st_fitemp);
					}	

					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);
					
					dagtemp.setisfillback(true);
				}
				else
				{
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}
				
			}
			else
			{
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finishmin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}
			
			computeSlot(dagmap.getsubmittime(),dagmap.getDAGdeadline());
		}
		
		return findsuc;
		
	}
	
	/**
     * 判断backfilling操作能否成功
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @return fillbacksuc，backfilling操作的成功与否
     */
	public static boolean fillback(DAGMap dagmap){

		int runtime = dagmap.getsubmittime();
		boolean fillbacksuc = true; //one task fail to fillback
		boolean fini = true; //is all the tasks have been finished
		
		ArrayList<DAG> readylist = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}
		
		do
		{
			for(DAG dag:DAGTaskList)
			{
				if((dag.getfillbackstarttime()+dag.getts()) == runtime && dag.getfillbackready() && dag.getfillbackdone() == false)
				{
					dag.setfillbackfinishtime(runtime);
					dag.setfillbackdone(true);
				}
			}
			
			for(DAG dag:DAGTaskList)
			{
				if(dag.getid()==0 && dag.getfillbackready()==false)
				{
					if(findfirsttaskslot(dagmap,DAGTaskList.get(0)))
					{
						DAGTaskList.get(0).setprefillbackready(true);
						DAGTaskList.get(0).setprefillbackdone(true);
						if(dag.getts()==0)
						{
							dag.setfillbackfinishtime(dag.getfillbackstarttime());
							dag.setfillbackdone(true);
						}
					}
					else
					{
						fillbacksuc = false;
						return fillbacksuc;
					}
				}

				if(dag.getfillbackdone() == false && dag.getfillbackready() == false)
				{
					ArrayList<DAG> pre_queue = new ArrayList<DAG>();
					ArrayList<Integer> pre = new ArrayList<Integer>(); 
					pre = dag.getpre();
				
					if(pre.size()>0)
					{
						boolean ready = true;
						for(int j = 0;j<pre.size();j++)
						{
							DAG buf = new DAG();
							buf = getDAGById(dag.getdagid(),pre.get(j));
							pre_queue.add(buf);
							if(!buf.getfillbackdone())
							{
								ready = false;
								break;
							}
						}
						if(ready)
						{
							readylist.add(dag);
							dag.setprefillbackready(true);
							dag.setfillbackready(true);
						}
					}
				}
			}
			
			if(readylist.size()>0)
			{
				if(!scheduling(dagmap,readylist))
				{
					fillbacksuc = false;
					return fillbacksuc;
				}
			}
			
			fini = true;
			for(DAG dag:DAGTaskList)
			{
				if(dag.getfillbackdone()==false)
				{
					fini = false;
					break;
				}
			}
			
			runtime = runtime + T;
		
		}while(runtime <= dagmap.getDAGdeadline() && !fini && fillbacksuc);
		
		if(fini)
		{
			for(DAG dag:DAGTaskList)
			{
				dag.setfillbackfinishtime(dag.getfillbackstarttime()+dag.getts());
			}
		}
		else
		{
			fillbacksuc = false;
		}
		
		return fillbacksuc;
	}
	
	/**
     * 还原SlotListInPes和TASKListInPes
     * @param SlotListInPestemp，用于还原的SlotListInPes
     * @param TASKListInPestemp，用于还原的TASKListInPes
     */
	public static void restoreSlotandTASK(HashMap<Integer,ArrayList> SlotListInPestemp,HashMap<Integer,HashMap> TASKListInPestemp)
	{
		SlotListInPes.clear();
		TASKListInPes.clear();
		
		for(int k=0;k<SlotListInPestemp.size();k++)
		{
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			for(int j=0;j<SlotListInPestemp.get(k).size();j++)
			{
				slot slottemp = new slot();
				slottemp = (slot)SlotListInPestemp.get(k).get(j);
				slotListinpe.add(slottemp);
			}
			SlotListInPes.put(k, slotListinpe);
		}
		for(int k=0;k<TASKListInPestemp.size();k++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			for(int j=0;j<TASKListInPestemp.get(k).size();j++)
			{
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPestemp.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPes.put(k, TASKInPe);
		}
	}
	
	/**
     * 使用Backfilling调度第i个DAG，若调度成功，进行LevelRelaxing操作，并且修改TASKListInPes中各个TASK的开始结束时间，若调度不成功，取消该DAG的执行
     * @param i，DAG的ID
     * @param SlotListInPestemp，用于还原的SlotListInPes
     * @param TASKListInPestemp，用于还原的TASKListInPes
     */
	public static void scheduleOtherDAG(int i,HashMap<Integer,ArrayList> SlotListInPestemp,HashMap<Integer,HashMap> TASKListInPestemp)
	{
		int arrive = DAGMapList.get(i).getsubmittime();
		if(arrive>current_time)
			current_time = arrive;
		
		boolean fillbacksuc = fillback(DAGMapList.get(i));
		
		if(!fillbacksuc)
		{
			restoreSlotandTASK(SlotListInPestemp,TASKListInPestemp);
			
			DAGMapList.get(i).setfillbackdone(false);
			DAGMapList.get(i).setfillbackpass(true);
			
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for(int j=0;j<DAGMapList.get(i).gettasklist().size();j++)
			{
				DAGTaskList.add((DAG) DAGMapList.get(i).gettasklist().get(j));
				DAGTaskList.get(j).setfillbackpass(true);
			}
		}
		else
		{
			DAGMapList.get(i).setfillbackdone(true);
			wholerelax(DAGMapList.get(i));
		}
	}
	
	/**
     * 保存现在的SlotListInPes，用于还原
     */
	public static HashMap copySlot()
	{
		HashMap<Integer,ArrayList> SlotListInPestemp = new HashMap<Integer,ArrayList>();
		
		for(int k=0;k<SlotListInPes.size();k++)
		{
			ArrayList<slot> slotListinpe = new ArrayList<slot>();
			for(int j=0;j<SlotListInPes.get(k).size();j++)
			{
				slot slottemp = new slot();
				slottemp = (slot)SlotListInPes.get(k).get(j);
				slotListinpe.add(slottemp);
			}
			SlotListInPestemp.put(k, slotListinpe);
		}
		
		return SlotListInPestemp;
	}
	
	/**
     * 保存现在的TASKListInPes，用于还原
     */
	public static HashMap copyTASK()
	{
		HashMap<Integer,HashMap> TASKListInPestemp = new HashMap<Integer,HashMap>();
		
		for(int k=0;k<TASKListInPes.size();k++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			for(int j=0;j<TASKListInPes.get(k).size();j++)
			{
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPes.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPestemp.put(k, TASKInPe);
		}
		
		return TASKListInPestemp;
	}
	
	/**
     * 计算调度结果中的关键路径
     *
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @return Criticalnumber，关键路径上的子任务个数
     */
	private static int CriticalPath(DAGMap dagmap)
	{
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}
		int Criticalnumber = 0;
		int i=DAGTaskList.size()-1;
		
		while(i>=0)
		{
			if(i==(DAGTaskList.size()-1))
			{
				DAGTaskList.get(i).setinCriticalPath(true);
				Criticalnumber++;
			}
			
			int max = -1;
			int maxid = -1;
			Iterator <Integer>it = DAGTaskList.get(i).getpre().iterator();
			while(it.hasNext())
			{
				int pretempid = it.next();	
				int temp = (int) ((int) DAGTaskList.get(pretempid).getheftaft()+(double)DAGTaskDependValue.get(String.valueOf(pretempid+" "+i)));
				if(temp>max)
				{
					max = temp;
					maxid = pretempid;
				}
			}
			DAGTaskList.get(maxid).setinCriticalPath(true);
			Criticalnumber++;
			i = maxid;
			if(maxid == 0)
				i=-1;
		}
		return Criticalnumber;
	}
	
	/**
     * 为调度成功的DAG进行levelRelaxing操作
     *
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     * @param deadline，DAG的截止时间
     * @param Criticalnumber，关键路径上的子任务个数
     */
	private static void levelrelax(DAGMap dagmap,int deadline,int Criticalnumber)
	{
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		ArrayList<DAG> DAGTaskListtemp = new ArrayList<DAG>();
		ArrayList<DAG> setorderbystarttime = new ArrayList<DAG>();
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
			DAGTaskListtemp.add((DAG) dagmap.gettasklist().get(i));
		}
		
		calculateoriginallevel(DAGTaskList);
		calculatenewlevel(dagmap,DAGTaskList,DAGTaskListtemp,setorderbystarttime);
		
		//calculate weight
		int levelnumber = DAGTaskList.get(DAGTaskList.size()-1).getnewlevel();
		int[] weight = new int[levelnumber];
		int[] relax = new int[DAGTaskList.size()];
		int[] maxlength = new int[levelnumber+1];
		int weightsum = 0;
		int finishtime = DAGTaskList.get(DAGTaskList.size()-1).getfillbackfinishtime();
		int totalrelax = deadline-finishtime;
		for(int j=1;j<=levelnumber;j++)
		{
			ArrayList<Integer> samelevel = new ArrayList<Integer>();
			for(int i=0;i<dagmap.gettasklist().size();i++)
			{
				if(DAGTaskList.get(i).getnewlevel()==j)
					samelevel.add(i);
			}
			dagmap.taskinlevel.put(j, samelevel);
		}

		for(int i=1;i<=levelnumber;i++)
		{
			int max = 0,maxid = 0;
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				if(i==levelnumber)
				{
					max = dagtem.getts();
					maxid = i;
				}
				else
				{
					int value = dagtem.getts();
					for(int k=0;k<dagmap.taskinlevel.get(i+1).size();k++)
					{
						DAG dagsuc = new DAG();
						dagsuc = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i+1).get(k));
						if(dagmap.isDepend(String.valueOf(dagtem.getid()), String.valueOf(dagsuc.getid())))
						{
							if(dagtem.getfillbackpeid()!=dagsuc.getfillbackpeid())
							{
								int tempp = dagtem.getts()+(int)(double)DAGTaskDependValue.get(dagtem.getid()+" "+dagsuc.getid());
								if(value<tempp)
								{
									value = tempp;
									maxid = dagtem.getid();
								}
							}
						}
					}
					if(max<value)
					{
						max = value;
						maxid = dagtem.getid();
					}
				}
			}
			weight[i-1] = max;
			maxlength[i-1] = maxid;
		}
		
		for(int i=0;i<levelnumber;i++)
		{
			weightsum = weight[i]+weightsum;
		}
		
		
		//findcriticalnode 是最后结束的  还是执行时间最长的
		int maxpelength = 0;
		int maxpeid = 0;
		for(int i=0;i<pe_number;i++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			if(TASKInPe.size()>0)
			{
				if(maxpelength < TASKInPe.get(TASKInPe.size()-1)[1])
				{
					maxpelength = TASKInPe.get(TASKInPe.size()-1)[1];
					maxpeid = i;
				}
			}
		}
		for(int i=0;i<TASKListInPes.get(maxpeid).size();i++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(maxpeid);
			DAG dagtem = new DAG();
			dagtem = getDAGById((int)TASKInPe.get(i)[2],(int)TASKInPe.get(i)[3]);
			dagtem.setiscriticalnode(true);
		}
		
		
		//set every level's starttime and slide
		for(int i=1;i<=levelnumber;i++)
		{
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				int tem = weight[i-1]*totalrelax/weightsum;
				dagtem.setrelax(tem);

			}
		}
		
		int relaxlength = DAGTaskList.get(0).getrelax();
		DAGTaskList.get(0).setslidefinishdeadline(DAGTaskList.get(0).getrelax()+DAGTaskList.get(0).getfillbackfinishtime());
		DAGTaskList.get(0).setslidedeadline(DAGTaskList.get(0).getrelax()+DAGTaskList.get(0).getfillbackstarttime());
		DAGTaskList.get(0).setslidelength(DAGTaskList.get(0).getrelax());
		
		for(int i=2;i<=levelnumber;i++)
		{
			DAG dagtem1 = new DAG();
			dagtem1 = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i-1).get(0));
			int starttime = dagtem1.getslidefinishdeadline();
			int finishdeadline = -1;
			
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				
				dagtem.setfillbackstarttime(starttime);
				dagtem.setfillbackfinishtime(dagtem.getfillbackstarttime()+dagtem.getts());
			}
			
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
				if(dagtem.getiscriticalnode())
				{
					finishdeadline = dagtem.getfillbackfinishtime() +  dagtem.getrelax();
					break;
				}
			}
			
			if(finishdeadline == -1)
			{
				for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
				{
					DAG dagtem = new DAG();
					dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
					if(finishdeadline < dagtem.getfillbackfinishtime())
						finishdeadline = dagtem.getfillbackfinishtime();
				}
				
			}
			
			for(int j=0;j<dagmap.taskinlevel.get(i).size();j++)
			{
				DAG dagtem = new DAG();
				dagtem = getDAGById(dagmap.getDAGId(),(int)dagmap.taskinlevel.get(i).get(j));
	
				dagtem.setslidefinishdeadline(finishdeadline);
				dagtem.setslidedeadline(finishdeadline-dagtem.getts());
				dagtem.setslidelength(dagtem.getslidedeadline()-dagtem.getfillbackstarttime());
			}

		}

	}
	
	/**
     * 计算本算法的makespan
     *
     * @param dagmap，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
     */
	public static void FIFO(DAGMap dagmap){
		
		int time = current_time;
		
		ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
		Map<String,Double> DAGTaskDependValue = new HashMap<String,Double>();
		DAGTaskDependValue = dagmap.getdependvalue();
		for(int i=0;i<dagmap.gettasklist().size();i++)
		{
			DAGTaskList.add((DAG) dagmap.gettasklist().get(i));
		}
		
		while(time <= timeWindow)
		{
			
			boolean fini = true;
			for(DAG dag:DAGTaskList)
			{
				if(dag.getfillbackdone()==false && dag.getfillbackpass()==false)
				{
					fini = false;
					break;
				}
			}
			if(fini)
			{	
				break;
			}
			
			for(DAG dag:DAGTaskList)
			{
				if((dag.getfillbackstarttime()+dag.getts()) == time && dag.getfillbackready() && dag.getfillbackdone() == false)
				{
					dag.setfillbackfinishtime(time);
					dag.setfillbackdone(true);
					PEList.get(dag.getfillbackpeid()).setfree(true);
					//System.out.println("the task " + dag.getid() +" in DAG "+dag.getdagid()+" : start at "+ dag.getfillbackstarttime() +" finish at "+ dag.getfillbackfinishtime() + " at PE"+ dag.getfillbackpeid());
				}
			}
			
			for(DAG dag:DAGTaskList)
			{
				if(dag.getarrive() <= time && dag.getfillbackdone() == false && dag.getfillbackready() == false && dag.getfillbackpass() == false)
				{
					boolean ifready = checkready(dag,DAGTaskList,DAGTaskDependValue,time);
					if(ifready)
					{
						dag.setfillbackready(true);
						readyqueue.add(dag);
					}
				}
			}
			
			schedule(DAGTaskList,DAGTaskDependValue,time);
			
			for(DAG dag:DAGTaskList)
			{
				if(dag.getfillbackstarttime() == time && dag.getfillbackready() && dag.getfillbackdone() == false)
				{
					if(dag.getdeadline() >= time)
					{
						if(dag.getts() == 0)
						{
							dag.setfillbackfinishtime(time);
							dag.setfillbackdone(true);
							time = time -T;
						}
						else
						{
							PEList.get(dag.getfillbackpeid()).setfree(false);
							PEList.get(dag.getfillbackpeid()).settask(dag.getid());
						}
					}	
					else
					{
						dag.setfillbackpass(true);
					}
				}
				
			}
			time = time + T;
		}

	}
	
	/**
     * 调度readyList
     *
     * @param DAGTaskList，DAG任务列表
     * @param DAGTaskDependValue，该子任务所在的DAG的子任务间依赖关系
     * @param time，当前时刻
     */	
	private static void schedule(ArrayList<DAG> DAGTaskList,Map<String,Double> DAGTaskDependValue,int time){
		
		ArrayList<DAG> buff = new ArrayList<DAG>();
		DAG min = new DAG();
		DAG temp = new DAG();
		for(int k = 0 ; k < readyqueue.size() ; k++)
		{
			int tag = k;
			min = readyqueue.get(k);
			temp = readyqueue.get(k);
			for(int p = k+1 ; p < readyqueue.size() ; p++ )
			{
				if(readyqueue.get(p).getarrive() < min.getarrive() )
				{
					min = readyqueue.get(p);
					tag = p;
				}
			}
			if(tag != k)
			{
				readyqueue.set(k, min);
				readyqueue.set(tag, temp);
			}
		}

		for(int i = 0;i<readyqueue.size();i++)
		{
			DAG buf1 = new DAG();
			buf1 = readyqueue.get(i);
			
			for(DAG dag:DAGTaskList)
			{
				if(buf1.getid() == dag.getid())
				{
					choosePE(dag,DAGTaskDependValue,time);
					break;
				}
			}
		}
		
		readyqueue.clear();
			
	}
	
    /**
     * 判断某一子任务是否达到就绪状态
     *
     * @param dag，要判断的子任务dag
     * @param DAGTaskList，DAG任务列表
     * @param DAGTaskDependValue，该子任务所在的DAG的子任务间依赖关系
     * @param time，当前时刻
     * @return isready，该子任务dag是否可以加入readyList
     */
	private static boolean checkready(DAG dag,ArrayList<DAG> DAGTaskList,Map<String,Double> DAGTaskDependValue,int time){
		
		boolean isready = true;
		
		if(dag.getfillbackpass() == false && dag.getfillbackdone() == false)
		{
			if(time > dag.getdeadline())
			{
				dag.setfillbackpass(true);
			}
			if(dag.getfillbackstarttime()==0 && dag.getfillbackpass() == false)
			{
				ArrayList<DAG> pre_queue = new ArrayList<DAG>();
				ArrayList<Integer> pre = new ArrayList<Integer>(); 
				pre = dag.getpre();
				if(pre.size()>=0)
				{
					for(int j = 0;j<pre.size();j++)
					{
						DAG buf3 = new DAG();
						buf3 = getDAGById(dag.getdagid(),pre.get(j));
						pre_queue.add(buf3);
						
						if(buf3.getfillbackpass())
						{
							dag.setfillbackpass(true);
							isready = false;
							break;
						}
					
						if(!buf3.getfillbackdone())
						{
							isready = false;
							break;
						}
						
					}
				}
			}
		}
		
		return isready;
	}
	
	/**
     * 为子任务选择处理器，选择可以最早开始处理的PE
     *
     * @param dag_temp，要选择处理器的子任务
     * @param DAGTaskDependValue，该子任务所在的DAG的子任务间依赖关系
     * @param time，当前时刻
     */
	private static void choosePE(DAG dag_temp,Map<String,Double> DAGTaskDependValue,int time){
		
		ArrayList<DAG> pre_queue = new ArrayList<DAG>();
		ArrayList<Integer> pre = new ArrayList<Integer>(); 
		pre = dag_temp.getpre();
		if(pre.size()>=0)
		{
			for(int j = 0;j<pre.size();j++)
			{
				DAG buf = new DAG();
				buf = getDAGById(dag_temp.getdagid(),pre.get(j));
				pre_queue.add(buf);
			}
		}
		
		int temp[] = new int[PEList.size()];
		for(int i=0;i<PEList.size();i++)
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			
			if(pre_queue.size() == 0)
			{
				if(TASKInPe.size()==0)
				{
					temp[i] = time;
				}
				else
				{
					if(time>TASKInPe.get(TASKInPe.size()-1)[1])
						temp[i] = time;
					else
						temp[i] = TASKInPe.get(TASKInPe.size()-1)[1];
				}
			}
			else if(pre_queue.size() == 1)
			{
				if(pre_queue.get(0).getfillbackpeid() == PEList.get(i).getID())
				{
					if(TASKInPe.size()==0)
					{
						temp[i] = time;
					}
					else
					{
						if(time>TASKInPe.get(TASKInPe.size()-1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size()-1)[1];
					}
				}
				else
				{
					int value = (int)(double)DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+" "+String.valueOf(dag_temp.getid()));
					if(TASKInPe.size()==0)
					{
						if((pre_queue.get(0).getfillbackfinishtime()+value) < time)
							temp[i] = time;
						else
							temp[i] = pre_queue.get(0).getfillbackfinishtime() + value;
					}
					else
					{
						if((pre_queue.get(0).getfillbackfinishtime()+value) > TASKInPe.get(TASKInPe.size()-1)[1] && (pre_queue.get(0).getfillbackfinishtime()+value) > time)
							temp[i] = pre_queue.get(0).getfillbackfinishtime() + value;
						else if(time>(pre_queue.get(0).getfillbackfinishtime()+value) && time>TASKInPe.get(TASKInPe.size()-1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size()-1)[1];
					}
				}
			}
			else
			{		 
				int max = time;
				for(int j=0;j<pre_queue.size();j++)
				{
					if(pre_queue.get(j).getfillbackpeid() == PEList.get(i).getID())
					{
						if(TASKInPe.size()!=0)
						{
							if(max < TASKInPe.get(TASKInPe.size()-1)[1])
								max = TASKInPe.get(TASKInPe.size()-1)[1];
						}
					}
					else
					{
						int valu = (int)(double)DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+" "+String.valueOf(dag_temp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+valu;

						if(TASKInPe.size()==0)
						{
							if(max<value)
								max = value;
						}
						else
						{
							if(value <= TASKInPe.get(TASKInPe.size()-1)[1])
							{
								if(max <TASKInPe.get(TASKInPe.size()-1)[1])
									max = TASKInPe.get(TASKInPe.size()-1)[1];
							}
							else
							{
								if(max < value)
									max = value;
							}
						}
					}
					
				}
				temp[i] = max;
			}
		}		
		
		int min = timewindowmax;
		int minpeid = -1;
		for(int i=0;i<PEList.size();i++){
			if(min > temp[i])
				{
					min = temp[i];
					minpeid = i;
				}
		}
		
		if(min <= dag_temp.getdeadline())
		{
			HashMap<Integer,Integer[]> TASKInPe = new HashMap<Integer,Integer[]>();
			TASKInPe = TASKListInPes.get(minpeid);
			
			dag_temp.setfillbackpeid(minpeid);
			dag_temp.setts(dag_temp.getlength());
			dag_temp.setfillbackstarttime(min);
			dag_temp.setfinish_suppose(dag_temp.getfillbackstarttime()+dag_temp.getts());
			
			Integer[] st_fi = new Integer[4];
			st_fi[0] = dag_temp.getfillbackstarttime();
			st_fi[1] = dag_temp.getfillbackstarttime()+dag_temp.getts();
			st_fi[2] = dag_temp.getdagid();
			st_fi[3] = dag_temp.getid();
			TASKInPe.put(TASKInPe.size(), st_fi);			
			
		}
		else
		{
			dag_temp.setfillbackpass(true);
		}

	}
	
	/**
     * 使用FIFO调度第一个DAG，若调度成功，进行LevelRelaxing操作，并且修改TASKListInPes中各个TASK的开始结束时间
     */
	public static void scheduleFirstDAG()
	{
		FIFO(DAGMapList.get(0));
		DAG tem = (DAG)DAGMapList.get(0).gettasklist().get(DAGMapList.get(0).gettasknumber()-1);
		if(tem.getfillbackdone())
		{
			DAGMapList.get(0).setfillbackdone(true);
			DAGMapList.get(0).setfillbackpass(false);
		}

		int Criticalnumber = CriticalPath(DAGMapList.get(0)); //find the critical path and get the task number on it
		levelrelax(DAGMapList.get(0),DAGMapList.get(0).getDAGdeadline(),Criticalnumber); //relax the scheduling result
		changetasklistinpe(DAGMapList.get(0));
	}
	
	/**
	 * 根据DAX文件为DAG添加相互依赖关系
	 * @param i，DAGID
	 * @param preexist，将所有的工作流中子任务全部添加到一个队列，在本DAG前已有preexist个任务
	 * @param tasknumber，DAG中任务个数
	 * @param arrivetimes，DAG到达时间
	 * @return back，将所有的工作流中子任务全部添加到一个队列，在本DAG全部添加后，有back个任务
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i,int preexist,int tasknumber,int arrivetimes) throws NumberFormatException, IOException, JDOMException{
		
		int back = 0;
		DAGDependMap_personal = new HashMap<Integer,Integer>();
		DAGDependValueMap_personal = new HashMap<String,Double>();
		ComputeCostMap = new HashMap<Integer,int[]>();
		AveComputeCostMap = new HashMap<Integer,Integer>();
	
		//获取XML解析器
		SAXBuilder builder = new SAXBuilder();
		//获取document对象
		Document doc = builder.build("DAG_XML/dag"+(i+1)+".xml");
		//获取根节点
		Element adag = doc.getRootElement();
						
		for(int j = 0;j<tasknumber;j++)
		{
			DAG dag = new DAG();
			DAG dag_persional = new DAG();

			dag.setid(Integer.valueOf(preexist+j).intValue());
			dag.setarrive(arrivetimes);
			dag.setdagid(i);
			dag_persional.setid(Integer.valueOf(j).intValue());
			dag_persional.setarrive(arrivetimes);
			dag_persional.setdagid(i);
			
			XPath path = XPath.newInstance("//job[@id='"+j+"']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute)list.get(0);
			int x = Integer.valueOf(attribute.getValue()).intValue();
			dag.setlength(x);
			dag.setts(x);
			dag_persional.setlength(x);
			dag_persional.setts(x);
			
			if(j == tasknumber-1)
			{
				dag.setislast(true);
				islastnum++;
			}
			
			DAG_queue.add(dag);
			DAG_queue_personal.add(dag_persional);
			
			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for(int k=0;k<PEList.size();k++){
				bufferedDouble[k] = Integer.valueOf(x/PEList.get(k).getability());
				sum = sum + Integer.valueOf(x/PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble);
			AveComputeCostMap.put(j,(sum/PEList.size()));
		} 
		
		XPath path1 = XPath.newInstance("//uses[@link='output']/@file");
		List list1 = path1.selectNodes(doc);
		for(int k=0;k<list1.size();k++)
		{			
			Attribute attribute1 = (Attribute)list1.get(k);
			String[] pre_suc = attribute1.getValue().split("_");
			int[] presuc = new int[2];
			presuc[0] = Integer.valueOf(pre_suc[0]).intValue()+preexist;
			presuc[1] = Integer.valueOf(pre_suc[1]).intValue()+preexist;

			XPath path2 = XPath.newInstance("//uses[@file='"+attribute1.getValue()+"']/@size");
			List list2 = path2.selectNodes(doc);
			Attribute attribute2 = (Attribute)list2.get(0);
			int datasize = Integer.valueOf(attribute2.getValue()).intValue();
			
			DAGDependMap.put(presuc[0],presuc[1]);
			DAGDependValueMap.put((presuc[0]+" "+presuc[1]), (double) datasize);

			DAG_queue.get(presuc[0]).addToSuc(presuc[1]);
			DAG_queue.get(presuc[1]).addToPre(presuc[0]);
			
			DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),Integer.valueOf(pre_suc[1]).intValue());
			DAGDependValueMap_personal.put((pre_suc[0]+" "+pre_suc[1]),(double) datasize);

			int tem0 = Integer.parseInt(pre_suc[0]);
			int tem1 = Integer.parseInt(pre_suc[1]);
			
			DAG_queue_personal.get(tem0).addToSuc(tem1);
			DAG_queue_personal.get(tem1).addToPre(tem0);
			
		}
		
		back = preexist+tasknumber;
		return back;
	}

	/**
	 * 为DAG根据deadline，给每个子任务计算相应的最迟截止时间
	 * @param dead_line，DAG的deadline
	 * @param dagdepend_persion，DAG中相互依赖关系
	 */
	private static void createDeadline_XML(int dead_line,DAGdepend dagdepend_persion) throws Throwable{
		int maxability = 1;
		int max = 10000;

		for(int k=DAG_queue_personal.size()-1 ;k >= 0 ; k--)
		{
			ArrayList<DAG> suc_queue = new ArrayList<DAG>();
			ArrayList<Integer> suc = new ArrayList<Integer>(); 
			suc = DAG_queue_personal.get(k).getsuc();
			if(suc.size()>0)
			{
				for(int j = 0;j<suc.size();j++)
				{
					int tem = 0;
					DAG buf3 = new DAG();
					buf3 = getDAGById_1(suc.get(j));
					suc_queue.add(buf3);
					tem = (int) (buf3.getdeadline() - (buf3.getlength()/maxability));
					//tem = (int) (buf3.getdeadline() - (buf3.getlength()/maxability) - dagdepend_persion.getDependValue(DAG_queue_personal.get(k).getid(),suc.get(j)));
					if(max > tem)
						max = tem;
				}	
				DAG_queue_personal.get(k).setdeadline(max);
			}
			else
			{
				DAG_queue_personal.get(k).setdeadline(dead_line);
			}	
		}
	}	

	/**
	 * 创建DAGMAP实例并初始化
	 * @param dagdepend，工作流依赖关系
	 * @param vcc，计算能力
	 */
	public static void initdagmap(DAGdepend dagdepend,computerability vcc) throws Throwable
	{
		int pre_exist = 0;
		File file = new File(System.getProperty("user.dir")+"\\DAG_XML\\");
		String[] fileNames =file.list();
		int num = fileNames.length-1;
		
		BufferedReader bd = new BufferedReader(new FileReader("DAG_XML/Deadline.txt"));
		String buffered;
		for(int i = 0;i<num;i++)
		{
			DAGMap dagmap = new DAGMap();
			DAGdepend dagdepend_persional = new DAGdepend();
			DAG_queue_personal.clear();

			//获取DAG的arrivetime和deadline，task个数
			buffered=bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];
			
			buff[0]= Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();//dagID
			buff[1]= Integer.valueOf(bufferedA[1]).intValue();//tasknum
			buff[2]= Integer.valueOf(bufferedA[2]).intValue();//arrivetime
			buff[3]= Integer.valueOf(bufferedA[3]).intValue();//deadline
			int deadline = buff[3];
			int tasknum = buff[1];
			int arrivetime = buff[2];
			
			pre_exist = initDAG_createDAGdepend_XML(i,pre_exist,tasknum,arrivetime);
	
			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);
			dagdepend_persional.setDAGList(DAG_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional.setDAGDependValueMap(DAGDependValueMap_personal);
						
			createDeadline_XML(deadline,dagdepend_persional);
			
			int number_1 = DAG_queue.size();
			int number_2 = DAG_queue_personal.size();
			for(int k=0;k<number_2;k++)
			{
				DAG_queue.get(number_1-number_2+k).setdeadline(DAG_queue_personal.get(k).getdeadline());
			}
			
			dagmap.settasknumber(tasknum);
			dagmap.setDAGId(i);
			dagmap.setDAGdeadline(deadline);
			dagmap.setsubmittime(arrivetime);
			dagmap.settasklist(DAG_queue_personal);
			dagmap.setdepandmap(DAGDependMap_personal);
			dagmap.setdependvalue(DAGDependValueMap_personal);
			DAGMapList.add(dagmap);

		}
	
		dagdepend.setdagmaplist(DAGMapList);
		dagdepend.setDAGList(DAG_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);
		
	}
	
	/**
	 * 创建PE实例并初始化
	 */
	private static void initPE() throws Throwable{
		
		for(int i=0;i<pe_number;i++)
		{
			PE pe = new PE();
			pe.setID(i);
			pe.setability(1);
			pe.setfree(true);
			pe.setAvail(0);
			PEList.add(pe);
		}
	}
	
	/**
	 * 根据DAGID和TASKID返回该TASK实例
	 * @param DAGId，DAGID
	 * @param dagId，TASKID
	 * @return DAG，TASK实例
	 */
	private static DAG getDAGById(int DAGId,int dagId){
		for(int i=0;i<DAGMapList.get(DAGId).gettasknumber();i++)
		{
			DAG temp = (DAG)DAGMapList.get(DAGId).gettasklist().get(i);
			if(temp.getid() == dagId)
				return temp;
		}

		return null;
	}

	/**
	 * 根据TASKID返回该TASK实例
	 * @param dagId，TASKID
	 * @return DAG，TASK实例
	 */
	private static DAG getDAGById_1(int dagId){
		for(DAG dag:DAG_queue_personal){
			if(dag.getid() == dagId)
				return dag;
		}
		return null;
	}
	
}
