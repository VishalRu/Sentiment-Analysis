Sentiment-Analysis
==================

Sentiment Analysis of Twitter users during release of Apple iPhone 6

In this modern and technical era, world is so connected by platforms like Facebook and Twitter. 
These are social platforms used world over by nearly 541 million users who maintain a steady stream of information.
Various users express their opinions, their viewpoints about numerous products and important events.
These opinions and viewpoints can be captured and can be used to observe the sentiments of various users 
and then can be used for further purposes. In this project, we have captured various tweets about new iPhone 6 
during the release of new iPhone 6. We have captured the data before its release, on the day of its release and 
certain days after its release, so we can analyze the sentiments of users during that time interval. 
We have fetched those tweets using twitter API’s. We stored all these tweets on Hadoop distributed file system. 
We chose Hadoop and map reduce since the size of data was very large. 

We used two approaches to analyse the sentiment of twitter users:

1.	Simple Lexical Token Classification:  In this approach , we breaks each tweet in an array of words, now we check 
all those words against two hash maps which contain positive words and negative words. 
If we find a positive word then we increment the score of tweet by one and if we encounter a negative word
then we decrement the score. Once, all the words in tweet are checked then we check the score of tweet.
If the score of tweet is greater than zero then we can classify that tweet as positive and if the score of tweet is 
less then zero then we classify that tweet as negative and neutral otherwise.
However, the problem with above approach is that we will not be able to find out which word influences 
the positivity and negativity of a tweet by what percentage. 
A particular word would have been used in more positive tweets hence every time it comes in  a tweet, 
there are more chances that a tweet will be positive. Hence, we need to implement an approach that 
will take care of this fact. 

2.	Naïve Bayes Classification:  This is a probabilistic method to determine which word in a tweet contributed to 
the positive-ness and negative-ness of an individual tweet.  

Now, if the score of tweet is greater than zero and its positive probability is greater than negative probability
then we will classify the tweet as positive. We have used the same approach to classify as negative tweet as well.
Those tweets, which do not fall under those two categories, will be classified as neutral. 
Both these implementation are being written in Map reduce framework so we can easily execute these
algorithms on a huge data set quickly. Also, writing these algorithms in map reduce enabled us to 
scale our infrastructure at anytime.  


	DESCRIPTION OF RESULTS

We executed our map-reduce program on the data set, which was gathered from different days. 
And, below mentioned table shows the positive, negative and neutral count of tweets.	

From table 1, we can easily deduce that there was more number of positive tweets in comparison to
negative tweets since more number of users liked the features in new iPhone 6. However, 
if we analyze the difference between positive and negative tweets after the day of its release
we will observe a decline in that difference. On the basis of keywords, which were captured in negative tweets, 
twitter users felt that iPhone 6 is expensive. However, positive tweets started to dominate negative tweets 
from Sep 19 onwards since Apple started shipping their new iPhone on this date. More neutral tweets came 
due to the fact that we were operating on the noisy data. 
Such data enables us to perform statistical analysis on data to gain more insight about a trend and 
then using that trend to predict market shares of any firm.  


Table1: Count of Positive, Negative and Neutral tweets at various intervals.
 
Date	        Neutral		 Negative  	Positive    % difference between Positive and Negative
09/08/2014	  7663         	  500         	842         	25%
09/09/2014	  1705878	  62043	        203004		 53%
09/10/2014	  740936	  21842	        67418	        51%
09/12/2014	  780455	  29700	        49297	        25%
09/13/2014	  99771	    	   3499	        4744	        15%
09/18/2014	  2518490	  347924      342623      	1%
09/19/2014	  1606683	  112927      125224		 5%
09/20/2014  	  1059099	  38039	        74383	        33%
09/21/2014	  294910  	  10537       	26667	        43%



