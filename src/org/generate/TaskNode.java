package org.generate;

public class TaskNode {
	public String nodeId;//任务节点编号
	public int taskLength;//任务的长度
//	public static int baseLength = 100;//任务的随机基准长度
	public int startTime;//任务的开始时间
	public int endTime;//任务的结束时间
	
	public TaskNode(String nodeId, int taskLength, int startTime,int endTime) {
		this.nodeId = nodeId;
		this.taskLength = taskLength;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	/**
	 * @return 返回对应处理器的id
	 */
	public int getProcessorId(){
		String[] processorId = nodeId.split("_");
		if(!processorId[0].equals("root")&&!processorId[0].equals("foot"))
		  return Integer.parseInt(processorId[0]);	
		else
		  return 0;
	}
}

