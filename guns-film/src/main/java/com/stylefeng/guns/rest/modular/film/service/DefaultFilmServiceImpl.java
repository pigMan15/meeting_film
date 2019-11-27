package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.FilteredImageSource;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;


@Component
@Service(interfaceClass =  FilmServiceApi.class, loadbalance="roundrobin")
public class DefaultFilmServiceImpl implements FilmServiceApi {


    @Autowired
    private MoocFilmTMapper moocFilmTMapper;

    @Autowired
    private MoocBannerTMapper moocBannerTMapper;

    @Autowired
    private MoocCatDictTMapper moocCatDictTMapper;

    @Autowired
    private MoocYearDictTMapper moocYearDictTMapper;

    @Autowired
    private MoocSourceDictTMapper moocSourceDictTMapper;

    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;

    @Autowired
    private MoocActorTMapper moocActorTMapper;

    @Override
    public List<BannerVO> getBanners() {
        EntityWrapper<MoocBannerT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("is_valid",0);
        Page<MoocBannerT> page = new Page<>(1,5);
        List<MoocBannerT> moocBannerTS = moocBannerTMapper.selectPage(page,entityWrapper);
        List<BannerVO> banners = new ArrayList<>();
        for(MoocBannerT moocBannerT : moocBannerTS){
            BannerVO bannerVO = new BannerVO();
            bannerVO.setBannerId(String.valueOf(moocBannerT.getUuid()));
            bannerVO.setBannerUrl(moocBannerT.getBannerUrl());
            bannerVO.setBannerAddress(moocBannerT.getBannerAddress());
            banners.add(bannerVO);
        }
        return banners;
    }

    @Override
    public FilmVO getHotFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();


        //热映影片的限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","1");

        if(isLimit){
            //首页
            Page<MoocFilmT> page = new Page<>(1,nums);
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilms);
            filmVO.setFilmNum(moocFilms.size());
            filmVO.setFilmInfo(filmInfos);
        }else{
            //列表页

            //排序
            Page<MoocFilmT> page = null;

            switch(sortId){
                case 1:
                    page = new Page<>(nowPage,nums,"film_box_office");
                    break;
                case 2:
                    page = new Page<>(nowPage,nums,"film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage,nums,"film_score");
                    break;
                default:
                    page = new Page<>(nowPage,nums,"film_box_office");
                    break;
            }

            if(sourceId != 99){
                entityWrapper.eq("film_source",sourceId);
            }
            if(yearId != 99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId != 99){
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }



            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilms);
            filmVO.setFilmNum(moocFilms.size());

            int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts/nums)+1;


            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);

        }




        return filmVO;
    }

    @Override
    public FilmVO getSoonFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();


        //热映影片的限制条件
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","2");

        if(isLimit){
            Page<MoocFilmT> page = new Page<>(1,nums);
            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilms);
            filmVO.setFilmNum(moocFilms.size());
            filmVO.setFilmInfo(filmInfos);
        }else{
            //列表页


            Page<MoocFilmT> page = null;

            switch(sortId){
                case 1:
                    page = new Page<>(nowPage,nums,"film_preSaleNum");
                    break;
                case 2:
                    page = new Page<>(nowPage,nums,"film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage,nums,"film_preSaleNum");
                    break;
                default:
                    page = new Page<>(nowPage,nums,"film_preSaleNum");
                    break;
            }

            if(sourceId != 99){
                entityWrapper.eq("film_source",sourceId);
            }
            if(yearId != 99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId != 99){
                String catStr = "%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }



            List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
            filmInfos = getFilmInfos(moocFilms);
            filmVO.setFilmNum(moocFilms.size());

            int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts/nums)+1;


            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);
        }




        return filmVO;
    }

    @Override
    public FilmVO getClassicFilms(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {

        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos =  new ArrayList<>();


        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper();
        entityWrapper.eq("film_status","3");
        //列表页

        Page<MoocFilmT> page = null;

        switch(sortId){
            case 1:
                page = new Page<>(nowPage,nums,"film_box_office");
                break;
            case 2:
                page = new Page<>(nowPage,nums,"film_time");
                break;
            case 3:
                page = new Page<>(nowPage,nums,"film_score");
                break;
            default:
                page = new Page<>(nowPage,nums,"film_box_office");
                break;
        }


        if(sourceId != 99){
            entityWrapper.eq("film_source",sourceId);
        }
        if(yearId != 99){
            entityWrapper.eq("film_date",yearId);
        }
        if(catId != 99){
            String catStr = "%#"+catId+"#%";
            entityWrapper.like("film_cats",catStr);
        }



        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);
        filmInfos = getFilmInfos(moocFilms);
        filmVO.setFilmNum(moocFilms.size());

        int totalCounts = moocFilmTMapper.selectCount(entityWrapper);
        int totalPages = (totalCounts/nums)+1;


        filmVO.setFilmInfo(filmInfos);
        filmVO.setTotalPage(totalPages);
        filmVO.setNowPage(nowPage);

        return filmVO;
    }

    private List<FilmInfo> getFilmInfos(List<MoocFilmT> moocFilms){
        List<FilmInfo> filmInfos = new ArrayList<>();
        for(MoocFilmT moocFilmT : moocFilms){
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setScore(moocFilmT.getFilmScore());
            filmInfo.setImgAddress(moocFilmT.getImgAddress());
            filmInfo.setFilmType(String.valueOf(moocFilmT.getFilmType()));
            filmInfo.setFilmScore(moocFilmT.getFilmScore());
            filmInfo.setFilmName(moocFilmT.getFilmName());
            filmInfo.setFilmId(moocFilmT.getUuid()+"");
            filmInfo.setExpectNum(moocFilmT.getFilmPresalenum());
            filmInfo.setBoxNum(moocFilmT.getFilmBoxOffice());
            filmInfo.setShowTime(DateUtil.getDay(moocFilmT.getFilmTime()));

            filmInfos.add(filmInfo);
        }
        return filmInfos;
    }




    @Override
    public List<FilmInfo> getBoxRanking() {

        //正在上映的，票房前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","1");

        Page<MoocFilmT> page = new Page<>(1,10,"film_box_office");

        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List<FilmInfo> getExpectRanking() {
        //即将上映的，预售前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","2");

        Page<MoocFilmT> page = new Page<>(1,10,"film_preSaleNum");

        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List<FilmInfo> getTop() {
        //经典影片，评分前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status","2");
            Page<MoocFilmT> page = new Page<>(1,10,"film_score");

        List<MoocFilmT> moocFilms = moocFilmTMapper.selectPage(page,entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List<CatVO> getCats() {
        List<CatVO> cats = new ArrayList<>();
        List<MoocCatDictT> moocCats = moocCatDictTMapper.selectList(null);
        for(MoocCatDictT moocCatDictT : moocCats){
            CatVO catVO = new CatVO();
            catVO.setCatId(String.valueOf(moocCatDictT.getUuid()));
            catVO.setCatName(moocCatDictT.getShowName());
            cats.add(catVO);
        }
        return cats;
    }

    @Override
    public List<SourceVO> getSources() {
        List<SourceVO> sources = new ArrayList<>();
        List<MoocSourceDictT> moocSources = moocSourceDictTMapper.selectList(null);
        for(MoocSourceDictT moocSourceDictT : moocSources){
            SourceVO sourceVO = new SourceVO();
            sourceVO.setSourceId(String.valueOf(moocSourceDictT.getUuid()));
            sourceVO.setSourceName(moocSourceDictT.getShowName());
            sources.add(sourceVO);
        }
        return sources;
    }

    @Override
    public List<YearVO> getYears() {

        List<YearVO> years = new ArrayList<>();
        List<MoocYearDictT> moocYears = moocYearDictTMapper.selectList(null);
        for(MoocYearDictT moocYearDictT : moocYears){
            YearVO yearVO = new YearVO();
            yearVO.setYearId(String.valueOf(moocYearDictT.getUuid()));
            yearVO.setYearName(moocYearDictT.getShowName());
            years.add(yearVO);
        }
        return years;
    }

    @Override
    public FilmDetailVO getFilmDetail(int searchType, String searchParam) {

        // Type:1 按名称  2 按ID
        FilmDetailVO filmDetailVO = null;
        if(searchType == 1){
            filmDetailVO =  moocFilmTMapper.getFilmDetailByName("%"+searchParam+"%");
        }else{
            filmDetailVO = moocFilmTMapper.getFilmDetailById(searchParam);
        }

        return filmDetailVO;
    }

    private MoocFilmInfoT getFilmInfo(String filmId){
        MoocFilmInfoT moocFilmInfoT = new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);
        return moocFilmInfoTMapper.selectOne(moocFilmInfoT);
    }

    @Override
    public FilmDescVO getFilmDesc(String filmId) {
        MoocFilmInfoT moocFilmInfoT = getFilmInfo(filmId);
        FilmDescVO filmDescVO = new FilmDescVO();
        filmDescVO.setBiography(moocFilmInfoT.getBiography());
        filmDescVO.setFilmId(moocFilmInfoT.getFilmId());
        return filmDescVO;
    }

    @Override
    public ImgVO getImgs(String filmId) {
        MoocFilmInfoT moocFilmInfoT = getFilmInfo(filmId);
        String filmImgStr = moocFilmInfoT.getFilmImgs();
        String[] filmImgs = filmImgStr.split(",");

        ImgVO imgVO = new ImgVO();
        imgVO.setMainImg(filmImgs[0]);
        imgVO.setImg01(filmImgs[1]);
        imgVO.setImg02(filmImgs[2]);
        imgVO.setImg03(filmImgs[3]);
        imgVO.setImg04(filmImgs[4]);
        return imgVO;
    }

    @Override
    public ActorVO getDectInfo(String filmId) {
        MoocFilmInfoT moocFilmInfoT = getFilmInfo(filmId);

        Integer directId = moocFilmInfoT.getDirectorId();
        MoocActorT moocActorT = moocActorTMapper.selectById(directId);
        ActorVO actorVO = new ActorVO();
        actorVO.setImgAddress(moocActorT.getActorImg());
        actorVO.setDirectotName(moocActorT.getActorName());
        return actorVO;
    }

    @Override
    public List<ActorVO> getActors(String filmId) {

        List<ActorVO> actorVOS = moocActorTMapper.getActors(filmId);

        return actorVOS;
    }


}
