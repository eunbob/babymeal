package com.example.devkorproject.post.service;


import com.example.devkorproject.alarm.entity.AlarmEntity;
import com.example.devkorproject.alarm.repository.AlarmRepository;
import com.example.devkorproject.alarm.service.AlarmService;
import com.example.devkorproject.auth.jwt.JwtUtil;
import com.example.devkorproject.common.constants.ErrorCode;
import com.example.devkorproject.common.exception.GeneralException;
import com.example.devkorproject.customer.entity.CustomerEntity;
import com.example.devkorproject.customer.repository.CustomerRepository;
import com.example.devkorproject.post.dto.*;
import com.example.devkorproject.post.entity.*;
import com.example.devkorproject.post.exception.PostDoesNotExistException;
import com.example.devkorproject.post.repository.CommentRepository;
import com.example.devkorproject.post.repository.PhotoRepository;
import com.example.devkorproject.post.repository.LikeRepository;
import com.example.devkorproject.post.repository.PostRepository;
import com.example.devkorproject.post.repository.ScrapRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class PostService {

    private static final Logger logger = Logger.getLogger(PostService.class.getName());
    private final CustomerRepository customerRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final LikeRepository likeRepository;
    private final PhotoRepository photoRepository;
    private final AlarmRepository alarmRepository;
    private final JwtUtil jwtUtil;
    private final AlarmService alarmService;


    public List<GetPostRes> keywordSearchPost(String keyword,Long startPostId){
        List<PostEntity> foundPosts;
        if(startPostId==0){
            foundPosts=postRepository.findTop20ByTitleContainingOrBodyContainingOrderByUpdateDateDesc(keyword,keyword);
            if(foundPosts.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST.getMessage());
        }
        else{
            Optional<PostEntity> startPost = postRepository.findById(startPostId);

            if(startPost.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST.getMessage());

            foundPosts = postRepository.findNext20ByTitleContainingOrBodyContainingAndUpdateDateBeforeOrderByUpdateDateDesc(
                        keyword, keyword, startPost.get().getUpdateDate());

            if(foundPosts.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST.getMessage());
        }

        return foundPosts.stream().map(post -> {
            String firstPhotoData = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst() // 첫 번째 사진 데이터만 가져옵니다.
                    .orElse(null); // 사진이 없을 경우 null 반환

            List<String> photo = new ArrayList<>();
            if (firstPhotoData != null) {
                photo.add(firstPhotoData);
            }
            return new GetPostRes(
                post.getPostId(),
                post.getUpdateDate().toString(),
                post.getComments(),
                post.getLikes(),
                post.getTitle(),
                photo,
                post.getType(),
                post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public List<GetPostRes> typeSearchPost(String type,Long startPostId){
        List<PostEntity> foundPosts;
        if(startPostId==0){
            foundPosts = postRepository.findTop20ByTypeOrderByUpdateDateDesc(type);
            if(foundPosts.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }else{
            Optional<PostEntity> startPost=postRepository.findById(startPostId);
            if(startPost.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
            foundPosts=postRepository.findTop20ByTypeAndUpdateDateBeforeOrderByUpdateDateDesc(type, startPost.get().getUpdateDate());
            if(foundPosts.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
        return foundPosts.stream().map(post -> {
            String firstPhotoPath = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst() // 첫 번째 사진 데이터만 가져옵니다.
                    .orElse(null); // 사진이 없을 경우 null 반환

            List<String> photo = new ArrayList<>();
            if (firstPhotoPath != null) {
                photo.add(firstPhotoPath);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photo,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public List<GetPostRes> getAllPosts(Long startPostId){
        List<PostEntity> postEntities;
        if (startPostId == 0) {
            postEntities = postRepository.findTop20ByOrderByUpdateDateDesc();
            if(postEntities.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        } else {
            Optional<PostEntity> startPost = postRepository.findById(startPostId);
            if(startPost.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
            postEntities = postRepository.findTop20ByUpdateDateBeforeOrderByUpdateDateDesc(startPost.get().getUpdateDate());
            if(postEntities.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
        return postEntities.stream().map(post -> {
            String firstPhotoData = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst() // 첫 번째 사진 데이터만 가져옵니다.
                    .orElse(null); // 사진이 없을 경우 null 반환

            List<String> photo = new ArrayList<>();
            if (firstPhotoData != null) {
                photo.add(firstPhotoData);
            }
            if(post.getCustomer().getCustomerName().isEmpty())
                throw new GeneralException(ErrorCode.CUSTOMER_NAME_DOES_NOT_EXIST);
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photo,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public List<GetPostRes> getCustomerPosts(String token,Long startPostId) {
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        List<PostEntity> postEntities;
        if(startPostId==0){
            postEntities = postRepository.findTop20ByCustomer_CustomerIdOrderByUpdateDateDesc(customerId);
            if(postEntities.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        } else{
            Optional<PostEntity> startPost = postRepository.findById(startPostId);
            if(startPost.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
            postEntities = postRepository.findNext20ByCustomer_CustomerIdAndUpdateDateBeforeOrderByUpdateDateDesc(
                        customerId, startPost.get().getUpdateDate());
            if(postEntities.isEmpty())
                throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
        return postEntities.stream().map(post -> {
            String firstPhotoPath = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst()
                    .orElse(null);

            List<String> photos = new ArrayList<>();
            if (firstPhotoPath != null) {
                photos.add(firstPhotoPath);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photos,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public PostRes getUniquePost(String token,Long postId) {
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        Boolean isLiked;
        Optional<LikeEntity> opLike=likeRepository.findByCustomer_CustomerIdAndPost_PostId(customerId,postId);
        if(opLike.isEmpty())
            isLiked=false;
        else
            isLiked=true;
        try {
            PostEntity foundPost = postRepository.findById(postId).orElseThrow(PostDoesNotExistException::new);
            List<String> photoPaths = foundPost.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .toList();
            return new PostRes(
                    foundPost.getPostId(),
                    foundPost.getUpdateDate(),
                    foundPost.getComments(),
                    foundPost.getLikes(),
                    foundPost.getTitle(),
                    foundPost.getBody(),
                    photoPaths,
                    foundPost.getScrap(),
                    foundPost.getType(),
                    isLiked
            );
        } catch (PostDoesNotExistException ex) {
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
    }
    public PostRes updatePost(String token,PostUpdateReq postUpdateReq, List<MultipartFile> files) throws IOException{
        Long customerId = validateTokenAndGetCustomerId(token);
        PostEntity foundPost = validateAndGetPost(postUpdateReq.getPostId(), customerId);
        
        updatePostFields(foundPost, postUpdateReq);
        List<String> photoPaths = postUpdateReq.getFilePaths();
        
        if (hasValidFiles(files)) {
            List<String> newPhotoPaths = processNewFiles(files, foundPost);
            photoPaths.addAll(newPhotoPaths);
        }
        
        removeOldPhotos(foundPost, photoPaths);
        postRepository.save(foundPost);
        
        Boolean isLiked = checkIsLiked(customerId, postUpdateReq.getPostId());
        return buildPostRes(foundPost, photoPaths, isLiked);
    }

    private Long validateTokenAndGetCustomerId(String token) throws GeneralException{
    if(!jwtUtil.validateToken(token)){
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        }
        return jwtUtil.getCustomerIdFromToken(token);
    }

    private PostEntity validateAndGetPost(Long postId, Long customerId) throws GeneralException{
        Optional<PostEntity> postEntity = postRepository.findById(postId);
    if(postEntity.isEmpty()){
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
    if(postEntity.get().getCustomer().getCustomerId().equals(customerId)){
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_MATCH);
        }
        return postEntity.get();
    }

    private void updatePostFields(PostEntity post, PostUpdateReq postUpdateReq) {
        post.setUpdateDate(LocalDateTime.now());
        post.setComments(postUpdateReq.getComments());
        post.setLikes(postUpdateReq.getLikes());
        post.setTitle(postUpdateReq.getTitle());
        post.setBody(postUpdateReq.getBody());
        post.setType(postUpdateReq.getType());
        post.setScrap(postUpdateReq.getScrap());
    }

    private boolean hasValidFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        return files.stream().noneMatch(MultipartFile::isEmpty);
    }

    private List<String> processNewFiles(List<MultipartFile> files, PostEntity post) throws IOException {
        List<PhotoEntity> fileList = new ArrayList<>();
        String path = createImageDirectory();
        String absolutePath = new File("").getAbsolutePath() + File.separator;

        for(MultipartFile multipartFile : files) {
            String fileExtension = getFileExtension(multipartFile.getContentType());
            if (fileExtension == null) {
                break;
            }

            UUID uuid = UUID.randomUUID();
            String newFileName = uuid + fileExtension;
            String filePath = path + File.separator + newFileName;

            PhotoEntity photo = PhotoEntity.builder()
                    .origFileName(multipartFile.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(multipartFile.getSize())
                    .build();
            fileList.add(photo);

            File file = new File(absolutePath + filePath);
            multipartFile.transferTo(file);
            boolean writableSet = file.setWritable(true);
            boolean readableSet = file.setReadable(true);
            if (!writableSet || !readableSet) {
                logger.warning("Failed to set file permissions (read/write) for: " + file.getAbsolutePath());
            }
        }

        List<String> newPhotoPaths = new ArrayList<>();
        for(PhotoEntity photo : fileList) {
            photo.setPost(post);
            photoRepository.save(photo);
            newPhotoPaths.add(photo.getFilePath());
        }
        return newPhotoPaths;
    }

    private String createImageDirectory() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDate = now.format(dateTimeFormatter);
        String path = "images" + File.separator + currentDate;
        File file = new File(path);

        if(!file.exists()) {
            boolean wasSuccessful = file.mkdirs();
            if (!wasSuccessful) {
                logger.info("file: was not successful");
            }
        }
        return path;
    }

    private String getFileExtension(String contentType) {
        if(ObjectUtils.isEmpty(contentType)) {
            return null;
        }
        if (contentType.contains("image/jpeg")) {
            return ".jpg";
        } else if (contentType.contains("image/png")) {
            return ".png";
        }
        return null;
    }

    private void removeOldPhotos(PostEntity post, List<String> photoPaths) {
        List<PhotoEntity> photoEntitiesToRemove = photoRepository.findByFilePathNotIn(photoPaths);
        post.getPhoto().removeAll(photoEntitiesToRemove);
    }

    private Boolean checkIsLiked(Long customerId, Long postId) {
        Optional<LikeEntity> opLike = likeRepository.findByCustomer_CustomerIdAndPost_PostId(customerId, postId);
        return opLike.isPresent();
    }

    private PostRes buildPostRes(PostEntity post, List<String> photoPaths, Boolean isLiked) {
        return new PostRes(
                post.getPostId(),
                post.getUpdateDate(),
                post.getComments(),
                post.getLikes(),
                post.getTitle(),
                post.getBody(),
                photoPaths,
                post.getScrap(),
                post.getType(),
                isLiked
        );
    }
    public Boolean deletePost(String token,PostDeleteReq postDeleteReq){
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        Optional<PostEntity> toDeletePost=postRepository.findById(postDeleteReq.getPostId());
        if(toDeletePost.isEmpty())
            throw new  GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        PostEntity deletePost=toDeletePost.get();
        if(deletePost.getCustomer().getCustomerId().equals(customerId))
            throw new  GeneralException(ErrorCode.CUSTOMER_DOES_NOT_MATCH);
        postRepository.delete(deletePost);
        Optional<PostEntity> checkDelete=postRepository.findById(postDeleteReq.getPostId());
        
        return checkDelete.isEmpty();
    }

    public CommentRes giveComment(String token,CommentReq commentReq) throws IOException {
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        Optional<PostEntity> opPost=postRepository.findById(commentReq.getPostId());
        if(opPost.isEmpty())
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        PostEntity post=opPost.get();
        Optional<CustomerEntity> opCustomer=customerRepository.findById(customerId);
        if(opCustomer.isEmpty())
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_EXIST);
        CustomerEntity customer=opCustomer.get();

        CommentEntity comment=CommentEntity.builder()
                .post(post)
                .customer(customer)
                .contents(commentReq.getContents())
                .time(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        customer.setMyComments(customer.getMyComments()+1);
        post.setComments(post.getComments()+1);
        post.getCommentEntities().add(comment);
        postRepository.save(post);
        if(customer.getFcmToken() != null && !customerId.equals(post.getCustomer().getCustomerId())) {
            String targetToken = searchFCMTokenByPostId(commentReq.getPostId());
            String postTitle = post.getTitle();
            String customerName = customer.getCustomerName();

            String message = customerName + "님이 " +
                    postTitle + " 글에 댓글을 달았습니다.";

            alarmService.sendMessageTo(targetToken, "BabyMeal", message);
            LocalDateTime now = LocalDateTime.now();

            AlarmEntity alarmEntity = AlarmEntity.builder()
                    .body(message)
                    .date(now)
                    .customer(post.getCustomer())
                    .build();
            alarmRepository.save(alarmEntity);
        }


        return new CommentRes(comment.getPost().getPostId(),comment.getContents(),comment.getCustomer().getCustomerName(),comment.getTime());
    }
    public String searchFCMTokenByPostId(Long postId){
        Optional<String> opFCMToken = postRepository.findCustomerFcmTokenByPostId(postId);
        if(opFCMToken.isEmpty())
            throw new GeneralException(ErrorCode.FCMTOKEN_DOES_NOT_EXIST);
        return opFCMToken.get();
    }

    public List<CommentRes> getComments(Long postId){
        Optional<PostEntity> opPost=postRepository.findById(postId);
        if(opPost.isEmpty())
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        PostEntity postEntity=opPost.get();
        List<CommentEntity> comments = postEntity.getCommentEntities();
        return comments.stream()
                .map(commentEntity -> new CommentRes(commentEntity.getPost().getPostId(),commentEntity.getContents(),commentEntity.getCustomer().getCustomerName(),commentEntity.getTime())                )
                .toList();
    }

    public List<GetPostRes> weeklyLiked(){
        List<PostEntity> postEntities=postRepository.findTop10ByLikesWithinLastWeek();
        if(postEntities.isEmpty())
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        return postEntities.stream().map(post -> {
            String firstPhotoPath = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst()
                    .orElse(null);

            List<String> photos = new ArrayList<>();
            if (firstPhotoPath != null) {
                photos.add(firstPhotoPath);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photos,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();    
    }
    public List<GetPostRes> getLikedPost(Long startPostId){
        List<PostEntity> postEntities;
        if(startPostId==0)
        {
            postEntities=postRepository.findTop20ByOrderByLikesDesc();
            if(postEntities.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
        else
        {
            int pageSize = 20;
            Optional<PostEntity> opPost=postRepository.findById(startPostId);
            if(opPost.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
            PostEntity realPost=opPost.get();
            Long likes=realPost.getLikes();
            postEntities = postRepository.findByLikesLessThanAndPostIdGreaterThanOrderByLikesDescPostIdAsc(
                likes, startPostId, PageRequest.of(0, pageSize)
        );}
        return postEntities.stream().map(post -> {
            String firstPhotoData = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst()
                    .orElse(null);

            List<String> photos = new ArrayList<>();
            if (firstPhotoData != null) {
                photos.add(firstPhotoData);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photos,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public List<GetPostRes> getLikedPostByType(String type,Long startPostId){
        List<PostEntity> postEntities;
        if(startPostId==0)
        {
            postEntities=postRepository.findTop20ByTypeLikesOrderByLikesDesc(type);
            if(postEntities.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }
        else{
            int pageSize = 20;
            Optional<PostEntity> opPost=postRepository.findById(startPostId);
            if(opPost.isEmpty())
                throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
            PostEntity realPost=opPost.get();
            Long likes=realPost.getLikes();
            postEntities = postRepository.findByTypeAndLikesLessThanEqualAndPostIdLessThanOrderByLikesDescUpdateDateAsc(
                    type,likes, startPostId, PageRequest.of(0, pageSize)
            );
        }
        return postEntities.stream().map(post -> {
            String firstPhotoData = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst()
                    .orElse(null);

            List<String> photos = new ArrayList<>();
            if (firstPhotoData != null) {
                photos.add(firstPhotoData);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photos,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    public LikesRes giveLikes(String token,LikesReq likesReq)throws IOException{
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);

        Optional<PostEntity> opPost=postRepository.findById(likesReq.getPostId());
        if(opPost.isEmpty()){
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        }

        PostEntity post = opPost.get();
        Optional<CustomerEntity> opCustomer=customerRepository.findById(post.getCustomer().getCustomerId());
        
        if(opCustomer.isEmpty()){
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_EXIST);
        }
        CustomerEntity getCustomer = opCustomer.get();

        Optional<CustomerEntity> opGiveCustomer = customerRepository.findById(customerId);
        if(opGiveCustomer.isEmpty()){
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_EXIST);
        }
        CustomerEntity giveCustomer = opGiveCustomer.get();

        if(giveCustomer == getCustomer)
            throw new GeneralException(ErrorCode.CANNOT_GIVE_LIKE);
        Optional<LikeEntity> opLikeEntity=likeRepository.findByCustomer_CustomerIdAndPost_PostId(customerId, post.getPostId());
        if(opLikeEntity.isPresent())
            throw new GeneralException(ErrorCode.CANNOT_GIVE_LIKE);
        LikeEntity like=LikeEntity.builder()
                .post(post)
                .customer(giveCustomer)
                .build();
        likeRepository.save(like);
        post.setLikes(post.getLikes()+1);

        getCustomer.setMyLikes(getCustomer.getMyLikes()+1);
        if(giveCustomer.getFcmToken() != null && !customerId.equals(post.getCustomer().getCustomerId())) {

            String targetToken = searchFCMTokenByPostId(likesReq.getPostId());
            String postTitle = post.getTitle();
            String customerName = giveCustomer.getCustomerName();

            String message = customerName + "님이 " +
                    postTitle + " 글에 찜을 눌렀습니다.";

            alarmService.sendMessageTo(targetToken, "BabyMeal", message);

            LocalDateTime now = LocalDateTime.now();

            AlarmEntity alarmEntity = AlarmEntity.builder()
                    .body(message)
                    .date(now)
                    .customer(post.getCustomer())
                    .build();
            alarmRepository.save(alarmEntity);

        }

        return new LikesRes(post.getPostId(), post.getLikes());
    }

    public ScrapRes giveScrap(String token,ScrapReq scrapReq){
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        Optional<PostEntity> opPost=postRepository.findById(scrapReq.getPostId());
        if(opPost.isEmpty())
            throw new GeneralException(ErrorCode.POST_DOES_NOT_EXIST);
        PostEntity post=opPost.get();
        Optional<CustomerEntity> opCustomer=customerRepository.findById(customerId);
        if(opCustomer.isEmpty())
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_EXIST);
        CustomerEntity customer=opCustomer.get();
        if(customerId.equals(post.getCustomer().getCustomerId()))
            throw new GeneralException(ErrorCode.CANNOT_GIVE_SCRAP);
        Optional<ScrapEntity> opScrapEntity=scrapRepository.findByCustomer_CustomerIdAndPost_PostId(customerId, post.getPostId());
        if(opScrapEntity.isPresent())
            throw new GeneralException(ErrorCode.CANNOT_GIVE_SCRAP);
        ScrapEntity scrap=ScrapEntity.builder()
                .post(post)
                .customer(customer)
                .build();
        scrapRepository.save(scrap);
        post.setScrap(post.getScrap()+1);
        return new ScrapRes(post.getPostId(),post.getScrap());
    }

    public List<GetPostRes> getScrap(String token, String type){
        if(!jwtUtil.validateToken(token))
            throw new GeneralException(ErrorCode.WRONG_TOKEN);
        Long customerId= jwtUtil.getCustomerIdFromToken(token);
        List<ScrapEntity> scrapEntities=scrapRepository.findByCustomer_CustomerId(customerId);
        if(scrapEntities.isEmpty())
            throw new GeneralException(ErrorCode.SCRAP_DOES_NOT_EXIST);
        return scrapEntities.stream()
                .filter(scrap -> scrap.getPost().getType().equals(type)).map(scrap -> {
            PostEntity post=scrap.getPost();
            String firstPhotoData = post.getPhoto().stream()
                    .map(PhotoEntity::getFilePath)
                    .findFirst() // 첫 번째 사진 데이터만 가져옵니다.
                    .orElse(null); // 사진이 없을 경우 null 반환

            List<String> photo = new ArrayList<>();
            if (firstPhotoData != null) {
                photo.add(firstPhotoData);
            }
            return new GetPostRes(
                    post.getPostId(),
                    post.getUpdateDate().toString(),
                    post.getComments(),
                    post.getLikes(),
                    post.getTitle(),
                    photo,
                    post.getType(),
                    post.getCustomer().getCustomerName()
            );
        }).toList();
    }
    
    public Long create(String token,PostCreateReqDto requestDto, List<MultipartFile> files) throws IOException{
        Long customerId = validateTokenAndGetCustomerId(token);
        CustomerEntity customer = validateAndGetCustomer(customerId);
        PostEntity post = createPostEntity(requestDto, customer);
        
        if (hasValidFiles(files)) {
            processFilesForPost(files, post);
        }
        
        return postRepository.save(post).getPostId();
    }

    private CustomerEntity validateAndGetCustomer(Long customerId) throws GeneralException {
        Optional<CustomerEntity> opCustomer = customerRepository.findCustomerEntityByCustomerId(customerId);
        if(opCustomer.isEmpty()) {
            throw new GeneralException(ErrorCode.CUSTOMER_DOES_NOT_EXIST);
        }
        return opCustomer.get();
    }

    private PostEntity createPostEntity(PostCreateReqDto requestDto, CustomerEntity customer) {
        return PostEntity.builder()
                .updateDate(LocalDateTime.now())
                .comments(Long.valueOf(0))
                .likes(Long.valueOf(0))
                .title(requestDto.getTitle())
                .body(requestDto.getBody())
                .scrap(Long.valueOf(0))
                .type(requestDto.getType())
                .customer(customer)
                .build();
    }

    private void processFilesForPost(List<MultipartFile> files, PostEntity post) throws IOException {
        List<PhotoEntity> fileList = new ArrayList<>();
        String path = createImageDirectory();
        String absolutePath = new File("").getAbsolutePath() + File.separator;

        for(MultipartFile multipartFile : files) {
            String fileExtension = getFileExtension(multipartFile.getContentType());
            if (fileExtension == null) {
                break;
            }

            UUID uuid = UUID.randomUUID();
            String newFileName = uuid + fileExtension;
            String filePath = path + File.separator + newFileName;

            PhotoEntity photo = PhotoEntity.builder()
                    .origFileName(multipartFile.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(multipartFile.getSize())
                    .build();
            fileList.add(photo);

            File file = new File(absolutePath + filePath);
            multipartFile.transferTo(file);
            boolean writableSet = file.setWritable(true);
            boolean readableSet = file.setReadable(true);
            if (!writableSet || !readableSet) {
                logger.warning("Failed to set file permissions (read/write) for: " + file.getAbsolutePath());
            }
        }

        for(PhotoEntity photo : fileList) {
            photo.setPost(post);
            photoRepository.save(photo);
        }
    }
}
