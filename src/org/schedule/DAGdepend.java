package org.schedule;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DAGdepend { //一个工作流中的TASK间依赖关系，以下的DAG均为工作流中的子任务TASK

	private List<DAG> DAGList;
	
	private Map<Integer,Integer> DAGDependMap;
	
	private Map<String,Double> DAGDependValueMap;

	public ArrayList<DAGMap> DAGMapList;

	public void setdagmaplist(ArrayList<DAGMap> list){
		this.DAGMapList = list;
	}
	
	public ArrayList getdagmaplist(){
		return DAGMapList;
	}
	
	public boolean isDepend(String src,String des){
		if(DAGDependValueMap.containsKey(src+" "+des)){
			return true;
		}
		else return false;
	}
	
	public double getDependValue(int src,int des){
		return DAGDependValueMap.get(String.valueOf(src)+" "+String.valueOf(des));
	}
	

	public void setDAGList(List cl){
		this.DAGList = cl;
	}

	public List getDAGList(){
		return DAGList;		
	}
	

	public void setDAGDependMap(Map cd){
		this.DAGDependMap = cd;
	}

	public Map getDAGDependMap(){
		return DAGDependMap;
	}
	

	public void setDAGDependValueMap(Map cdv){
		this.DAGDependValueMap = cdv;
	}

	public Map getDAGDependValueMap(){
		return DAGDependValueMap;
	}

}
