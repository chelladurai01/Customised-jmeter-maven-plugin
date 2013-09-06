perf-plugin
===========

This is an customized jmeter maven plugin


Difference between the existing and customized jmeter maven plugin

    1.In the existing plugin we are not able to specify test result name but we are to give the result name in customized plugin

    2.In the existing plugin we are not able to grenerate the graphs as image file but here we can the graphs

    3.In customized jmeter maven plugin we can specify the test result directory but it's not possible in existing one.
  
      If want to specify the use the below tag
    
       <resultFilesDirectory>results/jmeter/</resultFilesDirectory>

Specifying the test result name for executing multiple jmx file

    In this the Number of jmx  and the result name count should be equal
    First test result name is assigned to first jmx file (ex.for "sample1.jmx" testresult name is "test1").
           <configuration>
             <testFilesIncluded>
                <jMeterTestFile>Sample1.jmx</jMeterTestFile>
                <jMeterTestFile>Sample2.jmx</jMeterTestFile>
                <jMeterTestFile>Sample3.jmx</jMeterTestFile>
              </testFilesIncluded>
              <resultFilesName>
                <resultName>test1</resultName>
                <resultName>test2</resultName>
                <resultName>test3</resultName>
              </resultFilesName>	
           </configuration>
           
Graphs

    Using the "JMeterPlugins" we are generating graphs and the listed below are the available plugin types.
  
  
      1.AggregateReport = JMeter's native Aggregate Report, can be saved only as CSV
      2.ThreadsStateOverTime = Active Threads Over Time
      3.BytesThroughputOverTime
      4.HitsPerSecond
      5.LatenciesOverTime
      6.PerfMon = PerfMon Metrics Collector
      7.ResponseCodesPerSecond
      8.ResponseTimesDistribution
      9.ResponseTimesOverTime
      10.ResponseTimesPercentiles
      11.ThroughputOverTime
      12.ThroughputVsThreads
      13.TimesVsThreads = Response Times VS Threads
      14.TransactionsPerSecond
      15.PageDataExtractorOverTime
      
    To generate graph we should specify atleast one plugin types  like below mentioned
       <pluginTypes>
             <types>ThreadsStateOverTime</types>
             <types>BytesThroughputOverTime</types>
             <types>HitsPerSecond</types>
             <types>LatenciesOverTime</types>
             <types>ResponseCodesPerSecond</types>
             <types>ResponseTimesDistribution</types>
             <types>ResponseTimesOverTime</types>
             <types>ResponseTimesPercentiles</types>
             <types>ThroughputVsThreads</types>
             <types>TimesVsThreads</types>
             <types>TransactionsPerSecond</types>
          </pluginTypes>
  
