package com.demo.mybatis.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务缓存，提供事务性缓存操作
 * 在事务提交前，所有的查询结果都缓存在本地，只有当事务提交时，才会将结果写入二级缓存
 */
public class TransactionalCache implements Cache {
    
    private final Cache delegate;
    private boolean clearOnCommit;
    private final Map<Object, Object> entriesToAddOnCommit;
    private boolean readOnly;
    
    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<>();
        this.readOnly = false;
    }
    
    @Override
    public String getId() {
        return delegate.getId();
    }
    
    @Override
    public void putObject(Object key, Object value) {
        // 延迟写入，先放入暂存区
        entriesToAddOnCommit.put(key, value);
    }
    
    @Override
    public Object getObject(Object key) {
        // 从委托缓存中获取
        return delegate.getObject(key);
    }
    
    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }
    
    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }
    
    @Override
    public int getSize() {
        return delegate.getSize();
    }
    
    /**
     * 提交事务，将暂存的数据写入委托缓存
     */
    public void commit() {
        if (clearOnCommit) {
            delegate.clear();
        }
        flushPendingEntries();
        reset();
    }
    
    /**
     * 回滚事务，清空暂存的数据
     */
    public void rollback() {
        reset();
    }
    
    /**
     * 将暂存的数据写入委托缓存
     */
    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 重置状态
     */
    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
    }
    
    /**
     * 设置是否只读
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    /**
     * 是否只读
     */
    public boolean isReadOnly() {
        return readOnly;
    }
}
