package com.phuc.shop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SalesReportResponse {

    private String shopId;
    private String shopName;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer totalOrders;
    private Integer totalProductsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;

    public SalesReportResponse(String s) {
    }

    // Optional: thống kê theo ngày
    // private List<DailySalesSummary> dailySummaries;

    // List<ProductSalesSummary> productSalesSummaryList;
}

