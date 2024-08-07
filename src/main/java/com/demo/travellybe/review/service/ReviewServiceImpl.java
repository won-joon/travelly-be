package com.demo.travellybe.review.service;

import com.demo.travellybe.comment.domain.Comment;
import com.demo.travellybe.comment.repository.CommentRepository;
import com.demo.travellybe.comment.dto.CommentResponseDto;
import com.demo.travellybe.exception.CustomException;
import com.demo.travellybe.exception.ErrorCode;
import com.demo.travellybe.member.domain.Member;
import com.demo.travellybe.member.repository.MemberRepository;
import com.demo.travellybe.product.domain.Product;
import com.demo.travellybe.product.dto.response.ProductReviewResponseDto;
import com.demo.travellybe.product.repository.ProductRepository;
import com.demo.travellybe.review.domain.Review;
import com.demo.travellybe.review.repository.ReviewRepository;
import com.demo.travellybe.review.dto.ReviewRequestDto;
import com.demo.travellybe.review.dto.ReviewResponseDto;
import com.demo.travellybe.review.dto.ReviewUpdateResponseDto;
import com.demo.travellybe.util.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    public void saveReview(List<MultipartFile> files, ReviewRequestDto reviewRequestDto, String email, Long productId) {

        // 유저 검색
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 상품 검색
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 파일 저장 및 URL 생성
        List<String> filesUrls = new ArrayList<>();
        if (files != null) {
            filesUrls = s3Service.uploadFiles(files, "review");
        }

        // 리뷰 저장
        Review review = Review.builder()
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .imageUrls(filesUrls)
                .build();

        member.addReview(review);
        product.addReview(review);

        reviewRepository.save(review);
    }

    public ReviewResponseDto getReview(Long productId, Long reviewId, String email) {

        // 유저 검색
        Member member;
        if (email != null) {
            member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        } else {
            member = null;
        }

        // 상품 검색
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 리뷰 검색
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 댓글 검색
        List<Comment> comments = commentRepository.findByReviewId(reviewId);

        List<CommentResponseDto> commentResponseDtoList = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .toList();


        return new ReviewResponseDto(product, review, member, commentResponseDtoList);

    }

    @Override
    public Page<ProductReviewResponseDto> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByProductId(productId, pageable);

        return reviews.map(ProductReviewResponseDto::new);
    }

    public ReviewUpdateResponseDto getUpdateReview(Long reviewId) {

        // 리뷰 검색
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        return new ReviewUpdateResponseDto(review);
    }

    public void updateReview(List<MultipartFile> files, ReviewRequestDto reviewRequestDto, Long reviewId) {

        // 리뷰 검색
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 파일 저장 및 URL 생성
        List<String> filesUrls = new ArrayList<>();
        if (files != null) {
            filesUrls = s3Service.uploadFiles(files, "review");
        }

        // 리뷰 수정
        review.update(filesUrls, reviewRequestDto);
    }

    public void deleteReview(Long reviewId) {

        // 리뷰 검색
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.deleteById(reviewId);
    }
}
