# MINI STATIC SQL DataBase

------

这是我在Cornell里的Database Practicum课上做的一个项目，是一个静态的SQL数据库。
## 支持的SQL语句以及功能
由于是静态的，所以只能支持Select查询。SELECT,FROM,WHERE,ORDER BY,DISTINCT, ALIAS均支持.
如以下查询：
```sql

SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D;
SELECT Sailors.C, Reserves.H FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Sailors.B < 150;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S1, Sailors S2, Reserves R WHERE S1.A < S2.A AND S1.A = R.G;
SELECT S1.A, S2.A, S3.A FROM Sailors S1, Sailors S2, Sailors S3 WHERE S1.A < S2.A AND S2.A < S3.A AND S3.A < 5;
SELECT * FROM TestTable2 WHERE TestTable2.K >= TestTable2.L AND TestTable2.L <= TestTable2.M; 
SELECT TestTable2.M, TestTable2.L FROM TestTable2 ORDER BY TestTable2.L;
SELECT * FROM TestTable3 ORDER BY TestTable3.N;
SELECT * FROM Sailors ORDER BY Sailors.B;
SELECT Boats.F, Boats.D FROM Boats ORDER BY Boats.D;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C;
SELECT DISTINCT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F;
SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;

```
------
## SampleTest以及输入输出格式
### 1.总配置文件
以SampleTest中的largeData文件夹为例（具体测试在test包中PJ4Test里面有）。文件`interpreter_config_file.txt`里有五个参数
> * 第一行是输入的路径
> * 第二行是输出的路径
> * 第三行是临时用来排序的路径
> * 第四行是是否建立索引（0=no，1=yes）
> * 第五行是是否执行SQL语句（0=no，1=yes）

Notice: 如果想要在本机上执行此文件，请检查第一二三行的路径是当前interpreter_config_file.txt的绝对路径下的input、output、temp文件夹路径。
Notice2: 需要导入jsqlparser中的jsql外部parser包，作用是可以方便的提取SELECT语句中的String字段。

### 2.查询计划配置文件
SampleTest/largeData/input文件夹中。文件`plan_builder_config`有三个参数
> * 第一行来指示Join的计划。第一行有一或者两个数字，如果 第一个数字是0则只有一个数字，表示使用Tuple Nested Loop Join（最原始一条一条进行比较的多表相加）。如果第一个数字为1，则表示使用Block Nested Loop Join（在复杂的查询一侧一次性读一定buffer大小的Tuple，在简单的查询侧与buffer中的tuple进行连接。），第二个数字则为buffer的大小（1 buffer = 4028bytes）。如果第一个数字为2，则表示使用Sort Merge Join（只在等于号情况下有效）， 先对Join的两侧进行排序后，再采取Join
> * 第二行是来指示排序的计划。如果第一个数字为0，则表示使用内存排序。如果第一个数字为1，则代表使用外排序，接下来还有第二个数字来指示排序的Buffer的大小。（1 Buffer Page = 4028bytes）
> * 第三行是用来指示建立聚簇（1）和非聚簇索引（0）。

剩余一个queries.sql文件内存sql查询语句。第n行的sql对应output文件夹中的queryn输出结果。
### 3. 数据库内部文件
Sample/largeData/input/db 中有两个文件 `index_info.txt`和`schema.txt`
对于`index_info.txt`索引建立文件,例如：
>Boats E 0 10
>Sailors A 0 15

表示Boats表中的E属性 建立非聚簇的索引（如果是1代表聚簇索引），树的大小(order)为10（每个数节点容量最大关键字为2*order）。Sailor表中的A属性 建立非聚簇的索引，树的order为15。

对于`schema.txt`
>Sailors A B C

>Boats D E F

>Reserves G H

代表某个表中的各列的名称。

进入Sample/largeData/input/db/data里面是我们的数据库具体关系的存储文件，里面有二进制版本和人可读的两个版本，读入的是二进制版本，人可读版本是为了方便Debug所生成的。
人可读文件每一行就是一个tuple，数字类型都为Int类型，用逗号分开。

### 4. 文件逻辑 
*  _client_ package 是最顶层的类。
*  _btree_ 目录是一个生成B+树索引的，同时包含了B+树序列化和反序列化的功能。
*  _operators_ 包各种各样的查询操作符，分为逻辑操作符和实际操作符，逻辑操作符可以被不同实际操作符实现从而达成同一个效果，例如SortOperator可以用In Memory Sort和Extern Sort 两种实际操作符所实现。
*  _test_ 包含了一些debug用的自动化测试工具（例如计算期望输出文件和实际输出文件差异的函数等）。
*  _util_ 包含了一些查询语句，表达式，树的一些调用集成的方法。
*  _visitors_ 包含了访问表达式，查询树的一些访问者。
*  _nio_ 包含了一些二进制读入写出，二进制和人可读方式互相转换的一些类

### 5. 查询计划
EG：`SELECT * FROM R, S, T WHERE R.A = 1 AND R.B = S.C AND T.G < 5 AND T.G = S.H`
逻辑查找符树如下：
![LogicOperatorTree](https://github.com/lzyLuke/MINI-STATIC-SQL/blob/master/pic/LogicOperatorTree.png)

首先先查找FROM中有三个相关联的表R，S，T。然后再构建相关WHERE表达式。
对于R，与之前表有关联的只有R.A=1；对于S，与之前表有关联（与R表有关的）的涉及有R.B=S.C；对于T，与之前表有关联的（R或S表或R，S均有)的是T.G=S.H，再加上一个自己的SelectOprator：T.G<5。再在最顶上再放置一个ProjectOperator用来选择Select后面的具体表项（由于此处是*，所以不需要有ProjectOperator也行）。

如果此时使用Index查询的话，R.A和T.G都建立了索引的话，在从R表中选择R.A=1和在T表中选择T.G<5返回一个一个的Tuple都会用到B+树加速查询。Index查询只有在Select（单表过滤）或者Scan（单表全部扫描）中使用。

------

If you have any questions，please sent an email to zeyu.luke@gmail.com
