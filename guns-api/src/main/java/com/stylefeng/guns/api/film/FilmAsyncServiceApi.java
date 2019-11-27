package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

public interface FilmAsyncServiceApi {



    //获取影片相关的其他信息【演员表】
    FilmDescVO getFilmDesc(String filmId);

    //获取图片信息
    ImgVO getImgs(String filmId);

    //获取导演信息
    ActorVO getDectInfo(String filmId);

    //获取演员信息
    List<ActorVO> getActors(String filmId);
}
