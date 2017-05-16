package org.generate;

import java.util.ArrayList;
import java.util.List;

public class Processor {
    public String processorId;//处理器的编号
    public static int capacity;//处理器的处理能力    
    public int startWorkTime;//处理器开始时间
    public int endWorkTime;//处理器结束工作时间
    public List<TaskNode> nodeList;//处理器包含的任务节点
	
    public Processor(int id,int endTime) {
		processorId = "processor"+id;
		capacity = 1;		
		startWorkTime = 0;
		endWorkTime = endTime;
		nodeList = new ArrayList<TaskNode>();
		RandomCreater randomCreater = new RandomCreater();
		randomCreater.randomCreateNodes(id, nodeList, capacity, endWorkTime);	
	}
    
    public void printNodes(){
    	System.out.print(processorId+":");
    	for(TaskNode node:nodeList)
    	{
    		System.out.print(node.nodeId+":"+node.taskLength+" ");
    	}
    	System.out.println();
    }
    
    
}
