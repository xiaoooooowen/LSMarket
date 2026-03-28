package com.lsmarket.service;

import com.lsmarket.dto.Result;
import com.lsmarket.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();
}


