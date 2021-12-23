package cs321.search;

import cs321.btree.*;
import cs321.create.*;
import cs321.common.ParseArgumentException;
import cs321.common.ParseArgumentUtils;
import cs321.common.Cache;
import cs321.common.SearchTree;
import cs321.common.SearchTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.nio.ByteBuffer;

public class GeneBankSearch
{
    private static Cache<BTreeNode> cache;

    public static void main(String[] args){
        GeneBankSearchArguments searchArguments = null;
        try {
        	long start = java.util.Calendar.getInstance().getTimeInMillis();
            searchArguments = parseArguments(args);
            if(Integer.parseInt(searchArguments.getQueryFile().substring(searchArguments.getQueryFile().length()-1)) != searchArguments.getSequenceLength()) {
            	throw new ParseArgumentException("You must have the same query length as your BTree sequence length");
            }
            searchFile(searchArguments.getQueryFile(), searchArguments.getBTreeFile(), searchArguments.getDegree(), searchArguments.getSequenceLength(), searchArguments.getUseCache());
            long end = java.util.Calendar.getInstance().getTimeInMillis();
            //System.out.println("Runtime: "+(end-start)+ " milliseconds");
        } catch (ParseArgumentException parseArgumentException) {
            System.out.println(parseArgumentException.getMessage());
        }
    }

    public static GeneBankSearchArguments parseArguments(String[] args) throws ParseArgumentException
    {
        try{
            //Too few or too many args
            if(args.length < 3 || args.length > 5){
                usage();
            }

            //useCache parameter
            boolean useCache = false;
            try {
                int useCacheArg = Integer.parseInt(args[0]);
                if(useCacheArg != 0 && useCacheArg != 1){
                    throw new ParseArgumentException("<Use Cache> argument must be a 0 or a 1");
                }
                else{
                    if(useCacheArg == 1){
                        useCache = true;
                    }
                    else{
                        useCache = false;
                    }
                }
            } catch (ParseArgumentException parseArgumentException) {
                throw new ParseArgumentException(parseArgumentException.getMessage());
            }

            String bTreeFile = "";
            try {
                //BTreeFile
                bTreeFile = args[1];
                if(bTreeFile == ""){
                    throw new ParseArgumentException("A binary tree file must be supplied");
                }
            } catch (ParseArgumentException parseArgumentException) {
                throw new ParseArgumentException(parseArgumentException.getMessage());
            }

            String queryFile = "";
            try {
                queryFile = args[2];
                if(queryFile == ""){
                    throw new ParseArgumentException("A query file must be supplied");
                }
            } catch (ParseArgumentException parseArgumentException) {
                throw new ParseArgumentException(parseArgumentException.getMessage());
            }

            ////////////////////////////
            //////Use the Cache/////////
            ////////////////////////////
            if(useCache){
                if(args.length < 4){
                    throw new ParseArgumentException("When using cache, please supply a cache size");
                }
                //annoying "variable might not be set" warning was popping up
                int cacheSize = 0;
                cacheSize = Integer.parseInt(args[3]);
                if(cacheSize < 1){
                    throw new ParseArgumentException("Please supply a cache size above 0");
                }

                cache = new Cache<BTreeNode>(cacheSize);
                return new GeneBankSearchArguments(useCache, bTreeFile, queryFile, cacheSize, 0);
            }

            ////////////////////////////
            //////////No Cache//////////
            ////////////////////////////
            else{
                //Useless cache size parameter still in there and debug level supplied
                if(args.length >= 4){
                    throw new ParseArgumentException("Cache size shouldn't be supplied if the cache isn't enabled");
                }
                return new GeneBankSearchArguments(useCache, bTreeFile, queryFile, 0, 0);
            }
        }
        catch(Exception ex){
            throw new ParseArgumentException(ex.getMessage());
        }
    }

    /**
     * Prints usage of GeneBankSearch command
     * @throws ParseArgumentException
     */
    private static void usage() throws ParseArgumentException{
        throw new ParseArgumentException("Usage: java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> [<cache size>] [<debug level>]");
    }

    public static void searchFile(String queryFile, String bTreeFile, int degree, int sequenceLength, boolean useCache){
        BufferedReader reader;
        FileInputStream inputStream;
        try {
        	File file = new File(queryFile);
        	File bFile = new File(bTreeFile);
            reader = new BufferedReader(new FileReader(file));
            inputStream = new FileInputStream(bFile);

            String queryString = reader.readLine().toLowerCase();
            byte[] binaryFileData = new byte[(int) new File(bTreeFile).length()];

            SearchTree searchTree;
            if(useCache) {
            	searchTree = new SearchTree(degree, binaryFileData, sequenceLength, cache);
            }
            else {
            	searchTree = new SearchTree(degree, binaryFileData, sequenceLength);
            }

            //Reads data from file into byte array
            inputStream.read(binaryFileData);

            //To be compliant with sequenceToLong() method
            long binaryQueryString = 0;
            while(queryString != null){
                queryString = queryString.toLowerCase();
                binaryQueryString =  GeneBankCreateBTree.sequenceToLong(queryString);// = Parse Query String to long
            	
                TreeObject foundObject = searchTree.searchForLong(binaryQueryString, ByteBuffer.wrap(binaryFileData, 0, 4).getInt());
                if (foundObject != null) {
                    System.out.println(foundObject.toString());
                }
                queryString = reader.readLine();
            }

            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param queryStringInBinary string we're trying to find.
     * @return 0: found element; 1: to greater than current node; -1: less than current node;
     */
    public static int searchBTree(long queryStringInBinary){
        return 0;
    }
}

