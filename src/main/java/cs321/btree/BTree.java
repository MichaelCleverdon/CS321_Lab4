package cs321.btree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import cs321.common.Cache;

public class BTree
{
	private int rootLoc; //location of root node
	private BTreeNode root; //root node to be stored in memory
	private int t; //not stored in file, retrieved from file name
	private int k; //not stored in file, retrieved from file name
	private String binDataFileName; //not stored in file, obviously
	private Cache<BTreeNode> cache; //not stored in file
	private int currentFileSize;
	
	/**
	 * Constructor for new BTree with no nodes, nodes will later be added with insert method
	 * @param t - degree
	 * @param k - sequence length
	 * @param fileName - the gbkFile that we are parsing to create our BTree
	 */
	public BTree(int t, int k, String gbkFileName) {
		this.rootLoc = -1; //no root yet
		root = null;
		this.t = t;
		this.k = k;
		setCache(null);
		setCurrentFileSize(4);
		
		createBTreeFile(gbkFileName);
	}
	
	/**
	 * Creates a new empty BTree without creating a file, may be useful for Search
	 * @param t - degree
	 * @param k - sequence length
	 */
	public BTree(int t, int k) {
		this.rootLoc = -1; //no root yet
		root = null;
		this.t = t;
		this.k = k;
		setCurrentFileSize(0);
	}
	
	/**
	 * Creates new non-empty BTree without creating file
	 */
	public BTree(int t, int k, int rootLoc) {
		this.t = t;
		this.k = k;
		this.rootLoc = rootLoc;
		root = getNode(rootLoc);
	}
	
	public int getRootLoc() {
		return rootLoc;
	}
	
	public void setRootLoc(int rootLoc) {
		this.rootLoc = rootLoc;
	}
	
	public BTreeNode getRoot() {
		return root;
	}

	public void setRoot(BTreeNode root) {
		this.root = root;
	}

	public Cache<BTreeNode> getCache() {
		return cache;
	}

	public void setCache(Cache<BTreeNode> cache) {
		this.cache = cache;
	}

	public int getCurrentFileSize() {
		return currentFileSize;
	}

	public void setCurrentFileSize(int currentFileSize) {
		this.currentFileSize = currentFileSize;
	}

	private String generateOutputFileName(String gbkFileName) {
		//create the file name for binary file that represents the bTree
		return gbkFileName + ".btree.data." + k +"."+t;
	}
	
	/**
	 * Writes meta data to new file to store BTree
	 */
	private void createBTreeFile(String gbkFileName) {
		//create the file name for binary file that represents the bTree
		binDataFileName = generateOutputFileName(gbkFileName);
			
		//write meta data to file
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(binDataFileName);
			int metaDataSize = 4; //int rootLoc
			byte[] metaBytes = ByteBuffer.allocate(metaDataSize).putInt(rootLoc).array(); 
			//write the updated byte array to the file
			outputStream.write(metaBytes);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method searches the tree for the appropriate location of the new TreeObject
	 * As it searches it splits full nodes
	 * The file updates file information as it goes (when nodes are added or changed)
	 * @param object - the object to be inserted into the tree
	 */
	public void insert(BTree tree, TreeObject object) {		 
		try {
			//get root node, if it exists
			if (tree.root != null) {
				BTreeNode r = tree.getRoot(); //get root node
				//check if root is full
				if (r.getN() == 2*t-1) {
					//allocate space for new node at end of file
					BTreeNode s = new BTreeNode(t, -1, 0, false, currentFileSize);
					//increase file size
					currentFileSize += maxNodeSize(); 
					int[] sChildren = s.getChildren();
					sChildren[0] = r.getLoc();
					rootLoc = s.getLoc();
					root = s;
					//automatically update s for after split
					s = splitChild(s,0,r);
					
					insertNonFull(s,object);
				} else {
					insertNonFull(r, object);
				}
			//if there is no root node, create a root node with the TreeObject
			} else {
				BTreeNode node = new BTreeNode(object, t, currentFileSize); //first node is at 4 bytes in
				currentFileSize += maxNodeSize(); //increment so future nodes locations will be after this node
				root = node;
				rootLoc = root.getLoc();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Splits a node
	 * 
	 * @param x - the parent of the node being split
	 * @param i - the index in the child-pointer array pointing to the node to be split
	 * @param y - the node to be split
	 */
	private BTreeNode splitChild(BTreeNode x, int i, BTreeNode y) {
		//create new node for splitting y, parent is x, will have t-1 tree objects
		//create new node at end of file
		BTreeNode z = new BTreeNode(t, x.getLoc(), t-1, y.isLeaf(), currentFileSize);
		//increase file size so future nodes are added at the right location
		currentFileSize += maxNodeSize();
		
		//put half the tree objects from y into z
		LinkedList<TreeObject> yObjects = y.getObjects();
		LinkedList<TreeObject> zObjects = new LinkedList<TreeObject>();
		//loop runs for all t-1 objects
		for (int j = 1; j <= t-1; j++) {
			//t-1 is middle index
			zObjects.add(yObjects.get(t+j-1));
		}
		z.setObjects(zObjects);
		
		//add child pointers to z if not a leaf
		if (!y.isLeaf()) {
			int[] zChildren = z.getChildren(); //gets correctly sized array
			int[] yChildren = y.getChildren();
			//if not a leaf, we have t many child pointers
			for (int j = 1; j <= t; j++) {
				zChildren[j-1] = yChildren[t+j-1];
				
			}
		}
		
		//insert middle object into parent linked list
		LinkedList<TreeObject> xObjects = x.getObjects();
		xObjects.add(i, yObjects.get(t-1)); //insert median object and shifts automatically
		if (xObjects.size() > 2*t - 1) {
			xObjects.remove(xObjects.size()-1); //remove the extra empty node that was not deleted during the insert
		}
		x.incrementN();
		x.setObjects(xObjects);
		
		//update parent's child pointers
		//shift elements in array starting at and after i+1 to the right once
		int[] xChildren = x.getChildren();
		for (int j = x.getN()-1; j >= i + 1; j--) {
			xChildren[j+1] = xChildren[j];
		}
		xChildren[i+1] = z.getLoc();
		x.setChildren(xChildren);
		
		//'remove' extra stuff from y by just changing the value of n
		y.setN(t-1);
		//update y's parent
		y.setParent(x.getLoc());
		
		//store the nodes, automatically stores to cache or file as needed. 
		storeNode(x);
		storeNode(y);
		storeNode(z);		
		
		return x;
	}
	
	private void insertNonFull(BTreeNode x, TreeObject object) {
		int i = x.getN()-1;
		LinkedList<TreeObject> xObjects = x.getObjects();
		if (x.isLeaf()) {
			//identify where to insert, starting at the right and moving left
			while (i >= 0 && object.getKey() < xObjects.get(i).getKey()) {
				i -= 1;
			}
			if (i > -1) {
				if (object.getKey() == xObjects.get(i).getKey()) {
					xObjects.get(i).incrementFrequency();
				} else {
					xObjects.add(i+1,object);
					if (xObjects.size() > 2*t-1) {
						xObjects.remove(xObjects.size()-1); //remove the extra empty node that was not deleted during the insert
					}
					x.setObjects(xObjects);
					x.incrementN();
				}
			} else {
				xObjects.add(i+1,object);
				if (xObjects.size() > 2*t-1) {
					xObjects.remove(xObjects.size()-1); //remove the extra empty node that was not deleted during the insert
				}
				x.setObjects(xObjects);
				x.incrementN();
			}
			
			storeNode(x);
			
		} else {
			//identify which child pointer to use
			if (xObjects.size() >0) {
				while (i >= 0 && object.getKey() < xObjects.get(i).getKey()) {
					i -= 1;
				}
				if (i > -1) {
					if (object.getKey() == xObjects.get(i).getKey()) {
						xObjects.get(i).incrementFrequency();
						
						storeNode(x);
						return;
					}
				}
				i++;
					
			}
			int childLocation = x.getChildren()[i];
			BTreeNode child = getNode(childLocation);
			//check if the child is full
			if (child.getN() == 2*t-1) {
				//split
				x = splitChild(x,i,child);
				//check if object is equal to what has just been added to x
				if (object.getKey() == x.getObjectAtIndex(i).getKey()) {
					if (cache == null) {
						x.getObjectAtIndex(i).incrementFrequency();
					}
					
					storeNode(x);
				} else {
					//check if we need to add to right child
					if (object.getKey() > x.getObjectAtIndex(i).getKey()) {
						//need next pointer
						i++;
					} 
					//insert into child at index i
					insertNonFull(getNode(x.getChildLocAtIndex(i)),object);	
				}
				
				if (x.getN() > i) {
					if (object.getKey() == x.getObjectAtIndex(i).getKey()) {
						x.getObjectAtIndex(i).incrementFrequency();
					} else {
						
					}
				} 
				
			} else {
				if (x.getN() > i) {
					if (object.getKey() == x.getObjectAtIndex(i).getKey()) {
						x.getObjectAtIndex(i).incrementFrequency();
					} else {
						insertNonFull(child,object);
					}
				} else {
					insertNonFull(child,object);
				}
				
			}
			
		}
		
	}

	public BTreeNode getNode(int address){
		if (cache != null) {
			//loop through cache until we find object with correct location (if it is there)
			LinkedList<BTreeNode> cacheObjects = cache.getCacheObjects();
			int index = -1;
			for (int i = 0; i < cache.getCurrentSize(); i++) {
				if (cacheObjects.get(i).getLoc() == address) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				return cache.getObject(index);
			} else {
				return parseNodeFromAddress(address);
			}
		} else {
			return parseNodeFromAddress(address);
		}
    }
	
	public BTreeNode parseNodeFromAddress(int address) {
		ByteBuffer bb = ByteBuffer.wrap(getByteArrayFromFile(binDataFileName), address, maxNodeSize());
        int n = bb.getInt();
        byte leafByte = bb.get();
        boolean leaf;
        if (leafByte == 1) {
        	leaf = true;
        } else {
        	leaf  = false;
        }
        int location = bb.getInt();
        int parent = bb.getInt();
        int[] children = new int[2*t];
        LinkedList<TreeObject> objects = new LinkedList<TreeObject>();
        for(int i = 0; i < 2*t; i++){
            children[i] = bb.getInt();
        }
        for(int j = 0; j < 2*t - 1; j++){
            //Sequence, frequency, k
            objects.add(new TreeObject(bb.getLong(), bb.getInt(), k));
        }
        return new BTreeNode(children, parent, objects, n,leaf, location);
	}

	private int maxNodeSize() {
		int treeObjectSize = 12; //long for seq and int for frequency
		int pointerSize = 4; //int's
		int nodeMetaData = 9; //int n, boolean leaf, int loc
        return treeObjectSize*(2*t-1)+pointerSize*(2*t+1)+nodeMetaData;
	}
	
	public void writeRootToDisk(String gbkFileName) {
		byte[] rootLocAsBytes = ByteBuffer.allocate(4).putInt(rootLoc).array();
		String fileName = generateOutputFileName(gbkFileName);
		try {
			//store current file to byte array
			InputStream inputStream = new FileInputStream(fileName);
			int fileSize = (int) new File(fileName).length();
			byte[] allBytes =  new byte[fileSize];
		
 	        inputStream.read(allBytes);
			inputStream.close();
			
			//write the info
			OutputStream outputStream = new FileOutputStream(fileName);

			//replace correctly located bytes in file's array with the byte array created above (nodeAsBytes)
			for (int i = 0; i < 4; i++) {
				allBytes[i] = rootLocAsBytes[i];
			}
			//write the updated byte array to the file
			outputStream.write(allBytes);
			outputStream.close();
			
			root.diskWrite(maxNodeSize(), binDataFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getByteArrayFromFile(String fileName) {
		try {
			InputStream inputStream = new FileInputStream(fileName);
			int fileSize = (int) new File(fileName).length();
			byte[] allBytes =  new byte[fileSize];
 	        inputStream.read(allBytes);
			inputStream.close();
			
			return allBytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Automatically inserts into cache or writes to disk as needed.
	 * If anything is kicked out of the cache, it is written to the disk. 
	 * @param node
	 */
	private void storeNode(BTreeNode node) {
		if (cache != null) {
			if (node.getLoc() != rootLoc) {
				BTreeNode write = cache.addObject(node);
				if (write != null) {
					write.diskWrite(maxNodeSize(), binDataFileName);
				}
			} 
		} else {
			node.diskWrite(maxNodeSize(), binDataFileName);
		}
		
	}
	
	/**
	 * Write entire contents of cache to file
	 */
	public void writeCacheToFile() {
		LinkedList<BTreeNode> cacheObjects = cache.getCacheObjects();
		for (int i = 0; i < cache.getCurrentSize(); i++) {
			cacheObjects.get(i).diskWrite(maxNodeSize(), binDataFileName);
		}
	}
	
	@Override
	public String toString() {
		return toString(rootLoc);
	}
	
	/**
	 * Creates dump file for GeneBankCreateBTree debugging.
	 * @param bTreeFileName
	 */
	public String toString(int rootAddress){
        BTreeNode currentNode = null;
        String output ="";
        if(rootAddress == -1){
        	return output;
        }
        
        currentNode = getNode(rootAddress);
        
        if (currentNode.isLeaf()) {
        	output += currentNode.toString();
        } else {
	        //get the list of children locations 
	        int[] children = currentNode.getChildren();
	        LinkedList<TreeObject> objects = currentNode.getObjects();
	        //get number of keys in node
	        int n = currentNode.getN(); 
	        //recursive call to children and nodes in order
	        for (int i = 0; i < n; i++) {
	        	output += toString(children[i]) + "\n";
	        	output += (objects.get(i).toString()) + "\n";
	        }
	        //recursive call to the last (most-right) child
	        output += toString(children[n]); 
        }
        
        return output;
    }
}
