package com.team012.server.common.aws.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.team012.server.posts.entity.Posts;
import com.team012.server.posts.img.entity.PostsImg;
import com.team012.server.review.entity.ReviewImg;
import com.team012.server.common.aws.utils.CommonUtils;
import com.team012.server.review.repository.ReviewImgRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;

    private final ReviewImgRepository reviewImgRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 단일 파일 업로드
     */

    public String singleUploadFile(MultipartFile multipartFile) {


        String fileName = originalFileName(multipartFile);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {

            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

        } catch (IOException e) {
            log.error("Can Not Upload Image..!", e);
            throw new RuntimeException();
        }
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    //s3로 파일 업로드 하고 url 리턴하는 메서드

    public String uploadFile(MultipartFile multipartFile) throws IOException {

        String fileName = originalFileName(multipartFile);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        try (InputStream inputStream = multipartFile.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            objectMetadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, byteArrayInputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (Exception e) {
            log.error("Can not upload image, ", e);
            throw new FileUploadException();
        }
        String url = amazonS3Client.getUrl(bucketName, fileName).toString();

        return url;
    }

    @Transactional(readOnly = true)
    //파일 이름 추출하는 메서드
    public String originalFileName(MultipartFile multipartFile) {
        try {
            return CommonUtils.buildFileName(multipartFile.getOriginalFilename());
        } catch (Exception e) {
            log.error("Can not filename, ", e);
            return "filename";
        }

    }

    // review 이미지 업로드

    public List<ReviewImg> convertReviewImg(List<MultipartFile> multipartFileList) {
        if (multipartFileList.size() > 3) throw new RuntimeException("사진 파일 갯수 초과");

        List<ReviewImg> fileList = new ArrayList<>();
        multipartFileList.forEach(file -> {
            String url;
            try {
                url = uploadFile(file);
            } catch (IOException e) {
                log.error("Can not upload, ", e);
                throw new RuntimeException(e);
            }
            String fileName = originalFileName(file);

            ReviewImg reviewImg = ReviewImg.builder()
                    .fileName(fileName)
                    .imgUrl(url)
                    .build();

            reviewImgRepository.save(reviewImg);

            fileList.add(reviewImg);
        });

        return fileList;
    }

    // review 이미지 업데이트

    public List<ReviewImg> updateReviewImg(List<ReviewImg> reviewImgList, List<MultipartFile> multipartFileList) {
        if (multipartFileList.size() > 3) throw new RuntimeException("사진 파일 갯수 초과");

        // 기존에 있있던 파일이름 불러오기
        List<String> list = reviewImgList.stream().map(ReviewImg::getFileName).collect(Collectors.toList());
        for (String file : list) {
            if (!"".equals(file) && file != null) {
                boolean isExistObject = amazonS3Client.doesObjectExist(bucketName, file);

                // 파일이 있으면 삭제
                if (isExistObject)  {
                    DeleteObjectRequest request = new DeleteObjectRequest(bucketName, file);
                    amazonS3Client.deleteObject(request);
                }

            }
        }

        // 삭제후 새로운 이미지 업로드
        return convertReviewImg(multipartFileList);
    }

    //file 이름과 url 받아서 List<PostsImg> 로 리턴하는 메서드

    public List<PostsImg> convertPostImg(List<MultipartFile> multipartFileList, Posts posts) {
        if (multipartFileList.size() != 5) {
            throw new RuntimeException("you need to upload 5 image");
        }
        List<PostsImg> fileList = new ArrayList<>();

        for(MultipartFile file: multipartFileList) {
            String url;
            try {
                url = uploadFile(file);
            } catch (IOException e) {
                log.error("Can not upload, ", e);
                throw new RuntimeException(e);
            }
            String fileName = originalFileName(file);

            PostsImg postsImg = PostsImg.builder()
                    .fileName(fileName)
                    .imgUrl(url)
                    .posts(posts)
                    .build();
            fileList.add(postsImg);
        }
        return fileList;
    }



    //s3에 업로드된 파일 삭제 메서드
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteFile(List<PostsImg> imgList) {
        List<String> fileNameList = imgList.stream().map(PostsImg::getFileName).collect(Collectors.toList());
        for (String file : fileNameList) {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, file));
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, String> getImgUrlAndFilename(List<MultipartFile> multipartFile) throws FileUploadException {
        Map<String, String> filenameAndUrl  = new HashMap<>();

        for (MultipartFile file : multipartFile) {
            String fileName = originalFileName(file);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            try (InputStream inputStream = file.getInputStream()) {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                objectMetadata.setContentLength(bytes.length);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, byteArrayInputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (Exception e) {
                log.error("Can not upload image, ", e);
                throw new FileUploadException();
            }
            String url = amazonS3Client.getUrl(bucketName, fileName).toString();
            filenameAndUrl.put(fileName, url);
        }
        return filenameAndUrl;


    }
}
