package org.generate;

public class DagEdge {
	public TaskNode head;
	public TaskNode tail;
	public int transferData;
	
	public DagEdge(TaskNode head, TaskNode tail) {
		this.head = head;
		this.tail = tail;
		
		if(head.getProcessorId() == tail.getProcessorId()) //�����ͬһ̨����
			this.transferData = DagBuilder.randomCreater.randomTranferData((head.taskLength+tail.taskLength)/2);
		else //�������һ������������Ϊ�߲�ֵ�����ֵ
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
		System.out.print(head.nodeId+"����>"+tail.nodeId+" ");
	}
}
