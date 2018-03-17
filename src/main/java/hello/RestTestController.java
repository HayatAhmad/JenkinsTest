package hello;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RestTestController {

	@RequestMapping("/loginTest")
    public String login(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey,
                                @RequestParam(value = "secretkey") String secretKey, HttpServletResponse response){

        try {
        	MinioClient minioClient = new MinioClient(hosts, accessKey, secretKey);      
        	minioClient.listBuckets();
            response.setStatus(HttpServletResponse.SC_OK);          
            return "Ok";

        }catch(MinioException ex) {
        	System.out.println("Error occurred: " + ex);
        	  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }
	
    @RequestMapping("/createBucket")
    public String createBucket(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey,
                                @RequestParam(value = "secretkey") String secretKey, @RequestParam(value="bucket") String bucket , HttpServletResponse response){
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(hosts, accessKey, secretKey);
            boolean isBucketExist = minioClient.bucketExists(bucket);

            if(!isBucketExist){
                minioClient.makeBucket(bucket);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            return "bucket created";

        }catch(MinioException ex) {
        	System.out.println("Error occurred: " + ex);
        	  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "bucket creation failed";
        }
        catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "bucket creation failed";
        }
    }

    @RequestMapping("/fileUpload")
    public String uploadFile(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey, @RequestParam(value = "secretkey") String secretKey, 
    		@RequestParam(value="bucket") String bucket, @RequestParam(value="filename")String filename, HttpServletResponse response){
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(hosts, accessKey, secretKey);
            boolean isBucketExist = minioClient.bucketExists(bucket);

            if(!isBucketExist){
            	response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return "bucket doesn't exist";
            }
                      
           Path file = Paths.get(filename);
           List<String> lines = Arrays.asList("The is a test file");
           Files.write(file, lines, Charset.forName("UTF-8"));
            
           minioClient.putObject(bucket, filename, filename);
           response.setStatus(HttpServletResponse.SC_OK);
           return "file created";
            

        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "file creation failed";
        }
    }
    
    @RequestMapping("/checkFileExist")
    public String checkFile(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey, @RequestParam(value = "secretkey") String secretKey, 
    		@RequestParam(value="bucket") String bucket, @RequestParam(value="filename") String filename,  HttpServletResponse response){
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(hosts, accessKey, secretKey);
            boolean isBucketExist = minioClient.bucketExists(bucket);

            if(!isBucketExist){
            	response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            	return "bucket doesn't exist";
            }
           
           Iterable<Result<Item>> items = minioClient.listObjects(bucket);
           
           for (Result<Item> item : items) {
               if(item.get().objectName().equalsIgnoreCase(filename)) {
            	   response.setStatus(HttpServletResponse.SC_OK);
                   return String.format("file %s found", filename);
               }         	    
           }
                              
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "file not found";
        
    }
    
    @RequestMapping("/removeFile")
    public String removeFile(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey, @RequestParam(value = "secretkey") String secretKey, 
    		@RequestParam(value="bucket") String bucket, @RequestParam(value="filename") String filename,  HttpServletResponse response){
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(hosts, accessKey, secretKey);
            boolean isBucketExist = minioClient.bucketExists(bucket);

            if(!isBucketExist){
            	response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            	return "bucket doesn't exist";
            }
           
           Iterable<Result<Item>> items = minioClient.listObjects(bucket);
           
           for (Result<Item> item : items) {
               if(item.get().objectName().equalsIgnoreCase(filename)) {
            	   minioClient.removeObject(bucket, filename);
            	   response.setStatus(HttpServletResponse.SC_OK);
                   return String.format("file %s deleted", filename);
               }         	    
           }
                              
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        return "file not found";      
    }
    
    @RequestMapping("/removeBucket")
    public String removeFile(@RequestParam(value = "host") String hosts,@RequestParam(value = "accesskey") String accessKey, @RequestParam(value = "secretkey") String secretKey, 
    		@RequestParam(value="bucket") String bucket,  HttpServletResponse response){
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(hosts, accessKey, secretKey);
            boolean isBucketExist = minioClient.bucketExists(bucket);

            if(isBucketExist){
            	minioClient.removeBucket(bucket);
            	response.setStatus(HttpServletResponse.SC_OK);
            	return String.format("Bucket %s removed", bucket);
            }
           
                              
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        return "file not found";      
    }

    @RequestMapping("/minio_test")
    public List<String> method1(@RequestParam(value = "host") String hosts,
            @RequestParam(value = "accesskey") String accessKey, @RequestParam(value = "secretkey") String secretKey,
            @RequestParam(value = "bucket") String bucket) {
        try {
            List<String> objectList = new ArrayList<>();
            MinioClient minioClient = new MinioClient(hosts, accessKey, secretKey);
            Iterable<Result<Item>> items = minioClient.listObjects(bucket);
            for (Result<Item> item : items)
                objectList.add(item.get().objectName());
            return objectList;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
        }
        return null;
    }

}
