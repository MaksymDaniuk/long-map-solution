package de.comparus.opensource.longmap;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("LongMapImpl Test")
class LongMapImplTest {
    static final int DEFAULT_MAP_TEST_CAPACITY = 8;

    @Nested
    @Order(1)
    @DisplayName("1. Checking LongMapImpl creation")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateMapTest{
        Constructor<LongMapImpl> defaultConstructor;
        Constructor<LongMapImpl> constructor;

        @BeforeEach
        void  init() throws Exception {
            defaultConstructor = LongMapImpl.class.getConstructor();
            constructor = LongMapImpl.class.getConstructor(int.class);
        }

        @Test
        @Order(1)
        @DisplayName("Checking creation by default constructor")
        void defaultConstructor() throws Exception{
            LongMapImpl newLongMap = defaultConstructor.newInstance();
            Object[] table = (Object[]) getPrivateFieldValue("table", newLongMap);
            int defaultCapacity = (int) getPrivateFieldValue("DEFAULT_CAPACITY", newLongMap);

            assertThat(table).hasSize(defaultCapacity);
        }

        @Test
        @Order(2)
        @DisplayName("Checking creation by constructor with parameter of capacity")
        void constructorWithTableCapacity() throws Exception{
            LongMapImpl newLongMap = constructor.newInstance(33);
            Object[] table = (Object[]) getPrivateFieldValue("table", newLongMap);

            assertThat(table).hasSize(33);
        }

        @Test
        @Order(3)
        @DisplayName("Constructor throws exception when argument is negative or zero")
        void constructorWithNegativeOrZeroArgument() throws Exception{
            assertThatThrownBy(() -> constructor.newInstance(-2)).hasCauseInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> constructor.newInstance(0)).hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @Order(4)
        @DisplayName("Checking the limit of the maximum capacity of the table")
        void checkLimitCapacityWhenCreate() throws Exception {
            int maximumCapacity = (int) getPrivateFieldValue("MAXIMUM_CAPACITY", null);
            LongMapImpl longMap = constructor.newInstance(maximumCapacity + 1);
            Object[] table =(Object[]) getPrivateFieldValue("table", longMap);

            assertThat(table).hasSize(maximumCapacity);
        }
    }

    @Nested
    @Order(2)
    @DisplayName("2. Hash Function Test")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class HastFunctionTest{

        @Test
        @Order(1)
        @DisplayName("Calculate index returns the same value for the same key")
        void calculateIndexReturnTheSameValueWhenTheSameKey(){
            Set<Object> indexSet = Stream.generate(() -> 3L)
                    .limit(10)
                    .map(key -> LongMapImpl.calculateIndex(key, DEFAULT_MAP_TEST_CAPACITY))
                    .collect(Collectors.toSet());

            assertThat(indexSet.toArray())
                    .hasSize(1);
        }

        @Test
        @Order(2)
        @DisplayName("Calculate index return different values for different keys")
        void calculateIndexReturnDifferentValuesForDifferentKey(){
            Set<Object> indexSet = Stream.of( 1L, 2L, 3L, -1L, -4L)
                    .map(key -> LongMapImpl.calculateIndex(key, DEFAULT_MAP_TEST_CAPACITY))
                    .collect(Collectors.toSet());

            assertThat(indexSet.toArray())
                    .hasSizeGreaterThan(1);
        }

        @Test
        @Order(3)
        @DisplayName("Calculate index returns values in array bounds")
        void calculateIndexReturnIndexInArrayBounds(){
            final int LIMIT = 100;
            List<Long> keys = Stream.generate(() -> ThreadLocalRandom.current().nextLong())
                    .limit(LIMIT)
                    .collect(Collectors.toList());

            List<Integer> indexes = keys.stream()
                    .map(key -> LongMapImpl.calculateIndex(key, DEFAULT_MAP_TEST_CAPACITY))
                    .filter(i -> i >= 0 && i < DEFAULT_MAP_TEST_CAPACITY)
                    .collect(Collectors.toList());

            assertThat(indexes.toArray())
                    .hasSize(LIMIT);
        }

        @Test
        @Order(4)
        @DisplayName("Calculate index return non-negative value when key is negative")
        void calculateIndexReturnPositiveIndexKeyIsNegative(){
            int index = LongMapImpl.calculateIndex(Long.MIN_VALUE, DEFAULT_MAP_TEST_CAPACITY);

            assertThat(index).isNotNegative();
        }
    }

    @Nested
    @Order(3)
    @DisplayName("3. LongMapImpl methods Test")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LongMapImplMethodsTest{
        LongMapImpl<String> stringLongMap;

        @BeforeEach
        void init(){
            stringLongMap = new LongMapImpl<>(DEFAULT_MAP_TEST_CAPACITY);
        }

        @Test
        @Order(1)
        @DisplayName("Put new entry when LongMap is empty")
        void putNewEntryWhenMapIsEmpty() throws Exception{
            long sizeBefore = (long) getPrivateFieldValue("size", stringLongMap);
            String previousValue = stringLongMap.put( 1L, "Some text");
            long sizeAfter = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(sizeBefore).isEqualTo(0L);
            assertNull(previousValue);
            assertThat(sizeAfter).isEqualTo(1L);
        }

        @Test
        @Order(2)
        @DisplayName("Put new entry when LongMap is not empty")
        void putNewEntryWhenMapIsNotEmpty() throws Exception{
            int numberAdditionEntries = 10;
            addEntriesToMap(stringLongMap, numberAdditionEntries);
            long sizeBefore = (long) getPrivateFieldValue("size", stringLongMap);
            String previousValue = stringLongMap.put((long) (numberAdditionEntries + 1), "some value");
            long sizeAfter = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(sizeBefore).isEqualTo(numberAdditionEntries);
            assertNull(previousValue);
            assertThat(sizeAfter).isEqualTo(numberAdditionEntries + 1);
        }

        @Test
        @Order(3)
        @DisplayName("Put element updates the value and returns the previous one when key is the same")
        void putEntriesWithTheSameKey() throws Exception{
            addEntriesToMap(stringLongMap, 10);
            stringLongMap.put(25L, "Old value");
            long sizeBefore = (long) getPrivateFieldValue("size", stringLongMap);
            String previousValue = stringLongMap.put(25L, "New value");
            long sizeAfter = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(sizeAfter).isEqualTo(sizeBefore);
            assertThat(previousValue).isEqualTo("Old value");
        }

        @Test
        @Order(4)
        @DisplayName("Put method throws Exception when retrieve key null")
        void putThrowsExceptionWhenKeyOrValueIsNull(){
            Long keyNull = null;

            assertThrows(NullPointerException.class, () -> stringLongMap.put(keyNull, "value"));
        }

        @Test
        @Order(5)
        @DisplayName("Get return null when given key does not exist")
        void getWhenKeyDoesNotExists(){
            long defunctKey = 100L;
            String valueWhenMapIsEmpty = stringLongMap.get(defunctKey);
            addEntriesToMap(stringLongMap, 5);
            String valueWhenMapIsNotEmpty = stringLongMap.get(defunctKey);

            assertNull(valueWhenMapIsEmpty);
            assertNull(valueWhenMapIsNotEmpty);
        }

        @Test
        @Order(6)
        @DisplayName("Get return a corresponding value by the given key")
        void getWhenKeyExists(){
            long existKey = 3L;
            stringLongMap.put(existKey, "Correct value");
            String value = stringLongMap.get(existKey);

            assertThat(value).isEqualTo("Correct value");
        }

        @Test
        @Order(7)
        @DisplayName("Get return a corresponding value when there are other keys with the same index")
        void getWhenOtherKeysHaveTheSameIndex(){
            long[] keys = getKeysTheSameIndex(3, DEFAULT_MAP_TEST_CAPACITY);
            stringLongMap.put(keys[0], "Some value");
            stringLongMap.put(keys[1], "Some value");
            stringLongMap.put(keys[2], "Value to check");
            String value = stringLongMap.get(keys[2]);

            assertThat(value).isEqualTo("Value to check");
        }

        @Test
        @Order(8)
        @DisplayName("Get throws exception when parameter is null")
        void getThrowsExceptionWhenNull(){
            Long keyNull = null;
            assertThrows(NullPointerException.class, () -> stringLongMap.get(keyNull));
        }

        @Test
        @Order(9)
        @DisplayName("Remove return null when key does not exist")
        void removeReturnNullWhenKeyDoesNotExist(){
            long defunctKey = 10L;
            String resultWhenMapIsEmpty = stringLongMap.remove(defunctKey);
            stringLongMap.put(1L, "Some value");
            String resultWhenMapIsNotEmpty = stringLongMap.remove(defunctKey);

            assertNull(resultWhenMapIsEmpty);
            assertNull(resultWhenMapIsNotEmpty);
        }

        @Test
        @Order(10)
        @DisplayName("Remove deletes the entry when it`s exist")
        void removeWhenEntryExist() throws Exception{
            stringLongMap.put(1L, "value");
            String deleteValue = stringLongMap.remove(1L);
             long size =(long) getPrivateFieldValue("size", stringLongMap);

            assertThat(deleteValue).isEqualTo("value");
            assertThat(size).isEqualTo(0);
        }

        @Test
        @Order(11)
        @DisplayName("Remove deletes entry when it`s in the start of list")
        void removeWhenInStartOfList() throws Exception{
            String removedValue = addToCurrentMapThreeEntryInTheSameIndexEndRemoveBy(0);
            long size = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(removedValue).isEqualTo("First value");
            assertThat(size).isEqualTo(2L);
        }

        @Test
        @Order(12)
        @DisplayName("Remove deletes entry when it`s in the midl of list")
        void removeWhenInTheMidlOfList() throws Exception{
            String removedValue = addToCurrentMapThreeEntryInTheSameIndexEndRemoveBy(1);
            long size = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(removedValue).isEqualTo("Second value");
            assertThat(size).isEqualTo(2L);
        }

        @Test
        @Order(13)
        @DisplayName("Remove deletes entry when it`s in the end of list")
        void removeWhenInTheEndOfList() throws Exception{
            String removedValue = addToCurrentMapThreeEntryInTheSameIndexEndRemoveBy(2);
            long size = (long) getPrivateFieldValue("size", stringLongMap);

            assertThat(removedValue).isEqualTo("Third value");
            assertThat(size).isEqualTo(2L);
        }

        private String addToCurrentMapThreeEntryInTheSameIndexEndRemoveBy(int i) {
            long[] keys = getKeysTheSameIndex(3, DEFAULT_MAP_TEST_CAPACITY);
            stringLongMap.put(keys[0], "First value");
            stringLongMap.put(keys[1], "Second value");
            stringLongMap.put(keys[2], "Third value");
            return stringLongMap.remove(keys[i]);
        }

        @Test
        @Order(14)
        @DisplayName("Remove throws Exception when parameter is null")
        void removeThrowsExceptionWhenParameterIsNull(){
            Long keyNull = null;
            assertThrows(NullPointerException.class, () -> stringLongMap.remove(keyNull));
        }

        @Test
        @Order(15)
        @DisplayName("IsEmpty when map is empty and when is not one")
        void isEmptyTest(){
            boolean whenMapIsEmpty = stringLongMap.isEmpty();
            stringLongMap.put(1L, "Value");
            boolean whenMapIsNotEmpty = stringLongMap.isEmpty();
            stringLongMap.remove(1L);
            boolean whenMapIsEmptyAgan = stringLongMap.isEmpty();

            assertThat(whenMapIsEmpty).isTrue();
            assertThat(whenMapIsNotEmpty).isFalse();
            assertThat(whenMapIsEmptyAgan).isTrue();
        }

        @Test
        @Order(16)
        @DisplayName("ContainsKey when map is empty and when it is not")
        void containsKeyTest(){
            long key = 1L;
            boolean whenMapIsEmpty = stringLongMap.containsKey(key);
            stringLongMap.put(2L, "Value");
            boolean whenMapIsNotEmpty = stringLongMap.containsKey(key);
            stringLongMap.put(key, "Value");
            boolean whenKeyIsInTheMap = stringLongMap.containsKey(key);

            assertThat(whenMapIsEmpty).isFalse();
            assertThat(whenMapIsNotEmpty).isFalse();
            assertThat(whenKeyIsInTheMap).isTrue();
        }

        @Test
        @Order(17)
        @DisplayName("ContainsKey throws Exception when parameter is null")
        void containsKeyThrowsExceptionWhenParameterIsNull(){
            Long keyNull = null;
            assertThrows(NullPointerException.class, () -> stringLongMap.containsKey(keyNull));
        }

        @Test
        @Order(18)
        @DisplayName("ContainsValue when map is empty and when it is not")
        void containsValueTest(){
            String value = "Value";
            boolean whenMapIsEmpty = stringLongMap.containsValue(value);
            stringLongMap.put(1L, "Some value");
            boolean whenMapIsNotEmpty = stringLongMap.containsValue(value);
            stringLongMap.put(2L, value);
            boolean whenValueIsExist = stringLongMap.containsValue(value);
            stringLongMap.put(3L, value);
            boolean whenMultipleValueAreExist = stringLongMap.containsValue(value);

            assertThat(whenMapIsEmpty).isFalse();
            assertThat(whenMapIsNotEmpty).isFalse();
            assertThat(whenValueIsExist).isTrue();
            assertThat(whenMultipleValueAreExist).isTrue();
        }

        @Test
        @Order(19)
        @DisplayName("ContainsValue when value is null")
        void containsValueTestWhenValueIsNull(){
            String valueNull = null;
            boolean whenMapIsEmpty = stringLongMap.containsValue(valueNull);
            stringLongMap.put(1L, "Some value");
            boolean whenMapIsNotEmpty = stringLongMap.containsValue(valueNull);
            stringLongMap.put(2L, valueNull);
            boolean whenMapContainsNullValue = stringLongMap.containsValue(valueNull);
            stringLongMap.put(3L, valueNull);
            boolean whenMapContainsMultipleNullValues = stringLongMap.containsValue(valueNull);

            assertThat(whenMapIsEmpty).isFalse();
            assertThat(whenMapIsNotEmpty).isFalse();
            assertThat(whenMapContainsNullValue).isTrue();
            assertThat(whenMapContainsMultipleNullValues).isTrue();
        }
        @Test
        @Order(20)
        @DisplayName("Keys get correct array when map is empty, has one entry and has many entries")
        void keysWhenMapIsEmpty(){
            long[] keysArrayWhenMapEmpty = stringLongMap.keys();
            stringLongMap.put(1L, "value 1");
            long[] keysArrayWhenMapHasOneEntry = stringLongMap.keys();
            stringLongMap.put(2L, "value 2");
            long[] keysArrayWhenMapHasManyEntries = stringLongMap.keys();

            assertThat(keysArrayWhenMapEmpty).hasSize(0);

            assertThat(keysArrayWhenMapHasOneEntry).hasSize(1);
            assertThat(keysArrayWhenMapHasOneEntry[0]).isEqualTo(1L);

            assertThat(keysArrayWhenMapHasManyEntries).hasSize(2);
            assertThat(keysArrayWhenMapHasManyEntries[0]).isEqualTo(1L);
            assertThat(keysArrayWhenMapHasManyEntries[1]).isEqualTo(2L);
        }

        @Test
        @Order(21)
        @DisplayName("Values get correct array when map is empty, has one entry and has many entries when generic type is String")
        void valuesTestWhenString(){
            String[] whenMapIsEmpty = stringLongMap.values();
            stringLongMap.put(1L, "value 1");
            String[] whenMapHasOneEntry = stringLongMap.values();
            stringLongMap.put(2L, "value 2");
            String[] whenMapHasManyEntries = stringLongMap.values();

            assertThat(whenMapIsEmpty).isNull();

            assertThat(whenMapHasOneEntry).hasSize(1);
            assertThat(whenMapHasOneEntry[0]).isEqualTo("value 1");

            assertThat(whenMapHasManyEntries).hasSize(2);
            assertThat(whenMapHasManyEntries[0]).isEqualTo("value 1");
            assertThat(whenMapHasManyEntries[1]).isEqualTo("value 2");
        }

        @Test
        @Order(22)
        @DisplayName("Values get correct array when map is empty, has one entry and has many entries when generic type is Integer")
        void valuesTestWhenInteger(){
            LongMapImpl<Integer> integerLongMap = new LongMapImpl<>();

            Integer[] whenMapIsEmpty = integerLongMap.values();
            integerLongMap.put(1L, 12);
            Integer[] whenMapHasOneEntry = integerLongMap.values();
            integerLongMap.put(2L, 13);
            Integer[] whenMapHasManyEntries = integerLongMap.values();

            assertThat(whenMapIsEmpty).isNull();

            assertThat(whenMapHasOneEntry).hasSize(1);
            assertThat(whenMapHasOneEntry[0]).isEqualTo(12);

            assertThat(whenMapHasManyEntries).hasSize(2);
            assertThat(whenMapHasManyEntries[0]).isEqualTo(12);
            assertThat(whenMapHasManyEntries[1]).isEqualTo(13);
        }

        @Test
        @Order(23)
        @DisplayName("Size return correct value")
        void sizeTest(){
            long whenMapIsEmpty = stringLongMap.size();
            stringLongMap.put(1L, "value");
            long whenMapHasOneEntry = stringLongMap.size();
            stringLongMap.put(2L, "value");
            long whenMapHasManyEntries = stringLongMap.size();
            stringLongMap.remove(1L);
            stringLongMap.remove(2L);
            long whenMapIsEmptyAgan = stringLongMap.size();

            assertThat(whenMapIsEmpty).isEqualTo(0L);
            assertThat(whenMapHasOneEntry).isEqualTo(1L);
            assertThat(whenMapHasManyEntries).isEqualTo(2L);
            assertThat(whenMapIsEmptyAgan).isEqualTo(0L);
        }

        @Test
        @Order(24)
        @DisplayName("Clear when map is not empty")
        void clearTest() throws Exception{
            addEntriesToMap(stringLongMap, 1000);
            stringLongMap.clear();

            long newSize = (long) getPrivateFieldValue("size", stringLongMap);
            Object[] table = (Object[]) getPrivateFieldValue("table", stringLongMap);
            int defaultCapacity = (int) getPrivateFieldValue("DEFAULT_CAPACITY", stringLongMap);

            assertThat(newSize).isEqualTo(0L);
            assertThat(table.length).isEqualTo(defaultCapacity);
        }

        @Test
        @Order(25)
        @DisplayName("Clear when map is empty")
        void clearTestWhenMapIsEmpty() throws Exception{
            stringLongMap.clear();

            long newSize = (long) getPrivateFieldValue("size", stringLongMap);
            Object[] table = (Object[]) getPrivateFieldValue("table", stringLongMap);
            int defaultCapacity = (int) getPrivateFieldValue("DEFAULT_CAPACITY", stringLongMap);

            assertThat(newSize).isEqualTo(0L);
            assertThat(table.length).isEqualTo(defaultCapacity);
        }
    }

    private long[] getKeysTheSameIndex(int number, int mapCapacity) {
        long[] keys = new long[number];
        keys[0] = 1L;
        int currentIndex = LongMapImpl.calculateIndex(keys[0], mapCapacity);
        for (int i = 1; i < number; i++) {
            while(true) {
                keys[i] = ThreadLocalRandom.current().nextLong();
                if(currentIndex == LongMapImpl.calculateIndex(keys[i], DEFAULT_MAP_TEST_CAPACITY)) break;
            }
        }
        return keys;
    }

    public void addEntriesToMap(LongMapImpl<String> stringLongMap, int number){
        Stream.iterate(0, n -> n + 1)
                .limit(number)
                .forEach(key -> stringLongMap.put(key, "str" + key));
    }

    private Object getPrivateFieldValue(String fieldName, LongMapImpl newLongMap) throws Exception {
        Field field = LongMapImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(newLongMap);
    }
}