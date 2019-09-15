package cn.itcast.haoke.dubbo.server.service;

import cn.itcast.haoke.dubbo.server.pojo.HouseResources;
import cn.itcast.haoke.dubbo.server.vo.PageInfo;

public interface HouseResourcesService {

    /**
     * @param houseResources
     *
     * @return -1:输入的参数不符合要求，0：数据插入数据库失败，1：成功
     */
    int saveHouseResources(HouseResources houseResources);

    PageInfo<HouseResources> queryHouseResourcesList(int page, int pageSize, HouseResources queryCondition);

    /**
     * 根据房源id查询房源数据
     *
     * @param id
     * @return
     */
    HouseResources queryHouseResourcesById(Long id);

    /**
     * 更新房源数据
     *
     * @param houseResources
     * @return
     */
    boolean updateHouseResources(HouseResources houseResources);
}