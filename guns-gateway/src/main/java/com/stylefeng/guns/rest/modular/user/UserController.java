package com.stylefeng.guns.rest.modular.user;


import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.UserInfoModel;
import com.stylefeng.guns.api.user.UserModel;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {


    @Reference(interfaceClass = UserAPI.class)
    private UserAPI userAPI;

    @RequestMapping(value = "/register", method =  RequestMethod.POST)
    public ResponseVO register(UserModel userModel){
        if(userModel.getUsername() == null || userModel.getUsername().trim().length() == 0){
            return ResponseVO.serviceFail("用户名不为空");
        }
        if(userModel.getPassword() == null || userModel.getPassword().trim().length() == 0){
            return ResponseVO.serviceFail("密码不为空");
        }

        boolean isSuccess = userAPI.register(userModel);

        if(isSuccess){
            return ResponseVO.success("注册成功");
        }else{
            return ResponseVO.serviceFail("注册失败");
        }

    }


    @RequestMapping(value = "check", method = RequestMethod.POST)
    public ResponseVO check(String username){
        if(username != null && username.trim().length() > 0){
            boolean NotExists = userAPI.checkUsername(username);

            if(NotExists){
                return ResponseVO.success("用户名不存在");
            }else{
                return ResponseVO.serviceFail("用户名已存在");
            }
        }else{
            return ResponseVO.serviceFail("用户名不能为空");
        }
    }


    @RequestMapping(value = "logout",method = RequestMethod.GET)
    public ResponseVO logout(){
        return ResponseVO.success("用户退出成功");
    }



    @RequestMapping(value = "getUserInfo",method = RequestMethod.GET)
    public ResponseVO getUserInfo(){
        String userId = CurrentUser.getCurrentUser();
        if(userId != null && userId.trim().length() > 0){

            int uuid = Integer.parseInt(userId);
            UserInfoModel userInfoModel = userAPI.getUserInfo(uuid);
            if(userInfoModel != null){
                return ResponseVO.success(userInfoModel);
            }else{
                return ResponseVO.serviceFail("用户信息查询失败");
            }
        }else{
            return ResponseVO.serviceFail("用户未登录");
        }
    }


    @RequestMapping(value = "updateUserInfo",method = RequestMethod.POST)
    public ResponseVO updateUserInfo(UserInfoModel userInfoModel){
        String userId = CurrentUser.getCurrentUser();
        if(userId != null && userId.trim().length() > 0){

            int uuid = Integer.parseInt(userId);
            if(uuid != userInfoModel.getUuid()){
                System.out.println(uuid+" "+userInfoModel.getUuid());
                return ResponseVO.serviceFail("请修改您个人的信息");
            }

            UserInfoModel userInfo = userAPI.updateUserInfo(userInfoModel);
            if(userInfoModel != null){
                return ResponseVO.success(userInfo);
            }else{
                return ResponseVO.serviceFail("用户信息修改失败");
            }
        }else{
            return ResponseVO.serviceFail("用户未登录");
        }
    }

}