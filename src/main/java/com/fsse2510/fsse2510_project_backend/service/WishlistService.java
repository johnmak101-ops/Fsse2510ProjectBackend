package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.wishlist.domainObject.response.WishlistResponseData;

import java.util.List;

public interface WishlistService {
    void addWishlistItem(FirebaseUserData firebaseUser, Integer pid);

    List<WishlistResponseData> getUserWishlist(FirebaseUserData firebaseUser);

    void removeWishlistItem(FirebaseUserData firebaseUser, Integer pid);
}
