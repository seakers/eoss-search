package eoss.aws;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqsWrapper {




    public static String vassar_queue_url  = "http://localhost:4576/queue/vassar_queue";
    public static String endpoint_override = "http://172.18.0.8:4576";
    public static Region region = Region.US_EAST_2;



    public static SqsClient createSqsClient(){

        SqsClient sqsClient = SqsClient.builder()
                .region(SqsWrapper.region)
                .endpointOverride(URI.create(SqsWrapper.endpoint_override))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        return sqsClient;

    }

    public static String createQueue(String queue_name){
        Map<String, String> queueParams = new HashMap<>();
        queueParams.put("type", "add_eval_return");

        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queue_name)
                .tags(queueParams)
                .build();

        SqsClient sqsClient = SqsWrapper.createSqsClient();
        sqsClient.createQueue(createQueueRequest);
        GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queue_name).build());
        String queue_url = getQueueUrlResponse.queueUrl();
        return queue_url;
    }

    public static boolean purgeQueue(String queue_url){
        System.out.println("---> PURGE QUEUE: " + queue_url);
        // App.sleep(2);

        PurgeQueueRequest purgeQueueRequest = PurgeQueueRequest.builder()
                .queueUrl(queue_url)
                .build();

        SqsClient sqsClient = SqsWrapper.createSqsClient();
        sqsClient.purgeQueue(purgeQueueRequest);
        return true;
    }

    public static void sendEvalMessage(Map<String, MessageAttributeValue> message_attributes){
        SqsClient sqs = SqsWrapper.createSqsClient();

        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(SqsWrapper.vassar_queue_url)
                .messageBody("")
                .messageAttributes(message_attributes)
                .delaySeconds(0)
                .build());
    }

    public static HashMap<String, String> getEvalMessageResponse(String return_url, String design_UUID, int wait_time){
        SqsClient sqs = SqsWrapper.createSqsClient();

        HashMap<String, String> results = new HashMap<>();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(return_url)
                .waitTimeSeconds(wait_time)
                .maxNumberOfMessages(1)
                .attributeNames(QueueAttributeName.ALL)
                .messageAttributeNames("All")
                .build();
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).messages();
        for(Message message: messages){
            HashMap<String, String> msg_attributes = SqsWrapper.processMessage(message);
            if(msg_attributes.containsKey("UUID")){
                if(design_UUID.contains(msg_attributes.get("UUID")) || msg_attributes.get("UUID").contains(design_UUID)){

                    // DELETE ALL MESSAGES
                    SqsWrapper.deleteMessages(messages, return_url);

                    // RETURN MESSAGE ATTRIBUTES
                    return msg_attributes;
                }
                else{
                    System.out.println("----> UUID DOES NOT MATCH");
                    System.out.println(msg_attributes.get("UUID"));
                    System.out.println(design_UUID);
                }
            }
        }
        return results;
    }

    private static HashMap<String, String> processMessage(Message msg){
        HashMap<String, String> contents = new HashMap<>();
        contents.put("body", msg.body());
        System.out.println("\n--------------- SQS MESSAGE ---------------");
        System.out.println("--------> BODY: " + msg.body());
        for(String key: msg.messageAttributes().keySet()){
            contents.put(key, msg.messageAttributes().get(key).stringValue());
            if(!key.equals("design")){
                System.out.println("---> ATTRIBUTE: " + key + " - " + msg.messageAttributes().get(key).stringValue());
            }
        }
        System.out.println("-------------------------------------------\n");
        // App.sleep(2);
        return contents;
    }

    public static void deleteMessages(List<Message> messages, String url){
        SqsClient sqs = SqsWrapper.createSqsClient();
        for (Message message : messages) {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(url)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqs.deleteMessage(deleteMessageRequest);
        }
    }


}
