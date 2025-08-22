package com.volgoblob.internal.infrastructure.aggregation.nativeGo.aggregators;

import java.nio.ByteBuffer;

import com.volgoblob.internal.domain.interfaces.aggregations.Aggregator;
import com.volgoblob.internal.infrastructure.aggregation.java.errors.AggregatorsException;

import net.openhft.hashing.LongHashFunction;

public class NativeDc implements Aggregator {

    static {
        System.loadLibrary("shim");
    }

    // параметры off-heap буфера
    private final int BUFFER_CAPACITY = 1024;
    private final int ENTRY_SIZE = Long.BYTES;

    // адрес на структуру в go, где собираются наши уникальные хэши
    private long handle;

    // создание direct буфера, хэш-функции и счетчик индекса
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_CAPACITY * ENTRY_SIZE);
    private final LongHashFunction hashFunc = LongHashFunction.xx3();
    private int index = 0;

    public NativeDc() {
        System.out.println("вызвался конструктор нативки");
        this.handle = dcInit();
    }


    @Override
    public void add(Object value) {
        System.out.println("вызвался add нативки");
        if (value == null || value.getClass() != String.class) {
            throw new AggregatorsException("Passed argument is not string");
        }
        if (index >= BUFFER_CAPACITY) {
            getHashBatch(handle, buffer, index);
            index = 0;
        }
        long hashValue = hashFunc.hashChars(value.toString());
        buffer.putLong(index * ENTRY_SIZE, hashValue);
        index++;
    }

    @Override
    public Number finish() {
        System.out.println("вызвался finish нативки");
        getHashBatch(handle, buffer, index);
        return dcFinish(handle);
    }

    // нативные go методы
    private native long dcInit();
    private native void getHashBatch(long handle, ByteBuffer buffer, int index);
    private native long dcFinish(long handle);


    @Override
    public void combine(Aggregator aggregator) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'combine'");
    }

}