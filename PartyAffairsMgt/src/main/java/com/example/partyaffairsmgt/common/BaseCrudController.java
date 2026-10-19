package com.example.partyaffairsmgt.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 党务模块通用 CRUD 控制器。
 * 列表查询：查询参数名与实体属性名一致即按等值过滤；属性名 + "Like" 后缀按 LIKE 过滤。
 * 例：?status=1 等值；?nameLike=党委 模糊。
 */
public abstract class BaseCrudController<T> {

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected BaseCrudController() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected abstract BaseMapper<T> getMapper();

    @GetMapping("/list")
    public ResponseResult<Page<T>> list(@RequestParam(required = false) Map<String, Object> params,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (params != null && tableInfo != null) {
            for (TableFieldInfo fi : tableInfo.getFieldList()) {
                Object eqValue = params.get(fi.getProperty());
                if (eqValue != null && !eqValue.toString().trim().isEmpty()) {
                    qw.eq(fi.getColumn(), eqValue);
                }
                Object likeValue = params.get(fi.getProperty() + "Like");
                if (likeValue != null && !likeValue.toString().trim().isEmpty()) {
                    qw.like(fi.getColumn(), likeValue);
                }
            }
        }
        if (tableInfo != null && tableInfo.getKeyColumn() != null) {
            qw.orderByDesc(tableInfo.getKeyColumn());
        }
        return ResponseResult.success(getMapper().selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/{id}")
    public ResponseResult<T> getById(@PathVariable Long id) {
        return ResponseResult.success(getMapper().selectById(id));
    }

    @PostMapping
    public ResponseResult<T> create(@RequestBody T entity) {
        setField(entity, "id", null);
        setField(entity, "deleted", null);
        LocalDateTime now = LocalDateTime.now();
        setField(entity, "createTime", now);
        setField(entity, "updateTime", now);
        getMapper().insert(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<T> update(@PathVariable Long id, @RequestBody T entity) {
        setField(entity, "id", id);
        setField(entity, "updateTime", LocalDateTime.now());
        getMapper().updateById(entity);
        return ResponseResult.success(getMapper().selectById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(getMapper().deleteById(id) > 0);
    }

    private void setField(T entity, String name, Object value) {
        try {
            Field field = findField(entity.getClass(), name);
            if (field != null && (value == null || field.getType().isInstance(value))) {
                field.setAccessible(true);
                field.set(entity, value);
            }
        } catch (Exception ignored) {
            // 实体不含该字段时跳过
        }
    }

    private Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
