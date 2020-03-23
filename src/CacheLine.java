public class CacheLine {
	int valid;
    String tag;
    int dirty;
    int count;
    
    public CacheLine(){
    	valid=0;
    	tag=null;
    	dirty=0;
    	count=0;
    }
}
