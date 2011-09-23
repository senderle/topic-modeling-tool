package cc.mallet.topics.gui;
import java.io.*;
import java.util.*;

public class CsvBuilder {

	int numDocs;
	int numTopics;
	String START_DOC_ID = "0";//FIXME
	ArrayList<String> docNames ;			//May fail for LARGE collections
	int[][] Ntd;
	
	
	public void buildCsv1(String topicKeysFile,String outputCsv)
	{
		try
		{
            FileReader fread = new FileReader(topicKeysFile);
            BufferedReader in = new BufferedReader(fread);
            FileWriter fwrite = new FileWriter(outputCsv);  
        	BufferedWriter out = new BufferedWriter(fwrite);
        	String header = "topicId,words..";
        	out.write(header+"\n");
        	String line;
        	
        	while ((line = in.readLine()) != null)
			{  
            	String[] strArr = line.split("\\t| ");
            	line = strArr[0]+","+strArr[2];            	
            	for(int i=3;i<strArr.length;i++)
            	{
            		line = line+" "+strArr[i];
            	}       	
            	out.write(line+"\n");
			}
            out.flush();
		}catch (Exception e){
			System.err.println(e);
		}	     
	}
	
	
	
	
	public int[][] buildNtd(int T,int D,String stateFile)
	{
		int[][] Ntd = new int[T][D];
		try
		{
            FileReader fread = new FileReader(stateFile);
            BufferedReader in = new BufferedReader(fread);
            String line = null;
                        
            in.readLine();in.readLine();in.readLine();      //header lines            	
            String curDocId = START_DOC_ID;	//MAY BREAK
            int curDocIndex = 0;
           
            while ((line = in.readLine()) != null){
            	//System.out.println(line);
            	String[] strArr= line.split(" ");
            	
            	if(!strArr[0].equals(curDocId)){
            		curDocIndex++;
            		curDocId = strArr[0];
            	}
            	int wordTopicIndex = Integer.parseInt(strArr[strArr.length-1]);
            	Ntd[wordTopicIndex][curDocIndex]++;        	
			}
            
            in.close();  
            return Ntd;
            
        }catch (Exception e){
		System.err.println(e);
		return	null;
		}	       
	}
	
	private Integer[] sortTopicIdx(final int[] docScores)
	{
		final Integer[] idx = new Integer[numDocs];
		for(int i=0;i<numDocs;i++){
			idx[i] = i;
		
		}

		Arrays.sort(idx, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return docScores[o1]-docScores[o2];
		    }
		});

		return idx;
	}
	
	public void buildCsv3(String stateFile, int numDocsShown, String outputCsv)		//docs in topic
	{
		Ntd =  buildNtd(numTopics, numDocs,stateFile);
    	try{
		FileWriter fwrite = new FileWriter(outputCsv);  
    	BufferedWriter out = new BufferedWriter(fwrite);
    	String header = "topicId,rank,docId,filename";
    	out.write(header+"\n");
    	String line;
		for(int i=0;i<numTopics;i++){
			Integer[] idx = sortTopicIdx(Ntd[i]);
			for (int j=0;j<numDocsShown;j++){									//FIXME doc id and number are the same
				int k = idx[numDocs-j-1];										//Descending
				line = i+","+j+","+k+","+docNames.get(k)+"\n";
				out.write(line);
				//System.out.println(line);
			}
		}
		out.flush();
		
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	
	public String extractFileSubstring(String[] strArr,int startIndex){
		String filename=strArr[startIndex];
		for(int i = startIndex+1;i<strArr.length;i++){			
			if(new File(filename).exists()){
				filename = filename+","+i;				//FIXME change to object if possible
				break;
			}
			filename = filename + " " + strArr[i];
		}		
		return filename;
	}
	
	public String dtLine2Csv(String line)
	{
		
		try{
		int start;
		String[] str = line.split(" ");
		
		if(str.length>=2){	
		String csvLine = str[0];
		if(str[1].equals("null-source")){
			csvLine  = csvLine + ","+str[1];
			start = 2;
			docNames.add("null-source");
		}
		
		else{			
			String augfile = extractFileSubstring(str,1);
			String[] filewnum = augfile.split(",");
			docNames.add(filewnum[0]);
			csvLine  = csvLine + ","+filewnum[0];
			start = Integer.parseInt(filewnum[1]);
		}	
		for(int i=start;i<str.length-1;i=i+2)
		{
			csvLine = csvLine + "," + str[i]+","+str[i+1];
			
		}				
		return csvLine;
		}
		else 
			return line; 
		}
		catch (Exception e){
			e.printStackTrace();
			return	null;
		}		
	}
	
	public void buildCsv2(String docTopicsFile, String outputCsv)  //topics in doc
	{
		try
		{
            FileReader fread = new FileReader(docTopicsFile);
            BufferedReader in = new BufferedReader(fread);
            String line = null;
            int nd = 0;
            docNames = new ArrayList<String>();
                        
            line = in.readLine();      //skip mallet header line
            if(line!= null)
            {
            	FileWriter fwrite = new FileWriter(outputCsv);  
            	BufferedWriter out = new BufferedWriter(fwrite);
            	String header = "docId,filename,top topics...";			//variable number of topics for each doc
/*            	for(int i=0;i<numTopics;i++){
            		header = header+",top-"+i;
            	}*/
            	out.write(header+"\n");
	            while ((line = in.readLine()) != null)
				{   nd++;
	            	String csvLine = dtLine2Csv(line);
	            	//System.out.println(csvLine);
	            	out.write(csvLine+"\n");
				}
	            out.flush();
	            setNumDocs(nd);
            }
            
			in.close();
		} 
        catch (Exception e)
		{
			System.err.println("File input error");
		}
	}
	
	public void setNumDocs(int value)
	{
		numDocs = value;
	}
	
	public void setNumTopics(int value)
	{
		numTopics = value;
	}
	
	
	public void createCsvFiles(int numTopics,String outputDir)
	{			 
		File csvDir = new File(outputDir + File.separator+ "output_csv");	//FIXME replace all strings with constants
	 	csvDir.mkdir();
		setNumTopics(numTopics);		
		String csvDirPath = csvDir.getPath();
		buildCsv1(outputDir+File.separator+"output_topic_keys",csvDirPath+File.separator+"Topics_Words.csv");
		buildCsv2(outputDir+File.separator+"output_doc_topics.txt",csvDirPath+File.separator+"TopicsInDocs.csv");
		buildCsv3(outputDir+File.separator+"output_state", Math.min(500, numDocs),csvDirPath+File.separator+"DocsInTopics.csv");
	}
	
	public int[][] getNtd(){
		return Ntd;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CsvBuilder o = new CsvBuilder();
		//o.setNumTopics(10);	
		//o.buildCsv2("/Users/zom/Documents/workspace/MalletGui/output_doc_topics.txt", 4, "output.csv");
		//o.buildCsv3("/Users/zom/Documents/workspace/MalletGui/output_state",4,"output2.csv");
		//o.buildCsv1("/Users/zom/Documents/workspace/MalletGui/output_topic_keys", "output3");
	}

}
