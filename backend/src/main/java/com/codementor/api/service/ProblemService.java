package com.codementor.api.service;

import com.codementor.api.dto.ProblemDto;
import com.codementor.api.dto.QuestionGenerationResponse;
import com.codementor.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProblemService {

    public ProblemService() {
    }

    public List<ProblemDto> getAllProblems() {
        return getSeedProblemDtos();
    }

    public ProblemDto getProblemBySlugOrId(String identifier) {
        return getSeedProblemDtos().stream()
                .filter(p -> p.getId().equalsIgnoreCase(identifier))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with identifier: " + identifier));
    }

    private List<ProblemDto> getSeedProblemDtos() {
        ProblemDto twoSum = new ProblemDto(
                "two-sum",
                "Two Sum",
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
                "Easy",
                "Arrays & Hashing",
                "Java"
        );
        twoSum.setAcceptanceRate("49.8%");
        twoSum.setExamples(List.of(
                new QuestionGenerationResponse.ExampleDto("nums = [2,7,11,15], target = 9", "[0,1]", "Because nums[0] + nums[1] == 9, return [0, 1].")
        ));
        twoSum.setConstraints(List.of("2 <= nums.length <= 10^4", "-10^9 <= nums[i] <= 10^9"));
        twoSum.setStarterCode(Map.of(
                "Java", "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your implementation\n        return new int[]{};\n    }\n}",
                "Python", "class Solution:\n    def twoSum(self, nums: list[int], target: int) -> list[int]:\n        pass"
        ));

        ProblemDto binarySearch = new ProblemDto(
                "binary-search",
                "Binary Search",
                "Given an array of integers nums sorted in ascending order and a target value, write an algorithm with O(log n) runtime complexity.",
                "Easy",
                "Algorithms",
                "Java"
        );
        binarySearch.setAcceptanceRate("56.4%");

        ProblemDto lruCache = new ProblemDto(
                "lru-cache",
                "LRU Cache",
                "Design a data structure that follows the constraints of a Least Recently Used (LRU) cache with O(1) average get and put operations.",
                "Medium",
                "Data Structures",
                "Java"
        );
        lruCache.setAcceptanceRate("41.2%");

        return List.of(twoSum, binarySearch, lruCache);
    }
}
