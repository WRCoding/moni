package com.longjunwang.moni.mapper;

import com.longjunwang.moni.entity.Expense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpenseMapper {
    int insertSelective(Expense expense);

    int updateSelective(Expense expense);

    int deleteById(@Param("id") String id);

    Expense selectById(@Param("id") String id);

    List<Expense> selectByCondition(Expense Expense);

    List<Expense> selectByRangeDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<Expense> selectByRangeDateAndRemark(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("remark")String remark);

    List<Expense> selectByIds(@Param("ids") List<String> ids);
}
