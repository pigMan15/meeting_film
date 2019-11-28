package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Service(interfaceClass = CinemaServiceAPI.class,cache="lru",connections = 10)
public class DefaultCinemaServiceImpl implements CinemaServiceAPI {

    @Autowired
    private MoocCinemaTMapper moocCinemaTMapper;

    @Autowired
    private MoocAreaDictTMapper moocAreaDictTMapper;

    @Autowired
    private MoocBrandDictTMapper moocBrandDictTMapper;

    @Autowired
    private MoocHallDictTMapper moocHallDictTMapper;

    @Autowired
    private MoocHallFilmInfoTMapper moocHallFilmInfoTMapper;

    @Autowired
    private MoocFieldTMapper moocFieldTMapper;

    //1、根据CinemaQueryVO，查询影院列表
    @Override
    public Page<CinemaVO> getCinemas(CinemaQueryVO cinemaQueryVO){
        List<CinemaVO> cinemas = new ArrayList<>();

        Page<MoocCinemaT> page = new Page<>(cinemaQueryVO.getNowPage(),cinemaQueryVO.getPageSize());
        EntityWrapper<MoocCinemaT> entityWrapper = new EntityWrapper<>();
        if(cinemaQueryVO.getBrandId() != 99){
            entityWrapper.eq("brand_id",cinemaQueryVO.getBrandId());
        }
        if(cinemaQueryVO.getDistrictId() != 99){
            entityWrapper.eq("area_id",cinemaQueryVO.getDistrictId());
        }
        if(cinemaQueryVO.getHallType() != 99){
            entityWrapper.like("hall_ids","%#"+cinemaQueryVO.getHallType()+"#%");
        }



        List<MoocCinemaT> moocCinemaTS = moocCinemaTMapper.selectPage(page,entityWrapper);
        for(MoocCinemaT cinemaT : moocCinemaTS) {
            CinemaVO cinemaVO = new CinemaVO();
            cinemaVO.setUuid(cinemaT.getUuid()+"");
            cinemaVO.setAddress(cinemaT.getCinemaAddress());
            cinemaVO.setMinimumPrice(cinemaT.getMinimumPrice()+"");
            cinemaVO.setCinemaName(cinemaT.getCinemaName());
            cinemas.add(cinemaVO);
        }

        long counts = moocCinemaTMapper.selectCount(entityWrapper);

        Page<CinemaVO> result = new Page<>();
        result.setRecords(cinemas);
        result.setSize(cinemaQueryVO.getPageSize());
        result.setTotal(counts);

        return result;
    }
    @Override
    //2、根据条件获取品牌列表
    public List<BrandVO> getBrans(int brandId){
        boolean flag = false;
        List<BrandVO> brandVOS = new ArrayList<>();

        MoocBrandDictT moocBrandDictT = moocBrandDictTMapper.selectById(brandId);
        if(brandId == 99 || moocBrandDictT == null || moocBrandDictT.getUuid() == null){
            flag = true;
        }

        List<MoocBrandDictT> moocBrandDictTS = moocBrandDictTMapper.selectList(null);
        for(MoocBrandDictT brand : moocBrandDictTS) {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(brand.getUuid()+"");
            brandVO.setBrandName(brand.getShowName());

            if(flag){
                if(brand.getUuid() == 99){
                    brandVO.setActive(true);
                }
            }else{
                if (brand.getUuid() == brandId){
                    brandVO.setActive(true);
                }
            }

            brandVOS.add(brandVO);
        }

        return brandVOS;
    }

    @Override
    //3、获取行政区域列表
    public List<AreaVO> getAreas(int areaId){
        boolean flag = false;
        List<AreaVO> areaVOS = new ArrayList<>();

        MoocAreaDictT moocAreaDictT = moocAreaDictTMapper.selectById(areaId);
        if(areaId == 99 || moocAreaDictT == null || moocAreaDictT.getUuid() == null){
            flag = true;
        }

        List<MoocAreaDictT> moocAreaDictTS = moocAreaDictTMapper.selectList(null);
        for(MoocAreaDictT area : moocAreaDictTS) {
            AreaVO areaVO = new AreaVO();
            areaVO.setAreaId(area.getUuid()+"");
            areaVO.setAreaName(area.getShowName());

            if(flag){
                if(area.getUuid() == 99){
                    areaVO.setActive(true);
                }
            }else{
                if (area.getUuid() == areaId){
                    areaVO.setActive(true);
                }
            }

            areaVOS.add(areaVO);
        }

        return areaVOS;
    }
    //4、获取影厅类型列表
    public List<HallTypeVO> getHallTypes(int hallType){
        boolean flag = false;
        List<HallTypeVO> hallTypeVOS = new ArrayList<>();

        MoocHallDictT moocHallDictT = moocHallDictTMapper.selectById(hallType);
        if(hallType == 99 || moocHallDictT == null || moocHallDictT.getUuid() == null){
            flag = true;
        }

        List<MoocHallDictT> moocHallDictTS = moocHallDictTMapper.selectList(null);
        for(MoocHallDictT halltype : moocHallDictTS) {
            HallTypeVO hallTypeVO = new HallTypeVO();
            hallTypeVO.setHalltypeId(halltype.getUuid()+"");
            hallTypeVO.setHalltypeName(halltype.getShowName());

            if(flag){
                if(halltype.getUuid() == 99){
                    hallTypeVO.setActive(true);
                }
            }else{
                if (halltype.getUuid() == hallType){
                    hallTypeVO.setActive(true);
                }
            }

            hallTypeVOS.add(hallTypeVO);
        }

        return hallTypeVOS;
    }
    //5、根据影院编号，获取影院信息
    public CinemaInfoVO getCinemaInfoById(int cinemaId){
        MoocCinemaT moocCinemaT = moocCinemaTMapper.selectById(cinemaId);
        CinemaInfoVO cinemaInfoVO = new CinemaInfoVO();
        cinemaInfoVO.setCinemaAdress(moocCinemaT.getCinemaAddress());
        cinemaInfoVO.setCinemaId(moocCinemaT.getUuid()+"");
        cinemaInfoVO.setCinemaName(moocCinemaT.getCinemaName());
        cinemaInfoVO.setCinemaPhone(moocCinemaT.getCinemaPhone());
        cinemaInfoVO.setImgUrl(moocCinemaT.getImgAddress());
        return cinemaInfoVO;
    }

    //6、获取所有电影的信息和对应的放映场次信息，根据影院编号
    public List<FilmInfoVO> getFilmInfoByCinemaId(int cinemaId){
        List<FilmInfoVO> filmInfos = moocFieldTMapper.getFilmInfos(cinemaId);
        return filmInfos;
    }

    //7、根据放映场次ID获取放映信息
    public HallInfoVO getFilmFieldInfo(int fieldId){
        HallInfoVO hallInfoVO = moocFieldTMapper.getHallInfo(fieldId);
        return hallInfoVO;
    }

    //8、根据放映场次查询播放的电影编号，然后根据电影编号获取对应的电影信息
    public FilmInfoVO getFilmInfoByFieldId(int fieldId){
        FilmInfoVO filmInfoVO = moocFieldTMapper.getFilmInfoById(fieldId);
        return filmInfoVO;
    }

    @Override
    public OrderQueryVO getOrderNeeds(int fieldId) {
        OrderQueryVO orderQueryVO = new OrderQueryVO();
        MoocFieldT moocFieldT = moocFieldTMapper.selectById(fieldId);

        orderQueryVO.setCinemaId(moocFieldT.getCinemaId()+"");
        orderQueryVO.setFilmPrice(moocFieldT.getPrice()+"");
        return orderQueryVO;
    }

}
