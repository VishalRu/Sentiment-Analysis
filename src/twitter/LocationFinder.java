package twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
	/*
	 * This Program will find the location of positive,negative and neutral tweets and determine which state they have originated from. 
	 * 
	 */
	public class LocationFinder {
	  public static final String positiveWordFile= "positive-words.txt";
	  public static final String negativeWordFile= "negative-words.txt";
	  public static HashMap<String, Integer> positiveWordHashMap=new HashMap<String,Integer>();// HashMap to hold all the positive words
	  public static HashMap<String, Integer> negativeWordHashMap=new HashMap<String,Integer>();// HashMap to hold all the negative words
	  public static HashMap<String, Double> wordFreqHashMap=new HashMap<String,Double>();//HashMap to hold the frequency of words from naive bayes learner
	  public static double positiveTwets= 43957; // number of positive tweets from training data set
	  public static double negativeTweets=16853;// number of negative tweets from training data set
	  public static double totalTweets=11912679; // total number of tweets from training data set
	  public static double probabPositive=positiveTwets/totalTweets;
	  public static double probabNegative=negativeTweets/totalTweets;
	  public static ArrayList<String> states=new ArrayList<String>();
		
		public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
	      private final static IntWritable one = new IntWritable(1);
	     
	      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
		       
	    	  String line=value.toString();
	    	 // System.out.println("Line is :"+ line);
	    	  String[] tweetWords=line.split(" ");// Splitting the words of tweet on the basis of spaces
	    	  Random r = new Random();
	    	  String location=states.get(r.nextInt(states.size()));//Find the origin of tweet
	        //StringTokenizer tokenizer = new StringTokenizer(line);
	    	  int score=0;
	    	  double posProbab=1.0;//initial probability
	    	  double negProbab=1.0;
	    	  for(int i=0;i<tweetWords.length;i++)
	    	  {
	    		  String currentWord=tweetWords[i];
	    		  if(positiveWordHashMap.containsKey(currentWord))
	    		  {
	    			  score++;increase the score of tweet if it contains the positive word
	    			  if(wordFreqHashMap.containsKey(currentWord))
	    			  {
	    				  posProbab*=wordFreqHashMap.get(currentWord)/positiveTwets;// compute the probability of positive tweet given a word
	    			  }
	    			  
	    		  }
	    		  if(negativeWordHashMap.containsKey(currentWord))
	    		  {
	    			  score--;decrease the score of tweet if it contains the negative word
	    			  if(wordFreqHashMap.containsKey(currentWord))
	    			  {
	    				  negProbab*=wordFreqHashMap.get(currentWord)/negativeTweets;// compute the probability of negative tweet given a word
	    			  }
	    		  }
	    		  else
	    			  continue;
	    		  
	    	  }
	    	  posProbab*=probabPositive;
	    	  negProbab*=probabNegative;
	    	  
	    	  // On the basis of score and probability  classify  tweet as positive/negative/neutral with its location	    	 
	    	  if(score>0 && posProbab>negProbab)  
	    		  output.collect(new Text(location+"-positive"),one);
	    	  else if (score<0 && posProbab<negProbab )
	    		  output.collect(new Text(location+"-negative"),one);
	    	  else if(score<0 && posProbab>=negProbab)
	    		  output.collect(new Text(location+"-negative"),one);
	    	  else if(score >0 && posProbab<=negProbab )
	    		  output.collect(new Text(location+"-positive"),one);	    	  
	    	  else
	    		  output.collect(new Text(location+"-neutral"),one);       
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
                           line1=nbr.readLine();
                }
        }catch(Exception e){
        }
	    	
	    	FileSystem fs2 = FileSystem.get(new Configuration());
	    	try{
                Path pt2=new Path("/user/hadoop/dir1/wordFreq.txt");
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
                    		double temp=(double)Integer.parseInt(word[1]);
                			wordFreqHashMap.put(word[0].toString(), temp);
                		}
                		else
                		{
                			wordFreqHashMap.put(word[0].toString(), 0.0);
                		}
                		              		
                       
                        line2=wordFreqbr.readLine();
                }
        }catch(Exception e){
        }
	    	 //Setting all the states of United States
	    	  states.add("Alabama");
	            states.add("Alaska");
	            states.add("Arizona");
	            states.add("Arkansas");
	            states.add("California");
	            states.add("Colorado");
	            states.add("Connecticut");
	            states.add("Delaware");
	            states.add("Florida");
	            states.add("Georgia");
	            states.add("Hawaii");
	            states.add("Idaho");
	            states.add("Illinois");
	            states.add("Indiana");
	            states.add("Iowa");
	            states.add("Kansas");
	            states.add("Kentucky");
	            states.add("Louisiana");
	            states.add("Maine");
	            states.add("Maryland");
	            states.add("Massachusetts");
	            states.add("Michigan");
	            states.add("Minnesota");
	            states.add("Mississippi");
	            states.add("Missouri");
	            states.add("Montana");
	            states.add("Nebraska");
	            states.add("Nevada");
	            states.add("New Jersey");
	            states.add("New Hampshire");
	            states.add("New Jersey");
	            states.add("New Mexico");
	            states.add("New York");
	            states.add("North Carolina");
	            states.add("North Dakota");
	            states.add("Ohio");
	            states.add("Oklahoma");
	            states.add("Oregon");
	            states.add("Pennsylvania");
	            states.add("Rhode Island");
	            states.add("South Carolina");
	            states.add("South Dakota");
	            states.add("Tennessee");
	            states.add("Texas");
	            states.add("Utah");
	            states.add("Vermont");
	            states.add("Virginia");
	            states.add("Washington");
	            states.add("West Virginia");
	            states.add("Wisconsin");
	            states.add("Wyoming");	    	
	    	
	    	try {
				JobConf conf = new JobConf(LocationFinder.class);
			     conf.setJobName("LocationFinder");
			     System.out.println("Starting Map Reduce Algorithm");
			     conf.setOutputKeyClass(Text.class);
			     conf.setOutputValueClass(IntWritable.class);
			
			     conf.setMapperClass(Map.class);
			     conf.setCombinerClass(Reduce.class);
			     conf.setReducerClass(Reduce.class);
			
			     conf.setInputFormat(TextInputFormat.class);
			     conf.setOutputFormat(TextOutputFormat.class);
			  
			     FileInputFormat.setInputPaths(conf, new Path("/user/hadoop/dir2/"));
			     FileOutputFormat.setOutputPath(conf, new Path(args[0]));
			
			     JobClient.runJob(conf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}