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
open class Baffle {
    /**
     * 重置所有数据
     */
    open fun resetAll() {}

    /**
     * 重置个体的执行缓存
     *
     * @param id 个体序号
     */
    open fun reset(id: String) {}

    /**
     * 强制个体更新数据
     *
     * @param id 个体序号
     */
    open fun next(id: String) {}

    /**
     * 验证个体的执行结果
     *
     * @param id     个体序号
     * @param update 是否更新数据
     * @return 是否运行
     */
    open fun hasNext(id: String, update: Boolean): Boolean { return false }

    /**
     * 同 [Baffle.next]，个体序号为（*）
     */
    open fun reset() {
        reset("*")
    }

    /**
     * 同 [Baffle.next]，个体序号为（*）
     */
    operator fun next() {
        next("*")
    }
    /**
     * 同 [Baffle.hasNext]
     *
     * @param id 序号
     * @return boolean
     */
    /**
     * 同 [Baffle.hasNext]，个体序号为（*）
     *
     * @return boolean
     */
    @JvmOverloads
    fun hasNext(id: String = "*"): Boolean {
        return hasNext(id, true)
    }

    class BaffleTime(private val millis: Long) : Baffle() {
        private val data: MutableMap<String, Long> = Maps.newConcurrentMap()
        private var globalTime: Long = 0

        /**
         * 获取下次执行时间戳，该方法不会刷新数据
         *
         * @param id 个体序号
         * @return long
         */
        fun nextTime(id: String): Long {
            val result: Long
            result = if (id == "*") {
                globalTime + millis - System.currentTimeMillis()
            } else {
                data.getOrDefault(id, 0L) + millis - System.currentTimeMillis()
            }
            return if (result >= 0) result else 0L
        }

        override fun resetAll() {
            data.clear()
            globalTime = 0L
        }

        override fun reset(id: String) {
            if (id == "*") {
                globalTime = 0L
            } else {
                data.remove(id)
            }
        }

        override fun next(id: String) {
            if (id == "*") {
                globalTime = System.currentTimeMillis()
            } else {
                data[id] = System.currentTimeMillis()
            }
        }

        override fun hasNext(id: String, update: Boolean): Boolean {
            val time: Long
            time = if (id == "*") {
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

    class BaffleCounter(private val count: Int) : Baffle() {
        private val data: MutableMap<String, Int> = Maps.newConcurrentMap()
        private val globalCount = AtomicInteger()
        override fun resetAll() {
            data.clear()
            globalCount.set(0)
        }

        override fun reset(id: String) {
            if (id == "*") {
                globalCount.set(0)
            } else {
                data.remove(id)
            }
        }

        override fun next(id: String) {
            if (id == "*") {
                globalCount.getAndIncrement()
            } else {
                data[id] = data.computeIfAbsent(
                    id
                ) { a: String? -> 0 } + 1
            }
        }

        override fun hasNext(id: String, update: Boolean): Boolean {
            if (id == "*") {
                if (globalCount.get() < count) {
                    if (update) {
                        globalCount.getAndIncrement()
                    }
                    return false
                }
                if (update) {
                    globalCount.set(0)
                }
            } else {
                val i: Int
                if (data.containsKey(id)) {
                    i = data[id]!!
                } else {
                    i = 0
                    data[id] = 0
                }
                if (i < count) {
                    if (update) {
                        data[id] = i + 1
                    }
                    return false
                }
                if (update) {
                    data[id] = 0
                }
            }
            return true
        }
    }

    companion object {
        /**
         * 按时间阻断
         * 单位：毫秒
         *
         * @param duration 时间数值
         * @param timeUnit 时间单位
         * @return [Baffle]
         */
        fun of(duration: Long, timeUnit: TimeUnit): Baffle {
            return BaffleTime(timeUnit.toMillis(duration))
        }

        /**
         * 按次阻断（类似 SimpleCounter）
         *
         * @param count 次数
         * @return [Baffle]
         */
        fun of(count: Int): Baffle {
            return BaffleCounter(count)
        }
    }
}