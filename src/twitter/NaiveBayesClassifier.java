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
	 * This Program will count the positive and negative count of tweets from the training data set
	 */
	public class NaiveBayesClassifier {
		
		public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
	      	     
	      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
		       
	    	  String line = value.toString();
	    	  @SuppressWarnings("unused")
			int tweetStatus=0; // 0 means neutral
	    	//  System.out.println("Line is :"+ line);
	    	  if(line.contains(":)") || line.contains(":-)") || line.contains(":D")) // Checking positive emoticons in tweet
	    	  {
	    		  tweetStatus=2; // 2 means positive tweet;	
	    		  output.collect(new Text("positive "), new IntWritable(1));
	    	  }
	    	  else if (line.contains(":(") || line.contains(":-("))// Checking negative emoticons in tweet
	    	  {
	    		  tweetStatus=1;
	    		  output.collect(new Text("negative "), new IntWritable(1));
	    	  }
	    	  else 
	    	  {
	    		  output.collect(new Text("neutral "), new IntWritable(1));
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
	    
	    		    	
	    	try {
				JobConf conf = new JobConf(NaiveBayesClassifier.class);//Setting the naive bayes classifier class
			     conf.setJobName("NaiveBayesClassifier");
			     System.out.println("Starting Naive Bayes Map Reduce Algorithm");
			     conf.setOutputKeyClass(Text.class);
			     conf.setOutputValueClass(IntWritable.class);
			
			     conf.setMapperClass(Map.class);
			     conf.setCombinerClass(Reduce.class);
			     conf.setReducerClass(Reduce.class);
			
			     conf.setInputFormat(TextInputFormat.class);
			     conf.setOutputFormat(TextOutputFormat.class);
			     
			     FileInputFormat.setInputPaths(conf, new Path("/user/hadoop/dir2/"));//Setting the input path
			     FileOutputFormat.setOutputPath(conf, new Path(args[0]));
			     
			     JobClient.runJob(conf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}