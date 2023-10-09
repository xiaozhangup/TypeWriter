package me.gabber235.typewriter.utils

import com.google.common.collect.Maps
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 冷却工具
 *
 * @author sky
 * @since 2020-10-02 04:35
 */
class Baffle(private val millis: Long) {
    private val data: MutableMap<String, Long> = Maps.newConcurrentMap()
    private var globalTime: Long = 0

    /**
     * 获取下次执行时间戳，该方法不会刷新数据
     *
     * @param id 个体序号
     * @return long
     */
    fun nextTime(id: String): Long {
        val result: Long = if (id == "*") {
            globalTime + millis - System.currentTimeMillis()
        } else {
            data.getOrDefault(id, 0L) + millis - System.currentTimeMillis()
        }
        return if (result >= 0) result else 0L
    }

    fun resetAll() {
        data.clear()
        globalTime = 0L
    }

    fun reset(id: String) {
        if (id == "*") {
            globalTime = 0L
        } else {
            data.remove(id)
        }
    }

    fun next(id: String) {
        if (id == "*") {
            globalTime = System.currentTimeMillis()
        } else {
            data[id] = System.currentTimeMillis()
        }
    }

    fun hasNext(id: String, update: Boolean = true): Boolean {
        val time: Long = if (id == "*") {
            globalTime
        } else {
            data.getOrDefault(id, 0L)
        }
        if (time + millis < System.currentTimeMillis()) {
            if (update) {
                next(id)
            }
            return true
        }
        return false
    }
}