package builderb0y.autocodec.reflection;

import org.junit.Test;

import builderb0y.autocodec.annotations.UseGetter;
import builderb0y.autocodec.annotations.UseSetter;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.manipulators.InstanceReaderWriter;
import builderb0y.autocodec.reflection.manipulators.InstanceWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;

import static org.junit.Assert.*;

public class ReflectionManagerTest {

	@SuppressWarnings("unchecked")
	public static <T_Owner, T_Member> FieldLikeMemberView<T_Owner, T_Member> field(Class<T_Owner> owner, String name) {
		return (FieldLikeMemberView<T_Owner, T_Member>)(
			TestCommon.DEFAULT_CODEC.reflect(owner).searchFields(
				false,
				new FieldPredicate().name(name),
				MemberCollector.forceUnique()
			)
		);
	}

	@SuppressWarnings("unchecked")
	public static <T_Owner, T_Member> MethodLikeMemberView<T_Owner, T_Member> method(Class<T_Owner> owner, String name) {
		return (MethodLikeMemberView<T_Owner, T_Member>)(
			TestCommon.DEFAULT_CODEC.reflect(owner).searchMethods(
				false,
				new MethodPredicate().name(name),
				MemberCollector.forceUnique()
			)
		);
	}

	public static <T_Owner, T_Member> InstanceReader<T_Owner, T_Member> reader(Class<T_Owner> owner, String name) throws IllegalAccessException {
		return ReflectionManagerTest.<T_Owner, T_Member>field(owner, name).createInstanceReader(TestCommon.DEFAULT_CODEC);
	}

	public static <T_Owner, T_Member> InstanceWriter<T_Owner, T_Member> writer(Class<T_Owner> owner, String name) throws IllegalAccessException {
		return ReflectionManagerTest.<T_Owner, T_Member>field(owner, name).createInstanceWriter(TestCommon.DEFAULT_CODEC);
	}

	public static <T_Owner, T_Member> InstanceReaderWriter<T_Owner, T_Member> readerWriter(Class<T_Owner> owner, String name) throws IllegalAccessException {
		return ReflectionManagerTest.<T_Owner, T_Member>field(owner, name).createInstanceReaderWriter(TestCommon.DEFAULT_CODEC);
	}

	@Test
	public void testRecordReader() throws IllegalAccessException {
		RecordPoint point = new RecordPoint(3, 4);

		InstanceReader<RecordPoint, Integer>
			xReader = reader(RecordPoint.class, "x"),
			yReader = reader(RecordPoint.class, "y");

		assertEquals(3,  xReader.get(point).intValue());
		assertEquals(99, yReader.get(point).intValue());
	}

	public static record RecordPoint(int x, @UseGetter("fakeY") int y) {

		public int fakeY() {
			return 99;
		}
	}

	@Test
	public void testClassReader() throws IllegalAccessException {
		ClassPoint point = new ClassPoint(3, 4);

		InstanceReader<ClassPoint, Integer>
			xReader = reader(ClassPoint.class, "x"),
			yReader = reader(ClassPoint.class, "y");

		assertEquals(3,  xReader.get(point).intValue());
		assertEquals(99, yReader.get(point).intValue());
	}

	public static class ClassPoint {

		public final int x;
		@UseGetter("fakeY")
		public final int y;

		public ClassPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int fakeY() {
			return 99;
		}
	}

	@Test
	public void testWriters() throws IllegalAccessException {
		MutablePoint point = new MutablePoint();

		InstanceWriter<MutablePoint, Integer>
			standard = writer(MutablePoint.class, "standard"),
			getter   = writer(MutablePoint.class, "getter"),
			setter   = writer(MutablePoint.class, "setter"),
			both     = writer(MutablePoint.class, "both");

		standard.set(point, 101);
		getter  .set(point, 102);
		setter  .set(point, 103);
		both    .set(point, 104);

		assertEquals(101, point.standard);
		assertEquals(102, point.getter);
		assertEquals(3,   point.setter);
		assertEquals(4,   point.both);
	}

	@Test
	public void testReaderWriters() throws IllegalAccessException {
		MutablePoint point = new MutablePoint();

		InstanceReaderWriter<MutablePoint, Integer>
			standard = readerWriter(MutablePoint.class, "standard"),
			getter   = readerWriter(MutablePoint.class, "getter"),
			setter   = readerWriter(MutablePoint.class, "setter"),
			both     = readerWriter(MutablePoint.class, "both");

		assertEquals(1,  standard.get(point).intValue()); //should get direct
		assertEquals(99, getter  .get(point).intValue()); //should get fake
		assertEquals(3,  setter  .get(point).intValue()); //should get direct
		assertEquals(99, both    .get(point).intValue()); //should get fake

		standard.set(point, 101); //should work
		getter  .set(point, 102); //should work
		setter  .set(point, 103); //should do nothing
		both    .set(point, 104); //should do nothing

		assertEquals(101, point.standard);
		assertEquals(102, point.getter);
		assertEquals(3,   point.setter);
		assertEquals(4,   point.both);

		assertEquals(101, standard.get(point).intValue()); //should get direct
		assertEquals(99,  getter  .get(point).intValue()); //should get fake
		assertEquals(3,   setter  .get(point).intValue()); //should get direct
		assertEquals(99,  both    .get(point).intValue()); //should get fake
	}

	public static class MutablePoint {

		public int standard = 1;
		@UseGetter("specialGetter")
		public int getter = 2;
		@UseSetter("specialSetter")
		public int setter = 3;
		@UseGetter("specialGetter")
		@UseSetter("specialSetter")
		public int both = 4;

		public int specialGetter() {
			return 99;
		}

		public void specialSetter(int ignored) {}
	}

	@Test
	public void testVisibility() {
		assertNotNull(field(Visibility.class, "staticField"    ));
		try { field(Visibility.class, "transientField" ); fail(); } catch (ReflectException expected) {}
		try { field(Visibility.class, "staticTransient"); fail(); } catch (ReflectException expected) {}
		assertNotNull(field(Visibility.class, "regularField"   ));
		assertNotNull(method(Visibility.class, "staticMethod"  ));
		assertNotNull(method(Visibility.class, "instanceMethod"));
	}

	public static class Visibility {

		public static int staticField;
		public transient int transientField;
		public static transient int staticTransient;
		public int regularField;

		public static void staticMethod() {}
		public void instanceMethod() {}
	}
}