package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.Review;

/**
 * ReviewDao Interface
 * Defines database operations for Review entity
 */
public interface ReviewDao {
    Review getReviewById(int reviewId);
    List<Review> getReviewsByProduct(int productId);
    List<Review> getReviewsByUser(int userId);
    int createReview(Review review);
    boolean updateReview(Review review);
    boolean deleteReview(int reviewId);
}
