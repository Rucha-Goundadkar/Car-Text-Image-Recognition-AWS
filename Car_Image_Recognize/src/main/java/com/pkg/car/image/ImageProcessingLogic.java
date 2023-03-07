package com.pkg.car.image;

import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class ImageProcessingLogic {
	
	public static void processImagesWithCar(S3Client s3Client, RekognitionClient rClient, SqsClient sqsClient, String bucket,String queue, String group) 
	{

        String url = "";
        try 
        {
            ListQueuesRequest queueRequest = ListQueuesRequest.builder().queueNamePrefix(queue).build();
            ListQueuesResponse queueRespose = sqsClient.listQueues(queueRequest);

            if (queueRespose.queueUrls().size() == 0) 
            {
                CreateQueueRequest request = CreateQueueRequest.builder().attributesWithStrings(Map.of("FifoQueue", "true", "ContentBasedDeduplication", "true")).queueName(queue).build();
                sqsClient.createQueue(request);

                GetQueueUrlRequest getURL = GetQueueUrlRequest.builder().queueName(queue).build();
                url = sqsClient.getQueueUrl(getURL).queueUrl();
            } 
            else 
            {
            	url = queueRespose.queueUrls().get(0);
            }
        } 
        catch (QueueNameExistsException e)
        {
            e.printStackTrace();
        }

        try 
        {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).maxKeys(10).build();
            ListObjectsV2Response listObjectResponse = s3Client.listObjectsV2(listObjectsRequest);

            for (S3Object obj : listObjectResponse.contents()) 
            {
                System.out.println("Processing image: " + obj.key());

                Image image = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder().bucket(bucket).name(obj.key()).build()).build();
                DetectLabelsRequest request = DetectLabelsRequest.builder().image(image).minConfidence((float) 90).build();
                DetectLabelsResponse output = rClient.detectLabels(request);
                List<Label> l = output.labels();

                for (Label label : l) 
                {
                    if (label.name().equals("Car"))
                    {
                    	sqsClient.sendMessage(SendMessageRequest.builder().messageGroupId(group).queueUrl(url).messageBody(obj.key()).build());
                        break;
                    }
                }
            }

            sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(url).messageGroupId(group).messageBody("-1").build());
        } 
    catch (Exception e) 
        {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

}
