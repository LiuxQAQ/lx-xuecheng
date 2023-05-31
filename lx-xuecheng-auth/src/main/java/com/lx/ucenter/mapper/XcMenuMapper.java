package com.lx.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.lx.ucenter.model.entity.XcMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository

public interface XcMenuMapper extends BaseMapper<XcMenu> {
    /**
     * @author lx
     * @date 2023/5/31 20:39
     * @description 查询角色权限
     */
    @Select("SELECT	* FROM xc_menu WHERE id IN (SELECT menu_id FROM xc_permission WHERE role_id IN ( SELECT role_id FROM xc_user_role WHERE user_id = #{userId} ))")
    List<XcMenu> selectPermissionByUserId(@Param("userId") String userId);
}
