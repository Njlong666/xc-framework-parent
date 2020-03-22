package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;


    //查询xc_user信息
    public XcUser findXcUserByUsername(String username){
        XcUser xcUser = xcUserRepository.findXcUserByUsername(username);
        return xcUser;
    }

    //根据用户名查询xc_user信息
    public XcUserExt getUserExt(String username){
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null){
            return null;
        }

        //获取到user_id
        String userId = xcUser.getId();
        //查询用户权限
        List<XcMenu> xcMenuList = xcMenuMapper.findPrmissionByUserId(userId);
        //根据userId查询公司id
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findXcCompanyUserByUserId(userId);
        String companyId = null;
        if (xcCompanyUser != null){
            //公司id
            companyId = xcCompanyUser.getCompanyId();

        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(companyId);
        //用户权限
        xcUserExt.setPermissions(xcMenuList);

        return xcUserExt;
    }

}
