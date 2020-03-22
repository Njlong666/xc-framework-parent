package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;

import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/****
 * 查询CmsSite 下拉列表
 */
@Service
public class CmsSiteService {


    @Autowired
    CmsSiteRepository cmsSiteRepository;

    public List<CmsSite> findAll(){
       return cmsSiteRepository.findAll();
    }
}
