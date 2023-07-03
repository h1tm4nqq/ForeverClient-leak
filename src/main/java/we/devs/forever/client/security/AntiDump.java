package we.devs.forever.client.security;


import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import sun.management.VMManagement;
import sun.misc.Unsafe;
import we.devs.forever.client.security.hwid.HwidException;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

//@SuppressWarnings("all")
public class AntiDump {

    public static final Unsafe unsafe;
    private static Method findNative;
    private static ClassLoader classLoader;
    public static boolean isFuture = false;
    public static final List<String> danger = Arrays.asList(
//            "cat.yoink.dumper.Main",
//            "me.crystallinqq.dumper",
//            "tech.mmmax.dumper",
//            "fuck.you.multihryack"
    );

    private static final String[] naughtyFlags = {
            "-XBootclasspath",
            "-javaagent",
            "-Xdebug",
            "-agentlib",
            "-Xrunjdwp",
            "-Xnoagent",
            "-verbose",
            "-DproxySet",
            "-DproxyHost",
            "-DproxyPort",
            "-Djavax.net.ssl.trustStore",
            "-Djavax.net.ssl.trustStorePassword"
    };

    /* UnsafeProvider */
    static {
        Unsafe ref;
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = clazz.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            ref = (Unsafe) theUnsafe.get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            ref = null;
        }
        //Reflection.getCallerClass();
        unsafe = ref;
    }
    /* CookieFuckery */
    public static void check() {
        // if (!ENABLE) return;
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("resourceCache");
            field.setAccessible(true);
            Map<String, byte[]> mapa = (Map<String, byte[]>) field.get(Launch.classLoader);

            mapa.keySet().forEach(s -> {
                String[] strings = s.split("\\.");
                for (String s1 : strings) {
                    if (s1.toLowerCase().startsWith("dumper")) {
                        System.out.println(true);
                        we.devs.forever.loader.AntiDump.unsafe.putAddress(0, 0);
                        we.devs.forever.loader.AntiDump.unsafe.putAddress(1, 0);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
//        try {
//            Field field = LaunchClassLoader.class.getDeclaredField("invalidClasses");
//            field.setAccessible(true);
//            Set<String> set = (Set<String>) field.get(Launch.classLoader);
//            final Map<String, byte[]> resources = new HashMap<>(2000);
//            we.devs.forever.loader.AntiDump.danger.forEach(name -> {
//                try {
//                    set.add(name);
//                    resources.put(name, null);
//                } catch (Throwable ignored) {
//                }
//            });
//        } catch (Throwable ignored) {
//        }
        try {
            Field jvmField = ManagementFactory.getRuntimeMXBean().getClass().getDeclaredField("jvm");
            jvmField.setAccessible(true);
            VMManagement jvm = (VMManagement) jvmField.get(ManagementFactory.getRuntimeMXBean());
            List<String> inputArguments = jvm.getVmArguments();

            for (String arg : naughtyFlags) {
                for (String inputArgument : inputArguments) {
                    if (inputArgument.contains(arg)) {
                        System.out.println("Found illegal program arguments!");
                        dumpDetected();
                    }
                }
            }
            if (Launch.classLoader.findClass("net.futureclient.loader.launch.launchwrapper.LaunchWrapperEntryPoint") != null) {
                isFuture = true;
            }
            if (!isFuture) {
                try {
                    try {
                        byte[] bytes = createDummyClass("java/lang/instrument/Instrumentation");
                        unsafe.defineClass("java.lang.instrument.Instrumentation", bytes, 0, bytes.length, null, null);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        dumpDetected();
                    }
                    if (isClassLoaded("sun.instrument.InstrumentationImpl")) {
                        System.out.println("Found sun.instrument.InstrumentationImpl!");
                        dumpDetected();
                    }

                } catch (Throwable ignored) {
                }


                byte[] bytes = createDummyClass("dummy/class/path/MaliciousClassFilter");
                unsafe.defineClass("dummy.class.path.MaliciousClassFilter", bytes, 0, bytes.length, null, null); // Change this.
                System.setProperty("sun.jvm.hotspot.tools.jcore.filter", "dummy.class.path.MaliciousClassFilter"); // Change this.

                disassembleStruct();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            dumpDetected();
        }
    }


    public static byte[] dump(String path, String name) {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, path, null, "java/lang/Object", null);

        classWriter.visitSource(name, null);

        {
            annotationVisitor0 = classWriter.visitAnnotation("Lnet/minecraftforge/fml/common/Mod;", true);
            annotationVisitor0.visit("modid", "dumper");
            annotationVisitor0.visitEnd();
        }
        classWriter.visitInnerClass("net/minecraftforge/fml/common/Mod$EventHandler", "net/minecraftforge/fml/common/Mod", "EventHandler", ACC_PUBLIC | ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

        classWriter.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "logger", "Lorg/apache/logging/log4j/Logger;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(11, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(12, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitLdcInsn("Dumper");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "org/apache/logging/log4j/LogManager", "getLogger", "(Ljava/lang/String;)Lorg/apache/logging/log4j/Logger;", false);
            methodVisitor.visitFieldInsn(PUTFIELD, path, "logger", "Lorg/apache/logging/log4j/Logger;");
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lme/lel/Main;", null, label0, label2, 0);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "initialize", "(Lnet/minecraftforge/fml/common/event/FMLInitializationEvent;)V", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Lnet/minecraftforge/fml/common/Mod$EventHandler;", true);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(16, label0);
            methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitInvokeDynamicInsn("run", "(Lme/lel/Main;)Ljava/lang/Runnable;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("()V"), new Handle(Opcodes.H_INVOKESPECIAL, "me/lel/Main", "lambda$initialize$0", "()V", false), Type.getType("()V")});
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(19, label1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(20, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "L" + path + ";", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("event", "Lnet/minecraftforge/fml/common/event/FMLInitializationEvent;", null, label0, label3, 1);
            methodVisitor.visitMaxs(3, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, "lambda$initialize$0", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(18, label0);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, path, "logger", "Lorg/apache/logging/log4j/Logger;");
            methodVisitor.visitLdcInsn("Nice try");
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "error", "(Ljava/lang/String;)V", true);
            methodVisitor.visitJumpInsn(GOTO, label0);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + path + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private static boolean isClassLoaded(@SuppressWarnings("SameParameterValue") String clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        m.setAccessible(true);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        return m.invoke(cl, clazz) != null || m.invoke(scl, clazz) != null;
    }


    /* DummyClassProvider */
    public static byte[] createDummyClass(String name) {
        ClassNode classNode = new ClassNode();
        classNode.name = name.replace('.', '/');
        classNode.access = ACC_PUBLIC;
        classNode.version = V1_8;
        classNode.superName = "java/lang/Object";

        List<MethodNode> methods = new ArrayList<>();
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null);

        InsnList insn = new InsnList();
        insn.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        insn.add(new LdcInsnNode("Nice try"));
        insn.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        insn.add(new TypeInsnNode(NEW, "java/lang/Throwable"));
        insn.add(new InsnNode(DUP));
        insn.add(new LdcInsnNode("owned"));
        insn.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Throwable", "<init>", "(Ljava/lang/String;)V", false));
        insn.add(new InsnNode(ATHROW));

        methodNode.instructions = insn;

        methods.add(methodNode);
        classNode.methods = methods;

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static void dumpDetected() {
        try {
            unsafe.putAddress(0, 0);
        } catch (Exception e) {
        }
        unsafe.throwException(new HwidException("Nice try"));
        FMLCommonHandler.instance().exitJava(0, false); // Shutdown.

    }

    /* StructDissasembler */
    private static void resolveClassLoader() throws NoSuchMethodException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            String vmName = System.getProperty("java.vm.name");
            String dll = vmName.contains("Client VM") ? "/bin/client/jvm.dll" : "/bin/server/jvm.dll";
            try {
                System.load(System.getProperty("java.home") + dll);
            } catch (UnsatisfiedLinkError e) {
                throw new RuntimeException(e);
            }
            classLoader = AntiDump.class.getClassLoader();
        } else {
            classLoader = null;
        }

        findNative = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);

        try {
            Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            unsafe.putObjectVolatile(cls, unsafe.staticFieldOffset(logger), null);
        } catch (Throwable t) {
        }

        findNative.setAccessible(true);
    }

    private static void setupIntrospection() throws Throwable {
        resolveClassLoader();
    }

    public static void disassembleStruct() {
        try {
            setupIntrospection();
            long entry = getSymbol("gHotSpotVMStructs");
            unsafe.putLong(entry, 0);
        } catch (Throwable t) {
            t.printStackTrace();
            dumpDetected();
        }
    }

    private static long getSymbol(String symbol) throws InvocationTargetException, IllegalAccessException {
        long address = (Long) findNative.invoke(null, classLoader, symbol);
        if (address == 0)
            throw new NoSuchElementException(symbol);

        return unsafe.getLong(address);
    }

    private static String getString(long addr) {
        if (addr == 0) {
            return null;
        }

        char[] chars = new char[40];
        int offset = 0;
        for (byte b; (b = unsafe.getByte(addr + offset)) != 0; ) {
            if (offset >= chars.length) chars = Arrays.copyOf(chars, offset * 2);
            chars[offset++] = (char) b;
        }

        return new String(chars, 0, offset);
    }

    private static void readStructs(Map<String, Set<Object[]>> structs) throws InvocationTargetException, IllegalAccessException {
        long entry = getSymbol("gHotSpotVMStructs");
        long typeNameOffset = getSymbol("gHotSpotVMStructEntryTypeNameOffset");
        long fieldNameOffset = getSymbol("gHotSpotVMStructEntryFieldNameOffset");
        long typeStringOffset = getSymbol("gHotSpotVMStructEntryTypeStringOffset");
        long isStaticOffset = getSymbol("gHotSpotVMStructEntryIsStaticOffset");
        long offsetOffset = getSymbol("gHotSpotVMStructEntryOffsetOffset");
        long addressOffset = getSymbol("gHotSpotVMStructEntryAddressOffset");
        long arrayStride = getSymbol("gHotSpotVMStructEntryArrayStride");

        for (; ; entry += arrayStride) {
            String typeName = getString(unsafe.getLong(entry + typeNameOffset));
            String fieldName = getString(unsafe.getLong(entry + fieldNameOffset));
            if (fieldName == null) break;

            String typeString = getString(unsafe.getLong(entry + typeStringOffset));
            boolean isStatic = unsafe.getInt(entry + isStaticOffset) != 0;
            long offset = unsafe.getLong(entry + (isStatic ? addressOffset : offsetOffset));

            Set<Object[]> fields = structs.get(typeName);
            if (fields == null) structs.put(typeName, fields = new HashSet<>());
            fields.add(new Object[]{fieldName, typeString, offset, isStatic});
        }
        long address = (Long) findNative.invoke(null, classLoader, 2);
        if (address == 0)
            throw new NoSuchElementException("");

        unsafe.getLong(address);
    }

    private static void readTypes(Map<String, Object[]> types, Map<String, Set<Object[]>> structs) throws InvocationTargetException, IllegalAccessException {
        long entry = getSymbol("gHotSpotVMTypes");
        long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
        long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
        long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
        long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
        long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
        long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
        long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");

        for (; ; entry += arrayStride) {
            String typeName = getString(unsafe.getLong(entry + typeNameOffset));
            if (typeName == null) break;

            String superclassName = getString(unsafe.getLong(entry + superclassNameOffset));
            boolean isOop = unsafe.getInt(entry + isOopTypeOffset) != 0;
            boolean isInt = unsafe.getInt(entry + isIntegerTypeOffset) != 0;
            boolean isUnsigned = unsafe.getInt(entry + isUnsignedOffset) != 0;
            int size = unsafe.getInt(entry + sizeOffset);

            Set<Object[]> fields = structs.get(typeName);
            types.put(typeName, new Object[]{typeName, superclassName, size, isOop, isInt, isUnsigned, fields});
        }
    }
}
