/*
 * Copyright 2020-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.kotlinx.multik.api

import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.timesAssign
import kotlin.math.ceil

/**
 * Returns a new zero array with the specified shape.
 *
 * @param dims shape of the array.
 * @return [Ndarray] of [D] dimension
 * @sample samples.Ndarray.empty
 */
public inline fun <reified T : Number, reified D : Dimension> Multik.empty(vararg dims: Int): Ndarray<T, D> {
    val dim = dimensionClassOf<D>(dims.size)
    requireDimension(dim, dims.size)
    val dtype = DataType.of(T::class)
    val size = dims.reduce { acc, el -> acc * el }
    val data = initMemoryView<T>(size, dtype)
    return Ndarray<T, D>(data, shape = dims, dtype = dtype, dim = dim)
}

/**
 * Returns a new zero array of type [dtype] with the specified shape.
 *
 * Note: Generic type of elements [T] must match [dtype].
 *
 * @param dims shape of the array.
 * @param dtype array type.
 * @return [Ndarray] of [D] dimension.
 * @sample samples.Ndarray.emptyWithDtype
 */
public fun <T : Number, D : Dimension> Multik.empty(dims: IntArray, dtype: DataType): Ndarray<T, D> {
    // TODO check data type
    val dim = dimensionOf<D>(dims.size)
    requireDimension(dim, dims.size) // TODO (mk.empty<Float, D2>(intArrayOf(3), DataType.FloatDataType))
    val size = dims.fold(1, Int::times)
    val data = initMemoryView<T>(size, dtype)
    return Ndarray<T, D>(data, shape = dims, dtype = dtype, dim = dim)
}

/**
 * Returns the identity array.
 *
 * @param n number of rows and columns.
 * @return [D2Array].
 * @sample samples.Ndarray.identity
 */
public inline fun <reified T : Number> Multik.identity(n: Int): D2Array<T> {
    val dtype = DataType.of(T::class)
    return identity(n, dtype)
}

/**
 * Returns the identity array.
 *
 * Note: Generic type of elements must match [dtype].
 *
 * @param n number of rows and columns.
 * @param dtype array type.
 * @return [D2Array]
 * @sample samples.Ndarray.identityWithDtype
 */
public fun <T : Number> Multik.identity(n: Int, dtype: DataType): D2Array<T> {
    val shape = intArrayOf(n, n)
    val ret = D2Array<T>(initMemoryView<T>(n * n, dtype), shape = shape, dtype = dtype, dim = D2)
    val one: Number = when (dtype.nativeCode) {
        1 -> 1.toByte()
        2 -> 1.toShort()
        3 -> 1
        4 -> 1L
        5 -> 1f
        6 -> 1.0
        else -> throw Exception("Type not defined.")
    }
    for (i in 0 until n) {
        @Suppress("UNCHECKED_CAST")
        ret[i, i] = one as T
    }
    return ret
}

/**
 * Creates the 1-dimension array from [arg].
 *
 * Example:
 * ```
 * mk.ndarray(mk[1, 2, 3]
 * ```
 * @param arg list of elements.
 * @return [D1Array].
 * @sample samples.Ndarray.ndarray1D
 */
@JvmName("ndarray1D")
public inline fun <reified T : Number> Multik.ndarray(arg: List<T>): D1Array<T> {
    val dtype = DataType.of(T::class)
    listOf(1, 2)
    val data = arg.toViewPrimitiveArray(dtype)
    return D1Array(data, 0, intArrayOf(arg.size), dtype = dtype, dim = D1)
}

/**
 * Creates the 2-dimension array from [arg].
 *
 * @param arg list of rows.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarray2D
 */
@JvmName("ndarray2D")
public inline fun <reified T : Number> Multik.ndarray(arg: List<List<T>>): D2Array<T> {
    val dtype = DataType.of(T::class)
    val size = IntArray(2)
    size[0] = arg.size
    size[1] = arg.first().size
    val res = ArrayList<T>()
    for (ax0 in arg) {
        require(size[1] == ax0.size) { "The size of the incoming array $ax0 does not match the rest" }
        res.addAll(ax0)
    }
    val data = res.toViewPrimitiveArray(dtype)
    return D2Array(data, 0, size, dtype = dtype, dim = D2)
}

/**
 * Creates the 3-dimension array from [arg].
 *
 * @param arg elements.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarray3D
 */
@JvmName("ndarray3D")
public inline fun <reified T : Number> Multik.ndarray(arg: List<List<List<T>>>): D3Array<T> {
    val dtype = DataType.of(T::class)
    val size = IntArray(3)
    size[0] = arg.size
    size[1] = arg.first().size
    size[2] = arg.first().first().size
    val res = ArrayList<T>()
    for (ax0 in arg) {
        require(size[1] == ax0.size) { "The size of the incoming array $ax0 does not match the rest" }
        for (ax1 in ax0) {
            require(size[2] == ax1.size) { "The size of the incoming array $ax1 does not match the rest" }
            res.addAll(ax1)
        }
    }
    val data = res.toViewPrimitiveArray(dtype)
    return D3Array<T>(data, 0, size, dtype = dtype, dim = D3)
}

/**
 * Creates the 4-dimension array from [arg].
 *
 * @param arg elements.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarray4D
 */
@JvmName("ndarray4D")
public inline fun <reified T : Number> Multik.ndarray(arg: List<List<List<List<T>>>>): D4Array<T> {
    val dtype = DataType.of(T::class)
    val size = IntArray(4)
    size[0] = arg.size
    size[1] = arg.first().size
    size[2] = arg.first().first().size
    size[3] = arg.first().first().first().size
    val res = ArrayList<T>()
    for (ax0 in arg) {
        check(size[1] == ax0.size) { "The size of the incoming array $ax0 does not match the rest" }
        for (ax1 in ax0) {
            require(size[2] == ax1.size) { "The size of the incoming array $ax1 does not match the rest" }
            for (ax2 in ax1) {
                require(size[3] == ax2.size) { "The size of the incoming array $ax2 does not match the rest" }
                res.addAll(ax2)
            }
        }
    }
    val data = res.toViewPrimitiveArray(dtype)
    return D4Array(data, 0, size, dtype = dtype, dim = D4)
}


/**
 * Returns a new array given [shape] from collection.
 *
 * @param elements collection of elements.
 * @param shape array shape.
 * @return [Ndarray] of [D] dimension.
 * @sample samples.Ndarray.ndarrayCollections
 */
public inline fun <T : Number, reified D : Dimension> Multik.ndarray(
    elements: Collection<T>, shape: IntArray
): Ndarray<T, D> {
    requireShapeEmpty(shape)
    val dim = dimensionClassOf<D>(shape.size)
    requireDimension(dim, shape.size)
    return ndarray(elements, shape, dim)
}

/**
 * Returns a new array given shape and dimension from collection.
 *
 * Note: Generic type of dimension [D] must match dim.
 *
 * @param elements collection of elements.
 * @param shape array shape.
 * @param dim array dimension.
 * @return [Ndarray] of [D] dimension.
 * @sample samples.Ndarray.ndarrayCollectionsWithDim
 */
public fun <T : Number, D : Dimension> Multik.ndarray(elements: Collection<T>, shape: IntArray, dim: D): Ndarray<T, D> {
    requireShapeEmpty(shape)
    requireDimension(dim, shape.size)
    requireElementsWithShape(elements.size, shape.fold(1, Int::times))
    val size = shape.reduce { acc, el -> acc * el }
    val dtype = DataType.of(elements.first())
    val data = initMemoryView<T>(size, dtype).apply {
        var count = 0
        for (el in elements)
            this[count++] = el
    }
    return Ndarray<T, D>(data, shape = shape, dtype = dtype, dim = dim)
}

//_________________________________________________D1___________________________________________________________________

/**
 * Returns a new 1-dimension array given shape from a collection.
 *
 * @param elements collection of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayCollections1D
 */
public fun <T : Number> Multik.ndarray(elements: Collection<T>): D1Array<T> {
    return ndarray(elements, intArrayOf(elements.size), D1)
}

/**
 * Returns a new 1-dimension array from [ByteArray].
 *
 * @param args [ByteArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayByteArray1D
 */
public fun Multik.ndarray(args: ByteArray): D1Array<Byte> {
    val data = MemoryViewByteArray(args)
    return D1Array<Byte>(data, shape = intArrayOf(args.size), dtype = DataType.ByteDataType, dim = D1)
}

/**
 * Returns a new 1-dimension array from [ShortArray].
 *
 * @param args [ShortArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayShortArray1D
 */
public fun Multik.ndarray(args: ShortArray): D1Array<Short> {
    val data = MemoryViewShortArray(args)
    return D1Array<Short>(data, shape = intArrayOf(args.size), dtype = DataType.ShortDataType, dim = D1)
}

/**
 * Returns a new 1-dimension array from [IntArray].
 *
 * @param args [IntArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayIntArray1D
 */
public fun Multik.ndarray(args: IntArray): D1Array<Int> {
    val data = MemoryViewIntArray(args)
    return D1Array<Int>(data, shape = intArrayOf(args.size), dtype = DataType.IntDataType, dim = D1)
}

/**
 * Returns a new 1-dimension array from [LongArray].
 *
 * @param args [LongArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayLongArray1D
 */
public fun Multik.ndarray(args: LongArray): D1Array<Long> {
    val data = MemoryViewLongArray(args)
    return D1Array<Long>(data, shape = intArrayOf(args.size), dtype = DataType.LongDataType, dim = D1)
}

/**
 * Returns a new 1-dimension array from [FloatArray].
 *
 * @param args [FloatArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayFloatArray1D
 */
public fun Multik.ndarray(args: FloatArray): D1Array<Float> {
    val data = MemoryViewFloatArray(args)
    return D1Array<Float>(data, shape = intArrayOf(args.size), dtype = DataType.FloatDataType, dim = D1)
}

/**
 * Returns a new 1-dimension array from [DoubleArray].
 *
 * @param args [DoubleArray] of elements.
 * @return [D1Array]
 * @sample samples.Ndarray.ndarrayDoubleArray1D
 */
public fun Multik.ndarray(args: DoubleArray): D1Array<Double> {
    val data = MemoryViewDoubleArray(args)
    return D1Array<Double>(data, shape = intArrayOf(args.size), dtype = DataType.DoubleDataType, dim = D1)
}

//_________________________________________________D2___________________________________________________________________

/**
 * Returns a new 2-dimensions array given shape from a collection.
 *
 * @param elements collection of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayCollections2D
 */
public fun <T : Number> Multik.ndarray(elements: Collection<T>, dim1: Int, dim2: Int): D2Array<T> {
    return ndarray(elements, intArrayOf(dim1, dim2), D2)
}

/**
 * Returns a new 2-dimensions array from [ByteArray].
 *
 * @param args [ByteArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayByteArray2D
 */
public fun Multik.ndarray(args: ByteArray, dim1: Int, dim2: Int): D2Array<Byte> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewByteArray(args)
    return D2Array<Byte>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.ByteDataType, dim = D2)
}

/**
 * Returns a new 2-dimensions array from [ShortArray].
 *
 * @param args [ShortArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayShortArray2D
 */
public fun Multik.ndarray(args: ShortArray, dim1: Int, dim2: Int): D2Array<Short> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewShortArray(args)
    return D2Array<Short>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.ShortDataType, dim = D2)
}

/**
 * Returns a new 2-dimensions array from [IntArray].
 *
 * @param args [IntArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayIntArray2D
 */
public fun Multik.ndarray(args: IntArray, dim1: Int, dim2: Int): D2Array<Int> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewIntArray(args)
    return D2Array<Int>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.IntDataType, dim = D2)
}

/**
 * Returns a new 2-dimensions array from [LongArray].
 *
 * @param args [LongArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayLongArray2D
 */
public fun Multik.ndarray(args: LongArray, dim1: Int, dim2: Int): D2Array<Long> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewLongArray(args)
    return D2Array<Long>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.LongDataType, dim = D2)
}

/**
 * Returns a new 2-dimensions array from [FloatArray].
 *
 * @param args [FloatArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayFloatArray2D
 */
public fun Multik.ndarray(args: FloatArray, dim1: Int, dim2: Int): D2Array<Float> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewFloatArray(args)
    return D2Array<Float>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.FloatDataType, dim = D2)
}

/**
 * Returns a new 2-dimensions array from [DoubleArray].
 *
 * @param args [DoubleArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 1-dimension.
 * @return [D2Array].
 * @sample samples.Ndarray.ndarrayDoubleArray2D
 */
public fun Multik.ndarray(args: DoubleArray, dim1: Int, dim2: Int): D2Array<Double> {
    requireElementsWithShape(args.size, dim1 * dim2)
    val data = MemoryViewDoubleArray(args)
    return D2Array<Double>(data, shape = intArrayOf(dim1, dim2), dtype = DataType.DoubleDataType, dim = D2)
}

//_________________________________________________D3___________________________________________________________________

/**
 * Returns a new 3-dimensions array given shape from a collection.
 *
 * @param elements collection of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayCollections3D
 */
public fun <T : Number> Multik.ndarray(elements: Collection<T>, dim1: Int, dim2: Int, dim3: Int): D3Array<T> {
    return ndarray(elements, intArrayOf(dim1, dim2, dim3), D3)
}

/**
 * Returns a new 3-dimensions array from [ByteArray].
 *
 * @param args [ByteArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayByteArray3D
 */
public fun Multik.ndarray(args: ByteArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Byte> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewByteArray(args)
    return D3Array<Byte>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.ByteDataType, dim = D3)
}

/**
 * Returns a new 3-dimensions array from [ShortArray].
 *
 * @param args [ShortArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayShortArray3D
 */
public fun Multik.ndarray(args: ShortArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Short> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewShortArray(args)
    return D3Array<Short>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.ShortDataType, dim = D3)
}

/**
 * Returns a new 3-dimensions array from [IntArray].
 *
 * @param args [IntArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayIntArray3D
 */
public fun Multik.ndarray(args: IntArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Int> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewIntArray(args)
    return D3Array<Int>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.IntDataType, dim = D3)
}

/**
 * Returns a new 3-dimensions array from [LongArray].
 *
 * @param args [LongArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayLongArray3D
 */
public fun Multik.ndarray(args: LongArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Long> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewLongArray(args)
    return D3Array<Long>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.LongDataType, dim = D3)
}

/**
 * Returns a new 3-dimensions array from [FloatArray].
 *
 * @param args [FloatArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayFloatArray3D
 */
public fun Multik.ndarray(args: FloatArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Float> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewFloatArray(args)
    return D3Array<Float>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.FloatDataType, dim = D3)
}

/**
 * Returns a new 3-dimensions array from [DoubleArray].
 *
 * @param args [DoubleArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @return [D3Array].
 * @sample samples.Ndarray.ndarrayDoubleArray3D
 */
public fun Multik.ndarray(args: DoubleArray, dim1: Int, dim2: Int, dim3: Int): D3Array<Double> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3)
    val data = MemoryViewDoubleArray(args)
    return D3Array<Double>(data, shape = intArrayOf(dim1, dim2, dim3), dtype = DataType.DoubleDataType, dim = D3)
}

//_________________________________________________D4___________________________________________________________________

/**
 * Returns a new 4-dimensions array given shape from a collection.
 *
 * @param elements collection of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayCollections4D
 */
public fun <T : Number> Multik.ndarray(
    elements: Collection<T>, dim1: Int, dim2: Int, dim3: Int, dim4: Int
): D4Array<T> {
    return ndarray(elements, intArrayOf(dim1, dim2, dim3, dim4), D4)
}

/**
 * Returns a new 4-dimensions array from [ByteArray].
 *
 * @param args [ByteArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayByteArray4D
 */
public fun Multik.ndarray(args: ByteArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Byte> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewByteArray(args)
    return D4Array<Byte>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.ByteDataType, dim = D4)
}

/**
 * Returns a new 4-dimensions array from [ShortArray].
 *
 * @param args [ShortArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayShortArray4D
 */
public fun Multik.ndarray(args: ShortArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Short> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewShortArray(args)
    return D4Array<Short>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.ShortDataType, dim = D4)
}

/**
 * Returns a new 4-dimensions array from [IntArray].
 *
 * @param args [IntArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayIntArray4D
 */
public fun Multik.ndarray(args: IntArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Int> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewIntArray(args)
    return D4Array<Int>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.IntDataType, dim = D4)
}

/**
 * Returns a new 4-dimensions array from [LongArray].
 *
 * @param args [LongArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayLongArray4D
 */
public fun Multik.ndarray(args: LongArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Long> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewLongArray(args)
    return D4Array<Long>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.LongDataType, dim = D4)
}

/**
 * Returns a new 4-dimensions array from [FloatArray].
 *
 * @param args [FloatArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayFloatArray4D
 */
public fun Multik.ndarray(args: FloatArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Float> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewFloatArray(args)
    return D4Array<Float>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.FloatDataType, dim = D4)
}

/**
 * Returns a new 4-dimensions array from [DoubleArray].
 *
 * @param args [DoubleArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @return [D4Array].
 * @sample samples.Ndarray.ndarrayDoubleArray4D
 */
public fun Multik.ndarray(args: DoubleArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int): D4Array<Double> {
    requireElementsWithShape(args.size, dim1 * dim2 * dim3 * dim4)
    val data = MemoryViewDoubleArray(args)
    return D4Array<Double>(data, shape = intArrayOf(dim1, dim2, dim3, dim4), dtype = DataType.DoubleDataType, dim = D4)
}

//_________________________________________________DN___________________________________________________________________

/**
 * Returns a new n-dimension array given shape from a collection.
 *
 * @param elements collection of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayCollectionsDN
 */
public fun <T : Number> Multik.ndarray(
    elements: Collection<T>, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<T, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    return ndarray(elements, shape, DN(shape.size))
}

/**
 * Returns a new n-dimension array from [ByteArray].
 *
 * @param args [ByteArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayByteArrayDN
 */
public fun Multik.ndarray(
    args: ByteArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Byte, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewByteArray(args)
    return Ndarray<Byte, DN>(data, shape = shape, dtype = DataType.ByteDataType, dim = dimensionOf(shape.size))
}

/**
 * Returns a new n-dimension array from [ShortArray].
 *
 * @param args [ShortArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayShortArrayDN
 */
public fun Multik.ndarray(
    args: ShortArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Short, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewShortArray(args)
    return Ndarray<Short, DN>(data, shape = shape, dtype = DataType.ShortDataType, dim = dimensionOf(shape.size))
}

/**
 * Returns a new n-dimension array from [IntArray].
 *
 * @param args [IntArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayIntArrayDN
 */
public fun Multik.ndarray(
    args: IntArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Int, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewIntArray(args)
    return Ndarray<Int, DN>(data, shape = shape, dtype = DataType.IntDataType, dim = dimensionOf(shape.size))
}

/**
 * Returns a new n-dimension array from [LongArray].
 *
 * @param args [LongArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayLongArrayDN
 */
public fun Multik.ndarray(
    args: LongArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Long, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewLongArray(args)
    return Ndarray<Long, DN>(data, shape = shape, dtype = DataType.LongDataType, dim = dimensionOf(shape.size))
}

/**
 * Returns a new n-dimension array from [FloatArray].
 *
 * @param args [FloatArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayFloatArrayDN
 */
public fun Multik.ndarray(
    args: FloatArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Float, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewFloatArray(args)
    return Ndarray<Float, DN>(data, shape = shape, dtype = DataType.FloatDataType, dim = dimensionOf(shape.size))
}

/**
 * Returns a new n-dimension array from [DoubleArray].
 *
 * @param args [DoubleArray] of elements.
 * @param dim1 value of 1-dimension.
 * @param dim2 value of 2-dimension.
 * @param dim3 value of 3-dimension.
 * @param dim4 value of 4-dimension.
 * @param dims values of other dimensions.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.ndarrayDoubleArrayDN
 */
public fun Multik.ndarray(
    args: DoubleArray, dim1: Int, dim2: Int, dim3: Int, dim4: Int, vararg dims: Int
): Ndarray<Double, DN> {
    val shape = intArrayOf(dim1, dim2, dim3, dim4) + dims
    requireElementsWithShape(args.size, shape.fold(1, Int::times))
    val data = MemoryViewDoubleArray(args)
    return Ndarray<Double, DN>(data, shape = shape, dtype = DataType.DoubleDataType, dim = dimensionOf(shape.size))
}

//______________________________________________________________________________________________________________________
//TODO check negative dims
/**
 * Returns a 1-dimension array.
 *
 * @param sizeD1 value of 1-dimension.
 * @param init initialization function
 * @return [D1Array].
 * @sample samples.Ndarray.d1array
 */
public inline fun <reified T : Number> Multik.d1array(sizeD1: Int, noinline init: (Int) -> T): D1Array<T> {
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(sizeD1)
    val data = initMemoryView<T>(sizeD1, dtype, init)
    return D1Array<T>(data, shape = shape, dtype = dtype, dim = D1)
}

/**
 * Returns a 2-dimensions array.
 *
 * @param sizeD1 value of 1-dimension.
 * @param sizeD2 value of 2-dimension.
 * @param init initialization function.
 * @return [D2Array].
 * @sample samples.Ndarray.d2array
 */
public inline fun <reified T : Number> Multik.d2array(sizeD1: Int, sizeD2: Int, noinline init: (Int) -> T): D2Array<T> {
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(sizeD1, sizeD2)
    val data = initMemoryView<T>(sizeD1 * sizeD2, dtype, init)
    return D2Array<T>(data, shape = shape, dtype = dtype, dim = D2)
}

/**
 * Returns a 3-dimensions array.
 *
 * @param sizeD1 value of 1-dimension.
 * @param sizeD2 value of 2-dimension.
 * @param sizeD3 value of 3-dimension.
 * @param init initialization function.
 * @return [D3Array].
 * @sample samples.Ndarray.d3array
 */
public inline fun <reified T : Number> Multik.d3array(
    sizeD1: Int, sizeD2: Int, sizeD3: Int, noinline init: (Int) -> T
): D3Array<T> {
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(sizeD1, sizeD2, sizeD3)
    val data = initMemoryView<T>(sizeD1 * sizeD2 * sizeD3, dtype, init)
    return D3Array<T>(data, shape = shape, dtype = dtype, dim = D3)
}

/**
 * Returns a 4-dimensions array.
 *
 * @param sizeD1 value of 1-dimension.
 * @param sizeD2 value of 2-dimension.
 * @param sizeD3 value of 3-dimension.
 * @param sizeD4 value of 4-dimension.
 * @param init initialization function.
 * @return [D4Array].
 * @sample samples.Ndarray.d4array
 */
public inline fun <reified T : Number> Multik.d4array(
    sizeD1: Int, sizeD2: Int, sizeD3: Int, sizeD4: Int, noinline init: (Int) -> T
): D4Array<T> {
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(sizeD1, sizeD2, sizeD3, sizeD4)
    val data = initMemoryView<T>(sizeD1 * sizeD2 * sizeD3 * sizeD4, dtype, init)
    return D4Array<T>(data, shape = shape, dtype = dtype, dim = D4)
}

/**
 * Returns a new array with the specified shape, where each element is calculated by calling the specified
 * [init] function.
 *
 * @param sizeD1 value of 1-dimension.
 * @param sizeD2 value of 2-dimension.
 * @param sizeD3 value of 3-dimension.
 * @param sizeD4 value of 4-dimension.
 * @param dims values other dimensions.
 * @param init initialization function.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.dnarray
 */
public inline fun <reified T : Number> Multik.dnarray(
    sizeD1: Int, sizeD2: Int, sizeD3: Int, sizeD4: Int, vararg dims: Int, noinline init: (Int) -> T
): Ndarray<T, DN> {
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(sizeD1, sizeD2, sizeD3, sizeD4) + dims
    val size = shape.fold(1, Int::times)
    val data = initMemoryView<T>(size, dtype, init)
    return Ndarray<T, DN>(data, shape = shape, dtype = dtype, dim = dimensionOf(shape.size))
}

/**
 * Returns a new array with the specified shape [dims], where each element is calculated by calling the specified
 * [init] function.
 *
 * @param dims array shape.
 * @param init initialization function.
 * @return [Ndarray] of [DN] dimension.
 * @sample samples.Ndarray.dnarrayWithDims
 */
public inline fun <reified T : Number, D : Dimension> Multik.dnarray(
    dims: IntArray,
    noinline init: (Int) -> T
): Ndarray<T, D> {
    val dtype = DataType.of(T::class)
    val size = dims.fold(1, Int::times)
    val data = initMemoryView<T>(size, dtype, init)
    return Ndarray<T, D>(data, shape = dims, dtype = dtype, dim = dimensionOf(dims.size))
}

/**
 * Returns a new 1-dimension array from [items].
 *
 * @param items specified elements.
 * @return [D1Array].
 * @sample samples.Ndarray.ndarrayOf
 */
public fun <T : Number> Multik.ndarrayOf(vararg items: T): D1Array<T> {
    val dtype = DataType.of(items.first())
    val shape = intArrayOf(items.size)
    val data = initMemoryView<T>(items.size, dtype) { items[it] }
    return D1Array<T>(data, shape = shape, dtype = dtype, dim = D1)
}

/**
 * Returns evenly spaced values within a given interval, where [step] is Integer.
 *
 * @param start starting value of the interval. The interval includes this value.
 * @param stop end value of the interval. The interval doesn't include this value.
 * @param step spacing between value. The default step is 1.
 * @return [D1Array].
 * @sample samples.Ndarray.arange
 */
public inline fun <reified T : Number> Multik.arange(start: Int, stop: Int, step: Int = 1): D1Array<T> {
    return arange(start, stop, step.toDouble())
}

/**
 * Returns evenly spaced values within a given interval, where [step] is Double.
 *
 * @param start starting value of the interval. The interval includes this value.
 * @param stop end value of the interval. The interval doesn't include this value.
 * @param step spacing between value. The step is [Double].
 * @return [D1Array].
 * @sample samples.Ndarray.arangeDoubleStep
 */
public inline fun <reified T : Number> Multik.arange(start: Int, stop: Int, step: Double): D1Array<T> {
    if (start < stop) require(step > 0) { "Step must be positive." }
    else if (start > stop) require(step < 0) { "Step must be negative." }

    val size = ceil((stop.toDouble() - start) / step).toInt()
    val dtype = DataType.of(T::class)
    val shape = intArrayOf(size)
    val data = initMemoryView<T>(size, dtype).apply {
        var tmp = start.toDouble()
        for (i in indices) {
            this[i] = tmp.toPrimitiveType()
            tmp += step
        }
    }
    return D1Array<T>(data, shape = shape, dtype = dtype, dim = D1)
}

/**
 * Returns evenly spaced values within a given interval, where start of interval is 0.
 * see [Multik.arange]
 *
 * @param stop end of the interval. The interval doesn't include this value.
 * @param step spacing between value. The default step is 1.
 * @return [D1Array].
 * @sample samples.Ndarray.arangeWithoutStart
 */
public inline fun <reified T : Number> Multik.arange(stop: Int, step: Int = 1): D1Array<T> = arange(0, stop, step)

/**
 * Returns evenly spaced values within a given interval, where start of interval is 0 and the [step] is Double.
 * see [Multik.arange]
 *
 * @param stop end value of the interval. The interval doesn't include this value.
 * @param step spacing between value. The step is [Double].
 * @return [D1Array].
 * @sample samples.Ndarray.arangeDoubleStepWithoutStart
 */
public inline fun <reified T : Number> Multik.arange(stop: Int, step: Double): D1Array<T> = arange(0, stop, step)


/**
 * Returns evenly spaced values within a given interval [start, stop], more precisely than [arange].
 *
 * @param start starting value of the interval.
 * @param stop end of the interval.
 * @param num number of values. Default is 50.
 * @return [D1Array].
 * @sample samples.Ndarray.linspace
 */
public inline fun <reified T : Number> Multik.linspace(start: Int, stop: Int, num: Int = 50): D1Array<T> {
    return linspace(start.toDouble(), stop.toDouble(), num)
}

/**
 * Returns evenly spaced values within a given interval [start, stop], where [start] and [stop] is Double.
 * @see [Multik.linspace]
 *
 * @param start starting value of the interval.
 * @param stop end of the interval.
 * @param num number of values.
 * @return [D1Array].
 * @sample samples.Ndarray.linspaceDouble
 */
public inline fun <reified T : Number> Multik.linspace(start: Double, stop: Double, num: Int = 50): D1Array<T> {
    require(num > 0) { "The number of elements cannot be less than zero or equal to zero." }
    val div = num - 1.0
    val delta = stop - start
    val ret = arange<Double>(0, stop = num)
    if (num > 1) {
        val step = delta / div
        ret *= step
    }

    ret += start
    ret[ret.size - 1] = stop
    return ret.asType<T>()
}

/**
 * Returns [D1Array] containing all elements.
 * @sample samples.Ndarray.toNdarray
 */
public fun <T : Number> Iterable<T>.toNdarray(): D1Array<T> {
    if (this is Collection<T>)
        return Multik.ndarray<T, D1>(this, intArrayOf(this.size), D1)

    val tmp = ArrayList<T>()
    for (item in this) {
        tmp.add(item)
    }
    return Multik.ndarray<T, D1>(tmp, intArrayOf(tmp.size), D1)
}
