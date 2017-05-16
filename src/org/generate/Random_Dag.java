package org.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Random_Dag {
	public String dagId;
	public int dagLevel;//Dag的层数
	public int dagSize;//Dag的大小
	public int levelCount;//层数统计
	public int submitTime;//提交时间
	public int deadlineTime;//最迟需要完成的时间
	public int[] levelNodeNumber;//从第二层到最倒数第二层的节点的个数
	public List<TaskNode> taskList;
	public List<DagEdge> edgeList;
	public List<TaskNode> lastLevelList;//上一层节点列表
	public List<TaskNode> newLevelList;//新一层节点列表
	public List<TaskNode> leafNodeList;//当前叶子节点
	public int[] color;
	
	public Random_Dag(int dagId) {
		init(dagId);
		TaskNode root = new TaskNode("root_"+dagId, 0, 0, 0);
		taskList.add(root);
		lastLevelList.add(root);
		leafNodeList.add(root);
		submitTime = 0;
	}
	
	public Random_Dag(int dagId,TaskNode root,int lastDagTime) {	
		init(dagId);
		taskList.add(root);
		lastLevelList.add(root);	
		leafNodeList.add(root);
		submitTime = DagBuilder.randomCreater.randomSubmitTime(lastDagTime,root.startTime);	   
	}
	
	public void init(int dagId){
		this.dagId = "dag"+dagId;
		taskList = new ArrayList<TaskNode>();
		edgeList = new ArrayList<DagEdge>();
		lastLevelList = new ArrayList<TaskNode>();
		leafNodeList = new ArrayList<TaskNode>();
		newLevelList = new ArrayList<TaskNode>();
		dagSize = DagBuilder.randomCreater.randomDagSize(BuildParameters.dagAverageSize);
		dagLevel = DagBuilder.randomCreater.randomLevelNum(dagSize,BuildParameters.dagLevelFlag);
		levelNodeNumber = new int[dagLevel];
		DagBuilder.randomCreater.randomLevelSizes(levelNodeNumber,dagSize);
		levelCount = 1;
	  
	}

    public void addToNewLevel(TaskNode taskNode){
    	newLevelList.add(taskNode);
 //   	System.out.println("dag的层数:"+dagLevel+" "+"现在的层数:"+levelCount);
    	if(newLevelList.size() == levelNodeNumber[levelCount-1])//当新一层填满后，变旧一层
    		{
    		levelCount++;    		
    		lastLevelList.clear();
    		lastLevelList.addAll(newLevelList);
    		newLevelList.clear();
    		}
    } 
	public void generateNode(TaskNode taskNode){
		taskList.add(taskNode);
	}
	
	public void generateEdge(TaskNode head,TaskNode tail){
		DagEdge dagEdge = new DagEdge(head, tail);
		edgeList.add(dagEdge);		
	}
	
	public boolean containTaskNode(TaskNode taskNode){
		return taskList.contains(taskNode);
	}
	
	public void computeDeadLine()
	{
		int proceesorEndTime = BuildParameters.timeWindow/BuildParameters.processorNumber;
		deadlineTime = (int) (submitTime + (taskList.get(taskList.size()-1).endTime - submitTime)*BuildParameters.deadLineTimes);
		if(deadlineTime > proceesorEndTime)
			deadlineTime = proceesorEndTime;
	}
	
	public void printDag(){
		System.out.println(dagId+":");
		for(TaskNode taskNode:taskList)
			System.out.print(taskNode.nodeId+" ");
		System.out.println();
		System.out.println("节点数:"+taskList.size());
		for(DagEdge edge:edgeList)
			 edge.printEdge();
		System.out.println();
		System.out.println();
	}
   
}
