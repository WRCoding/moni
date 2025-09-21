package com.longjunwang.moni.mapper;

import com.longjunwang.moni.entity.Recon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReconMapper {

    int insertSelective(Recon recon);

    int updateByPrimaryKeySelective(Recon recon);

    int deleteByPrimaryKey(Integer id);

    Recon selectByPrimaryKey(Integer id);

    List<Recon> selectAll();

    List<Recon> selectByStatus(@Param("status") String status);
}