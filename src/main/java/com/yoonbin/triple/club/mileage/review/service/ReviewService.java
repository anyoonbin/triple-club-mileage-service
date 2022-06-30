package com.yoonbin.triple.club.mileage.review.service;

import com.yoonbin.triple.club.mileage.point.domain.Point;
import com.yoonbin.triple.club.mileage.point.repository.PointRepository;
import com.yoonbin.triple.club.mileage.review.domain.Review;
import com.yoonbin.triple.club.mileage.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PointRepository pointRepository;

    public List<Review> getReviews(){
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsByPlaceId(String placeId){
        return reviewRepository.findByPlaceIdOrderByCreatedAt(placeId);
    }

    public List<Review> getReviewsByUserId(String userId){
        return reviewRepository.findByUserId(userId);
    }

    public Map.Entry<Integer, String> checkAmount(Review review){
        int amount = 0;
        String remarks ="";

        if(review.getContent().length() > 0){
            amount++;
            remarks+="1자 이상 텍스트 작성";
        }
        if(!review.getAttachedPhotoIds().get(0).isEmpty()){
            amount++;
            if(remarks.isEmpty()){
                remarks+="1장 이상 사진 첨부";
            }else {
                remarks+=", 1장 이상 사진 첨부";
            }
        }
        if(getReviewsByPlaceId(review.getPlaceId()).isEmpty()){
            amount++;
            if(remarks.isEmpty()){
                remarks+="특정 장소에 첫 리뷰 작성";
            }else {
                remarks+=", 특정 장소에 첫 리뷰 작성";
            }
        }
        AbstractMap.Entry<Integer, String> checkAmount =new AbstractMap.SimpleEntry<>(amount , remarks);
        return checkAmount;
    }

    public List<String> getUserIdsByPlaceId(String placeId){
        List<String> UserIds = getReviewsByPlaceId(placeId).stream().map(Review::getUserId).collect(Collectors.toList());
        return UserIds;
    }

    public boolean visitedCheck(Review review){
        boolean visited = false;
        if(getUserIdsByPlaceId(review.getPlaceId()).contains(review.getUserId())) {
            visited = true;
        }
        return visited;
    }

    @Transactional
    public Review insertReview(Review review){
        AbstractMap.Entry<Integer, String> checkAmount = checkAmount(review);

        if(!visitedCheck(review)){
            reviewRepository.save(review);

            Point point = Point.builder()
                    .reviewId(review.getReviewId())
                    .remarks(checkAmount.getValue())
                    .amount(checkAmount.getKey())
                    .placeId(review.getPlaceId())
                    .userId(review.getUserId())
                    .build();
            pointRepository.save(point);
        }
        return review;
    }
}

