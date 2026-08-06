package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = RedisConstants.CACHE_SHOPLIST_KEY;

        //redis中有数据从redis中查询
        String shop = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(shop)){
            List<ShopType> typeList = JSONUtil.toList(shop, ShopType.class);
            return Result.ok(typeList);
        }
        //redis中没数据，从数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();
        //数据库无数据时返回异常
        if (typeList == null || typeList.isEmpty()){
            return Result.fail("未找到数据");
        }
        //将查到的列表，遍历放入redis中
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList),
                RedisConstants.CACHE_SHOP_TTL,TimeUnit.MINUTES);
        //返回内容
        return Result.ok(typeList);
    }
}
