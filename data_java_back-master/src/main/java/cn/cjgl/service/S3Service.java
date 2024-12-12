package cn.cjgl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    /**
     * Uploads a file to AWS S3
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) under which to store the file
     * @param file The file to upload
     * @throws IOException If there's an error reading or uploading the file
     */
    public void saveFile2aws(String bucketName, String keyName, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build(),
                    RequestBody.fromInputStream(fis, file.length())
            );
        }
    }

    /**
     * Uploads byte array data to AWS S3
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) under which to store the data
     * @param bytes The byte array to upload
     * @throws IOException If there's an error during upload
     */
    public void saveBytes2aws(String bucketName, String keyName, byte[] bytes) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(bytes);

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .build(),
                RequestBody.fromInputStream(inputStream,bytes.length));
    }

    /**
     * Uploads a JSON string to AWS S3
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) under which to store the JSON
     * @param json The JSON string to upload
     * @throws IOException If there's an error during upload
     */
    public void saveString2aws(String bucketName, String keyName, String json) throws IOException {
        byte[] data=json.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(data);

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .build(),
                RequestBody.fromInputStream(inputStream,data.length));
    }

//    public String loadStringfromAws(String bucketName, String keyName) throws IOException {
//        try {
//            ResponseInputStream responseInputStream = s3Client.getObject(GetObjectRequest.builder().build().builder()
//                    .bucket(bucketName)
//                    .key(keyName)
//                    .build());
//            BufferedReader reader = new BufferedReader(new InputStreamReader(responseInputStream, StandardCharsets.UTF_8));
//            return reader.lines()
//                    .collect(Collectors.joining()); // 使用Collectors.joining将行连接成一个字符串，并添加换行符作为分隔符
//        } catch (Exception e) {
//            // 处理异常，例如记录错误日志
//            e.printStackTrace();
//            throw new RuntimeException("Failed to read string from S3", e);
//        }
//    }

    /**
     * Retrieves a string content from AWS S3
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) of the object to retrieve
     * @return The content as a string
     * @throws IOException If there's an error reading from S3
     */
    public String getStringFromAws(String bucketName, String keyName) throws IOException {
        ResponseInputStream inputStream  = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());
        return inputStream2String(inputStream);
    }

    /**
     * Downloads a file from AWS S3 to a local destination
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) of the object to download
     * @param destinationPath The local path where the file should be saved
     * @throws IOException If there's an error during download or saving
     */
    public void getFileFromAws(String bucketName, String keyName, String destinationPath) throws IOException {
        ResponseInputStream inputStream  = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());
        Path dest = Paths.get(destinationPath);
        saveToFile(inputStream, dest.toString());;
    }

    /**
     * Deletes an object from AWS S3
     * @param bucketName The name of the S3 bucket
     * @param keyName The key (path) of the object to delete
     */
    public void deleteFile(String bucketName, String keyName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());
    }

    /**
     * Converts an InputStream to a String
     * @param inputStream The input stream to convert
     * @return The resulting string, or empty string if conversion fails
     */
    private String inputStream2String(InputStream inputStream){

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // Convert a byte array to a string
            String result = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());

            return result;

        } catch (IOException e) {
            e.printStackTrace();
            // Handle IOException, such as logging or re-throwing the exception
        } finally {
            // Ensure that the InputStream is closed to prevent resource leakage.
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle IOException, such as logging
                }
            }
        }
        return "";
    }

    /**
     * Saves the content of an InputStream to a file
     * @param inputStream The input stream to read from
     * @param filePath The path where the file should be saved
     * @throws IOException If there's an error writing to the file
     */
    private static void saveToFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
