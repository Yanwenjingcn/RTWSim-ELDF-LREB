package org.generate;

public class TaskNode {
	public String nodeId;//����ڵ���
	public int taskLength;//����ĳ���
//	public static int baseLength = 100;//����������׼����
	public int startTime;//����Ŀ�ʼʱ��
	public int endTime;//����Ľ���ʱ��
	
	public TaskNode(String nodeId, int taskLength, int startTime,int endTime) {
		this.nodeId = nodeId;
		this.taskLength = taskLength;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	/**
	 * @return ���ض�Ӧ��������id
	 */
	public int getProcessorId(){
		String[] processorId = nodeId.split("_");
		if(!processorId[0].equals("root")&&!processorId[0].equals("foot"))
		  return Integer.parseInt(processorId[0]);	
		else
		  return 0;
	}
}

