# Cache Simulator
The cache simulator was written in Java, with the ability to calculate the following parameters:

• Average response time 

• Ratio of write size 

• Ratio of write 

• Hit ratio

according to the trace files.



 **Some parameters of the cache simulator:**

| description          | size  |
| -------------------- | ----- |
| Cache Size           | 2MB   |
| Memory Size          | 16GB  |
| Memory Write Latency | 500us |
| Memory Read Latency  | 200us |
| Cache W/R latency    | 20us  |
| Size of Cache Line   | 4KB   |
| Size of Memory Page  | 16KB  |



**Description:**

I implemented Direct Mapping, Fully Associative Mapping and particular set associative mapping for specific files. Meanwhile, I used LRU to replace the blocks which are not hit and also tried random replacement that is not good so that I gave it up.

One thing should be stressed out that the size of each line in the trace file matters. If the write/read size is less than 4096B(4KB), it is correct to consider it as one time of read or one time of write. Nevertheless, when the size is larger than 4KB, then this line is supposed to be partitioned from one large write/read access into many contiguous small write/read accesses. This point will stiff affect the hit times and write/read times as well. If one dose not take it into account, the ratio of hit will be larger than it really is. Although my average hit rate is low, the authenticity of my result is guaranteed.

Trace files can be downloaded from this website: https://pan.baidu.com/s/1AVwnn3nR_HOnTfTNPjDi-w (codes: x8c3), containing 36 csv files.



**The optimization result:**

| Trace file            | DM(Direct Mapping)                                           | FAM(Fully Associative Mapping)                               | Optimization                                                 |
| --------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| CAMWEBDEV-lvm1.csv    | Average response time: 192.8910Ratio of write size: 1.0000Ratio of write: 1.0000Ratio of hit: 0.6595 | Average response time: 176.5498Ratio of write size: 1.0000Ratio of write: 1.0000Ratio of hit: 0.7275 | SAM(4)*Average response time: 113.9905Ratio of write size: 1.0000Ratio of write: 1.0000Ratio of hit: 0.7275 |
| CAMWEBDEV-lvm2.csv    | Average response time: 1323.0899Ratio of write size: 0.9992Ratio of write: 0.9991Ratio of hit: 0.1239 | Average response time: 1268.9891Ratio of write size: 0.9992Ratio of write: 0.9991Ratio of hit: 0.1609 | Average response time: 1268.9891Ratio of write size: 0.9992Ratio of write: 0.9991Ratio of hit: 0.1609 |
| CAMRESIRA01-lvm1.csv  | Average response time: 1992.5631Ratio of write size: 0.9967Ratio of write: 0.9965Ratio of hit: 0.1185 | Average response time: 1954.2772Ratio of write size: 0.9967Ratio of write: 0.9965Ratio of hit: 0.1371 | Average response time: 1954.2772Ratio of write size: 0.9967Ratio of write: 0.9965Ratio of hit: 0.1371 |
| CAMRESISAA02-lvm0.csv | Average response time: 688.0945Ratio of write size: 0.9464Ratio of write: 0.9636Ratio of hit: 0.4707 | Average response time: 672.3591Ratio of write size: 0.9464Ratio of write: 0.9636Ratio of hit: 0.4847 | SAM(8)*Response time: 7885971060 Average response time: 629.9218 Ratio of write size: 0.9464 Ratio of write: 0.9682 Ratio of hit: 0.8069 |
| CAM-02-SRV-lvm0.csv   | Average response time: 6530.8253Ratio of write size: 0.9415Ratio of write: 0.9410Ratio of hit: 0.0376 | Average response time: 6513.2142Ratio of write size: 0.9415Ratio of write: 0.9410Ratio of hit: 0.0413 | SAM(16)*Average response time: 6508.3606 Ratio of write size: 0.9415Ratio of write: 0.9410Ratio of hit: 0.0413 |

*SAM(n) is denoted to n-way set associative mapping



**How I optimize the cache simulator and improve the hit rate:**

CAMWEBDEV-lvm1.csv:

Certainly, the fully associative mapping has the best performance of hit ratio, it is time-consuming and memory-consuming. Therefore, I tried set associative mapping to reduce the memory the process takes. Fortunately, when the cache line number with a set is 4, it reaches the summit value 0.7275 but also reduce the average response time to 113.991. After that, I thought maybe there is something more could be done to break the bound. So I started to analyze the trace file. It turns out that the trace line hits mostly at the first line of each set, intriguing that maybe I could increase the hit rate by reducing the times that the first line being replaced. So I rewrite the write method of set associative mapping--when it does not hit and is going to replace the first cache line of the corresponded set, I replace it with the fourth one. The result is that it did reduce the replace times at the first line of each set, but this modification did not increase the hit times which is really frustrated. In conclusion, this improvement dose not has essential effect.

 

![img](file:///C:\Users\JOHNNY~1\AppData\Local\Temp\ksohtml12188\wps1.jpg)![img](file:///C:\Users\JOHNNY~1\AppData\Local\Temp\ksohtml12188\wps2.jpg) 

*Before VS. After*

 

CAMWEBDEV-lvm2.csv and CAMRESIRA01-lvm1.csv:

These two file’s hit ratio can not reach the apex of fully associative mapping by set associative mapping. Besides, the response time could be even longer. Meanwhile, the results shows that there are barely hot data that hit around some particular range of cache lines. All in all, there is nothing I can do to improve the performance of these two file, which means the best solution is the fully associative mapping method.

 

CAMRESISAA02-lvm0.csv:

When I used the set associative mapping to process this file, I got even better hit rates than the fully associative mapping did. In addition, the hit rate is not linear to the number of cache lines within a set. When the number is 2, it is nearly approximate to direct mapping. When the number is 8, it reaches the culmination, 0.5151. When the number is larger, the hit rate starts to do down. Therefore, the 8-way set associative mapping is the best method. 

[Update]After asking help from my classmates, I realized that I can improve the hit rate by changing the read/write strategy. Now, if there is a write request, the 4 contiguous cache lines will be updated concurrently. Because there are lots of large size write request, this method could considerably increase the hit ratio. Besides, I try different set associative mapping strategy, from 2-line set to 16-line set. It turns out that the 8-line set has the optimal performance. The result is 80.69%, which is much better than before.

 

CAM-02-SRV-lvm0.csv

The feature of this file is similar to CAMWEBDEV-lvm2.csv and CAMRESIRA01-lvm1.csv. The results shows that there are barely hot data that hit around some particular range of cache lines. Thus, no optimization of the code in the set associative mapping method could be done. However, the 16-way set associative mapping can reach the best hit rate as the fully associative mapping and reduce the average respond time at the same time.