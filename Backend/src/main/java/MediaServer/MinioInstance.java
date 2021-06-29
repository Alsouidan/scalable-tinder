package MediaServer;

import Config.Config;
import com.arangodb.ArangoDB;
import io.minio.*;
import io.minio.errors.*;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
public class MinioInstance {
    private Config conf = Config.getInstance();
    private String minioSecretKey = conf.getMinioSecretKey();
    private String minioAccessKey=conf.getMinioAccessKey();
    private String minioPort=conf.getMinioPort();
    private String minioHost=conf.getMinioHost();
    private String minioBucketName=conf.getMinioBucketName();
    private String minioHost=conf.getMinioHost();
    private MinioClient minioClient;

    public MinioInstance(){

            try {
                // Create a minioClient with the MinIO server playground, its access key and secret key.
                 minioClient =
                        MinioClient.builder()
                                .endpoint("http://"+minioHost+":"+minioPort)
                                .credentials(minioAccessKey,minioSecretKey )
                                .build();

                // Make ' bucket if not exist.
                boolean found =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucketName).build());
                if (!found) {
                    // Make a new bucket called 'asiatrip'.
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucketName).build());
                } else {
                    System.out.println("Bucket  already exists.");
                }

            } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                System.out.println("Error occurred: " + e);
                System.out.println("HTTP trace: " );
            }
        }
           public String uploadFile(String path,String name){
               try {
                   ObjectWriteResponse res=minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(minioBucketName)
                                    .object(name)
                                    .filename(path)
                                    .build());
                   return name;
               } catch (ErrorResponseException e) {
                   e.printStackTrace();
               } catch (InsufficientDataException e) {
                   e.printStackTrace();
               } catch (InternalException e) {
                   e.printStackTrace();
               } catch (InvalidKeyException e) {
                   e.printStackTrace();
               } catch (InvalidResponseException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                   e.printStackTrace();
               } catch (NoSuchAlgorithmException e) {
                   e.printStackTrace();
               } catch (ServerException e) {
                   e.printStackTrace();
               } catch (XmlParserException e) {
                   e.printStackTrace();
               }
               return null;
           }
    public String uploadFile(byte[] data,String fileName){
        String fileType="image";
        UUID uuid = UUID.randomUUID();
        String name = uuid.toString();
        String extension=FilenameUtils.getExtension(fileName);
        name=name+"."+extension;
        switch(extension.toLowerCase()){
            case "jpg":
            case "png":
            case "jpeg":
            case "bmp": fileType="image";break;
            case"avi":
            case "mp4":
            case "mkv":fileType="video";
        }
        try {
            String contentType="image/"+extension;
            if(fileType.equals("video")){

                contentType="video/"+extension;
            }

            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioBucketName).object(name).stream(
                            new ByteArrayInputStream(data), -1, 10485760)
                            .contentType(contentType)
                            .build());
            return name;
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte [] downloadFile(String name){

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(name)
                            .build());

            return stream.readAllBytes();
        }
            // Read data from stream

     catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        MinioInstance minio=new MinioInstance();
        byte[] downloadedFile= minio.downloadFile("f0eac2c4-2a83-4823-83d0-ebed6a4f41f2.png");
        File fileToWrite=new File("/home/vm/Desktop/Scalable/test");
        FileOutputStream fos = new FileOutputStream(fileToWrite); 
        try  {
            fos.write(downloadedFile);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            fos.close();
        }
        
    }
}