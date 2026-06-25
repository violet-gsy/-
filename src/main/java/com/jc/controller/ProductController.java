package com.jc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jc.allenum.FacilityTypeEnum;
import com.jc.allenum.ProductTypeEnum;
import com.jc.allenum.StatusEnum;
import com.jc.entity.Product;
import com.jc.entity.ProductComponent;
import com.jc.service.ProductComponentService;
import com.jc.service.ProductService;
import com.jc.util.ApiResponse;
import com.jc.vo.TreeComponentVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@Slf4j
@Tag(name = "产品类")
public class ProductController extends BaseController<ProductService, Product> {
    public ProductController(ProductService service) {
        super(service);
    }

    @Resource
    ProductComponentService componentService;

    // 自带全部CRUD 继承basecontroller


    @Operation(summary = "查看子组件")
    @GetMapping("/selChildComponent")
    public ApiResponse<List<TreeComponentVo>> selChildComponent(@RequestParam String productid) {
       List<ProductComponent> components = componentService.list(new QueryWrapper<ProductComponent>().eq("productid",productid));

        // 1. 复制并清洗数据（去除 id 和 parentid 的前后空格）
        List<TreeComponentVo> treeComponentVos = components.stream()
                .map(productComponent -> {
                    TreeComponentVo vo = new TreeComponentVo();
                    BeanUtils.copyProperties(productComponent, vo);
                    // 关键：trim 掉空格
                    if (vo.getId() != null) vo.setId(vo.getId().trim());
                    if (vo.getParentid() != null) vo.setParentid(vo.getParentid().trim());
                    return vo;
                })
                .collect(Collectors.toList());

        // 2. 按 parentId 分组（仅对非空且非空字符串的 parentId 分组）
        Map<String, List<TreeComponentVo>> groupByParent = treeComponentVos.stream()
                .filter(node -> node.getParentid() != null && !node.getParentid().isEmpty())
                .collect(Collectors.groupingBy(TreeComponentVo::getParentid));

        // 3. 为每个节点设置子节点
        treeComponentVos.forEach(node -> {
            List<TreeComponentVo> children = groupByParent.getOrDefault(node.getId(), new ArrayList<>());
            node.setChildren(children);
        });

        // 4. 过滤出根节点（parentId 为 null 或空字符串，或 parentId 不在所有节点 ID 集合中）
        Set<String> allIds = treeComponentVos.stream()
                .map(TreeComponentVo::getId)
                .collect(Collectors.toSet());

        List<TreeComponentVo> roots = treeComponentVos.stream()
                .filter(node -> {
                    String pid = node.getParentid();
                    return pid == null || pid.isEmpty() || !allIds.contains(pid);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(roots);
    }
}