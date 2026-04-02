package cn.juntaozhang.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.file.Paths;

public class S3RustFSExample {
    public static void main(String[] args) {
        // 1. 创建客户端
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:32000"))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "11111111")))
                .forcePathStyle(true)
                .build()) {

            String bucket = "test1";
            String key = "hello.txt";

            // 2. 建桶（忽略已存在）
            try {
                s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignore) {
            }

            // 3. 上传文件
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), Paths.get("hello.txt"));
            System.out.println("uploaded hello.txt");

            // 4. 下载文件
            s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(),
                    Paths.get("downloaded-hello.txt"));
            System.out.println("downloaded to downloaded-hello.txt");

            // 5. 列出对象
            ListObjectsV2Response list = s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).build());
            list.contents().forEach(obj -> System.out.println("object: " + obj.key()));

            // 6. 删除对象 & 桶（可选）
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build());
        }
    }
}