package com.alin.lin.dto;

import com.alin.lin.entity.ChangeReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeReviewPageDto {
    private List<ChangeReview> items;
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}
