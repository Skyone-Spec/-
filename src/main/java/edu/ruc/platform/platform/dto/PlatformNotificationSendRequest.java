package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record PlatformNotificationSendRequest(
        @NotBlank(message = "通知标题不能为空") @Size(max = 200, message = "通知标题长度不能超过 200") String title,
        @Pattern(regexp = "IN_APP|EMAIL|WECHAT", message = "发送渠道仅支持 IN_APP、EMAIL、WECHAT")
        @NotBlank(message = "发送渠道不能为空") @Size(max = 32, message = "发送渠道长度不能超过 32") String channel,
        @Pattern(regexp = "ALL|GRADE|CLASS|SELF", message = "目标类型仅支持 ALL、GRADE、CLASS、SELF")
        @NotBlank(message = "目标类型不能为空") @Size(max = 32, message = "目标类型长度不能超过 32") String targetType,
        @NotBlank(message = "目标描述不能为空") @Size(max = 255, message = "目标描述长度不能超过 255") String targetDescription,
        @Pattern(regexp = "PENDING|SENT|FAILED", message = "发送状态仅支持 PENDING、SENT、FAILED")
        @NotBlank(message = "发送状态不能为空") @Size(max = 32, message = "发送状态长度不能超过 32") String status,
        @NotNull(message = "接收人数不能为空") @Min(value = 0, message = "接收人数不能为负数") Integer recipientCount,
        LocalDateTime sentAt,
        List<
                @Pattern(regexp = "IN_APP|EMAIL|WECHAT", message = "扩展渠道仅支持 IN_APP、EMAIL、WECHAT")
                @Size(max = 32, message = "扩展渠道长度不能超过 32") String> extensionChannels
) {
}
