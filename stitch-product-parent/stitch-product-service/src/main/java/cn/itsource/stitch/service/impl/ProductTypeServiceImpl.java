package cn.itsource.stitch.service.impl;

import cn.itsource.stitch.client.RedisClient;
import cn.itsource.stitch.client.StaticPageClient;
import cn.itsource.stitch.domain.ProductType;
import cn.itsource.stitch.mapper.ProductTypeMapper;
import cn.itsource.stitch.service.IProductTypeService;
import cn.itsource.stitch.util.AjaxResult;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品目录 服务实现类
 * </p>
 *
 * @author å²è¿ªä»
 * @since 2019-07-31
 */
@Service
public class ProductTypeServiceImpl extends ServiceImpl<ProductTypeMapper, ProductType> implements IProductTypeService {



    @Autowired
    private RedisClient redisClient;

    @Autowired
    private StaticPageClient staticPageClient;



    @Override
    public void genHomePage(){
        Map<String,Object> map = new HashMap<>();

        String templatePath = "E:\\GITHub\\stitch-parent\\stitch-product-parent\\stitch-product-service\\src\\main\\resources\\template\\product.type.vm";
        String targetPath = "E:\\GITHub\\stitch-parent\\stitch-product-parent\\stitch-product-service\\src\\main\\resources\\template\\product.type.vm.html";
        //model 就是List 存放所有的商品类型
        List<ProductType> productTypes = loadTypeTree();
        map.put("model",productTypes);
        map.put("templatePath",templatePath);
        map.put("targetPath",targetPath);
        staticPageClient.genStaticPage(map);

        //第二步 ： 生成home.html
        map = new HashMap<>();
        templatePath = "E:\\GITHub\\stitch-parent\\stitch-product-parent\\stitch-product-service\\src\\main\\resources\\template\\home.vm";
        targetPath = "E:\\GITHub\\stitch-web-parent\\stitch-web-home\\home.html";
        //model 中要有一个数据是staticRoot
        Map<String,String> model = new HashMap<>();
        model.put("staticRoot","E:\\GITHub\\stitch-parent\\stitch-product-parent\\stitch-product-service\\src\\main\\resources\\");
        map.put("model",model);
        map.put("templatePath",templatePath);
        map.put("targetPath",targetPath);

        staticPageClient.genStaticPage(map);
    }



    @Override
    public List<ProductType> loadTypeTree() {
        //从redis中获取数据
        AjaxResult result = redisClient.get("productTypes");
        String productTypesJsonStr = (String) result.getRestObj();
        List<ProductType> productTypes = JSON.parseArray(productTypesJsonStr, ProductType.class);
        //判断是否有值
        if(productTypes==null||productTypes.size()<=0){
            //没有则查询数据库，并将数据缓存到redis中
            productTypes = loop();
            redisClient.set("productTypes", JSON.toJSONString(productTypes));
        }
        //返回数据
        return productTypes;
    }

        /**
         * 循环方式
         * @return
         */
        private List<ProductType> loop(){
            List<ProductType> productTypes = baseMapper.selectList(null);
            //定义一个List存放一级菜单
            List<ProductType> list = new ArrayList<>();
            //定义一个Map存放所有的ProductType，key是id，value是类型对象
            Map<Long,ProductType> map = new HashMap<>();
            for (ProductType pt : productTypes) {
                map.put(pt.getId(),pt);
            }
            //循环
            for (ProductType productType : productTypes) {
                if(productType.getPid()==0){
                    list.add(productType);
                }else{
                    ProductType parent = map.get(productType.getPid());
                    parent.getChildren().add(productType);
                }
            }
            return list;
        }

        /**
         * 递归方式实现加载类型树
         * 缺点：
         * （1）性能很低，要发送多次sql
         * （2）递归的深度可能会导致栈溢出
         *
         * @return
         */
        private List<ProductType> recursive(Long id) {
            //查询所有一级菜单
            List<ProductType> parents = baseMapper.selectList(new QueryWrapper<ProductType>().eq("pid", id));
            for (ProductType parent : parents) {
                //取到所有的子
                List<ProductType> children = recursive(parent.getId());
                if (!children.isEmpty()) {
                    parent.setChildren(children);
                }
            }
            return parents;

        }
}

