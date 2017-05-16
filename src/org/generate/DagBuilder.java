package org.generate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DagBuilder {
	

	private int endNodeNumber = 0;
	
	public static int endTime = 100*10000;
	
    public static RandomCreater randomCreater;
	
	public List<TaskNode> unCompleteTaskList;//δ��������б�
	
    public List<Random_Dag> dagList;//dag�б�
    
    public static List<Random_Dag> finishDagList;//������ɵ�Dag�б�
    
    public List<String> endNodeList;//�����ڵ��б�
    
	
	public List<Processor> createProcessor(int number, int endTime) {
		List<Processor> processorList = new ArrayList<Processor>();
		for (int i = 1; i <= number; i++) {
			Processor processor = new Processor(i, endTime);
			processorList.add(processor);
		}  
		return processorList;
    }
	
	/*public BandWidth createBandWidth(List<Processor> processorList){
		 BandWidth bandWidth = new BandWidth(processorList);	
		 return bandWidth;
	}*/
	
	/** ��ӡ�ڵ���Ϣ
	 * @param processorList �������б�
	 */
	public void printNodes(List<Processor>	processorList){
		for(Processor processor:processorList)
		   { 
			//processor.printNodes();	
			System.out.print(processor.processorId +":"+processor.nodeList.size()+" ");
			System.out.println();
		   }
	}
   
	public void initList(List<Processor> processorList)
	{
		//��ʼ��δ��ɵ��б�
		int maxsize = 0;
		for(Processor processor:processorList)
		{
			int size = processor.nodeList.size();
			  if(size > maxsize)
			     maxsize = size;			
		}	 
		
		for(int i = 0;i < maxsize; i++)
		    for(Processor processor:processorList)
		     {
		       if(i < processor.nodeList.size())
		        unCompleteTaskList.add(processor.nodeList.get(i));
		     }		
//		for(TaskNode taskNode:unCompleteTaskList)
//		       System.out.print(taskNode.nodeId+" ");
//		System.out.println();
		
		//��ʼ�������ڵ��б�
//		 System.out.print("�����ڵ��б�:");
		for(Processor processor:processorList)
		{
			//System.out.print(processor.nodeList.size());
			String endNodeId = processor.nodeList.get(processor.nodeList.size()-1).nodeId;
//			 System.out.print(endNodeId+" ");
			endNodeList.add(endNodeId);
		}
		System.out.println();
	}	
	
	public boolean isEndNode(String taskId)
	{
		for(String endNodeId:endNodeList)
			if(taskId.equals(endNodeId))
			    return true;
		return false;
	}
	
	public boolean tryFinishDag(TaskNode taskNode,Random_Dag dag)
	{
		int m = 0;
		  for(TaskNode leafTask:dag.leafNodeList)
			  if(taskNode.startTime >= leafTask.endTime)
			  { 
				  if(taskNode.startTime == leafTask.endTime)//���ʱ���Ϊ0,�ж��Ƿ���ͬһ����������
			       {
			    	   if(taskNode.getProcessorId() != leafTask.getProcessorId())
			    		   continue;
			       }			
				  m++;
			  }
		  if(m == dag.leafNodeList.size())//���Թ���������Dagͼ
		  {
			  dag.generateNode(taskNode);
			  for(TaskNode leafTask:dag.leafNodeList)
			   	 dag.generateEdge(leafTask, taskNode);
			  dagList.remove(dag);
			  
			  finishDagList.add(dag);//�ŵ�����б���ȥ
			   return true;	  
		   }				  
		 return false;
	}
	
	public void searchParentNode(List<TaskNode> unCompleteTaskList,List<Random_Dag> dagList)
	{
		for(int i=0;i<unCompleteTaskList.size();i++)
		{
			TaskNode taskNode = unCompleteTaskList.get(i);
			Collections.shuffle(dagList);//��nodeȥ���ƥ��һ��dag
			boolean match = false;				
			for(int n = 0;n < dagList.size();n++) //���Dagƥ��
			{
				Random_Dag dag = dagList.get(n);
				if(dag.levelCount == dag.dagLevel+1)
					match = tryFinishDag(taskNode, dag);
				if(match)
					   break;
			}
			if(match)
				continue;			
			if(isEndNode(taskNode.nodeId))//�����ڵ�ƥ��
			  {
			    for(int k=0;k<dagList.size();k++)
			    {
			    	Random_Dag dag = dagList.get(k);
				   match = tryFinishDag(taskNode, dag);
				   if(match)
					   break;
			    }
			  }		
			if(match)
				continue;
		//	  boolean newDagFlag = true;
			  for(int k=0;k<dagList.size();k++)
			    {
				  Random_Dag dag = dagList.get(k);
				    if(dag.levelCount == dag.dagLevel+1)
				    	continue;
				    boolean matchFlag = false;
				    int edgeNum = 0;//�͵�ǰ�ڵ������ӵıߵ�����
					for(int j= 0;j<dag.lastLevelList.size();j++)	
				     {
					  TaskNode leafNode = dag.lastLevelList.get(j);
					    if(taskNode.startTime >=  leafNode.endTime)//�Ƿ�͵�ǰDagƥ��	  	        
			  	         { 
					       if(taskNode.startTime == leafNode.endTime)//���ʱ���Ϊ0,�ж��Ƿ���ͬһ����������
					       {
					    	   if(taskNode.getProcessorId() != leafNode.getProcessorId()&&leafNode.getProcessorId()!=0)
					    		   continue;
					       }				    	   
					       edgeNum++;
					       matchFlag = true;
					      // newDagFlag = false;//ƥ���Ͳ���newDag��root��
					       match = true;
					       dag.leafNodeList.remove(leafNode);//ƥ���Ͳ���Ҷ�ӽڵ���
					       if(!dag.containTaskNode(taskNode))//����Ѿ���ӣ������ظ�
					       {
					        dag.addToNewLevel(taskNode);
			  	    	    dag.generateNode(taskNode);	
//			  	    	    addnum++;
			  	    	    dag.leafNodeList.add(taskNode);//���Ҷ�ӽڵ�
					       }
		                   if(edgeNum > 1)//����Ѿ�����һ��ƥ�����
		                   {
		                	   if(Math.random()>0.5)
		                		   continue;
		                   }
			  	    	   dag.generateEdge(leafNode,taskNode);			  	    	   
			  	         }					    
				     }
				  if(matchFlag) break;//����͵�ǰDagƥ������				  
			     }
			  if(!match)//����ƥ����Ϊ��Dag��root
				  {
//				    System.out.println(dagList.size());
//				    System.out.println(finishDagList.size());
				    int foreDagTime;
				    if(dagList.size()>0)
				    foreDagTime = dagList.get(dagList.size()-1).submitTime;
				    else
				    foreDagTime = finishDagList.get(finishDagList.size()-1).submitTime;	
				    Random_Dag dag = new Random_Dag(dagList.size()+finishDagList.size()+1,taskNode,foreDagTime);
//				    addnum++;
//				    System.out.println("����Dag:"+dag.dagId);
				    dagList.add(dag);
				  }
			}	  	       
		
	}
	
	
	public void generateDags(int number){
		for(int i=1;i <= number;i++){
			Random_Dag dag = new Random_Dag(i);
			dagList.add(dag);
		}
		searchParentNode(unCompleteTaskList,dagList);
	}
	
	public void fillDags(){
		for(int k=0;k<dagList.size();k++)
	    {
			Random_Dag dag = dagList.get(k);
		   TaskNode footNode = new TaskNode("foot_"+(k+1), 0, endTime, endTime);
		   dag.generateNode(footNode);
		   endNodeNumber++;//β�ڵ�����һ
		   for(TaskNode leafTask:dag.leafNodeList)
			   dag.generateEdge(leafTask,footNode);
		   finishDagList.add(dag);   
	    }
	}
	
	public void finishDags()
	{
		for(Random_Dag dag:finishDagList)
		{
			dag.computeDeadLine();
	//		dag.printDag();
		}
	}
	
	
	public void checkDags()//������ɵ�dag�Ƿ���ȷ
	{
		//���ڵ�����
		int nodeSum = 0;
		for(Random_Dag dag:finishDagList)
		  {
			  nodeSum += dag.taskList.size();
			  for(TaskNode node:dag.taskList)
				  if(!unCompleteTaskList.contains(node))
				  {
					  System.err.print("�������ڵ㣺"+node.nodeId+" ");						  
				  }  
			  for(DagEdge edge:dag.edgeList)
				  if(edge.tail.startTime < edge.head.endTime)
					  System.err.print("�ߴ���"+edge.head+"����>"+edge.tail+"��");	
		  }
			 
		System.err.println();
		
		int number = 0;
		for(TaskNode taskNode:unCompleteTaskList)
		{
			
			boolean containflag = false;
			   for(Random_Dag dag:finishDagList)
			     if(dag.taskList.contains(taskNode))
			    	   {
			    	      containflag =true;
			    	      break;
			    	   }
			if(!containflag)
				{
				System.err.print(taskNode.nodeId + ":"+taskNode.startTime +" ")	;
				number++;
				}
			
		
		}
//		System.err.print(" dag����©�ڵ���" + number);
		
		if(nodeSum == unCompleteTaskList.size()+1+endNodeNumber)
			System.out.println("Success");
		else
			System.out.println("check dags and fix bugs");
		
	}
	public void writeDags()
	{
		//�����txt�ļ���Ϊxml�ļ�
	   FileDag fileDag = new FileDag() ;
	   XMLDag xmldag = new XMLDag();
	   
	   fileDag.clearDir();
	   xmldag.clearDir();
	   
	   //System.out.println("finishDagList.size:"+finishDagList.size()+" "+DagBuilder.finishDagList.size()); 
	   
	   try {
		   String basePath = System.getProperty("user.dir")+"\\DAG_XML\\";
		   String filePathxml =basePath+"Deadline.txt";	
	   
		   PrintStream out = System.out;
		   PrintStream ps=new PrintStream(new FileOutputStream(filePathxml));
		   System.setOut(ps);   //�ض��������  	
		   for(int i=1;i<=finishDagList.size();i++)
		   {
			   for(Random_Dag dag:finishDagList)
			   {
				   String[] number = dag.dagId.split("dag");
				   if(i == Integer.valueOf(number[1]).intValue())
				   {
					   System.out.println(dag.dagId+" "+dag.taskList.size()+" "+dag.submitTime+" "+dag.deadlineTime);
					   break;
				   }
			   }		
		   }
		   ps.close();
		   System.setOut(out); 
	   }catch (IOException e) {
			// TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   
	   for(Random_Dag dag:finishDagList)
	   {
		   fileDag.writeData(dag);
		   xmldag.writeDataToXML(dag);
	   }
	}
	public void initDags(){
		unCompleteTaskList = new ArrayList<TaskNode>();
		dagList = new ArrayList<Random_Dag>();
		finishDagList = new ArrayList<Random_Dag>();
		endNodeList = new ArrayList<String>();
		randomCreater = new RandomCreater();
		
		List<Processor>	processorList = createProcessor(BuildParameters.processorNumber,BuildParameters.timeWindow/BuildParameters.processorNumber);
		printNodes(processorList);		
		initList(processorList);
		generateDags(1);//�տ�ʼ����һ����root��dag
		fillDags();
		finishDags();//����deadline�ʹ�ӡ��Ϣ
		checkDags();//�������Dag����ȷ��
		writeDags();
	}

	//public static void main(String[] args){
	public void BuildDAG() {
		// TODO Auto-generated method stub
        DagBuilder dagBuilder = new DagBuilder();
        dagBuilder.initDags();
	   
	}
    
}

