package org.generate;

import java.util.ArrayList;
import java.util.List;

public class Processor {
    public String processorId;//�������ı��
    public static int capacity;//�������Ĵ�������    
    public int startWorkTime;//��������ʼʱ��
    public int endWorkTime;//��������������ʱ��
    public List<TaskNode> nodeList;//����������������ڵ�
	
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
