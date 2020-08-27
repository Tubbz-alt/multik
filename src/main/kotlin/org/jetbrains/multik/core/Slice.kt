package org.jetbrains.multik.core

public interface Indexing


public class Slice(start: Int, stop: Int, step: Int) : Indexing {
    init {
        if (step == 0 && start != 0 && stop != 0) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    private var _start: Int = if (stop in 0 until start) stop else start
    private var _stop: Int = if (_start == stop) start else stop

    public val step: Int = if (step < 0) -step else step
    public val start: Int get() = _start
    public val stop: Int get() = _stop


    // TODO?
    public fun indices(size: Int) {

    }

    public operator fun rangeTo(step: RInt): Slice = Slice(_start, _stop, step.data)

    public operator fun rangeTo(step: Int): Slice = Slice(_start, _stop, step)

    //todo
//    override fun contains(value: Int): Boolean = value in start..end

    //fun isEmpty(): Boolean = start == 0 && end == 0

//    override fun equals(other: Any?): Boolean =
//        other is IntRange && (isEmpty() && other.isEmpty() ||
//                start == other.first && end == other.last)

//    override fun hashCode(): Int =
//        if (isEmpty()) -1 else (31 * start + end + step)

    override fun toString(): String = "$start..$stop..$step"

    public companion object {
        /** An empty range of values of type Int. */
        public val EMPTY: Slice = Slice(0, 0, 0)
    }
}

public val Int.r: RInt get() = RInt(this)

public inline class RInt(internal val data: Int) : Indexing {
    public operator fun rangeTo(that: RInt): Slice = Slice(data, that.data, 1)

    public operator fun rangeTo(that: Int): Slice = Slice(data, that, 1)
}

public operator fun Int.rangeTo(that: RInt): Slice = Slice(this, that.data, 1)

//TODO (Experimental)
public operator fun IntProgression.rangeTo(step: Int): Slice {
    return Slice(this.first, this.last, step)
}
