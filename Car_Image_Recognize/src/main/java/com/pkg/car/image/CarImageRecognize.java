package com.pkg.car.image;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public class CarImageRecognize {

    public static void main(String[] args) {
    	
    	Region region = Region.US_EAST_1;
    	
        String bucket = "njit-cs-643";
        String queue = "CS643.fifo";
        String group = "Mygroup";

        S3Client s3Client = S3Client.builder().region(region).build();
        RekognitionClient rClient = RekognitionClient.builder().region(region).build();
        SqsClient sqsClient = SqsClient.builder().region(region.US_EAST_1).build();

        ImageProcessingLogic.processImagesWithCar(s3Client, rClient, sqsClient, bucket, queue, group);
    }

    
}
