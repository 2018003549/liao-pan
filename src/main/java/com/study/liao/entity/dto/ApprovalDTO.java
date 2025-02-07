package com.study.liao.entity.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class ApprovalDTO {
    String userId;
    Integer groupId;
    Boolean isPassed;
}
