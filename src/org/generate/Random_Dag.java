package org.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Random_Dag {
	public String dagId;
	public int dagLevel;//Dag�Ĳ���
	public int dagSize;//Dag�Ĵ�С
	public int levelCount;//����ͳ��
	public int submitTime;//�ύʱ��
	public int deadlineTime;//�����Ҫ��ɵ�ʱ��
	public int[] levelNodeNumber;//�ӵڶ��㵽����ڶ���Ľڵ�ĸ���
	public List<TaskNode> taskList;
	public List<DagEdge> edgeList;
	public List<TaskNode> lastLevelList;//��һ��ڵ��б�
	public List<TaskNode> newLevelList;//��һ��ڵ��б�
	public List<TaskNode> leafNodeList;//��ǰҶ�ӽڵ�
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
 //   	System.out.println("dag�Ĳ���:"+dagLevel+" "+"���ڵĲ���:"+levelCount);
    	if(newLevelList.size() == levelNodeNumber[levelCount-1])//����һ�������󣬱��һ��
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
		System.out.println("�ڵ���:"+taskList.size());
		for(DagEdge edge:edgeList)
			 edge.printEdge();
		System.out.println();
		System.out.println();
	}
   
}
