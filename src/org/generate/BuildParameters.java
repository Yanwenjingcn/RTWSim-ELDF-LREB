package org.generate;

public class BuildParameters {
      public static int timeWindow=200000;//ʱ�䴰 Ĭ��200000
      public static int taskAverageLength=30;//�����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
      public static int dagAverageSize=30;//dag��ƽ����С��20��30��40��50 Ĭ��ֵ30��
      public static int dagLevelFlag=2;//(1,2,3)����([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
      public static double deadLineTimes=1.3;//deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
      public static int processorNumber=8;//����Ԫ�ĸ�����2,4,8,16,32��
      //public int proceesorEndTime = timeWindow/processorNumber;
      
}
