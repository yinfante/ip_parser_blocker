package com.ef.domain;

import lombok.Data;

/**
 * DTO object
 *
 * @author yinfante
 */
@Data
public class ParserDTO {

    private String fileUrl;
    private String startDate;
    private String duration;
    private long threshold;

    public ParserDTO(String fileUrl, String startDate, String duration, long threshold) {
        this.fileUrl = fileUrl;
        this.startDate = startDate;
        this.duration = duration;
        this.threshold = threshold;
    }

    public String getDuration() {
        switch (duration) {
            case "hourly":
                return "HOUR";
            case "daily":
                return "DAY";
        }
        return duration;
    }

}
