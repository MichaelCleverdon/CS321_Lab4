package cs321.btree;

import java.nio.ByteBuffer;

public class TreeObject
{
	private long sequence;
	private int frequency;
	private int k; //length of sequence

	public TreeObject(long key, int k) {
		this.sequence = key;
		this.k = k;
		frequency = 1;
	}

	public TreeObject(long key, int frequency, int k){
		this.sequence = key;
		this.frequency = frequency;
		this.k = k;
	}

	public long getKey() {
		return sequence;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void incrementFrequency() {
		frequency++;
	}
	
	public String getSequence() {
		String bin = Long.toBinaryString(sequence);
		//prepend 0's as needed until the string is the length of the actual sequence
		while (bin.length() != 2*k) {
			bin = "0" + bin;
		}
		String seq = "";
		for (int i=0; i < bin.length(); i += 2) {
			String temp = bin.substring(i, i+2);
			if (temp.equals("00")) {
				seq += "a";
			} else if (temp.equals("11")) {
				seq += "t";
			} else if (temp.equals("01")) {
				seq += "c";
			} else if (temp.equals("10")) {
				seq += "g";
			}
		}
		return seq;
	}

	public byte[] toByteArray() {
		//note may need to change the 12 if we change the data stored in a TreeObject
		byte[] byteArray = ByteBuffer.allocate(12).putLong(sequence).putInt(frequency).array();
		return byteArray;
	}

	public String toString() {
		return this.getSequence() + ": " + frequency;
	}
}
