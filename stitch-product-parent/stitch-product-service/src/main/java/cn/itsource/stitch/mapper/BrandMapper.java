package cn.itsource.stitch.mapper;

import cn.itsource.stitch.domain.Brand;
import cn.itsource.stitch.query.BrandQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 品牌信息 Mapper 接口
 * </p>
 *
 * @author å²è¿ªä»
 * @since 2019-07-31
 */
public interface BrandMapper extends BaseMapper<Brand> {


    IPage<Brand> queryPage(Page<Brand> page, @Param("query") BrandQuery query);
}
