package com.demo.travellybe.member.domain;

import com.demo.travellybe.product.domain.Product;
import com.demo.travellybe.review.domain.Review;
import com.demo.travellybe.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private int point = 0;

    private String imageUrl = "https://travelly-bucket.s3.ap-northeast-2.amazonaws.com/images/default-profile.png";

    private String type;

    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Builder
    public Member(String email, String password, String nickname, String type) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.type = type;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setReview(Review review) {
        reviews.add(review);
        review.setMember(this);
    }

    public void setProduct(Product product) {
        products.add(product);
        product.setMember(this);
    }


}
