
public class Cache {
	public int cachSize;
	public int cacheLineSize;
	public int cacheLineNum;
	public int dataSize;
	public int offset;
	public int tag;
	public int hitNum;
	public int readNum;
	public int writeNum;
	public long responseTime;
	public long writeSize;
	public long readSize;
	public CacheLine[] cacheLine;
	public final int PAGE_SIZE = 4096;
	public final int LINE_NUM_SET =8;
	public final int CACHE_LINE_NUM = 512;
	public final int CACHE_WR_LATENCY = 20;
	public final int MEMORY_READ_LATENCY = 200;
	public final int MEMORY_WRITE_LATENCY = 500;
	public final int CACHE_SIZE = 2097152;
	public final long MEMORY_SIZE = 17179869184L;
	public boolean a=true;
	public Cache() {
		hitNum=0;
		readNum=0;
		writeNum=0;
		responseTime=0;
		writeSize=0;
		readSize=0;
		cacheLineNum=CACHE_LINE_NUM;
		cacheLine = new CacheLine[cacheLineNum];
		for(int i=0;i<cacheLineNum;i++) {
			cacheLine[i] = new CacheLine();
		}
		cachSize = CACHE_SIZE;
	}
	
	public void largeRead(long address, int size, int map) {
		readNum++;
		readSize+=size;
		while(size>PAGE_SIZE) {
        	String offset = Long.toBinaryString(address);
        	//Fill in the binary string with zeros to keep the length at 34 bits long
        	if(offset.length()>34) {
//        		System.out.println("*************************************************");
        		a=false;
        	}
//        	System.out.println("before:"+offset);
        	while(offset.length()<34) {
            	offset="0"+offset;
            }
//        	System.out.println(" after:"+offset);
        	read(offset, PAGE_SIZE, map);
        	size-=PAGE_SIZE;
        	address+=PAGE_SIZE;
        	address=address%MEMORY_SIZE;                                                                
        }
		String offset = Long.toBinaryString(address);
        while(offset.length()<34) {
          	offset="0"+offset;
        }
        read(offset, size, map);
	}
	public void largeWrite(Long address, int size, int map) {
		writeNum++;
		writeSize+=size;
		while(size>PAGE_SIZE) {
        	String offset = Long.toBinaryString(address);
        	if(offset.length()>34) {
//        		System.out.println("*************************************************");
        		a=false;
        	}
        	//Fill in the binary string with zeros to keep the length at 34 bits long
        	while(offset.length()<34) {
            	offset="0"+offset;
            }
        	write(offset, PAGE_SIZE, map);
        	size-=PAGE_SIZE;
        	address+=PAGE_SIZE;
        	address=address%MEMORY_SIZE;                                                                
        }
		String offset = Long.toBinaryString(address);
        while(offset.length()<34) {
          	offset="0"+offset;
        }
        write(offset, size, map);
	}
	
	public void read(String offset, int size, int map) {
//		readNum++;
//		readSize+=size;		
		//Direct Mapping
		if(map==1) {
			String tag = offset.substring(0, 13);
			int index = Integer.parseInt(offset.substring(13, 22), 2);
			if(tag.equals(cacheLine[index].tag) && cacheLine[index].valid==1) {//hit
				hitNum++;
				//Return data
			}else {//miss			
				if(cacheLine[index].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark not dirty
				cacheLine[index].dirty=0;
				//Mark valid
				cacheLine[index].valid=1;
				//Return data
				cacheLine[index].tag=tag;
			}
			responseTime+=CACHE_WR_LATENCY;
		}
		
		//Set Associate Mapping
		else if(map==2) {
			int bound;
			switch (LINE_NUM_SET) {
			case 2:
				bound = 14;
				break;
			case 4:
				bound = 15;
				break;
			case 8:
				bound = 16;
				break;
			case 16:
				bound = 17;
				break;
			default:
				bound = 13;
				break;
			}
			String tag = offset.substring(0, bound);
			int index = Integer.parseInt(offset.substring(bound, 22), 2);
			int maxCount=0;
			boolean hit=false;
			int maxIndex=0;
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(maxCount<cacheLine[i].count)
					maxCount=cacheLine[i].count;
			}
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(cacheLine[i].count==maxCount) {
					maxIndex=i;
					break;
				}	
			}
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(tag.equals(cacheLine[i].tag) && cacheLine[i].valid==1) {//hit
					hitNum++;
					cacheLine[i].count=0;
					hit=true;
					
				}else {//miss
					cacheLine[i].count++;
				}
			}
			if(!hit){//replace block by LRU
				cacheLine[maxIndex].count=0;
				if(cacheLine[maxIndex].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark not dirty
				cacheLine[maxIndex].dirty=0;
				//Mark valid
				cacheLine[maxIndex].valid=1;
				//Return data
				cacheLine[maxIndex].tag=tag;
			}
			responseTime+=CACHE_WR_LATENCY;
		}
		
		//Fully Associate Mapping
		else {
			String tag = offset.substring(0, 22);
			boolean hit=false;
			int maxCount=0;
			int maxIndex=0;
			for(int i=0;i<cacheLineNum;i++) {
				if(maxCount<cacheLine[i].count)
					maxCount=cacheLine[i].count;
			}
			for(int i=0;i<cacheLineNum;i++) {
				if(cacheLine[i].count==maxCount) {
					maxIndex=i;
					break;
				}	
			}
			for(int i=0;i<cacheLineNum;i++) {	
				cacheLine[i].count++;
				if(tag.equals(cacheLine[i].tag) && cacheLine[i].valid==1) {//hit
					hitNum++;
					cacheLine[i].count=0;
					hit=true;
				}
			}
			if(!hit){//replace block by LRU
				cacheLine[maxIndex].count=0;
				if(cacheLine[maxIndex].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark valid
				cacheLine[maxIndex].valid=1;
				//Return data
				cacheLine[maxIndex].tag=tag;
				//Mark not dirty
				cacheLine[maxIndex].dirty=0;
			}
			responseTime+=CACHE_WR_LATENCY;
		}
	}
	
	public void write(String offset, int size, int map) {	
//		writeNum++;
//		writeSize+=size;
		//Direct Mapping
		if(map==1) {
			String tag = offset.substring(0, 13);
			int index = Integer.parseInt(offset.substring(13, 22), 2);
			if(tag.equals(cacheLine[index].tag) && cacheLine[index].valid==1) {//hit
				hitNum++;
				//Return data
			}else {//miss
				if(cacheLine[index].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark valid
				cacheLine[index].valid=1;
				//Return data
				cacheLine[index].tag=tag;
			}
			responseTime+=CACHE_WR_LATENCY;
			//Mark dirty
			cacheLine[index].dirty=1;
		}
		
		//Set Associate Mapping
		else if(map==2) {
			int bound;
			switch (LINE_NUM_SET) {
			case 2:
				bound = 14;
				break;
			case 4:
				bound = 15;
				break;
			case 8:
				bound = 16;
				break;
			case 16:
				bound = 17;
				break;
			default:
				bound = 13;
				break;
			}
			String tag = offset.substring(0, bound);
			int index = Integer.parseInt(offset.substring(bound, 22), 2);
			int maxCount=0;
			boolean hit=false;
			int maxIndex=0;
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(maxCount<cacheLine[i].count)
					maxCount=cacheLine[i].count;
			}
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(cacheLine[i].count==maxCount) {
					maxIndex=i;
					break;
				}
			}
			
			for(int i=index*LINE_NUM_SET;i<(index+1)*LINE_NUM_SET;i++) {
				if(tag.equals(cacheLine[i].tag) && cacheLine[i].valid==1) {//hit
					hitNum++;
					cacheLine[i].count=0;
					hit=true;
				}else {//miss
					cacheLine[i].count++;
				}
			}
			if(!hit){//replace block by LRU
				cacheLine[maxIndex].count=0;
				if(cacheLine[maxIndex].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark valid
				cacheLine[maxIndex].valid=1;
				//Return data
				cacheLine[maxIndex].tag=tag;
			}
			responseTime+=CACHE_WR_LATENCY;
			//Mark dirty
			cacheLine[maxIndex].dirty=1;
		}
		
		//Fully Associate Mapping
		else {
			String tag = offset.substring(0, 22);
			boolean hit=false;
			int maxCount=0;
			int maxIndex=0;
			for(int i=0;i<cacheLineNum;i++) {
				if(maxCount<cacheLine[i].count)
					maxCount=cacheLine[i].count;
			}
			for(int i=0;i<cacheLineNum;i++) {
				if(cacheLine[i].count==maxCount) {
					maxIndex=i;
					break;
				}	
			}
			for(int i=0;i<cacheLineNum;i++) {	
				cacheLine[i].count++;
				if(tag.equals(cacheLine[i].tag) && cacheLine[i].valid==1) {//hit
					hitNum++;
					cacheLine[i].count=0;
					hit=true;
				}
			}
			if(!hit){//replace block by LRU
				cacheLine[maxIndex].count=0;
				if(cacheLine[maxIndex].dirty==1) 
					//write previous data into the memory
					responseTime+=MEMORY_WRITE_LATENCY;
				//Read data from memory into the cache
				responseTime+=MEMORY_READ_LATENCY;
				responseTime+=CACHE_WR_LATENCY;
				//Mark valid
				cacheLine[maxIndex].valid=1;
				//Return data
				cacheLine[maxIndex].tag=tag;
			}
			responseTime+=CACHE_WR_LATENCY;
			//Mark dirty
			cacheLine[maxIndex].dirty=1;
		}
	}	
}
