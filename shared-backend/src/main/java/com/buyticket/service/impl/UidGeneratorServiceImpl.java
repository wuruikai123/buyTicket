package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.SysUser;
import com.buyticket.mapper.SysUserMapper;
import com.buyticket.service.UidGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UidGeneratorServiceImpl implements UidGeneratorService {

    @Autowired
    private SysUserMapper sysUserMapper;

    private static final String UID_PREFIX = "AIYSG8";
    private static final int UID_START_NUMBER = 1;
    private static final int UID_NUMBER_LENGTH = 6; // 数字部分长度

    @Override
    @Transactional
    public synchronized String generateNextUid() {
        // 查询当前最大的UID（按创建时间倒序，确保获取最新的）
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysUser::getUid)
                    .isNotNull(SysUser::getUid)
                    .like(SysUser::getUid, UID_PREFIX)
                    .orderByDesc(SysUser::getId)
                    .last("LIMIT 1");
        
        SysUser lastUser = sysUserMapper.selectOne(queryWrapper);
        
        int nextNumber;
        if (lastUser == null || lastUser.getUid() == null || lastUser.getUid().isEmpty()) {
            // 没有用户或没有UID，从起始值开始
            nextNumber = UID_START_NUMBER;
        } else {
            String lastUid = lastUser.getUid();
            try {
                // 提取数字部分（去掉前缀 AIYSG8）
                if (lastUid.startsWith(UID_PREFIX) && lastUid.length() > UID_PREFIX.length()) {
                    String numberPart = lastUid.substring(UID_PREFIX.length());
                    int lastNumber = Integer.parseInt(numberPart);
                    nextNumber = lastNumber + 1;
                } else {
                    // 格式不匹配，从起始值开始
                    nextNumber = UID_START_NUMBER;
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，从起始值开始
                nextNumber = UID_START_NUMBER;
            }
        }
        
        // 格式化为固定长度的数字（例如：000001）
        String numberPart = String.format("%0" + UID_NUMBER_LENGTH + "d", nextNumber);
        
        return UID_PREFIX + numberPart;
    }
}
