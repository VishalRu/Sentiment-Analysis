package twitter;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
	/*
	This Class will classify tweets on the basis of simple lexical token analysis and Naive bayes classifier.
	This class needs naive bayes learner to set the frequency of words from training set
	*/
	public class SentimentAnalysis {
	  public static final String positiveWordFile= "positive-words.txt"; 
	  public static final String negativeWordFile= "negative-words.txt";
	  public static HashMap<String, Integer> positiveWordHashMap=new HashMap<String,Integer>();// HashMap to hold all the positive words
	  public static HashMap<String, Integer> negativeWordHashMap=new HashMap<String,Integer>();// HashMap to hold all the negative words
	  public static HashMap<String, Double> wordFreqHashMap=new HashMap<String,Double>(); //HashMap to hold the frequency of words from naive bayes learner
	  public static double positiveTwets= 43957; // number of positive tweets from training data set
	  public static double negativeTweets=16853;// number of negative tweets from training data set
	  public static double totalTweets=11912679; // total number of tweets from training data set
	  public static double probabPositive=positiveTwets/totalTweets; //positive probability of tweets on the basis of training data set
	  public static double probabNegative=negativeTweets/totalTweets;// negative probability of tweets on the basis of training data set
		
		public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
	      private final static IntWritable one = new IntWritable(1);
	     
	      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
		       
	    	  String line=value.toString();
	    	
	    	  String[] tweetWords=line.split(" "); // Splitting the words of tweet on the basis of spaces
	        
	    	  int score=0;
	    	  double posProbab=1.0;//initial probability
	    	  double negProbab=1.0;
	    	  for(int i=0;i<tweetWords.length;i++)
	    	  {
	    		  String currentWord=tweetWords[i];
	    		  if(positiveWordHashMap.containsKey(currentWord))
	    		  {
	    			  score++;// increase the score of tweet if it contains the positive word
	    			  if(wordFreqHashMap.containsKey(currentWord))
	    			  {
	    				  posProbab*=wordFreqHashMap.get(currentWord)/positiveTwets;// compute the probability of positive tweet given a word
	    			  }
	    			  
	    		  }
	    		  if(negativeWordHashMap.containsKey(currentWord))
	    		  {
	    			  score--;
	    			  if(wordFreqHashMap.containsKey(currentWord))
	    			  {
	    				  negProbab*=wordFreqHashMap.get(currentWord)/negativeTweets;// compute the probability of positive tweet given a word
	    			  }
	    		  }
	    		  else
	    			  continue;
	    		  
	    	  }
	    	  posProbab*=probabPositive;
	    	  negProbab*=probabNegative;
	    	  
	    	 // System.out.println("Tweet is : "+ line);
	    	  //System.out.println("Values of posP and NegP and score is: " + posProbab + ", "+ negProbab + ", " +score);
	    	 
	    	  if(score>0 && posProbab>negProbab) // if score of tweet is greater than zero and positive probability is greater than negative probability then classify as a positive tweet
	    		  output.collect(new Text("positive"), one);
	    	  else if (score<0 && posProbab<negProbab )
	    		  output.collect(new Text("negative"), one);// if score of tweet is less than zero and negative probability is greater than positive probability then classify as a negative tweet
	    	  else if(score<0 && posProbab>=negProbab)
	    		  output.collect(new Text("negative"), one);
	    	  else if(score >0 && posProbab<=negProbab )
	    		  output.collect(new Text("positive"), one);	    	  
	    	  else
	    		  output.collect(new Text("neutral"), one);	  //Classify the tweet as neutral if it does not fall in any of the conditions above.      
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
                	//System.out.println("Negative hahshmap loop:"+ line1);	
                	negativeWordHashMap.put(line1.toString(), 1);
                        //System.out.println(line);
                        line1=nbr.readLine();
                }
        }catch(Exception e){
        }
	    	
	    	FileSystem fs2 = FileSystem.get(new Configuration());
	    	try{
                Path pt2=new Path("/user/hadoop/dir1/wordFreq.txt");//Path of text file that contains all the frequency of words from naive bayes learner
                System.out.println("In word freq map");
                
                BufferedReader wordFreqbr=new BufferedReader(new InputStreamReader(fs2.open(pt2)));
                String line2;
                line2=wordFreqbr.readLine();
                while (line2 != null){
                	 System.out.println("In word freq map count");
                	 System.out.println(line2);
                		String[] word=line2.split(" ");
                		if(word.length>1)
                		{
                			//System.out.println("Length: "+ word.length);
                			//System.out.println("WORDs are "+ word[0].toString() + " and "+ Integer.parseInt(word[1] ));
                    		double temp=(double)Integer.parseInt(word[1]);
                			wordFreqHashMap.put(word[0].toString(), temp);
                		}
                		else
                		{
                			//System.out.println("Length: "+ word.length);
                			//System.out.println("WORDs are "+ word[0].toString() );
                    		wordFreqHashMap.put(word[0].toString(), 0.0);
                		}
                		              		
                       
                        line2=wordFreqbr.readLine();
                }
        }catch(Exception e){
        }
	    	
	    	
	    	
	    	
	    	
	    	try {
				JobConf conf = new JobConf(SentimentAnalysis.class);//setting Sentiment Analysis class as job conf.
			     conf.setJobName("SentimentAnalysis");
			     System.out.println("Starting Map Reduce Algorithm");
			     conf.setOutputKeyClass(Text.class);//setting the output as text file
			     conf.setOutputValueClass(IntWritable.class);
			
			     conf.setMapperClass(Map.class);//Setting the Map class
			     conf.setCombinerClass(Reduce.class);//Setting the combiner class
			     conf.setReducerClass(Reduce.class);//Setting the reducer class
			
			     conf.setInputFormat(TextInputFormat.class);
			     conf.setOutputFormat(TextOutputFormat.class);
			  
			     FileInputFormat.setInputPaths(conf, new Path("/user/hadoop/dir2/"));// Setting the path to look for input file
			     FileOutputFormat.setOutputPath(conf, new Path(args[0]));
			
			     JobClient.runJob(conf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}