package com.study.liao.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.liao.dao.FileInfoMapper;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.enums.PageSize;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.query.SimplePage;
import com.study.liao.entity.vo.PaginationResultVO;
import jakarta.annotation.Resource;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.study.liao.service.FileInfoService;


@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {
    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery param) {
        return fileInfoMapper.selectList(param);
    }
    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfo> list = this.findListByParam(param);
        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }
    /**
     * 根据条件查询列表
     */
    public Integer findCountByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectCount(param);
    }

    @Override
    public boolean saveBatch(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(FileInfoEntity entity) {
        return false;
    }

    @Override
    public FileInfoEntity getOne(Wrapper<FileInfoEntity> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<FileInfoEntity> getOneOpt(Wrapper<FileInfoEntity> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<FileInfoEntity> queryWrapper) {
        return null;
    }

    @Override
    public <V> V getObj(Wrapper<FileInfoEntity> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<FileInfoEntity> getBaseMapper() {
        return null;
    }

    @Override
    public Class<FileInfoEntity> getEntityClass() {
        return null;
    }
}