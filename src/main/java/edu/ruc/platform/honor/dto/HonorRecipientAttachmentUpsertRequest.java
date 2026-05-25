package edu.ruc.platform.honor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HonorRecipientAttachmentUpsertRequest(
        Long fileId,
        @NotBlank(message = "附件类型不能为空") @Size(max = 32, message = "附件类型不能超过 32 个字符") String attachmentType,
        @Size(max = 255, message = "文件名不能超过 255 个字符") String fileName,
        @Size(max = 128, message = "文件类型不能超过 128 个字符") String contentType,
        Long fileSize,
        @Size(max = 500, message = "存储路径不能超过 500 个字符") String storagePath,
        @Size(max = 255, message = "说明不能超过 255 个字符") String caption,
        Boolean publicVisible,
        Integer displayOrder
) {
}
