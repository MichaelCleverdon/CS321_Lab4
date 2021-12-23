package cs321.btree;

import cs321.*;

public class BTreeNodePlusIndex{
    private BTreeNode bTreeNode;
    private int index;

    public BTreeNodePlusIndex(BTreeNode bTreeNode, int index){
        this.bTreeNode = bTreeNode;
        this.index = index;
    }

    public BTreeNode getBTreeNode() {
        return bTreeNode;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString(){
        return bTreeNode.toString() + " index: " + index;
    }
}