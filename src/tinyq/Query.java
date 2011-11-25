/*
Copyright (c) 2011, Vladimir Vasiltsov
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tinyq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Query<T> implements Iterable<T>{

    private final Iterator<T> _iterator;

    public interface Func<TIN, TOUT> {
        public TOUT run(TIN in);
    }

    private static class ArrayIterator<T> implements Iterator<T>{

        private T[] _array;
        private int _index =0;

        public ArrayIterator(T[] array){
            _array = array;
        }

        public boolean hasNext() {
            return _index < _array.length;
        }

        public T next() {
            if(hasNext())
                return _array[_index++];
            throw new IndexOutOfBoundsException();
        }

        public void remove() {
            throw new IndexOutOfBoundsException("Operation not supported");
        }
    }

    private static class WhereIterator<T1> implements Iterator<T1>{

        private Iterator<T1> _iterator;
        private Func<T1, Boolean> _selector;
        boolean hasCached = false;
        T1 _cached = null;

        public WhereIterator(Iterator<T1> iterator, Func<T1, Boolean> selector) {
            _iterator = iterator;
            _selector = selector;
        }

        public boolean hasNext() {
            if(hasCached) return true;
            if(!_iterator.hasNext()) return false;
            while(_iterator.hasNext()){
                _cached = _iterator.next();
                if(_selector.run(_cached)){
                    hasCached = true;
                    return true;
                }
            }
            return false;
        }

        public T1 next() {
            if(hasCached){
                hasCached = false;
                return _cached;
            }
            if(hasNext()){
                hasCached = false;
                return _cached;
            }
            throw new IndexOutOfBoundsException("no more elements");
        }

        public void remove() {
            throw new IndexOutOfBoundsException("Operation not supported");
        }
    }

    private static class SelectIterator<TIN,TOUT> implements Iterator<TOUT>{

        private Iterator<TIN> _inInIterator;
        private Func<TIN, TOUT> _selector;

        public SelectIterator(Iterator<TIN> inIterator, Func<TIN,TOUT> selector){
            _inInIterator = inIterator;
            _selector = selector;
        }

        public boolean hasNext() {
            return _inInIterator.hasNext();
        }

        public TOUT next() {
            if(hasNext()){
                return _selector.run(_inInIterator.next());
            }
            throw new IndexOutOfBoundsException();
        }

        public void remove() {
            _inInIterator.remove();
        }
    }

    private static class SelectManyIterator<T1IN, T1OUT> implements  Iterator<T1OUT>{

        private final Iterator<T1IN> _iterator;
        private final Func<T1IN, Query<T1OUT>> _selector;
        private Query<T1OUT> _cached = null;
        private boolean _hasCached = false;

        public SelectManyIterator (Iterator<T1IN> inIterator, Func<T1IN, Query<T1OUT>> selector){
            _iterator = inIterator;
            _selector = selector;
        }

        public boolean hasNext() {
            if(!_hasCached){
                if(!_iterator.hasNext()) return false;
                _cached = _selector.run(_iterator.next());
                _hasCached = true;
            }
            if(_cached.iterator().hasNext()){
                return true;
            }
            if(_iterator.hasNext()){
                _cached = null;
                _hasCached = false;
                return hasNext();
            }
            return false;
        }

        public T1OUT next() {
            if(!hasNext()){
                throw new IndexOutOfBoundsException();
            }
            return _cached.iterator().next();
        }

        public void remove() {
            throw new IndexOutOfBoundsException("Operation not supported");
        }
    }

    public int size() {
        int size = 0;
        while(_iterator.hasNext()){
            _iterator.next();
            ++size;
        }
        return size;
    }

    public boolean isEmpty() {
        return !_iterator.hasNext();
    }

    public Iterator<T> iterator() {
        return _iterator;
    }

    public List<T> toList(){
        ArrayList<T> r = new ArrayList<T>();
        for(T t : this){
            r.add(t);
        }
        return r;
    }

    public Object[] toArray() {
        List<T> ts = toList();
        Object[] objects = new Object[ts.size()];
        int idx = 0;
        for(T t : ts){
            objects[idx] = t;
            idx++;
        }
        return objects;
    }

    public T[] toArray(T[] a) {
        int idx = 0;
        while(_iterator.hasNext()){
            idx++;
            a[idx] = _iterator.next();
        }
        return a;
    }

    public Query<T> where(Func<T,Boolean> selector){
        return new Query<T>(new WhereIterator<T>(_iterator,selector));
    }

    public <TOUT> Query<TOUT> select(Func<T,TOUT> selector){
        return new Query<TOUT>(new SelectIterator<T,TOUT>(_iterator,selector));
    }

    public <TOUT> Query<TOUT> selectMany(Func<T, Query<TOUT>> selector){
        return new Query<TOUT>(new SelectManyIterator<T,TOUT>(_iterator,selector));
    }

    public Query(List<T> list){
        _iterator = list.iterator();
    }

    public Query(T[] array) {
        _iterator = new ArrayIterator<T>(array);
    }

    public Query(Iterator<T> iterator) {
        _iterator = iterator;
    }

}