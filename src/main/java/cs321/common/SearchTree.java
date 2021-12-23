package cs321.common;

import cs321.btree.*;
import cs321.create.*;
import cs321.search.*;
import java.util.LinkedList;
import java.nio.ByteBuffer;

public class SearchTree{
    private int lengthOfSequence, degree;
    private byte[] bTree;
    Cache<BTreeNode> cache;
    boolean useCache = false;
    public SearchTree( int degree, byte[] bTree, int lengthOfSequence){
        this.degree = degree;
        this.bTree = bTree;
        this.lengthOfSequence = lengthOfSequence;
    }
    
    public SearchTree( int degree, byte[] bTree, int lengthOfSequence, Cache<BTreeNode> cache){
        this.degree = degree;
        this.bTree = bTree;
        this.lengthOfSequence = lengthOfSequence;
        this.cache = cache;
        this.useCache = true;
    }
    

    public TreeObject searchForLong(long thingToSearchFor, int rootAddress){
        int currentAddress = rootAddress;
        boolean breakFromLeaf = false;
        BTreeNode currentNode = null;
        int indexOfElement = -1;
        long keyAtIndex = -1;

        //Don't search if empty
        if(rootAddress == -1){return null;}

        if(useCache) {
        	BTreeNode currentCacheNode = null;
        	
        	for(int i = 0; i < cache.getCurrentSize(); i++) {
        		currentCacheNode = cache.getCacheObjects().get(i);
        		if(currentCacheNode.getLoc() == currentAddress) {
        			break;
        		}
        	}
        	
        	if(cache.getCurrentSize() > 0) {
	        	for(int i = 0; i < currentCacheNode.getN(); i++) {
					TreeObject currentObject = currentCacheNode.getObjectAtIndex(i);
					if(currentObject.getKey() == thingToSearchFor) {
						//If it didn't find an empty object
						if(!(currentObject.getKey() == 0 && i != 0)) {
							return currentCacheNode.getObjectAtIndex(i);
						}
					}
					//No need to keep searching if we're bigger than the thing we're searching for
					else if(thingToSearchFor < currentObject.getKey()) {
						break;
					}
				}
        	}
    	}
        
        currentNode = parseNodeFromBinaryFileAddress(rootAddress);
        if(useCache) {
        	cache.addObject(currentNode);
        }
        while(keyAtIndex != thingToSearchFor && !breakFromLeaf){ //Keep looping until it hits the end of the line, it'll break if it finds the element
        	//If using the cache, search it first
        	
            LinkedList<TreeObject> objects = currentNode.getObjects();
            //No objects (shouldn't happen), break
            if(objects == null){ break; }
            //Loop through all data objects in the node
            int numberOfObjects = currentNode.getN();
            for(int i = 0; i < numberOfObjects; i++){
            	keyAtIndex = objects.get(i).getKey();
            	
                //If it finds the long
                if(keyAtIndex == thingToSearchFor){
                	//If it finds empty entries, which contain 0 for a long, break
                	if(keyAtIndex == 0 && i != 0) {
                		if(currentNode.isLeaf()) {
                			breakFromLeaf = true;
                			break;
                		}
                		else {
	                		currentNode = parseNodeFromBinaryFileAddress((currentNode.getChildren()[i+1]));
	                		break;
                		}
                	}
                    indexOfElement = i;
                    break;
                }
                //If it finds the right exit to keep searching
                else if(thingToSearchFor < keyAtIndex){
                	if(currentNode.isLeaf()) {
                		breakFromLeaf = true;
                		break;
                	}
                	else {
	                    //Update pointer
	                    currentNode = parseNodeFromBinaryFileAddress(currentNode.getChildren()[i]);
	                    break;
                	}
                }
                //If it gets to the end, go to the right most pointer
                else if(i == numberOfObjects-1){
                	if(currentNode.isLeaf()) {
                		breakFromLeaf = true;
                		break;
                	}
                	else {
	                    //Rightmost pointer
	                    currentNode = parseNodeFromBinaryFileAddress((currentNode.getChildren()[i+1]));
                	}
                }
                //System.out.println("Index: " + indexOfElement + " objects: " + objects.toString() + "thingToSearchFor: "+ thingToSearchFor);
            }
        }

        //Final check
        if(keyAtIndex != thingToSearchFor){
            //Didn't find it
            return null;
        }
        return currentNode.getObjects().get(indexOfElement);
    }

    public BTreeNode parseNodeFromBinaryFileAddress(int address){
        ByteBuffer bb = ByteBuffer.wrap(bTree, address, calculateMaxSizeOfNode(degree));
        int n = bb.getInt();
        byte leafByte = bb.get();
        boolean leaf = false;
        if(leafByte == 1){
            leaf = true;
        }
        int location = bb.getInt();
        int parent = bb.getInt();
        int[] children = new int[2*degree];
        LinkedList<TreeObject> objects = new LinkedList<TreeObject>();
        for(int i = 0; i < 2*degree; i++){
            children[i] = bb.getInt();
        }
        for(int k = 0; k < 2*degree-1; k++){
            //Sequence, frequency
            objects.add(new TreeObject(bb.getLong(), bb.getInt(), lengthOfSequence));
        }
        return new BTreeNode(children, parent, objects, n, leaf, location);
    }

    public int calculateMaxSizeOfNode(int degree){
        ///////////////Data///////Pointers///Metadata//////
        return 12*(2*degree - 1)+4*(2*degree+1)+9;
    }
}