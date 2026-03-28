package edu.ruc.platform.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_operation_log")
public class LatestSysOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String moduleCode;

    @Column(length = 64)
    private String businessType;

    @Column
    private Long businessId;

    @Column(nullable = false, length = 64)
    private String operationType;

    @Column
    private String operationDesc;

    @Column
    private Long operatorUserId;

    @Column(length = 128)
    private String traceId;

    @Column(length = 512)
    private String requestUri;

    @Column(length = 16)
    private String requestMethod;

    @Column(length = 64)
    private String requestIp;

    @Column(length = 512)
    private String userAgent;

    @Column(nullable = false, length = 16)
    private String logLevel;

    @Column(nullable = false, length = 16)
    private String resultStatus;

    @Column
    private String errorMessage;

    @Column
    private String extJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
