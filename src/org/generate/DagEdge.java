package org.generate;

public class DagEdge {
	public TaskNode head;
	public TaskNode tail;
	public int transferData;
	
	public DagEdge(TaskNode head, TaskNode tail) {
		this.head = head;
		this.tail = tail;
		
		if(head.getProcessorId() == tail.getProcessorId()) //如果是同一台机器
			this.transferData = DagBuilder.randomCreater.randomTranferData((head.taskLength+tail.taskLength)/2);
		else //如果不是一个机器数据量为边差值的随机值
		{
			if(tail.startTime!=100*10000)
				this.transferData = DagBuilder.randomCreater.randomTranferData(tail.startTime - head.endTime);
			else
				this.transferData = 0;
		}
		//System.out.println(head.nodeId+" "+head.getProcessorId()+" "+tail.nodeId+" "+tail.getProcessorId()+" "+this.transferData);
	}
	
	public DagEdge(TaskNode head, TaskNode tail , int transferData) {
		this.head = head;
		this.tail = tail;
		this.transferData = 0;	
	}
	
	public void printEdge(){
		System.out.print(head.nodeId+"——>"+tail.nodeId+" ");
	}
}
