package cs321.btree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BTreeNode {
	//pointers
	private int[] children; //array of ints representing location of child nodes
	private int parent; //int representing location of parent node
	//objects
	private LinkedList<TreeObject> objects;
	//meta data
	private int n; //number of objects in node
	private boolean leaf; //is this node a leaf?
	private int loc; //location of node
	
	/*
	 * Constructor for making complete node, primarily used when reading from file
	 */
	public BTreeNode(int[] children, int parent, LinkedList<TreeObject> objects, int n, boolean leaf, int loc){
		this.children = children;
		this.parent = parent;
		this.objects = objects;
		this.n = n;
		this.leaf = leaf;
		this.loc = loc;
	}

	/*
	 * Creating a brand new node from one TreeObject and the location of the node
	 * The degree determines the size of the data and children arrays
	 * A parent, children, and other TreeObjects can be added later
	 */
	public BTreeNode(int parent, TreeObject object, int degree, int loc) {
		int maxChildren = 2*degree;
		children = new int[maxChildren];
		LinkedList<TreeObject> objects = new LinkedList<TreeObject>();
		objects.add(object);
		this.parent = parent;
		this.objects = objects;
		n = 1;
		leaf = true;
		this.loc = loc;
	}

	/*
	 * Constructor without parent input for root node. 
	 * Because it wouldn't let me put 'null' for an integer, 
	 * I put -1 as the default parent location.
	 */
	public BTreeNode(TreeObject object, int degree, int loc) {
		int maxChildren = 2*degree;
		children = new int[maxChildren];
		parent = -1;
		objects = new LinkedList<TreeObject>();
		objects.add(object);
		n=1;
		leaf = true;
		this.loc = loc;
	}

	/**
	 * Constructor without objects for allocating a new empty node at the end of the file
	 * Autmoatically write the node to the file
	 */
	public BTreeNode(int degree, int parent, int n, boolean leaf, int maxNodeSize, String fileName){
		int maxChildren = 2*degree;
		children = new int[maxChildren];
		this.parent = parent;
		objects = new LinkedList<TreeObject>();
		this.n = n;
		this.leaf = leaf;
		loc = -1;
		
		this.diskWrite(maxNodeSize, fileName);
	}
	
	/**
	 * creates new node with location without immediately writing to file
	 * @param degree
	 * @param parent
	 * @param n
	 * @param leaf
	 * @param maxNodeSize
	 * @param loc
	 */
	public BTreeNode(int degree, int parent, int n, boolean leaf, int loc){
		int maxChildren = 2*degree;
		children = new int[maxChildren];
		this.parent = parent;
		objects = new LinkedList<TreeObject>();
		this.n = n;
		this.leaf = leaf;
		this.loc = loc;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public int[] getChildren() {
		return children;
	}

	public int getChildLocAtIndex(int i) {
		return children[i];
	}

	public void setChildren(int[] children) {
		this.children = children;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}
	
	public void incrementN() {
		n++;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public int getLoc() {
		return loc;
	}

	public void setLoc(int loc) {
		this.loc = loc;
	}

	@SuppressWarnings("unchecked")
	public LinkedList<TreeObject> getObjects(){
		return (LinkedList<TreeObject>) objects.clone();
	}

	public TreeObject getObjectAtIndex(int i) {
		return objects.get(i);
	}

	public void setObjects(LinkedList<TreeObject> objects) {
		this.objects = objects;
	}

	public byte[] toByteArray(int maxNodeSize) {
		int treeObjectSize = 12;
		int pointerSize = 4;

		//allocate full space and store meta data and parent pointer
		byte leafByte = 0;
		if (leaf) {
			leafByte = 1;
		}
		byte[] nodeAsBytes = ByteBuffer.allocate(maxNodeSize).putInt(n).put(leafByte).putInt(loc).putInt(parent).array();

		//store child pointers
		int c = 13; //current location after inserting above: 4+1+4+4
		for (int i : children) {
			byte[] temp = ByteBuffer.allocate(4).putInt(i).array();
			for (int j = 0; j < pointerSize; j++) {
				nodeAsBytes[c+j] = temp[j];
			}
			c += pointerSize;
		}

		//store the actual tree objects to the byte array
		for (int i = 0; i < objects.size(); i++) {
			byte[] temp = objects.get(i).toByteArray();
			for (int j = 0; j < treeObjectSize; j++) {
				nodeAsBytes[c+j] = temp[j];
			}
			c += treeObjectSize;
		}

		return nodeAsBytes;
	}

	/**
	 * Converts BTreeNode to byte array and write this information to the appropriately located bytes in file
	 * Reads the whole file to a byte array, modifies the appropriate elements of byte array and then writes the file again
	 * @param maxNodeSize - amount of space needed to store a full node
	 * @param fileName - the file where the information will be written/updated;
	 * the provided file should always exist because there should be meta data stored already
	 */
	public void diskWrite(int maxNodeSize, String fileName) {
		byte[] nodeAsBytes = this.toByteArray(maxNodeSize);

		try {
			//store current file to byte array
			InputStream inputStream = new FileInputStream(fileName);
			int fileSize = (int) new File(fileName).length();

			//generate location at end of file
			if (loc == -1) {
				loc = fileSize;
			}

			//if we are writing our node onto the end, of the file need to add space in the array for the node.
			int allBytesSize;
			if (fileSize <= loc) {
				int missingSpace = loc-fileSize;
				allBytesSize = fileSize + missingSpace + maxNodeSize; 
			} else {
				allBytesSize = fileSize;
			}
			byte[] allBytes =  new byte[allBytesSize];

 	        inputStream.read(allBytes);
			inputStream.close();

			//write the info
			OutputStream outputStream = new FileOutputStream(fileName);

			//replace correctly located bytes in file's array with the byte array created above (nodeAsBytes)
			for (int i = 0; i < maxNodeSize; i++) {
				allBytes[loc+i] = nodeAsBytes[i];
			}

			//write the updated byte array to the file
			outputStream.write(allBytes);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		String temp = "";
		for (int i = 0; i < n; i++) {
			temp += objects.get(i).toString();
			temp +="\n";
		}
		String output = temp.substring(0, temp.length() - 1);
		return output;
	}
	
}