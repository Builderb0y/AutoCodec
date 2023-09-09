package builderb0y.autocodec.reflection.reification;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import org.junit.Test;

import builderb0y.autocodec.reflection.AnnotationContainer;
import builderb0y.autocodec.reflection.reification.ReifiedTypeTest.Outer.Inner;
import builderb0y.autocodec.reflection.reification.ReifiedTypeTest.Outer.ParameterizedInner;

import static org.junit.Assert.*;

@SuppressWarnings({ "ConstantConditions", "rawtypes" }) //suppresses null analysis.
public class ReifiedTypeTest {

	@Test
	public void testAnnotatedList() {
		ReifiedType<List<String>> listType = new ReifiedType<@A("list") List<@A("element") String>>() {};
		assertEquals(List.class, listType.getRawClass());
		assertEquals("list", onlyA(listType.getAnnotations()));

		assertNotNull("listType has no parameters", listType.getParameters());
		assertEquals(1, listType.getParameters().length);
		ReifiedType<?> elementType = listType.getParameters()[0];
		assertEquals(String.class, elementType.getRawClass());
		assertEquals("element", onlyA(elementType.getAnnotations()));
	}

	@Test
	public void testInterfaceResolution() {
		//reminder: ArrayList directly implements List *in addition* to
		//extending AbstractList, which ALSO directly implements List.
		ReifiedType<ArrayList<String>> arrayListType = new ReifiedType<@A("list") ArrayList<@A("element") String>>() {};
		ReifiedType<? extends List> listType = arrayListType.resolveAncestor(List.class);
		assertEquals(List.class, listType.getRawClass());
		assertNotNull("listType has no parameters", listType.getParameters());
		assertEquals(1, listType.getParameters().length);
		ReifiedType<?> elementType = listType.getParameters()[0];
		assertEquals(String.class, elementType.getRawClass());
		assertEquals("element", onlyA(elementType.getAnnotations()));
	}

	@Test
	public void testAnnotatedArray() {
		ReifiedType<?> arrayType = new ReifiedType<@A("element") String @A("array") []>() {};
		assertEquals(String[].class, arrayType.getRawClass());
		assertEquals("array", onlyA(arrayType.getAnnotations()));

		ReifiedType<?> elementType = arrayType.getArrayComponentType();
		assertNotNull(elementType);
		assertEquals(String.class, elementType.getRawClass());
		assertEquals("element", onlyA(elementType.getAnnotations()));

		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(arrayType, ReifiedType.from(String[].class)));
	}

	@Test
	public void testInheritance() {
		ReifiedType<?> type = new ReifiedType<C3<@A("present") String>>() {};

		ReifiedType<? extends C1> parameterized = type.resolveAncestor(C1.class);
		assertEquals(1, parameterized.getParameters().length);
		ReifiedType<?> parameter = parameterized.getParameters()[0];
		assertEquals(String.class, parameter.getRawClass());
		assertEquals("present", onlyA(parameter.getAnnotations()));
	}

	public static class C1<T1> {}
	public static class C2<T2> extends C1<T2> {}
	public static class C3<T3> extends C2<T3> {}

	@Test
	public void testMultipleSubclasses() {
		//String and Integer both implement Comparable.
		ReifiedType<?> hashMapType = new ReifiedType<HashMap<@A("key") String, @A("value") Integer>>() {};
		assertNull(hashMapType.resolveAncestor(Comparable.class));

		ReifiedType<?> keyType = hashMapType.getParameters()[0];
		ReifiedType<?> valueType = hashMapType.getParameters()[1];

		assertEquals(String.class, keyType.getRawClass());
		assertEquals("key", onlyA(keyType.getAnnotations()));
		assertEquals(Integer.class, valueType.getRawClass());
		assertEquals("value", onlyA(valueType.getAnnotations()));

		ReifiedType<? extends Map> mapType = hashMapType.resolveAncestor(Map.class);
		assertNull(mapType.resolveAncestor(Comparable.class));

		keyType = mapType.getParameters()[0];
		valueType = mapType.getParameters()[1];

		assertEquals(String.class, keyType.getRawClass());
		assertEquals("key", onlyA(keyType.getAnnotations()));
		assertEquals(Integer.class, valueType.getRawClass());
		assertEquals("value", onlyA(valueType.getAnnotations()));
	}

	@Test
	public void testMismatchedParameterCounts() {
		ReifiedType<?> xyType = new ReifiedType<XY<@A("x") String, @A("y") Integer>>() {};

		ReifiedType<? extends X> xType = xyType.resolveAncestor(X.class);
		assertEquals(1, xType.getParameters().length);

		ReifiedType<?> xArgType = xType.getParameters()[0];
		assertEquals(String.class, xArgType.getRawClass());
		assertEquals("x", onlyA(xArgType.getAnnotations()));
	}

	@Test
	public void testNestedGenerics() {
		ReifiedType<?> one = new ReifiedType<@A("1") X<@A("2") X<@A("3") X<@A("4") String>>>>() {};
		assertEquals(1, one.getParameters().length);
		assertEquals(X.class, one.getRawClass());
		assertEquals("1", onlyA(one.getAnnotations()));

		ReifiedType<?> two = one.getParameters()[0];
		assertEquals(1, two.getParameters().length);
		assertEquals(X.class, two.getRawClass());
		assertEquals("2", onlyA(two.getAnnotations()));

		ReifiedType<?> three = two.getParameters()[0];
		assertEquals(1, three.getParameters().length);
		assertEquals(X.class, three.getRawClass());
		assertEquals("3", onlyA(three.getAnnotations()));

		ReifiedType<?> four = three.getParameters()[0];
		assertNull("four should not have parameters", four.getParameters());
		assertEquals(String.class, four.getRawClass());
		assertEquals("4", onlyA(four.getAnnotations()));
	}

	public static class X<T_X> {}
	public static class XY<T_X, T_Y> extends X<T_X> {}

	@Test
	public void testEnum() {
		ReifiedType<?> e = new ReifiedType<E>() {};
		assertEquals(E.class, e.getRawClass());
		ReifiedType<?> enumType = e.resolveAncestor(Enum.class); //Enum<E>
		assertEquals(Enum.class, enumType.getRawClass());
		assertEquals(1, enumType.getParameters().length);
		assertEquals(e, enumType.getParameters()[0]);
	}

	public static enum E {}

	@Test
	public void testEnumLike() {
		ReifiedType<?> e = new ReifiedType<ELike>() {};
		assertEquals(ELike.class, e.getRawClass());
		ReifiedType<?> enumType = e.resolveAncestor(EnumLike.class); //EnumLike<ELike>
		assertEquals(EnumLike.class, enumType.getRawClass());
		assertEquals(1, enumType.getParameters().length);
		assertEquals(e, enumType.getParameters()[0]);
	}

	public static class EnumLike<E extends EnumLike<E>> {}

	public static class ELike extends EnumLike<ELike> {}

	@Test
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void testRaw() {
		try { ReifiedType.from(ArrayList.class);                   fail(); } catch (TypeReificationException expected) {}
		try { ReifiedType.from(List[].class);                      fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<ArrayList                    >() {}; fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<List<List>                   >() {}; fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<List<? extends List>         >() {}; fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<List<? super List>           >() {}; fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<List[]                       >() {}; fail(); } catch (TypeReificationException expected) {}
		try { new ReifiedType<Map<String, ? extends List[]>>() {}; fail(); } catch (TypeReificationException expected) {}
		BadReifiedTypeFactory<String> factory = new BadReifiedTypeFactory<>();
		try { factory.         create1();                          fail(); } catch (TypeReificationException expected) {}
		try { factory.<Integer>create2();                          fail(); } catch (TypeReificationException expected) {}
		ReifiedType.from(ExtendingRaw.class).getInheritanceHierarchy();
		new ReifiedType<ExtendingRaw>() {}.getInheritanceHierarchy();
		assertEquals(TypeClassification.UNRESOLVABLE_VARIABLE, ReifiedType.from(ExtendingRaw.class).resolveParameter(ArrayList.class).getClassification());
		assertEquals(TypeClassification.UNRESOLVABLE_VARIABLE, ReifiedType.from(ExtendingRaw.class).resolveParameter(List.class).getClassification());
	}

	public static class BadReifiedTypeFactory<T1> {

		public ReifiedType<T1> create1() {
			return new ReifiedType<T1>() {};
		}

		public <T2> ReifiedType<T2> create2() {
			return new ReifiedType<T2>() {};
		}
	}

	public static class ExtendingRaw extends ArrayList {}

	@Test
	public void testSuperClasses() {
		ReifiedType<?> arrayListType = new ReifiedType<ArrayList<@A("element") String>>() {};

		ReifiedType<?> abstractListType = arrayListType.getSuperClassType();
		assertEquals(AbstractList.class, abstractListType.getRawClass());
		assertEquals(1, abstractListType.getParameters().length);
		assertEquals(String.class, abstractListType.getParameters()[0].getRawClass());
		assertEquals("element", onlyA(abstractListType.getParameters()[0].getAnnotations()));

		ReifiedType<?> abstractCollectionType = abstractListType.getSuperClassType();
		assertEquals(AbstractCollection.class, abstractCollectionType.getRawClass());
		assertEquals(1, abstractCollectionType.getParameters().length);
		assertEquals(String.class, abstractCollectionType.getParameters()[0].getRawClass());
		assertEquals("element", onlyA(abstractCollectionType.getParameters()[0].getAnnotations()));

		ReifiedType<?> objectType = abstractCollectionType.getSuperClassType();
		assertEquals(Object.class, objectType.getRawClass());
		assertNull(objectType.getParameters());

		assertNull(objectType.getSuperClassType());
	}

	@Test
	public void testDeclarations() throws NoSuchFieldException {
		ReifiedType<?> boxType = new ReifiedType<Box<@A("element type") String>>() {};
		ReifiedType<?> boxParamType = boxType.getParameters()[0];

		ReifiedType<?> valueType = boxType.resolveDeclaration(Box.class.getDeclaredField("value"     ).getAnnotatedType());
		ReifiedType<?> arrayType = boxType.resolveDeclaration(Box.class.getDeclaredField("valueArray").getAnnotatedType());
		ReifiedType<?>  listType = boxType.resolveDeclaration(Box.class.getDeclaredField("valueList" ).getAnnotatedType());

		assertEquals(  String.class, valueType.getRawClass());
		assertEquals(String[].class, arrayType.getRawClass());
		assertEquals(    List.class,  listType.getRawClass());

		ReifiedType<?> arrayElementType = arrayType.getArrayComponentType();
		ReifiedType<?>  listElementType =  listType.getParameters()[0];

		assertEquals(String.class, arrayElementType.getRawClass());
		assertEquals(String.class,  listElementType.getRawClass());

		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(valueType,     boxParamType));
		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(valueType, arrayElementType));
		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(valueType,  listElementType));

		assertEquals(List.of("element type",                  "var dec"), allA(    boxParamType.getAnnotations()));
		assertEquals(List.of("element type", "box value",     "var dec"), allA(       valueType.getAnnotations()));
		assertEquals(List.of("element type", "array element", "var dec"), allA(arrayElementType.getAnnotations()));
		assertEquals(List.of("element type", "list element",  "var dec"), allA( listElementType.getAnnotations()));

		assertEquals("array type", onlyA(arrayType.getAnnotations()));
		assertEquals( "list type", onlyA( listType.getAnnotations()));
	}

	@Test
	public void testUnrawArrays() throws NoSuchFieldException {
		ReifiedType<?> boxType = new ReifiedType<Box<? extends Number>>() {};
		ReifiedType<?> arrayType = boxType.resolveDeclaration(Box.class.getDeclaredField("valueArray").getAnnotatedType());
		assertEquals(TypeClassification.ARRAY, arrayType.getClassification());
		assertNotNull(arrayType.getArrayComponentType());
		assertNull(arrayType.getRawClass());
	}

	public static class Box<@A("var dec") T> {

		public @A("box value") T value;
		public @A("array element") T @A("array type") [] valueArray;
		public @A("list type") List<@A("list element") T> valueList;
	}

	@Test
	public void testDoubleBox() throws NoSuchFieldException {
		ReifiedType<?> doubleBoxType = new ReifiedType<BoxBox<@A("the end") String>>() {};
		assertEquals(List.of(), allA(doubleBoxType.getAnnotations()));
		ReifiedType<?> paramType = doubleBoxType.getParameters()[0];
		assertEquals(List.of("the end", "double box"), allA(paramType.getAnnotations()));
		ReifiedType<?> singleBoxType = doubleBoxType.resolveDeclaration(BoxBox.class.getDeclaredField("box").getAnnotatedType());
		assertEquals(List.of(), allA(singleBoxType.getAnnotations()));
		ReifiedType<?> elementType = singleBoxType.getParameters()[0];
		assertEquals(List.of("the end", "single box", "double box", "var dec"), allA(elementType.getAnnotations()));
	}

	public static class BoxBox<@A("double box") E> {

		public Box<@A("single box") E> box;
	}

	@Test
	public void testMultipleImplementationsOfTheSameInterface() {
		ReifiedType<?> extendedType = new ReifiedType<@A("Extended") Extended>() {};
		assertEquals("Extended", onlyA(extendedType.getAnnotations()));
		assertEquals("Extended (Base)", onlyA(extendedType.getSuperClassType().getAnnotations()));
		assertEquals("Extended (I)", onlyA(extendedType.getSuperInterfaceTypes()[0].getAnnotations()));
		assertEquals("Base (I)", onlyA(extendedType.getSuperClassType().getSuperInterfaceTypes()[0].getAnnotations()));

		ReifiedType<?> baseType = extendedType.resolveAncestor(Base.class);
		assertEquals("Extended (Base)", onlyA(baseType.getAnnotations()));

		ReifiedType<?> iType = extendedType.resolveAncestor(I.class);
		assertEquals("Extended (I)", onlyA(iType.getAnnotations()));
	}

	public static interface I {}

	public static class Base implements @A("Base (I)") I {}

	public static class Extended extends @A("Extended (Base)") Base implements @A("Extended (I)") I {}

	@Test
	public void testInheritanceHierarchy() {
		assertEquals(
			List.of(
				ArrayList.class,
				List.class,
				Collection.class,
				Iterable.class,
				RandomAccess.class,
				Cloneable.class,
				Serializable.class,
				AbstractList.class,
				AbstractCollection.class,
				Object.class
			),
			new ReifiedType<ArrayList<String>>() {}
			.getInheritanceHierarchy()
			.stream()
			.map(ReifiedType::requireRawClass)
			.toList()
		);
	}

	@Test
	public void testRecursiveAnnotations() throws NoSuchFieldException {
		ReifiedType<?> top = ReifiedType.from(RecursiveAnnotations.class);
		ReifiedType<?> middle = top.resolveDeclaration(RecursiveAnnotations.class.getDeclaredField("recursiveAnnotations").getAnnotatedType());
		ReifiedType<?> bottom = middle.resolveDeclaration(RecursiveAnnotations.class.getDeclaredField("recursiveAnnotations").getAnnotatedType());
		assertEquals(middle, bottom);
		assertEquals(middle.getAnnotations(), bottom.getAnnotations());
		assertFalse(top.getAnnotations().has(A.class));
		assertEquals(onlyA(middle.getAnnotations()), onlyA(bottom.getAnnotations()));
	}

	public static class RecursiveAnnotations {

		public @A("recursive") RecursiveAnnotations recursiveAnnotations;
	}

	@Test
	public void testPrimitiveTypeParameters() throws NoSuchFieldException {
		ReifiedType<ParamBox<Float>> expected = ReifiedType.parameterize(ParamBox.class, ReifiedType.from(Float.class));
		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(expected, ReifiedType.parameterize(ParamBox.class, ReifiedType.from(float.class))));
		assertTrue(ReifiedType.GENERIC_TYPE_STRATEGY.equals(expected, ReifiedType.parameterize(ParamBox.class, ReifiedType.from(ParamBox.class.getDeclaredField("value").getAnnotatedType()))));
	}

	public static class ParamBox<T> {

		public float value;
	}

	@Test
	public void testFactoryMethods() {
		ReifiedType<List<String>> listType = ReifiedType.parameterize(List.class, ReifiedType.from(String.class));
		assertEquals(List.class, listType.getRawClass());
		assertArrayEquals(new ReifiedType[] { ReifiedType.from(String.class) }, listType.getParameters());

		ReifiedType<Outer<String>.Inner> innerType = new ReifiedType<Outer<String>.Inner>() {};
		assertEquals(Inner.class, innerType.getRawClass());
		assertEquals(Outer.class, innerType.getOwner().getRawClass());
		assertEquals(String.class, innerType.getOwner().getParameters()[0].getRawClass());

		innerType = ReifiedType.withOwner(ReifiedType.parameterize(Outer.class, ReifiedType.from(String.class)), Inner.class);
		assertEquals(Inner.class, innerType.getRawClass());
		assertEquals(Outer.class, innerType.getOwner().getRawClass());
		assertEquals(String.class, innerType.getOwner().getParameters()[0].getRawClass());

		ReifiedType<Outer<String>.ParameterizedInner<Integer>> parameterizedInnerType = new ReifiedType<Outer<String>.ParameterizedInner<Integer>>() {};
		assertEquals(ParameterizedInner.class, parameterizedInnerType.getRawClass());
		assertEquals(Integer.class, parameterizedInnerType.getParameters()[0].getRawClass());
		assertEquals(Outer.class, parameterizedInnerType.getOwner().getRawClass());
		assertEquals(String.class, parameterizedInnerType.getOwner().getParameters()[0].getRawClass());

		parameterizedInnerType = ReifiedType.parameterizeWithOwner(ReifiedType.parameterize(Outer.class, ReifiedType.from(String.class)), ParameterizedInner.class, ReifiedType.from(Integer.class));
		assertEquals(ParameterizedInner.class, parameterizedInnerType.getRawClass());
		assertEquals(Integer.class, parameterizedInnerType.getParameters()[0].getRawClass());
		assertEquals(Outer.class, parameterizedInnerType.getOwner().getRawClass());
		assertEquals(String.class, parameterizedInnerType.getOwner().getParameters()[0].getRawClass());
	}

	public static class Outer<A> {

		public class Inner {}

		public class ParameterizedInner<B> {}
	}

	@Test
	public void testInfiniteRecursion() throws NoSuchMethodException, NoSuchFieldException {
		ReifiedType<?> typeA = new TypeReifier(Collections.emptyMap(), true).reify(ReifiedTypeTest.class.getDeclaredMethod("infiniteRecursion", Comparable.class).getAnnotatedParameterTypes()[0]);
		ReifiedType<?> typeB = new TypeReifier(Collections.emptyMap(), true).reify(InfiniteRecursion.class.getDeclaredField("INSTANCE").getAnnotatedType());
		assertEquals("unresolvable C extends java.lang.Comparable<C>", typeA.toString());
		assertEquals("builderb0y.autocodec.reflection.reification.ReifiedTypeTest$InfiniteRecursion<unresolvable C extends java.lang.Comparable<C>>", typeB.toString());
	}

	public static <C extends Comparable<C>> void infiniteRecursion(C object) {}

	public static class InfiniteRecursion<C extends Comparable<C>> {

		@SuppressWarnings({ "rawtypes", "InstantiationOfUtilityClass" })
		public static final InfiniteRecursion INSTANCE = new InfiniteRecursion();
	}

	public static List<String> allA(AnnotationContainer container) {
		return Arrays.stream(container.getAll(A.class)).map(A::value).toList();
	}

	public static String onlyA(AnnotationContainer container) {
		A[] all = container.getAll(A.class);
		if (all.length == 0) throw new IllegalStateException("No A's");
		if (all.length == 1) return all[0].value();
		throw new IllegalStateException("Multiple A's");
	}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface A {

		public abstract String value();
	}
}