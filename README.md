# ThoughtWorks家庭作业——羽毛球馆
### 运行方式
* **开发环境**<br/>
整个题目使用Java（Java8）语言在IntelliJ IDEA平台下进行开发，编译结果位于out/production/ThoughtWorksHomework目录下。其中Main.class是整个程序的入口。进入上述文件夹位置后在控制台执行Java Main进入程序，然后输入相应指令会得到反馈提示。因为本程序使用了Java8相关的时间处理函数所以请务必使用jre1.8运行。<br/>
* **运行指令**<br/>
指令中userId为用户ID可以随意填写；court为场地名称，程序中默认建立4个场地A,B,C,D。
    * _预定场地_<br/>
    ${userId} YYYY-MM-DD HH:MM~HH:MM ${court}<br/>
    * _取消预定_<br/>
    ${userId} YYYY-MM-DD HH:MM~HH:MM ${court} C<br/>
    * _打印收入汇总_<br/>
    '\r\n'<br/>
### 项目结构
* Main.java是整个程序入口主要作用是初始化场地，然后接收指令执行。
* 在edu.drc包下面的Court.java包含Court类<br/>
```java
public class Court{
    private static Set<Court> courts = new HashSet<>();
    private Map<Schedule, Schedule> schedules = new HashMap<>();
    private List<Event> bill = new ArrayList<>();
}
```
里面通过类成员记录了所有的场地信息
```java
private static Set<Court> courts = new HashSet<>();
```
针对每个场地通过实例成员记录每天的预定安排表
```java
private Map<Schedule, Schedule> schedules = new HashMap<>();
```
同样每个场地都有自己的账单需要记录
```java
private List<Event> bill = new ArrayList<>();
```
* 在Court类内部有两个内部类<br/>
    * **Schedule**日期安排表
        * data 记录安排表的日期是区分某个场地下不同日期的安排表的唯一依据
        * acceptTime 作为一个数组记录了当天每个小时段的预定状态：false表示未预定；true表示已预定。
        * baseTime 因为acceptTime数组从0开始，所以需要一个偏移量表示正常时间。
        * price 对应acceptTime表示每个小时段的价格。  
    ```java
    private class Schedule{
          private double[] price;
    
          private LocalDate date;
    
          private boolean[] acceptTime;
    
          private int baseTime;
    }
    ```
    * **Event**订单
        * userId 用户ID
        * date 预定日期
        * startTime-endTime 预定时间段
        * income 该订单收入
        * punishment 如果违约违约金比例
        * isPunished 该订单是否为违约订单
        * 其中userId,date,startTime,endTime,isPunishment作为是否是重复订单的依据，但是账单（bill）里会存在重复订单的不同实体。
    ```java
    private class Event implements Comparable<Event> {
            private String userId;
            private LocalDate date;
            private LocalTime startTime;
            private LocalTime endTime;
            private double income;
            private double punishment;
            private boolean isPunished;
    }
    ```
* edu.drc.exception下是一些自定义异常类用来处理输入错误，预定冲突，找不到预定这些错误。