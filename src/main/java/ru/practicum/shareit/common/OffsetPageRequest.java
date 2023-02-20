package ru.practicum.shareit.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import java.util.LinkedList;
import java.util.List;

public class OffsetPageRequest extends PageRequest {
    private final int offset;

    protected OffsetPageRequest(int offset, int page, int size, Sort sort) {
        super(page, size, sort);
        this.offset = offset;
    }

    public static @NonNull OffsetPageRequest ofOffset(@Nullable Integer offset, @Nullable Integer size,
                                                      @Nullable Sort sort) {
        offset = offset == null ? 0 : offset;
        size = size == null ? Integer.MAX_VALUE : size;
        sort = sort == null ? Sort.unsorted() : sort;
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Size must not be less than one");
        }
        return new OffsetPageRequest(offset % size, Math.floorDiv(offset, size), size, sort);
    }

    @Override
    public long getOffset() {
        return (long) offset + super.getOffset();
    }

    @Override
    public boolean hasPrevious() {
        return getOffset() > 0;
    }

    @Override
    public @NonNull OffsetPageRequest next() {
        if (getPageSize() == Integer.MAX_VALUE) {
            return this;
        }
        return new OffsetPageRequest(offset, getPageNumber() + 1, getPageSize(), getSort());
    }

    @Override
    public @NonNull OffsetPageRequest previous() {
        if (!hasPrevious()) {
            return this;
        }

        if (getPageNumber() == 0) {
            return new OffsetPageRequest(0, 0, getPageSize(), getSort());
        }

        return new OffsetPageRequest(offset, getPageNumber() - 1, getPageSize(), getSort());
    }

    @Override
    public @NonNull OffsetPageRequest first() {
        if (!hasPrevious()) {
            return this;
        }

        return new OffsetPageRequest(0, 0, getPageSize(), getSort());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OffsetPageRequest)) {
            return false;
        }

        OffsetPageRequest that = (OffsetPageRequest) obj;

        return super.equals(that) && this.offset == that.offset;
    }

    @Override
    public @NonNull OffsetPageRequest withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (pageNumber == getPageNumber() || getPageSize() == Integer.MAX_VALUE) {
            return this;
        }
        return new OffsetPageRequest(offset, pageNumber, getPageSize(), getSort());
    }

    @Override
    public @NonNull OffsetPageRequest withSort(@Nullable Sort.Direction direction, @Nullable String... properties) {
        List<String> columnsList = new LinkedList<>();
        String[] columns = new String[0];
        if (properties != null) {
            for (String column : properties) {
                if (column != null) {
                    columnsList.add(column);
                } else {
                    break;
                }
            }
            columns = columnsList.toArray(columns);
        }

        Sort sort = columns.length == 0 ? Sort.unsorted()
                : Sort.by(direction == null ? Sort.Direction.ASC : direction, columns);

        if (getSort().equals(sort)) {
            return this;
        }

        return new OffsetPageRequest(offset, getPageNumber(), getPageSize(), sort);
    }

    @Override
    public @NonNull OffsetPageRequest withSort(@Nullable Sort sort) {
        if (sort == null) {
            sort = Sort.unsorted();
        }

        if (getSort().equals(sort)) {
            return this;
        }

        return new OffsetPageRequest(offset, getPageNumber(), getPageSize(), sort);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + offset;
    }

    @Override
    public @NonNull String toString() {
        return String.format("Page offset request [offset: %d, number: %d, size %d, sort: %s]", offset, getPageNumber(),
                getPageSize(), getSort());
    }
}