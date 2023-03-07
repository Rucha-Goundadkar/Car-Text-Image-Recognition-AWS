package com.pkg.car.text;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.rekognition.model.TextTypes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

public class TextProcessingLogic {
	
	  public static void processCarImagesWithText(S3Client s3Client, RekognitionClient rClient, SqsClient sqsClient, String bucket,String queue) 
	    
	    {    
	        boolean flag = false;
	        while (!flag) 
	        {
	            ListQueuesRequest list = ListQueuesRequest.builder().queueNamePrefix(queue).build();
	            ListQueuesResponse responseQueue = sqsClient.listQueues(list);
	            if (responseQueue.queueUrls().size() > 0)
	            {
	            	flag = true;
	            }	
	        }
	      
	        String url = "";
	        try 
	        {
	            GetQueueUrlRequest getURL = GetQueueUrlRequest.builder().queueName(queue).build();
	            url = sqsClient.getQueueUrl(getURL).queueUrl();
	        } 
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	        }

	        
	        try 
	        {
	            boolean emptyQueue = false;
	            HashMap<String, String> cars = new HashMap<String, String>();

	            while (!emptyQueue) 
	            {
	                
	                ReceiveMessageRequest msgObject = ReceiveMessageRequest.builder().queueUrl(url).maxNumberOfMessages(1).build();
	                List<Message> msg = sqsClient.receiveMessage(msgObject).messages();

	                if (msg.size() > 0) 
	                {
	                    Message message = msg.get(0);
	                    String index = message.body();

	                    if (index.equals("-1")) 
	                    {
	                       System.out.println("Queue is empty !");	
	                       emptyQueue = true;
	                    } 
	                    else 
	                    {
	                        System.out.println("Processing car image with text " + index);

	                        Image img = Image.builder().s3Object(S3Object.builder().bucket(bucket).name(index).build()).build();
	                        DetectTextRequest request = DetectTextRequest.builder().image(img).build();
	                        DetectTextResponse result = rClient.detectText(request);
	                        List<TextDetection> td = result.textDetections();

	                        if (td.size() != 0) 
	                        {
	                            String text = "";
	                            for (TextDetection i : td) 
	                            {
	                                if (i.type().equals(TextTypes.WORD))
	                                    text = text.concat(" " + i.detectedText());
	                            }
	                            cars.put(index, text);
	                        }
	                    }

	                    
	                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(url).receiptHandle(message.receiptHandle()).build();
	                    sqsClient.deleteMessage(deleteMessageRequest);
	                }
	            }
	            try 
	            {
	                FileWriter fw = new FileWriter("output.txt");

	                Iterator<Map.Entry<String, String>> iterator = cars.entrySet().iterator();
	                while (iterator.hasNext()) 
	                {
	                    Map.Entry<String, String> pair = iterator.next();
	                    fw.write(pair.getKey() + ":" + pair.getValue() + "\n");
	                    System.out.println("\n");
	                    iterator.remove();
	                }

	                fw.close();
	                System.out.println("output.txt file is created successfully !");
	            } 
	            catch (IOException e) 
	            {
	                System.out.println("Error in creating output.txt file !");
	                e.printStackTrace();
	            }
	        } 
	     catch (Exception e) 
	        {
	            System.err.println(e.getLocalizedMessage());
	            System.exit(1);
	        }
	    }

}
