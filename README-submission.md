# Team Members

Last Name       | First Name      | GitHub User Name
--------------- | --------------- | --------------------
Derrick         | Josie           | LittleNerdJos
Cleverdon       | Michael         | MichaelCleverdon

# Cache Performance Results
When running the GeneBankCreateBTree program with the test5.gbk, optimal degree (e.g. command line argument for degree was 0, calculated optimal degree was 127), a sequence length of 7, and a debug level of 0. With a cache of size 100, the run-time was 21148 milliseconds. With a cache size of 500 the run-time was 20930 milliseconds. From this, it seems with a degree of 127, having a cache size of 100 is big enough, and thus there wasn't a big change in run0time with a cache size of 500. Though the amount of time wasn't very different between these two cache sizes, the run-time was SIGNIFICANTLY less than not having a cache. When running the program with the same parameters, but no cache, the program was not able to complete within 10 minutes, and so I chose to terminate it prior to finishing. This reduction from over 10 minutes to less than half a second was very interesting to observe.  

I also ran the program with a slightly lower value for the degree. Using test5.gbk, a degree of 110, a sequence length of 7, and a debug level of 0, the run-time with a cache size of 100 was 80356 milliseconds (1 minute 20.356 seconds) and with a cache-size of 500 it took 19785 milliseconds (19.785 seconds). So, we can see that having a higher cache size definitely lowers the necessary amount of time for the program to run when the degree is lowered (because a lower degree requires more nodes). 

When running the GeneBankSearch program with the test5.gbk.btree.data.7.100 file (command line arguments <0> <teset5.gbk.btree.data.7.100> <query7>) it searched through the entire file with a runtime of about 500ms. When running it with a cache size of 100, the program had a runtime of 478ms. One thing I discovered was that the program actually had a slower runtime when running with a cache size larger than 100. When running the search program with a cache size of 500, the program slowed down and had a runtime of about 600ms. I believe this is because the cache search runs in linear time, so as the amount of cache objects increase, the time to search increases as well.

# BTree Binary File Format and Layout
The BTree file is formatted with 4 bytes of meta data containing an integer representing the location of the root node. 
We chose not to store the k and t values in the meta data because they were already contained in the title of the binary data file.
After the meta data each node is stored. Each node takes 12*(2*t-1)+4*(2*t+1)+9. 
This is because:
* The TreeObjects contain 12 bytes of data (long data type representing the sequence and int representing its frequency).
* The pointers are int data types and there are 2t child pointers and 1 parent pointer
* The meta data of the BTreeNode is an int data type representing the number of objects in the node, a boolean data type representing if the node is a leaf, and another int data type representing the location of the node.  
Each node is stored in the order: n, leaf, location, parent pointer, children pointers, tree objects (stored as sequence then frequency). "n" is the number of objects in the node. 
 

# Additional Notes
TBD

