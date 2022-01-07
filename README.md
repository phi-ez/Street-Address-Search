# AddressToDoc.py
This python code will take your text file of addresses and put them individually into the docs folder. After running the python file, enter the name of the text file **with** `.txt` written.

Also, in the text file, enter each address in a separate line and don't put any empty/whitespaces between any two lines.

# AddressSearch.java
To initiate the java code,
### For Linux/Mac
Compile
>`javac -cp "./lib/*" AddressSearch.java`

Run
>`java -cp "./lib/*" AddressSearch.java`

### For Windows
Compile
>`javac -classpath '.;.\lib\*' AddressSearch.java`

Run
>`java -classpath '.;.\lib\*' AddressSearch`

This will use the packages in the lib directory, in case you don't have them installed.

After running it, the addresses in `docs` directory will be indexed by lucene.

After it is indexed, you will be asked to enter the query address. It will then process the address and give you the addresses it thinks are on the same street as the query address. If it thinks there is no street in the query address it will output
>`Could not locate a street term`

and ask you to enter it again.

The output addresses will be in the form of `HITS` and `NOT HITS` with the score given to each address below them. We decided to include `NOT HITS` so that you could easily see which correct ones we missed.

## In-depth working of the code
### search(String indexPath,String textToFind, Double cut)
It takes the indexPath, textToFind, and cut (cutoff), and prints hits with score > cut under "HITS" and hits with score < cut under "NOT HITS"

### index(String indexPath,String docsPath)
Just indexes all docs at docsPath using the inbuilt `SimpleAnalyzer` in lucene, and stores them at indexPath.

### indexDoc(IndexWriter writer, Path file)
Helper function of index(String indexPath,String docsPath).
Adds file to writer.

### main()
This is where main stuff happens.

We first index the docs using the `index` function. Then we take the input and clean it of any non-alphanumeric symbols.

We locate the index of each "street" synonym and append `^0` to them.
Adding `^x` to a term, gives a boost of `x` to that term. We give a boost of `0` (effectively, removing it) so that the algorithm will not be swayed by the different ways people write "street".

Then, for each term i, find dist[i] which is distance from nearest "street" synonym and then append `"^"+str(e^(-dist[i]+1))` to it. Basically the boost decreases as we go farther form the "street" synonyms.

We set `cutoff = 1/n * (\sum_i e^(-dist[i]+1))`, i.e, the mean of the boost applied.

Lastly, we call the search function on the input and the cutoff.
