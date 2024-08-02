package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper menuMapper;

    /**
     * @description 根據賬號查詢用戶信息
     * @param s  賬號 ->AuthParamsDto 型別的json數據
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author Ian
     * @date 2023/7/23
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;
        try {
            //將認證參數轉為AuthParamsDto類型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("認證請求不符合項目要求:{}",s);
            throw new RuntimeException("認證請求數據格式不對");
        }
        //認證方法
        String authType = authParamsDto.getAuthType();
        AuthService authService =  applicationContext.getBean(authType + "_authservice",AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }


    /**
     * @description 查詢用戶信息
     * @param user  用戶id，主鍵
     * @return com.xuecheng.ucenter.model.po.XcUser 用戶信息
     * @author Mr.M
     * @date 2022/9/29 12:19
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        String password = user.getPassword();
        //查詢用戶權限
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size() <= 0) {
            //用戶權限,如果不加則報Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        } else {
            xcMenus.forEach(menu -> {
                permissions.add(menu.getCode());
            });
        }
        //將用戶權限放在XcUserExt中
        user.setPermissions(permissions);

        //為了安全在令牌中不放密碼
        user.setPassword(null);
        //將user對象轉json
        String userString = JSON.toJSONString(user);
        String[] authorities = permissions.toArray(new String[0]);
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;

    }
}

