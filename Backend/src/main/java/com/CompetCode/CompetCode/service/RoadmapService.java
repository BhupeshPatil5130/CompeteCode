package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.LeaderboardEntry;
import com.CompetCode.CompetCode.dto.RoadmapItemDto;
import com.CompetCode.CompetCode.dto.RoadmapProgressDto;
import com.CompetCode.CompetCode.entity.Roadmap;
import com.CompetCode.CompetCode.entity.RoadmapItem;
import com.CompetCode.CompetCode.entity.User;
import com.CompetCode.CompetCode.entity.UserProgress;
import com.CompetCode.CompetCode.repository.RoadmapRepository;
import com.CompetCode.CompetCode.repository.UserProgressRepository;
import com.CompetCode.CompetCode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;

    public List<Roadmap> getAllRoadmaps() {
        return roadmapRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Roadmap getRoadmap(Long id) {
        return roadmapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found"));
    }

    public RoadmapProgressDto getProgress(Long userId, Long roadmapId) {
        Roadmap roadmap = getRoadmap(roadmapId);
        List<RoadmapItem> items = roadmap.getItems();

        List<RoadmapItemDto> itemDtos = items.stream().map(item -> {
            boolean completed = progressRepository
                    .findByUserIdAndRoadmapIdAndProblemId(userId, roadmapId, item.getProblem().getId())
                    .map(UserProgress::getCompleted)
                    .orElse(false);

            return RoadmapItemDto.builder()
                    .problemId(item.getProblem().getId())
                    .problemTitle(item.getProblem().getTitle())
                    .difficulty(item.getProblem().getDifficulty().name())
                    .sectionTitle(item.getSectionTitle())
                    .completed(completed)
                    .build();
        }).toList();

        long completed = itemDtos.stream().filter(RoadmapItemDto::isCompleted).count();

        return RoadmapProgressDto.builder()
                .roadmapId(roadmapId)
                .title(roadmap.getTitle())
                .totalProblems(items.size())
                .completedProblems((int) completed)
                .progressPercent(items.isEmpty() ? 0 : (completed * 100.0 / items.size()))
                .items(itemDtos)
                .build();
    }

    @Transactional
    public void markCompleted(Long userId, Long roadmapId, Long problemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Roadmap roadmap = getRoadmap(roadmapId);

        Optional<UserProgress> existing = progressRepository
                .findByUserIdAndRoadmapIdAndProblemId(userId, roadmapId, problemId);

        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());
            progressRepository.save(progress);
        } else {
            RoadmapItem item = roadmap.getItems().stream()
                    .filter(i -> i.getProblem().getId().equals(problemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Problem not in roadmap"));

            progressRepository.save(UserProgress.builder()
                    .user(user)
                    .roadmap(roadmap)
                    .problem(item.getProblem())
                    .completed(true)
                    .completedAt(Instant.now())
                    .build());
        }
    }

    public List<LeaderboardEntry> getGlobalLeaderboard() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingInt(User::getRating).reversed())
                .limit(100)
                .map(user -> LeaderboardEntry.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .rating(user.getRating())
                        .problemsSolved(user.getProblemsSolved())
                        .battlesWon(user.getBattlesWon())
                        .build())
                .toList();
    }
}
