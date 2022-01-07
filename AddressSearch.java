import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Arrays;
import java.util.*;
import java.nio.file.*;;

import org.apache.lucene.util.CharsRef;

import org.apache.commons.lang3.ArrayUtils;

import org.apache.lucene.analysis.synonym.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.queryparser.classic.QueryParser;

public class AddressSearch{
	
	public static void main(String [] args){
		String indexPath = "./fancy-index";
		String docsPath = "./docs";
		System.out.println("Indexing...");
		index(indexPath,docsPath);

		Scanner in = new Scanner(System.in);
		while(true){
			System.out.println("Please enter the query address: ");
			String inp = in.nextLine();
			String inp_dup = inp;
			if(inp.equals(":q"))
			{
				System.out.println("Quitting...");
				break;
			}
			else 
			{
				String[] roads = {"road", "street", "lane", "main", "rd", "marg", "cross", "to", "st", "bridge", "path"};
				String[] arr_inp = inp.replaceAll("[^A-za-z0-9 ]", " ").split("\\s+");
				List<Integer> indarr = new ArrayList<Integer>(); //array of indexes of all instances "road"
				int n = arr_inp.length;
				for(int i = 0;i<n;i++){
					arr_inp[i] = arr_inp[i].toLowerCase();
				}
				for(String road:roads){
					for(int i = 0;i<n;i++){
						if(arr_inp[i].equals(road)){
							indarr.add(i);
							arr_inp[i] = arr_inp[i] + "^0";//giving it a weight of 0, just becuase
						}
					}
				}
				if(indarr.isEmpty()){
					System.out.println("Could not locate a street term");
					continue;
				}
				else{
					inp = "";
					Double cutoff = 0.0;
					for(int i=0;i<n;i++){
						int dist = n;
						int val = 0;
						if((arr_inp[i].length() > 3) && (arr_inp[i].substring((arr_inp[i].length())-2).equals("^0"))){
								continue;
						}
						else{
							for(int ind:indarr){
								val = Math.abs(ind - i);
								// System.out.println(val);
									if(val < dist){
										dist = Math.abs(ind - i);
								}
							}
						inp = inp + arr_inp[i] + "^" + Double.toString(Math.exp(-dist+1)) + " ";
						cutoff = cutoff + Math.exp(-dist+1);
						}
					}
					// System.out.println(inp);
					search(indexPath, inp, cutoff/n);
				}
			}
		}
	}
	public static void index(String indexPath,String docsPath){
		//Only does depth one indexing 
		// Cause who does have time to do it
		// just add it to some todo list or ticket
		File folder = new File(docsPath);
		File[] fileList = folder.listFiles();

		// Attempted to make a synonym map. Ignore it
			// SynonymMap synonymMap = null;
			// SynonymMap.Builder builder = new SynonymMap.Builder();
			// for(File file:fileList){
			// 	String loc = file.getPath();
			// 	String word = "";
			// 	try{word = new String(Files.readAllBytes(Paths.get(loc)));}catch(IOException e){e.printStackTrace();}
			// 	String[] words = word.replaceAll("[^A-za-z0-9 ]", " ").split("\\s+");
			// 	for(int i=0;i<words.length-1;i++){
			// 		builder.add(new CharsRef(words[i] + " " + words[i+1]), new CharsRef(words[i].charAt(0) + " " + words[i+1].charAt(0)), true);
			// 		builder.add(new CharsRef(words[i].charAt(0) + " " + words[i+1].charAt(0)), new CharsRef(words[i] + " " + words[i+1]), true);
			// 	}
			// }
			// try {
			// 	synonymMap = builder.build();
			// } catch (IOException e) {
			// 	System.err.print(e);
			// }

		int fileListSize = (fileList==null)?0:fileList.length;
		//Boiler plate 
		// just pretend these are function instead of objects
		// and you'll be fine ig
		
		try {
		Analyzer anl  = new SimpleAnalyzer();
		Directory dir = FSDirectory.open(Paths.get(indexPath));
		IndexWriterConfig iwc = new IndexWriterConfig(anl);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		// System.out.println(iwc.toString());
		// end boilerplate
		for(int i=0;i<fileListSize;i++)
		{
			if(fileList[i].isFile())
			{
				System.out.println(fileList[i].getPath()+"");
				indexDoc(writer, fileList[i].toPath());
			}
		}
		writer.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}

	public static void indexDoc(IndexWriter writer, Path file)
	{
		try 
		{
			Document doc = new Document();
			doc.add(new StringField("path", file.toString(), Store.YES));
			doc.add(new TextField("content",new String(Files.readAllBytes(file)),Store.YES));
			writer.addDocument(doc);

		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void search(String indexPath,String textToFind, Double cut){
		try{
		IndexSearcher searcher =  new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexPath))));
		TopDocs hits = searcher.search(new QueryParser("content", new SimpleAnalyzer()).parse(textToFind),10);
		// System.out.println("Total Hits: "+hits.totalHits);
		int total = 0;
		System.out.println("\nHITS:");
		for(ScoreDoc s: hits.scoreDocs){
			if(s.score>cut){
				String doc = searcher.doc(s.doc).get("content");
				String docPath = searcher.doc(s.doc).get("path");
				System.out.println("Address: "+doc+"\nScore:\t "+s.score);
				total++;
			}
		}
		System.out.println("\n");//"Total Hits: "+total);
		System.out.println("NOT HITS:");
		for(ScoreDoc s: hits.scoreDocs){
			if(s.score<cut){
				String doc = searcher.doc(s.doc).get("content");
				String docPath = searcher.doc(s.doc).get("path");
				System.out.println("Address: "+doc+"\nScore:\t "+s.score);
				total++;
			}
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}