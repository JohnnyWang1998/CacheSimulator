import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Test {
	public static void main(String[] args) {
		String filePath = "E:\\Test\\MSRCambridge\\TraceFile";
		File file = new File(filePath);
		String[] filelist = file.list();
		//set different mapping algorithm
        int map = 2;
        for (int i = 15; i < filelist.length; i++) {
                File readfile = new File(filePath + "\\" + filelist[i]);
                BufferedReader bufferedReader = null;
                Cache cache = new Cache();               
                int lineNum =0;
        		try {   
                    bufferedReader = new BufferedReader(new FileReader(readfile));  
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null ) {
                    	lineNum++;
                        String item[] = line.split(",");
                        String type = item[3];
                        String rawOffset = item[4];
                        int size = Integer.parseInt(item[5]);
                        long address = Long.parseLong(rawOffset);
                        address=address%cache.MEMORY_SIZE;
                        //The loop is used to partition one large write/read access into many small write/read accesses
                        while(size>4*(cache.PAGE_SIZE)) {
//                        	System.out.println("large:");
//                        	String offset = Long.toBinaryString(address);
                        	//Fill in the binary string with zeros to keep the length at 34 bits long
//                        	while(offset.length()<34) {
//                            	offset="0"+offset;
//                            }
                        	
                        	if(type.equals("Read")) { 
                            	cache.largeRead(address, 4*(cache.PAGE_SIZE), map);
                            }
                            else if(type.equals("Write")) {
                            	cache.largeWrite(address, 4*(cache.PAGE_SIZE), map);
                            }
                        	size-=4*cache.PAGE_SIZE;
                        	address+=4*cache.PAGE_SIZE;
                        	address=address%cache.MEMORY_SIZE;                                                                
                        }
//                        String offset = Long.toBinaryString(address);
                      //Fill in the binary string with zeros to keep the length at 34 bits long
//                        while(offset.length()<34) {
//                        	offset="0"+offset;
//                        }
                        if(type.equals("Read")) { 
                        	cache.largeRead(address, size, map);
                        }
                        else if(type.equals("Write")) {
                        	cache.largeWrite(address, size, map);
                        }
                    }  
                    bufferedReader.close();  
                } catch (IOException e) {  
                    e.printStackTrace();
                }
//        		These lines of code are used to print out the result as txt files
//        		try {
//                    PrintWriter out = new PrintWriter(new FileWriter("E:\\Test\\MSRCambridge\\ResultFAM\\" + filelist[i].replace(".csv", ".txt")));
//                    out.printf("Average response time: %.4f\r\n",(double)cache.responseTime/lineNum);
//                    out.printf("Ratio of write size: %.4f\r\n",(double)cache.writeSize/(cache.writeSize+cache.readSize));
//                    out.printf("Ratio of write: %.4f\r\n",(double)cache.writeNum/(cache.readNum+cache.writeNum));
//                    out.printf("Ratio of hit: %.4f\r\n",(double)cache.hitNum/(cache.readNum+cache.writeNum));
//                    out.close();
//                } catch (IOException e) {
//                	e.printStackTrace();
//                }
        		
        		System.out.println("Response time: "+ cache.responseTime);
        		System.out.printf("Average response time: %.4f\n",(double)cache.responseTime/lineNum);
        		System.out.printf("Ratio of write size: %.4f\n",(double)cache.writeSize/(cache.writeSize+cache.readSize));
        		System.out.printf("Ratio of write: %.4f\n",(double)cache.writeNum/(cache.readNum+cache.writeNum));
        		System.out.printf("Ratio of hit: %.4f\n",(double)cache.hitNum/(cache.readNum+cache.writeNum));
        		System.out.println(i + ". " + filelist[i] + "\n"+cache.a);
        		break;
        	}
        System.out.println("All Done!");
        }
		
}


