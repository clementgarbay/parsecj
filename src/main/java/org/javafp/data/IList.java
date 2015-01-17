package org.javafp.data;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple recursive, immutable linked list.
 */
public abstract class IList<T> implements Iterable<T> {

    public static <T> IList<T> empty() {
        return Empty.EMPTY;
    }

    public static <T> IList<T> of(T elem) {
        return Empty.EMPTY.add(elem);
    }

    public static <T> IList<T> of(T... elems) {
        IList<T> list = Empty.EMPTY;
        for (int i = elems.length - 1; i >= 0; --i) {
            list = list.add(elems[i]);
        }
        return list;
    }

    public static <T> IList<T> add(T head, IList<T> tail) {
        return new Node<T>(head, tail);
    }

    public static <T> List<T> toJList(IList<T> list) {
        final List<T> result = new LinkedList<T>();
        for (; !list.isEmpty(); list = list.tail()) {
            result.add(list.head());
        }
        return result;
    }

    public static String listToString(IList<Character> list) {
        final StringBuilder sb = new StringBuilder();
        for (; !list.isEmpty(); list = list.tail()) {
            sb.append(list.head());
        }
        return sb.toString();
    }

    public static IList<Character> listToString(String s) {
        IList<Character> list = Empty.EMPTY;
        for (int i = s.length() - 1; i >= 0; --i) {
            list = list.add(s.charAt(i));
        }
        return list;
    }

    public IList<T> add(T head) {
        return new Node<T>(head, this);
    }

    public abstract boolean isEmpty();

    public abstract T head();

    public abstract IList<T> tail();

    protected abstract StringBuilder asString(StringBuilder sb);

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null || getClass() != rhs.getClass()) return false;
        return equals((IList<T>)rhs);
    }

    public abstract boolean equals(IList<T> rhs);

    public abstract <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty);

    public abstract IList<T> add(IList<T> l);

    public abstract int length();

    public IList<T> reverse() {
        return reverse(empty());
    }

    protected abstract IList<T> reverse(IList<T> acc);

    public abstract <U> IList<U> map(Function<T, U> f);

    public abstract T foldr(BinaryOperator<T> f, T z);
    public abstract T foldl(BinaryOperator<T> f, T z);

    public abstract T foldr1(BinaryOperator<T> f);
    public abstract T foldl1(BinaryOperator<T> f);

    public abstract Spliterator<T> spliterator();

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    public abstract Iterator<T> iterator();

    public static class Empty<T> extends IList<T> {
        static final Empty EMPTY = new Empty();

        private Empty() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public T head() {
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }

        @Override
        public IList<T> tail() {
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }

        @Override
        public String toString() {
            return "[]";
        }

        @Override
        public boolean equals(IList<T> rhs) {
            return rhs.isEmpty();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return sb;
        }

        @Override
        public <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty) {
            return empty.apply(this);
        }

        @Override
        public IList<T> add(IList<T> l) {
            return l;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        protected IList<T> reverse(IList<T> acc) {
            return acc;
        }

        @Override
        public <U> IList<U> map(Function<T, U> f) {
            return EMPTY;
        }

        @Override
        public T foldr(BinaryOperator<T> f, T z) {
            return z;
        }

        @Override
        public T foldl(BinaryOperator<T> f, T z) {
            return z;
        }

        @Override
        public T foldr1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldr1 on an empty list");
        }

        @Override
        public T foldl1(BinaryOperator<T> f) {
            throw new UnsupportedOperationException("Cannot call foldl1 on an empty list");
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Spliterator<T>() {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    return false;
                }

                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }

                @Override
                public long estimateSize() {
                    return 0;
                }

                @Override
                public int characteristics() {
                    return Spliterator.IMMUTABLE + Spliterator.SIZED;
                }
            };
        }

        @Override
        public Iterator<T> iterator() {

            return new Iterator<T>(){

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }
            };
        }
    }

    public static class Node<T> extends IList<T> {

        public final T head;
        public final IList<T> tail;

        Node(T head, IList<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public IList<T> tail() {
            return tail;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            asString(sb).setCharAt(sb.length() - 1, ']');
            return sb.toString();
        }

        @Override
        protected StringBuilder asString(StringBuilder sb) {
            return tail.asString(sb.append(head).append(','));
        }

        @Override
        public boolean equals(IList<T> rhs) {
            if (rhs.isEmpty()) {
                return false;
            } else {
                return head.equals(rhs.head()) && tail.equals(rhs.tail());
            }
        }

        @Override
        public <S> S match(Function<Node<T>, S> node, Function<Empty<T>, S> empty) {
            return node.apply(this);
        }

        @Override
        public IList<T> add(IList<T> l) {
            return new Node<T>(head, tail.add(l));
        }

        @Override
        public int length() {
            IList<T> pos = this;
            int length = 0;
            while (!pos.isEmpty()) {
                ++length;
                pos = pos.tail();
            }

            return length;
        }

        @Override
        protected IList<T> reverse(IList<T> acc) {
            return tail.reverse(acc.add(head));
        }

        @Override
        public <U> IList<U> map(Function<T, U> f) {
            return new Node<U>(f.apply(head), tail.map(f));
        }

        @Override
        public T foldr(BinaryOperator<T> f, T z) {
            return f.apply(head, tail.foldr(f, z));
        }

        @Override
        public T foldl(BinaryOperator<T> f, T z) {
            return tail.foldl(f, f.apply(z, head));
        }

        @Override
        public T foldr1(BinaryOperator<T> f) {
            return tail.isEmpty() ?
                    head :
                    f.apply(head, tail.foldr1(f));
        }

        @Override
        public T foldl1(BinaryOperator<T> f) {
            return tail.isEmpty() ?
                    head :
                    tail.foldl(f, head);
        }

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(this.iterator(), length(), Spliterator.IMMUTABLE + Spliterator.SIZED);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>(){

                IList<T> pos = Node.this;

                @Override
                public boolean hasNext() {
                    return !pos.isEmpty();
                }

                @Override
                public T next() {
                    final T head = pos.head();
                    pos = pos.tail();
                    return head;
                }
            };
        }
    }
}

