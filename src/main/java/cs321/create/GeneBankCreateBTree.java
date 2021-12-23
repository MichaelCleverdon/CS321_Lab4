package cs321.create;

import cs321.btree.*;
import cs321.common.ParseArgumentException;
import cs321.common.Cache;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is a driver class that Runs data parsing and then BTree insertion in a while loop
 * 
 * Fall 2020
 * @author Josie Derrick, Michael Cleverdon, and Paul Ellis
 */

public class GeneBankCreateBTree
{
	private static Cache<BTreeNode> cache;
    public static void main(String[] args) throws Exception
    {
    	long start = java.util.Calendar.getInstance().getTimeInMillis();
    	
        GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = parseArgumentsAndHandleExceptions(args);
        //statement signaling program start
        //System.out.println(geneBankCreateBTreeArguments.toString());
        
        boolean useCache = geneBankCreateBTreeArguments.getUseCache();
        int t = geneBankCreateBTreeArguments.getDegree();
        String fileName = geneBankCreateBTreeArguments.getFileName();
        int k = geneBankCreateBTreeArguments.getSubsequenceLength();
        int cacheSize = geneBankCreateBTreeArguments.getCacheSize();
        int debugLevel = geneBankCreateBTreeArguments.getDebugLevel();
        
        
        BTree tree = new BTree(t, k, fileName);
        //create empty BTree and create file for it
        if (useCache) {
        	tree.setCache(cache);
        } 
        
       
        //go through the file and insert sequences to BTree
		BufferedReader input = new BufferedReader(new FileReader(fileName));
		String sequence = "";
		boolean foundOrigin = false;
		//Find first origin
		while(!foundOrigin) {
			String line = input.readLine();
			if(line == null){
				sequence = "EOF";
				break;
			}
			if (line.contains("ORIGIN")) {
				foundOrigin = true;
			}
		}
		//Read until the end of file
		while(!sequence.equals("EOF")) {
			sequence = getInputDataFromReader(input, k, sequence, false);

			//End of sequence
			if(sequence.equals("EOS")){
				//Reset search
				sequence = "";
				foundOrigin = false;
				String line = input.readLine();
				while(!foundOrigin && line != null) {
					if (line.contains("ORIGIN")) {
						foundOrigin = true;
					}
					else{
						line = input.readLine();
					}
				}
				if(line == null){
					sequence = "EOF";
				}
			}
			//Any other sequence
			else{
				long key = sequenceToLong(sequence);
				TreeObject obj = new TreeObject(key, k);
				tree.insert(tree, obj);
			}
		}

		input.close();
		System.out.print("\n");
       
        //write cache to file if needed
        if (useCache) {
        	tree.writeCacheToFile();
        }
        
        //after making BTree write the root and its location to the disk
        tree.writeRootToDisk(fileName);
        
        //create dump file if needed
        if (debugLevel == 1) {
        	String dumpString = tree.toString();
        	FileWriter fw = new FileWriter("dump");
        	fw.append(dumpString);
        	fw.close();
        }
        
        //statement signaling program finish
        long end = java.util.Calendar.getInstance().getTimeInMillis();
        //System.out.println("The program has ended. Run time: "+ (end-start) + " milliseconds.");

    }
    
    /**
     * This method calls the parseArguments method which will either
     * store the arguments to a GeneBankCreateBTreeArguments object
     * or the parseArgument function will throw an error
     * @param args (command line arguments)
     * @return GeneBankCreateBTreeArguments object if valid arguments are provided
     */
    private static GeneBankCreateBTreeArguments parseArgumentsAndHandleExceptions(String[] args)
    {
        GeneBankCreateBTreeArguments geneBankCreateBTreeArguments = null;
        try
        {
            geneBankCreateBTreeArguments = parseArguments(args);
        }
        catch (ParseArgumentException e)
        {
            printUsageAndExit(e.getMessage());
        }
        return geneBankCreateBTreeArguments;
    }

    /**
     * Ends the program and prints an error. 
     * @param errorMessage
     */
    private static void printUsageAndExit(String errorMessage)
    {
    	System.out.println(errorMessage);
        System.exit(1);
    }

    /**
     * This function is used to determine if the user has 
     * provided the proper command line arguments. 
     * If not, an exception is thrown describing the error.
     * @param args (command line arguments)
     * @return GeneBankCreateBTreeArguments object if valid arguments are provided
     * @throws ParseArgumentException - a custom exception with error message
     */
    public static GeneBankCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException
    {
    	//too few or too many arguments
    	if (args.length > 6 || args.length < 4) {
    		throw new ParseArgumentException("There must be at least 4 arguments and no more than 6.");
    	}
    	
    	//checks for first input: the cache
    	//must be either a 0 or 1
    	boolean useCache;
    	try {
    		int cache = Integer.parseInt(args[0]);
    		if (cache == 0) {
    			useCache = false;
    		} else if (cache == 1) {
    			useCache = true;
    		} else {
    			throw new Exception();
    		}
    	} catch (Exception e) {
    		throw new ParseArgumentException("The first command line argument must be 0 or 1.");
    	}
    	
    	//check for the second input: degree
    	//should be an integer >= 0
    	//if it is zero calculate t based on disk size
    	int degree;
    	try {
    		degree = Integer.parseInt(args[1]);
    		if (degree < 0) {
    			throw new Exception();
    		} else if (degree == 0) {
    			int objectBytes = 12; //long for the object and int for the frequency
    			int pointerBytes = 4; //ints
    			int metaData = 9; //int for n, bool for leaf, and int for loc
    			int diskSize = 4096;
    			degree = (diskSize - pointerBytes - metaData + objectBytes)/(2*objectBytes+2*pointerBytes); 
    			//Note: because degree (and all variables really) is an int the division will automatically take the floor which is what we want
    		}
    	} catch (Exception e) {
    		throw new ParseArgumentException("The second command line argument must be an integer greater than or equal to 0");
    	}
    	
    	//check for third input: gbk file
    	//first checks if the text ends in .gbk
    	//then checks if the file exists 
    	String fileName = args[2];
    	try {
    		String substr = fileName.substring(args[2].length() - 4);
    		if (!substr.toLowerCase().equals(".gbk")) {
    			throw new Exception();
    		} else {
    			File file = new File(fileName);
    			if(!(file.exists() && file.isFile())) {
    				throw new Exception();
    			}
    		}
    	} catch (Exception e) {
    		throw new ParseArgumentException("The third command line argument must be a filename that exists and ends in '.gbk'.");
    	}
    	
    	//check for fourth input: sequence length
    	//must be an integer and must be between 1 and 31 (inclusive)
    	int k;
    	try {
    		k = Integer.parseInt(args[3]);
    		if (k < 1 || k > 31) {
    			throw new Exception();
    		}
    	} catch (Exception e) {
    		throw new ParseArgumentException("The fourth command line argument must be an integer between 1 and 31 (inclusive)");
    	}
    
    	//check for fourth input: cache size
    	//if there is a cache and the cache size is less than one error
    	//if there is a cache but there is no fourth input throws exception
    	int cacheSize = 0;
    	if (useCache) {
    		if (args.length == 4) {
    			throw new ParseArgumentException("When using a cache, the fifth command line argument must be included to specify the cache size.");
    		} else {
    			try {
    				if (Integer.parseInt(args[4]) < 1) {
        				throw new Exception("");
        			} else {
        				cacheSize = Integer.parseInt(args[4]);
    				    cache = new Cache<BTreeNode>(cacheSize);
        			}
    			} catch (Exception e) {
    				throw new ParseArgumentException("The fifth command line argument, when using a cache, must be an integer greater than 0.");
    			}
    			
    		}
    	} 
    	//if there is no cache but there is a cache size greater than 0
    	else {
    		if (args.length > 4) {
	    		if (Integer.parseInt(args[4]) != 0 ) {
	    			throw new ParseArgumentException("The fifth command line argument must be 0 when not using a cache.");
	    		} else {
	    			cacheSize = 0;
	    		}
    		} else {
    			cacheSize = 0;
    		}
    	}
    	
    	//check for sixth input: debug level
    	//if included, check if it is 0 or 1 return error otherwise
    	int debugLevel;
    	if (args.length == 6) {
    		try {
    			if (Integer.parseInt(args[5]) == 0 || Integer.parseInt(args[5]) == 1) {
    				debugLevel = Integer.parseInt(args[5]);
    			} else {
    				throw new Exception();
    			}
    		} catch (Exception e) {
    			throw new ParseArgumentException("The sixth command line argument must be 0 or 1.");
    		}
    	}
    	//if not included default is 0
    	else {
    		debugLevel = 0;
    	}
    	
        return new GeneBankCreateBTreeArguments(useCache, degree, fileName, k, cacheSize, debugLevel);
    }

    /**
     * This function takes in the file name as string
     * and will read the file for the data between ORIGIN and //
     * @param fileName - string containing name of the data file
     * @return ArrayList<Character> - returns the ArrayList containing each sequence from file
     * @throws Exception
     */
    public static String getInputDataFromReader(BufferedReader reader, int size, String sequence, boolean readyForReturn) throws Exception {
    	try {
    		if(readyForReturn && sequence.length() == size){
    			return sequence.toLowerCase();
			}
			char newCharacter = (char)reader.read();
			if(sequence.length() < size){

    			if(newCharacter == -1){
    				return "EOF";
				}
    			if(newCharacter == 'a' || newCharacter == 't' || newCharacter == 'c' || newCharacter == 'g' || newCharacter == 'n'){
    				sequence += newCharacter;
    				if(sequence.contains("n")){
						return getInputDataFromReader(reader, size, sequence, false);
					}
					else{
						return getInputDataFromReader(reader, size, sequence, true);
					}

				}
				else if(newCharacter == '/'){
    				if(reader.read() == '/'){
    					return "EOS";
					}
				}
			}
			//Not readyForReturn but full
			else{
    			sequence = sequence.substring(1);
				if(newCharacter == 'a' || newCharacter == 't' || newCharacter == 'c' || newCharacter == 'g' || newCharacter == 'n'){
					sequence += newCharacter;
					if(sequence.contains("n")){
						return getInputDataFromReader(reader, size, sequence, false);
					}
					else{
						return getInputDataFromReader(reader, size, sequence, true);
					}
				}
				else if(newCharacter == '/'){
					if(reader.read() == '/'){
						return "EOS";
					}
				}
				return getInputDataFromReader(reader, size, sequence, false);
			}
			return getInputDataFromReader(reader, size, sequence, false);
    	} catch (Exception e) {
    		throw e;
    	}
    }
	
	/**
	 * Method that converts sequence in form of string to long
	 */
	public static long sequenceToLong(String seq) {
		//generate the binary sequence
		String binary = "";
		for (int i = 0; i < seq.length(); i++) {
			char current = seq.charAt(i);
			if (current == 'a') {
				binary += "00";
			} else if (current == 't') {
				binary += "11";
			} else if (current == 'c') {
				binary += "01";
			} else if (current == 'g') {
				binary += "10";
			}
		}
		
		//convert from binary to long
		long j=0;
		for(int i=0;i<binary.length();i++){
			if(binary.charAt(i)== '1'){
				j=(long) (j + Math.pow(2,binary.length()-1-i));
			}
		}
		return j;
	}
}
