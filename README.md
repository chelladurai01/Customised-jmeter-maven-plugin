## Customised-jmeter-maven-plugin


This is an customized jmeter maven plugin and it's details


## Advantages of customized jmeter maven plugin

    1.We can specify testresult name

    2.It will generate the graphs as images

    3.We can specify the test result directory 
  
      If want to specify the test result directory use the below tag
    
       <resultFilesDirectory>results/jmeter/</resultFilesDirectory>

## Specifying the test result name for executing multiple jmx file

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
           
## Graphs

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
        
## Sample TestScript
[Sample TestScript](https://github.com/chelladurai01/Examples/tree/master/jmeter-example-script)
<- You can find the sample testscript here
