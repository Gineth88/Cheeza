
package com.cheeza.Cheeza.service;

import com.cheeza.Cheeza.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserLoyaltyService {

    public void awardPoints(User user, double amount) {
        int pointsEarned = (int) (amount / 10.0); // 1 point per 10 LKR spent
        user.addLoyaltyPoints(pointsEarned);
    }

    public boolean redeemPoints(User user, int pointsToRedeem) {
        if (user.getLoyaltyPoints() < pointsToRedeem) return false;
        user.deductLoyaltyPoints(pointsToRedeem);
        return true;
    }
}
