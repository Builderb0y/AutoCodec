# Introduction

AutoCodec is a high-level abstraction layer over [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) (DFU). Specifically, the [serialization](https://github.com/Mojang/DataFixerUpper/tree/master/src/main/java/com/mojang/serialization) side of DFU. This library does not touch the [datafixers](https://github.com/Mojang/DataFixerUpper/tree/master/src/main/java/com/mojang/datafixers) side of DFU. If you've ever used [GSON](https://github.com/google/gson), you may be familiar with the concept of giving GSON an arbitrary java Type, and having GSON figure out how to serialize and deserialize that Type for you. AutoCodec aims to be like this.

By default, AutoCodec will handle a variety of types automatically, including:
* Primitive values
* BigInteger and BigDecimal
* Enums
* Arrays
* Collections
* Maps
  * Including EnumMaps
* Optionals
  * Including OptionalInt/Long/Double
* Dates, times, and other classes from the `java.time` package
* Records
* Record-like classes (classes with one constructor and several fields which are populated by that constructor)
* Generic type parameters (`List<String>`)
* Self-referencing types. For example:
```java
public class Node {
	Node parent;
	Node[] children;
}
```

# Getting started

First, you will want to add `-parameters` to your javac arguments. This will allow reflection to retrieve the names of method and constructor parameters. AutoCodec uses this information to determine which parameters are used to initialize which fields, which is important for record-like classes. If you are using Gradle, add the following to your build script:
```groovy
compileJava() {
	options.compilerArgs.add('-parameters')
}
```

Ok, got that? Good. Now onto the fun part: creating an AutoCodec instance. This part is fairly straightforward:
```java
public static final AutoCodec AUTO_CODEC = new AutoCodec();
```
Once you have that, you can now request a Codec from your AUTO_CODEC:
```java
Codec<MyClass> codec = AUTO_CODEC.createDFUCodec(MyClass.class);
```
Or, if your class is generic or needs to be annotated:
```java
Codec<List<String>> codec = AUTO_CODEC.createDFUCodec(new ReifiedType<@SingletonArray List<@VerifyIntRange(min = 0, max = 100) Integer>>() {});
```
Either way, the returned Codec will be capable of encoding and decoding instances of the requested class. Codecs are also cached, so you can request a Codec for the same Class or ReifiedType many times without additional overhead.

## Compare and contrast how Codecs are normally created:

I don't know how Codecs are normally created.

# Customization

Further customization can be achieved by use of [annotations](https://github.com/Builderb0y/AutoCodec/tree/master/src/main/java/builderb0y/autocodec/annotations), or by overriding methods in the AutoCodec object you create, typically as an anonymous subclass. For example:
```java
public static final AutoCodec AUTO_CODEC = new AutoCodec() {

	@Override
	public @NotNull DecoderFactoryList createDecoders() {
		return new DecoderFactoryList() {

			@Override
			public void setup() {
				super.setup();
				this.addFactoryAfter(LookupDecoderFactory.class, new MySpecialDecoderFactory());
				this.addFactoryBefore(RecordDecoder.Factory.INSTANCE, new MyOtherDecoderFactory());
			}
		}
	}
};
```

# How it works under the hood

When customizing AutoCodec to fit the types you need to (de)serialize, it helps to know how AutoCodec is internally structured. AutoCodec divides the work of encoding and decoding into 5 tasks:
* Encoding is the process of taking some data and converting it to a java object in some way.
* Decoding is the process of taking a java object and converting it to data in some way.
* Constructing is the process of creating a java object without any data.
* Imprinting is the process of taking a java object and some data, and applying that data to that object in some way.
* Verifying is the process of ensuring that a java object meets some criteria. For example, ensuring that it is not null.

Decoding is sometimes broken down into constructing and imprinting, and decoding almost always includes a verification step at the end. Encoding is not broken down into smaller tasks.

![A flowchart is worth a thousand words](https://github.com/Builderb0y/AutoCodec/blob/master/Handler%20chain.png)

There are a few classes associated with each task:
* Handlers (AutoEncoder, AutoImprinter, etc.) perform the task itself.
* Factories (EncoderFactory, ImprinterFactory, etc.) are responsible for creating Handlers for specific types, or possibly a set of related types.
* FactoryLists (EncoderFactoryList, ImprinterFactoryList, etc.) are what they say in the name: a list of Factories. They can create Handlers for a much wider range of types.
* AutoCodec contains a FactoryList for each task.

At the very end, an AutoEncoder and AutoDecoder are typically either wrapped to produce an Encoder and Decoder, which are combined to form a Codec, OR the AutoEncoder and AutoDecoder are combined into an AutoCoder, which is then wrapped to form a Codec. Regardless of whether wrapping or combining happens first, the result is the same: a Codec which can encode and decode instances of the requested type.

It is also worth mentioning that none of the above handlers rely on DataResult. When something goes wrong, a checked exception is thrown. The lack of reliance on DataResult also means that AutoCodec is more efficient at encoding and decoding than regular DFU is, due to the highly reduced number of lambda expressions involved.

# Logging

Logging is deeply intertwined with every action that AutoCodec performs, including encoding and decoding, but also the creation of handlers in the first place. This greatly aids in debugging when things go wrong.

There are a few TaskLogger implementations to choose from, and they all log different amounts of information. At the time of writing this, there are 4 loggers currently implemented: DisabledTaskLogger, BasicTaskLogger, IndentedTaskLogger, and StackContextLogger. All of these can be found in the [logging](https://github.com/Builderb0y/AutoCodec/tree/master/src/main/java/builderb0y/autocodec/logging) package. Documentation on which logger logs what can be found in the javadocs of these classes. You can choose one to use by overriding `AutoCodec.create<x>Logger()` in your AUTO_CODEC instance.

The default logger, StackContextLogger, will only log when something goes wrong, and will provide a task trace for what was happening at the time. A task trace is similar to a stack trace, but includes more useful information like what object was being encoded or what data was being decoded, along with what Handler was performing that task.

Logging can interact with any existing logger platform, including Log4j, Slf4j, writing to a file, or just printing to System.out. It does this via the Printer interface, which delegates a logging call to another logger. The above TaskLogger implementations only handle formatting of output, not actually printing it.

One other nice thing about loggers is that they only perform String concatenation when they're sure the result will actually be printed somewhere, which means there is almost no overhead from logging when there is nothing to log.

# A word on Minecraft

Yes, this library was designed to interact with Minecraft. However, Minecraft's obfuscation and unstable code base makes this impractical, even if I were to make it a mod for Minecraft. So instead, this library simply provides the boilerplate work, and it is up to the user to make it interact with Minecraft. For an example on how to do this, check out what Big Globe does with it.