package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OffsetPageRequestTest {
    private static Stream<Arguments> of() {
        return Stream.of(
                Arguments.of(null, null, null,
                        0L, 0, Integer.MAX_VALUE, Sort.unsorted()),

                Arguments.of(0, null, Sort.by("id").descending(),
                        0L, 0, Integer.MAX_VALUE, Sort.by("id").descending()),

                Arguments.of(null, 10, Sort.unsorted(),
                        0L, 0, 10, Sort.unsorted()),

                Arguments.of(1, 10, null,
                        1L, 0, 10, Sort.unsorted()),

                Arguments.of(19, 10, null,
                        19L, 1, 10, Sort.unsorted())
        );
    }

    @ParameterizedTest(name = "with of={0},sz={1},st={2}=>of={3},pn={4},ps={5},st={6}")
    @MethodSource("of")
    void of_shouldReturnNewOffsetPageRequest(Integer fromOffset, Integer size, Sort withSort, long offset,
                                             int pageNumber, int pageSize, Sort sort) {
        OffsetPageRequest target = OffsetPageRequest.ofOffset(fromOffset, size, withSort);

        assertThat(target, allOf(
                hasProperty("offset", equalTo(offset)),
                hasProperty("pageNumber", equalTo(pageNumber)),
                hasProperty("pageSize", equalTo(pageSize)),
                hasProperty("sort", equalTo(sort))
        ));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void of_withNotPositiveSize_shouldThrowIllegalArgumentException(Integer size) {
        assertThrows(IllegalArgumentException.class, () -> OffsetPageRequest.ofOffset(1, size, Sort.unsorted()));
    }

    @Test
    void of_withNegativeOffset_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> OffsetPageRequest.ofOffset(-1, 1,
                Sort.unsorted()));
    }

    @ParameterizedTest(name = "size={0}, offset=0")
    @NullSource
    @ValueSource(ints = {10})
    void hasPrevious_withZeroOffset_false(Integer size) {
        OffsetPageRequest target = OffsetPageRequest.ofOffset(0, size, null);

        assertThat(target.hasPrevious(), is(false));
    }

    private static Stream<Arguments> hasPrevious() {
        return Stream.of(
                Arguments.of(1, null),
                Arguments.of(1, 10),
                Arguments.of(12, 10)
        );
    }

    @ParameterizedTest(name = "size={1}, offset={0}")
    @MethodSource("hasPrevious")
    void hasPrevious_withNotZeroOffset_shouldReturnTrue(Integer offset, Integer size) {
        OffsetPageRequest target = OffsetPageRequest.ofOffset(offset, size, null);

        assertThat(target.hasPrevious(), is(true));
    }

    @ParameterizedTest(name = "size=null, offset={0}")
    @NullSource
    @ValueSource(ints = {100})
    void next_withNullSameMaxPageSize_shouldReturnSelf(Integer offset) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, null, Sort.unsorted());

        OffsetPageRequest target = source.next();

        assertThat(target == source, is(true));
    }

    private static Stream<Arguments> next() {
        return Stream.of(
                Arguments.of(null, 5L, 1),
                Arguments.of(0, 5L, 1),
                Arguments.of(1, 6L, 1),
                Arguments.of(4, 9L, 1),
                Arguments.of(6, 11L, 2)
        );
    }

    @ParameterizedTest(name = "of={0},ps=5=>of={1},pn={3}")
    @MethodSource("next")
    void next_withNotNullSameMaxPageSize_shouldReturnOffsetPageRequestWithNextPage(Integer fromOffset, Long offset,
                                                                                   Integer pageNumber) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(fromOffset, 5, Sort.unsorted());

        OffsetPageRequest target = source.next();

        assertThat(target, allOf(
                hasProperty("offset", equalTo(offset)),
                hasProperty("pageNumber", equalTo(pageNumber)),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(source.getSort()))
        ));
    }

    private static Stream<Arguments> previousSelf() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, 10),
                Arguments.of(0, null),
                Arguments.of(0, 10)
        );
    }

    @ParameterizedTest(name = "offset={0}, size={1}")
    @MethodSource("previousSelf")
    void previous_withNullOffsetOrNullSameMaxPageSize_shouldReturnSelf(Integer offset, Integer size) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, size, Sort.unsorted());

        OffsetPageRequest target = source.previous();

        assertThat(target == source, is(true));
    }

    @Test
    void previous_withOffsetNotLessThanPageSizeAndNotNullSameMaxPageSize_shouldReturnOffsetPageRequestWithPreviousPage() {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(11, 5, Sort.unsorted());

        OffsetPageRequest target = source.previous();

        assertThat(target, allOf(
                hasProperty("offset", equalTo(source.getOffset() - source.getPageSize())),
                hasProperty("pageNumber", equalTo(source.getPageNumber() - 1)),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(source.getSort()))
        ));
    }

    private static Stream<Arguments> previousZeroOffset() {
        return Stream.of(
                Arguments.of(100000, null),
                Arguments.of(5, 5),
                Arguments.of(3, 5)
        );
    }

    @ParameterizedTest(name = "offset={0}, size={1}")
    @MethodSource("previousZeroOffset")
    void previous_withOffsetLessThanPageSizeOrNullSameMaxPageSize_shouldReturnOffsetPageRequestWithZeroOffset(
            Integer fromOffset, Integer size) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(fromOffset, size, Sort.unsorted());

        OffsetPageRequest target = source.previous();

        assertThat(target, allOf(
                hasProperty("offset", equalTo(0L)),
                hasProperty("pageNumber", equalTo(0)),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(source.getSort()))
        ));
    }

    private static Stream<Arguments> firstSelf() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(0, null),
                Arguments.of(0, 10)
        );
    }

    @ParameterizedTest(name = "offset={0}, size={1}")
    @MethodSource("firstSelf")
    void first_withZeroOffset_shouldReturnSelf(Integer offset, Integer size) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, size, Sort.unsorted());

        OffsetPageRequest target = source.first();

        assertThat(target == source, is(true));
    }

    private static Stream<Arguments> first() {
        return Stream.of(
                Arguments.of(1, null),
                Arguments.of(1, 5),
                Arguments.of(111, 5)
        );
    }

    @ParameterizedTest(name = "offset={0}, size={1}")
    @MethodSource("first")
    void first_withNotZeroOffset_shouldReturnOffsetPageRequestWithZeroOffset(Integer offset, Integer size) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, size, Sort.unsorted());

        OffsetPageRequest target = source.first();

        assertThat(target, allOf(
                hasProperty("offset", equalTo(0L)),
                hasProperty("pageNumber", equalTo(0)),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(source.getSort()))
        ));
    }

    private static Stream<Arguments> equal() {
        return Stream.of(
                Arguments.of(0, null, Sort.unsorted(),
                        null, Integer.MAX_VALUE, null),

                Arguments.of(null, Integer.MAX_VALUE, Sort.by("id").descending(),
                        0, null, Sort.by("id").descending()),

                Arguments.of(0, 10, null,
                        null, 10, Sort.unsorted()),

                Arguments.of(5, null, Sort.unsorted(),
                        5, Integer.MAX_VALUE, null),

                Arguments.of(null, 10, Sort.by("id").descending(),
                        0, 10, Sort.by("id").descending()),

                Arguments.of(5, 10, null,
                        5, 10, Sort.unsorted()),

                Arguments.of(5, Integer.MAX_VALUE, Sort.by("id").descending(),
                        5, null, Sort.by("id").descending()),

                Arguments.of(5, 10, Sort.by("id").descending(),
                        5, 10, Sort.by("id").descending())
        );
    }

    @ParameterizedTest(name = "(of={0},sz={1},st={2})==(of={3},sz={4},st={5})")
    @MethodSource("equal")
    void equals_withEqualFields_shouldReturnTrue(Integer offset1, Integer size1, Sort sort1,
                                                 Integer offset2, Integer size2, Sort sort2) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset1, size1, sort1);
        OffsetPageRequest target = OffsetPageRequest.ofOffset(offset2, size2, sort2);

        assertThat(source.equals(target), is(true));
        assertThat(target.equals(source), is(true));
    }

    private static Stream<Arguments> notEqual() {
        return Stream.of(
                Arguments.of(5, 10, null,
                        5, 10, Sort.by("id").descending()),

                Arguments.of(5, 10, Sort.unsorted(),
                        5, null, Sort.unsorted()),

                Arguments.of(null, 10, Sort.unsorted(),
                        5, 10, Sort.unsorted())
        );
    }

    @ParameterizedTest(name = "(of={0},sz={1},st={2})!=(sz={4},of={3},st={5})")
    @MethodSource("notEqual")
    void equals_withNotEqualFields_shouldReturnFalse(Integer offset1, Integer size1, Sort sort1,
                                                     Integer offset2, Integer size2, Sort sort2) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset1, size1, sort1);
        OffsetPageRequest target = OffsetPageRequest.ofOffset(offset2, size2, sort2);

        assertThat(source.equals(target), is(false));
        assertThat(target.equals(source), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjects_shouldReturnTrue() {
        OffsetPageRequest target = OffsetPageRequest.ofOffset(1, 10, Sort.by("id").descending());

        assertThat(target.equals(target), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNull_shouldReturnfalse() {
        OffsetPageRequest target = OffsetPageRequest.ofOffset(1, 10, Sort.by("id").descending());

        assertThat(target.equals(null), is(false));
    }

    @Test
    void equals_withObjectOfOtherClass_shouldReturnFalse() {
        PageRequest source = PageRequest.of(0, 10, Sort.by("id").descending());
        OffsetPageRequest target =
                OffsetPageRequest.ofOffset(0, 10, Sort.by("id").descending());

        assertThat(target.equals(source), is(false));
    }

    private static Stream<Arguments> withPageSelf() {
        return Stream.of(
                Arguments.of(null, null, 0),
                Arguments.of(null, null, 1),
                Arguments.of(null, null, 10),
                Arguments.of(100, null, 0),
                Arguments.of(100, null, 1),
                Arguments.of(100, null, 10),
                Arguments.of(109, 10, 10),
                Arguments.of(100, 10, 10),
                Arguments.of(19, 10, 1),
                Arguments.of(10, 10, 1),
                Arguments.of(9, 10, 0),
                Arguments.of(0, 10, 0)
        );
    }

    @ParameterizedTest(name = "offset={0}, size={1}, page={2}")
    @MethodSource("withPageSelf")
    void withPage_withNullSameMaxPageSizeOrEqualPage_shouldReturnSelf(Integer offset, Integer size,
                                                                      Integer pageNumber) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, size, Sort.unsorted());

        OffsetPageRequest target = source.withPage(pageNumber);

        assertThat(target == source, is(true));
    }

    @Test
    void withPage_withNegativePage_shouldThrowIllegalArgumentException() {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(null, null, Sort.unsorted());

        assertThrows(IllegalArgumentException.class, () -> source.withPage(-1));
    }

    private static Stream<Arguments> withPage() {
        return Stream.of(
                Arguments.of(0, 2, 20L),
                Arguments.of(9, 2, 29L),
                Arguments.of(10, 0, 0L),
                Arguments.of(19, 0, 9L),
                Arguments.of(10, 5, 50L),
                Arguments.of(19, 5, 59L)
        );
    }

    @ParameterizedTest(name = "of={0},sz=10=>of={2},pn={1}")
    @MethodSource("withPage")
    void withPage_withNotNullSameMaxPageSizeAndNotEqualAndNotNegativePage_shouldReturnOffsetPageRequestWithSelectedPage(
            Integer fromOffset, Integer page, Long offset) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(fromOffset, 10, Sort.unsorted());

        OffsetPageRequest target = source.withPage(page);

        assertThat(target, allOf(
                hasProperty("offset", equalTo(offset)),
                hasProperty("pageNumber", equalTo(page)),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(source.getSort()))
        ));
    }

    private static Stream<Arguments> withSortProperties() {
        return Stream.of(
                Arguments.of(null, Sort.unsorted()),
                Arguments.of(new String[] {null}, Sort.unsorted()),
                Arguments.of(new String[0], Sort.unsorted()),
                Arguments.of(new String[] {"id", null, "name"}, Sort.by("id").ascending()),
                Arguments.of(new String[] {"id", null}, Sort.by("id").ascending()),
                Arguments.of(new String[] {"id"}, Sort.by("id").ascending())
        );
    }

    @ParameterizedTest(name = "sort={1}, properties={0}")
    @MethodSource("withSortProperties")
    void withSort_withSortPropertiesEqualToSort_shouldReturnSelf(String[] properties, Sort sort) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(1, 10, sort);

        OffsetPageRequest target = source.withSort(Sort.Direction.ASC, properties);

        assertThat(target == source, is(true));
    }

    @ParameterizedTest(name = "sort=>{1}, properties={0}")
    @MethodSource("withSortProperties")
    void withSort_withSortPropertiesNotEqualToSort_shouldReturnOffsetPageRequestWithSelectedSortProperties(
            String[] properties, Sort sort) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(1, 10,
                Sort.by("someSort").descending());

        OffsetPageRequest target = source.withSort(null, properties);

        assertThat(target, allOf(
                hasProperty("offset", equalTo(source.getOffset())),
                hasProperty("pageNumber", equalTo(source.getPageNumber())),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(sort))
        ));
    }

    private static Stream<Arguments> withSort() {
        return Stream.of(
                Arguments.of(null, Sort.unsorted()),
                Arguments.of(Sort.by("id").ascending(), Sort.by("id").ascending())
        );
    }

    @ParameterizedTest(name = "sort={0} = {1}")
    @MethodSource("withSort")
    void withSort_withSortEqualToSort_shouldReturnSelf(Sort sort, Sort withSort) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(1, 10, sort);

        OffsetPageRequest target = source.withSort(withSort);

        assertThat(target == source, is(true));
    }

    @ParameterizedTest(name = "sort={0}=>{1}")
    @MethodSource("withSort")
    void withSort_withSortNotEqualToSort_shouldReturnOffsetPageRequestWithSelectedSort(Sort withSort, Sort sort) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(1, 10, Sort.by("sort").descending());

        OffsetPageRequest target = source.withSort(withSort);

        assertThat(target, allOf(
                hasProperty("offset", equalTo(source.getOffset())),
                hasProperty("pageNumber", equalTo(source.getPageNumber())),
                hasProperty("pageSize", equalTo(source.getPageSize())),
                hasProperty("sort", equalTo(sort))
        ));
    }

    @ParameterizedTest(name = "(of={0},sz={1},st={2})!=(of={3},sz={4},st={5})")
    @MethodSource("notEqual")
    void hashCode_ofTwoOffsetPageRequestsWithNotEqualFields_shouldNotBeEqual(
            Integer offset1, Integer size1, Sort sort1, Integer offset2, Integer size2, Sort sort2) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset1, size1, sort1);
        OffsetPageRequest target = OffsetPageRequest.ofOffset(offset2, size2, sort2);

        assertThat(target.hashCode(), not(equalTo(source.hashCode())));
    }

    @ParameterizedTest(name = "(sz={1},of={0},st={2})==(sz={4},of={3},st={5})")
    @MethodSource("equal")
    void hashCode_ofTwoOffsetPageRequestsWithEqualFields_shouldBeEqual(Integer offset1, Integer size1, Sort sort1,
                                                                       Integer offset2, Integer size2, Sort sort2) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset1, size1, sort1);
        OffsetPageRequest target = OffsetPageRequest.ofOffset(offset2, size2, sort2);

        assertThat(target.hashCode(), equalTo(source.hashCode()));
    }

    private static Stream<Arguments> toStringSource() {
        return Stream.of(
                Arguments.of(null, null, null,
                        "Page offset request [offset: 0, number: 0, size " + Integer.MAX_VALUE + ", sort: UNSORTED]"),

                Arguments.of(9, 10, Sort.unsorted(),
                        "Page offset request [offset: 9, number: 0, size 10, sort: UNSORTED]"),

                Arguments.of(10, 10, Sort.by("id").descending(),
                        "Page offset request [offset: 0, number: 1, size 10, sort: id: DESC]"),

                Arguments.of(25, 10, Sort.by("id", "name").descending(),
                        "Page offset request [offset: 5, number: 2, size 10, sort: id: DESC,name: DESC]")
        );
    }

    @ParameterizedTest(name = "(sz={1},of={0},st={2})=>{3}")
    @MethodSource("toStringSource")
    void toString_shouldReturnStringWithAllFieldsCastedToString(Integer offset, Integer size, Sort sort,
                                                                String toString) {
        OffsetPageRequest source = OffsetPageRequest.ofOffset(offset, size, sort);

        String target = source.toString();

        assertThat(target, is(toString));
    }
}