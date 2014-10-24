package twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
	/*
	 * This Program will run  and count the frequency of positive and negative words in the program
	 */
	public class NaiveBayesLearner {
		public static HashMap<String, Integer> positiveWordHashMap=new HashMap<String,Integer>();
		public static HashMap<String, Integer> negativeWordHashMap=new HashMap<String,Integer>();		
		
		public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
	      //private final static IntWritable one = new IntWritable(1);
	     
	      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
		       
	    	  String line = value.toString();
	    	  int tweetStatus=0; // 0 means neutral
	    	
	    	  if(line.contains(":)") || line.contains(":-)") || line.contains(":D"))
	    	  {
	    		  tweetStatus=2; // 2 means positive tweet;	
	    		  System.out.println("Positive Tweet");
	       	  }
	    	  else if (line.contains(":(") || line.contains(":-("))
	    	  {
	    		  tweetStatus=1; // 1 means a negative tweet
	    		  System.out.println("Negative Tweet");
	    	  }
	    	  
	    	  String[] tweetWords=line.split(" ");
	       	  for(int i=0;i<tweetWords.length;i++)
	    	  {
	    		  String currentWord=tweetWords[i]; //fetch the current word from tweet
	    		  if(positiveWordHashMap.containsKey(currentWord) && tweetStatus==2) // If current word is positive word hashmap and tweet is positive
    			  {
	    			  currentWord=currentWord + " ";
	    			  output.collect(new Text(currentWord), new IntWritable(1));
    			  }
	    		  if(negativeWordHashMap.containsKey(currentWord) && tweetStatus==1)//If current word is negative word hashmap and tweet is negative
	    		  {
	    			  currentWord=currentWord + " ";
	    			  output.collect(new Text(currentWord), new IntWritable(1));
	    		  }
	    		  else
	    			  continue;
	    		  
	    	  }
	    	        
	      }	      
	    }
	
	    public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
	      public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
	        int sum = 0;
	        while (values.hasNext()) 
	        {
	          sum += values.next().get();
	        }
	       output.collect(key, new IntWritable(sum));
	      }
	    }
	
	    public static void main(String[] args) throws Exception {
	        	
	    	FileSystem fs = FileSystem.get(new Configuration());
	    	try{
                Path pt=new Path("/user/hadoop/dir1/positive-words.txt");//Path of text file that contains all the  positive words
                
                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
                String line;
                line=br.readLine();
                while (line != null){
                		positiveWordHashMap.put(line.toString(), 1);
                        //System.out.println(line);
                        line=br.readLine();
                }
        }catch(Exception e){
        }
	    	
	    	FileSystem fs1 = FileSystem.get(new Configuration());
	    	try{
                Path pt=new Path("/user/hadoop/dir1/negative-words.txt");//Path of text file that contains all the  negative words
                
                BufferedReader nbr=new BufferedReader(new InputStreamReader(fs1.open(pt)));
                String line1;
                line1=nbr.readLine();
                while (line1 != null){
                		negativeWordHashMap.put(line1.toString(), 1);
                        //System.out.println(line);
                        line1=nbr.readLine();
                }
        }catch(Exception e){
        }
	    	
	    	try {
				JobConf conf = new JobConf(NaiveBayesLearner.class);//Setting Naive Bayes Learner class as Job Conf
			     conf.setJobName("NaiveBayesLearner");
			     System.out.println("Starting Naive Bayes Learner Map Reduce Algorithm");
			     conf.setOutputKeyClass(Text.class);//Setting text file as output
			     conf.setOutputValueClass(IntWritable.class);
			
			     conf.setMapperClass(Map.class);//Setting Mapper class
			     conf.setCombinerClass(Reduce.class);//Setting Combiner class
			     conf.setReducerClass(Reduce.class);//Setting reducer class
			
			     conf.setInputFormat(TextInputFormat.class);
			     conf.setOutputFormat(TextOutputFormat.class);
			     
			     FileInputFormat.setInputPaths(conf, new Path("/user/hadoop/dir2/"));//Setting the path for input file
			     FileOutputFormat.setOutputPath(conf, new Path(args[0]));
			     
			     JobClient.runJob(conf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	
	
	