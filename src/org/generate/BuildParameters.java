package org.generate;

public class BuildParameters {
      public static int timeWindow=200000;//时间窗 默认200000
      public static int taskAverageLength=30;//任务的平均长度（20,30,40,50 默认值30）
      public static int dagAverageSize=30;//dag的平均大小（20，30，40，50 默认值30）
      public static int dagLevelFlag=2;//(1,2,3)代表([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
      public static double deadLineTimes=1.3;//deadline的倍数值 （1.1，1.3，1.6，2.0）
      public static int processorNumber=8;//处理单元的个数（2,4,8,16,32）
      //public int proceesorEndTime = timeWindow/processorNumber;
      
}
