package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
public class CommentRequest {
    private String commentType;
    @NotBlank(message = "Comment text is required")
    private String commentText;
    private LocalDate followUpDate;
}
