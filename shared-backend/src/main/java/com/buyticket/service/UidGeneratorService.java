package com.buyticket.service;

public interface UidGeneratorService {
    /**
     * 生成下一个用户UID
     * 格式：AIYSG8000001, AIYSG8000002, ...
     */
    String generateNextUid();
}
