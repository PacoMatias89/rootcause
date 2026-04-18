package com.rootcause.dto;
/**
 * Public response DTO representing a grouped count entry in analysis statistics.
 *
 * @param value grouped value, such as a category or severity name
 * @param count number of analyses associated with the grouped value
 */
public record AnalysisCountResponse(
        String value,
        long count
) {
}
