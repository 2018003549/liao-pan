package com.study.liao.dao;

import com.study.liao.entity.FileInfoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件信息 数据库操作接口
 */
public interface FileInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据FileIdAndUserId更新
     */
    Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId删除
     */
    Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId获取对象
     */
    T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    void updateFileStatusWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") T t,
                                       @Param("oldStatus") Integer oldStatus);


    Long selectUseSpace(@Param("userId") String userId);

    void deleteFileByUserId(@Param("userId") String userId);

    /**
     * 根据筛选条件去批量修改指定目录的删除标识
     *
     * @param fileInfo    修改信息
     * @param filePidList 根据父id，即按照目录来筛选，将指定目录下的所有文件修改状态
     * @param fileIdList  根据文件id，修改指定文件id的文件状态，和filePidList参数只能有一个生效
     * @param oldDelFlag  旧的删除标识，已经删除过的文件没必要删除
     */
    void updateFileDelFlagBatch(@Param("bean") FileInfoEntity fileInfo, @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList, @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);

    /**
     * 根据删选条件批量从数据库删除文件记录
     * @param filePidList 根据父id，即按照目录来筛选，将指定目录下的所有文件修改状态
     * @param fileIdList  根据文件id，修改指定文件id的文件状态，和filePidList参数只能有一个生效
     * @param oldDelFlag  旧的删除标识，已经删除过的文件没必要删除
     */
    void delFileBatch(@Param("userId") String userId,
                      @Param("filePidList") List<String> filePidList,
                      @Param("fileIdList") List<String> fileIdList,
                      @Param("oldDelFlag") Integer oldDelFlag);

}
