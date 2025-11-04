package com.mydea.mydea_backend.production.domain;

/**
 * 생산(커뮤니티 아님) 용량 산정에서 사용하는 상품(디자인) 유형.
 * DB에는 대문자 문자열로 저장됩니다 (e.g., 'BASIC', 'FLOWER').
 */
public enum ProductionProductType {
    BASIC, FLOWER
}
