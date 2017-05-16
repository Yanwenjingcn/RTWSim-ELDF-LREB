package org.generate;

import java.util.ArrayList;
import java.util.List;

public class RandomCreater {
	
	public double taskLengthRate = 0.5;//任务长度浮动比
//	public double bandWithRate = 0.25; //带宽浮动比	
	public double levelRate = 0.5;//每一层任务的浮动比
	
	/**
	 * @param id 处理器编号
	 * @param nodeList 处理器节点列表
	 * @param capacity 处理器的peak
	 * @param endTime  处理器运行结束时间
	 */
	public void randomCreateNodes(int id,List<TaskNode> nodeList,int capacity,int endTime){
		int capacityLength = (int) (capacity*endTime);
		int nodeNum = 0; //task二级编号
		int totalLength = 0;//任务长度	
		
		while(totalLength < capacityLength){
			nodeNum++;
			int taskLength = random((int)(BuildParameters.taskAverageLength*(1-taskLengthRate)), (int)(BuildParameters.taskAverageLength*(1+taskLengthRate)));//生成任务长度	
			TaskNode taskNode;
			if(taskLength+totalLength > capacityLength)//超过了任务长度
			taskLength = capacityLength -totalLength;
			taskNode = new TaskNode(Integer.toString(id)+"_"+Integer.toString(nodeNum),taskLength,(totalLength/capacity),(taskLength+totalLength)/capacity);	
			nodeList.add(taskNode);
			totalLength+=taskLength;
		}			
	}
	
	
	  /**
	 * @param bandWidth 带宽数组
	 * @param size 数组大小
	 */
	/*public void randomBandWidths(double[][] bandWidth,int size){  		
		   for(int i=0;i<size;i++)
			   for(int j=0;j<=i;j++)
			   {
				 if(i==j)
					 bandWidth[i][j] = -1.00;
				 else
				 { 
					 bandWidth[i][j] = BandWidth.baseRate*(1+random()*bandWithRate);
					 bandWidth[j][i] = bandWidth[i][j];				 
				 }
		         
			   }
	  }	*/
	
	/**
	 * @param lastDagtime 上一个Dag提交的时间
	 * @param startTime 当前Dag开始执行的时间
	 * @return 随机生成的Dag的提交时间
	 */
	public int randomSubmitTime(int lastDagtime,int startTime){
		return random(lastDagtime, startTime);
	}
	
	/**
	 * @param maxlength 边上的差值
	 * @return 随机生成的数据传送时间
	 */
	public int randomTranferData(int maxlength)
	{
		if(maxlength == 0)
			return 0;
		else
			return random(1, maxlength);
	}
	
	public int randomDagSize(int dagAverageSize){
		return random((int)(dagAverageSize*0.5),(int)(dagAverageSize*1.5));
		
	}
	
	public int randomLevelNum(int dagSize,int levelFlag){
		int sqrt = (int)Math.sqrt(dagSize-2);
		if(levelFlag == 1)
			return random(1, sqrt);
		else if(levelFlag == 2)
			return random(sqrt,sqrt+3);
		else if(levelFlag == 3)
			return random(sqrt+3,dagSize-2);
		else
			return sqrt;		
	}
	
	public void randomLevelSizes(int[] dagLevel,int nodeNumber){
		for(int j = 0;j < dagLevel.length;j++)
			 dagLevel[j] = 1;
		int i = nodeNumber - dagLevel.length;
		 while(i > 0)
		 {
			 for(int j = 0;j < dagLevel.length;j++)
		
				  if(random(0, 1) == 1)
				  {
					  dagLevel[j]++;
					  i--;
					  if(i == 0)
						  break;
				  }	
			 if(i == 0)
				 break;
		 }
	}
	
	/**
	 * @return 产生[min,max]之间的随机数
	 */
	public int random(int min,int max){
		return (int)(min + Math.random()*(max-min+1));
	}
	

}

