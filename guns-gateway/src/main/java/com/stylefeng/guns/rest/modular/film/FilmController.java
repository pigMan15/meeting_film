package com.stylefeng.guns.rest.modular.film;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmIndexVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequestMapping(value = "/film/")
@RestController
public class FilmController {

    private static final String  imgPre = "http://img.meetingshop.cn/";

    @Reference(interfaceClass = FilmServiceApi.class)
    private FilmServiceApi filmServiceApi;

    @Reference(interfaceClass = FilmAsyncServiceApi.class,async = true)
    private FilmAsyncServiceApi filmAsyncServiceApi;

    @RequestMapping(value = "getIndex", method = RequestMethod.GET)
    public ResponseVO<FilmIndexVO> getIndex(){


        FilmIndexVO filmIndexVO = new FilmIndexVO();
        filmIndexVO.setBanners(filmServiceApi.getBanners());
        filmIndexVO.setHotFilms(filmServiceApi.getHotFilms(true,8,1,1,99,99,99));
        filmIndexVO.setSoonFilms(filmServiceApi.getSoonFilms(true,8,1,1,99,99,99));
        filmIndexVO.setBoxRanking(filmServiceApi.getBoxRanking());
        filmIndexVO.setExpectRanking(filmServiceApi.getExpectRanking());
        filmIndexVO.setTop100(filmServiceApi.getTop());
        return ResponseVO.success(imgPre,filmIndexVO) ;
    }


    @RequestMapping(value = "getConditionList",method = RequestMethod.GET)
    public ResponseVO getConditionList(@RequestParam(name = "catId",required = false,defaultValue = "99") String catId,
                                       @RequestParam(name = "sourceId",required = false,defaultValue = "99")String sourceId,
                                       @RequestParam(name = "yearId",required = false,defaultValue = "99")String yearId){



        FilmConditionVO filmConditionVO = new FilmConditionVO();

        //类型集合

        boolean flag = false;
        List<CatVO> cats = filmServiceApi.getCats();
        List<CatVO> catResult = new ArrayList<>();
        CatVO cat = null;
        for(CatVO catVO : cats){
            if(catVO.getCatId().equals("99")){
                cat = catVO;
                continue;
            }

            if(catVO.getCatId().equals(catId)){
                flag = true;
                catVO.setActive(true);
            }else{
                catVO.setActive(false);
            }
            catResult.add(catVO);


        }
        if(!flag){
            cat.setActive(true);
            catResult.add(cat);
        }else{
            cat.setActive(false);
            catResult.add(cat);
        }
        //片源集合

        flag = false;
        List<SourceVO> sources = filmServiceApi.getSources();
        List<SourceVO> sourceResult = new ArrayList<>();
        SourceVO sourceVO = null;
        for(SourceVO source : sources){
            if(source.getSourceId().equals("99")){
                sourceVO = source;
                continue;
            }

            if(source.getSourceId().equals(sourceId)){
                flag = true;
                source.setActive(true);
            }else{
                source.setActive(false);
            }
            sourceResult.add(source);


        }
        if(!flag){
            sourceVO.setActive(true);
            sourceResult.add(sourceVO);
        }else{
            sourceVO.setActive(false);
            sourceResult.add(sourceVO);
        }
        //年代集合

        flag = false;
        List<YearVO> years = filmServiceApi.getYears();
        List<YearVO> yearResult = new ArrayList<>();
        YearVO yearVO = null;
        for(YearVO year : years){
            if(year.getYearId().equals("99")){
                yearVO = year;
                continue;
            }

            if(year.getYearId().equals(yearId)){
                flag = true;
                year.setActive(true);
            }else{
                year.setActive(false);
            }
            yearResult.add(year);


        }
        if(!flag){
            yearVO.setActive(true);
            yearResult.add(yearVO);
        }else{
            yearVO.setActive(false);
            yearResult.add(yearVO);
        }
        filmConditionVO.setCatInfo(catResult);
        filmConditionVO.setSourceInfo(sourceResult);
        filmConditionVO.setYearInfo(yearResult);
        return ResponseVO.success(filmConditionVO);

    }



    @RequestMapping(value = "getFilms",method = RequestMethod.GET)
    public ResponseVO getFilms(FilmRequestVO filmRequestVO) {
        FilmVO filmVO = null;
        switch (filmRequestVO.getShowType()) {
            case 1:
                filmVO = filmServiceApi.getHotFilms(false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(), filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
            case 2:
                filmVO = filmServiceApi.getSoonFilms(false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(), filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
            case 3:
                filmVO = filmServiceApi.getClassicFilms(filmRequestVO.getPageSize(), filmRequestVO.getNowPage(), filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
            default:
                filmVO = filmServiceApi.getHotFilms(false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(), filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
        }
        return ResponseVO.success(imgPre,filmVO.getNowPage(),filmVO.getTotalPage(),filmVO.getFilmInfo());
    }


    @RequestMapping(value = "films/{searchParam}",method = RequestMethod.GET)
    public ResponseVO films(@PathVariable("searchParam") String searchParam,int searchType) throws ExecutionException, InterruptedException {
        //根据searchType,判断查询类型

        FilmDetailVO filmDetailVO = filmServiceApi.getFilmDetail(searchType,searchParam);

        if(filmDetailVO == null){
            return ResponseVO.serviceFail("没有可查询的影片");
        }else if(filmDetailVO.getFilmId() == null || filmDetailVO.getFilmId().trim().length() == 0){
            return ResponseVO.serviceFail("没有可查询的影片");
        }

        //不同的查询类型，传入条件不一样

        //查询影片的详细信息 Dubbo异步获取
        String filmId = filmDetailVO.getFilmId();


        //获取影片描述信息
//        FilmDescVO filmDescVO = filmAsyncServiceApi.getFilmDesc(filmId);
        filmAsyncServiceApi.getFilmDesc(filmId);
        Future<FilmDescVO> filmDescVOFuture = RpcContext.getContext().getFuture();

        //获取图片信息
        filmAsyncServiceApi.getImgs(filmId);
        Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();
        //获取导演信息
        filmAsyncServiceApi.getDectInfo(filmId);
        Future<ActorVO> directorVOFuture = RpcContext.getContext().getFuture();

        //获取演员信息
        filmAsyncServiceApi.getActors(filmId);
        Future<List<ActorVO>> actorsVOFuture = RpcContext.getContext().getFuture();


        //组织Info属性
        InfoRequestVO infoRequestVO = new InfoRequestVO();

        //组织Actor属性
        ActorRequestVO actorRequestVO = new ActorRequestVO();
        actorRequestVO.setActors(actorsVOFuture.get());
        actorRequestVO.setDirector(directorVOFuture.get());

        infoRequestVO.setActors(actorRequestVO);
        infoRequestVO.setImgVO(imgVOFuture.get());
        infoRequestVO.setFilmId(filmId);
        infoRequestVO.setBiography(filmDescVOFuture.get().getBiography());

        filmDetailVO.setInfo04(infoRequestVO);

        return ResponseVO.success(imgPre,filmDetailVO);
    }


}
