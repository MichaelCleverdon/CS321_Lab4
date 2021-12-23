package cs321.search;

public class GeneBankSearchArguments
{
    private boolean useCache;
    private String bTreeFile = "";
    private String queryFile = "";
    private int cacheSize = 0;
    private int debugLevel = 0;

    public GeneBankSearchArguments(){}
    //Default
    public GeneBankSearchArguments(boolean UseCache, String BTreeFile, String QueryFile, int CacheSize, int DebugLevel){
        this.useCache = UseCache;
        this.bTreeFile = BTreeFile;
        this.queryFile = QueryFile;
        this.cacheSize = CacheSize;
        this.debugLevel = DebugLevel;
    }
//    //Overloaded if useCache is false but debugLevel is provided
//    public GeneBankSearchArguments(boolean useCache, String bTreeFile, String queryFile, int debugLevel){
//        this.useCache = useCache;
//        this.bTreeFile = bTreeFile;
//        this.queryFile = queryFile;
//        this.debugLevel = debugLevel;
//    }

    @Override
    public String toString(){
        return String.format("UseCache: %b, BTreeFile: \'%s\', QueryFile: \'%s\', CacheSize: %d, DebugLevel: %d", useCache, bTreeFile, queryFile, cacheSize, debugLevel);
    }

    public String getBTreeFile() {
        return bTreeFile;
    }
    public String getQueryFile() {
        return queryFile;
    }
    public int getCacheSize() {
        return cacheSize;
    }
    public int getDebugLevel() {
        return debugLevel;
    }
    public boolean getUseCache() {
    	return useCache;
    }
    public int getDegree() {
        String[] splitData = bTreeFile.split("\\.");
        //gbkFileName.gbk.btree.data.k.t
        return Integer.parseInt(splitData[splitData.length-1]);
    }
    public int getSequenceLength(){
        String[] splitData = bTreeFile.split("\\.");
        return Integer.parseInt(splitData[splitData.length-2]);
    }
}
