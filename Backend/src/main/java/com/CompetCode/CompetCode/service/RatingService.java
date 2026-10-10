package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RatingService {

    private static final int K_FACTOR = 32;

    public void updateRatingsAfterBattle(List<User> participants, List<Integer> ranks) {
        if (participants.size() != ranks.size() || participants.isEmpty()) {
            return;
        }

        int n = participants.size();
        double[] expected = new double[n];
        double[] actual = new double[n];

        for (int i = 0; i < n; i++) {
            actual[i] = scoreFromRank(ranks.get(i), n);
            expected[i] = 0;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    expected[i] += expectedScore(participants.get(i).getRating(), participants.get(j).getRating());
                }
            }
            if (n > 1) {
                expected[i] /= (n - 1);
            }
        }

        for (int i = 0; i < n; i++) {
            User user = participants.get(i);
            int delta = (int) Math.round(K_FACTOR * (actual[i] - expected[i]));
            user.setRating(Math.max(100, user.getRating() + delta));
            if (ranks.get(i) == 1) {
                user.setBattlesWon(user.getBattlesWon() + 1);
            }
        }
    }

    public int calculateSubmissionScore(int testsPassed, int testsTotal, Long executionTimeMs, int timeLimitMs) {
        if (testsTotal == 0) return 0;
        double passRatio = (double) testsPassed / testsTotal;
        int baseScore = (int) (passRatio * 1000);

        if (testsPassed == testsTotal && executionTimeMs != null && timeLimitMs > 0) {
            double efficiencyBonus = Math.max(0, 1.0 - ((double) executionTimeMs / timeLimitMs));
            baseScore += (int) (efficiencyBonus * 200);
        }

        return baseScore;
    }

    private double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
    }

    private double scoreFromRank(int rank, int total) {
        return (double) (total - rank) / Math.max(1, total - 1);
    }

}
