package com.stylefeng.guns.rest.common.persistence.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmT;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author pigMan
 * @since 2019-11-26
 */
public interface MoocFilmTMapper extends BaseMapper<MoocFilmT> {

    FilmDetailVO getFilmDetailByName(@Param("filmName") String filmName);

    FilmDetailVO getFilmDetailById(@Param("uuid") String uuid);

}
