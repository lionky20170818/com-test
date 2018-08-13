package com.ligl.trans.dal.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.ligl.trans.dal.entity.UserOperationRecord;

import java.util.List;

public interface UserOperationRecordDAO {
    int deleteByPrimaryKey(Long id);

    int insert(UserOperationRecord record);

    int insertSelective(UserOperationRecord record);

    UserOperationRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserOperationRecord record);

    int updateByPrimaryKey(UserOperationRecord record);

    List<UserOperationRecord> listByPayService();

    PageList<UserOperationRecord> listByPayServicePage(String pageStr, PageBounds pageBounds);
}