package com.stylefeng.guns.core.util;

public class TokenBucketUtil {

    private int bucketNums = 200; //桶容量
    private int rate = 1;         //流出速率
    private int nowTokens;        //当前令牌数
    private long timestamp=getNowTime(); //上一次获取令牌时间

    private long getNowTime(){
        return System.currentTimeMillis();
    }

    private int min(int tokens){
        if(tokens < bucketNums){
            return tokens;
        }else{
            return bucketNums;
        }
    }

    public boolean getToken(){
        //记录来拿令牌的时间
        long nowTime = getNowTime();

        //System.out.println(timestamp+" "+nowTime);

        //当前的令牌数
        nowTokens =(nowTokens+ (int) (nowTime-timestamp)*rate);
        //判断令牌数
        nowTokens = min(nowTokens);
        System.out.println("令牌数: "+nowTokens);
        //更新timestamp
        timestamp = nowTime;


        if(nowTokens < 1){
            return false;
        }else{
            nowTokens-=1;
            return true;
        }
    }



}
