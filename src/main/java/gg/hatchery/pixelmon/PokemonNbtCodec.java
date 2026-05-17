package gg.hatchery.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * (De)serializes a Pixelmon {@link Pokemon} to/from a base64 string for stable storage.
 *
 * NBT calls are routed through reflection so this class can compile without a
 * Mojang-mapped Minecraft jar on the classpath. At runtime Arclight provides the
 * Mojang-mapped NMS classes that Pixelmon was compiled against.
 */
public final class PokemonNbtCodec {

    private static final Class<?> COMPOUND_TAG_CLASS;
    private static final Class<?> NBT_IO_CLASS;
    private static final Class<?> NBT_ACCOUNTER_CLASS;
    private static final Method  WRITE_METHOD;        // NbtIo.write(CompoundTag, DataOutput)
    private static final Method  READ_METHOD;         // NbtIo.read(DataInput, NbtAccounter)
    private static final Method  UNLIMITED_HEAP;      // NbtAccounter.unlimitedHeap()
    private static final Method  POKEMON_WRITE_NBT;   // Pokemon.writeToNBT(CompoundTag)
    private static final Method  POKEMON_FACTORY_CREATE; // PokemonFactory.create(CompoundTag)

    static {
        Class<?> compoundTagClass = null;
        Class<?> nbtIoClass = null;
        Class<?> nbtAccounterClass = null;
        Method writeMethod = null;
        Method readMethod = null;
        Method unlimitedHeap = null;
        Method pokemonWriteNbt = null;
        Method pokemonFactoryCreate = null;
        try {
            compoundTagClass    = Class.forName("net.minecraft.nbt.CompoundTag");
            nbtIoClass          = Class.forName("net.minecraft.nbt.NbtIo");
            nbtAccounterClass   = Class.forName("net.minecraft.nbt.NbtAccounter");

            writeMethod         = nbtIoClass.getMethod("write", compoundTagClass, java.io.DataOutput.class);
            readMethod          = nbtIoClass.getMethod("read",  java.io.DataInput.class,  nbtAccounterClass);
            try {
                unlimitedHeap   = nbtAccounterClass.getMethod("unlimitedHeap");
            } catch (NoSuchMethodException ignored) {
                // older 1.20.2 builds — fallback constructor / static field
                try {
                    java.lang.reflect.Field f = nbtAccounterClass.getField("UNLIMITED");
                    Object inst = f.get(null);
                    // wrap into a 0-arg static method shim using lambdas not possible here; we'll
                    // set unlimitedHeap to null and handle in read().
                } catch (Throwable ignored2) {}
            }
            pokemonWriteNbt        = Pokemon.class.getMethod("writeToNBT", compoundTagClass);
            pokemonFactoryCreate   = PokemonFactory.class.getMethod("create", compoundTagClass);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        COMPOUND_TAG_CLASS = compoundTagClass;
        NBT_IO_CLASS = nbtIoClass;
        NBT_ACCOUNTER_CLASS = nbtAccounterClass;
        WRITE_METHOD = writeMethod;
        READ_METHOD = readMethod;
        UNLIMITED_HEAP = unlimitedHeap;
        POKEMON_WRITE_NBT = pokemonWriteNbt;
        POKEMON_FACTORY_CREATE = pokemonFactoryCreate;
    }

    private PokemonNbtCodec() {}

    public static String encode(Pokemon p) {
        if (p == null) return null;
        try {
            Object tag = COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
            POKEMON_WRITE_NBT.invoke(p, tag);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                WRITE_METHOD.invoke(null, tag, out);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Pokemon decode(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(s);
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                Object accounter = UNLIMITED_HEAP != null
                        ? UNLIMITED_HEAP.invoke(null)
                        : NBT_ACCOUNTER_CLASS.getField("UNLIMITED").get(null);
                Object tag = READ_METHOD.invoke(null, in, accounter);
                return (Pokemon) POKEMON_FACTORY_CREATE.invoke(null, tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Pair serializer: encodes both parents as a delimited string. */
    public static String encodePair(Pokemon a, Pokemon b) {
        return (a == null ? "" : encode(a)) + "|" + (b == null ? "" : encode(b));
    }

    public static Pokemon[] decodePair(String pairStr) {
        if (pairStr == null || !pairStr.contains("|")) return new Pokemon[]{null, null};
        String[] parts = pairStr.split("\\|", 2);
        return new Pokemon[]{
                parts[0].isEmpty() ? null : decode(parts[0]),
                parts.length > 1 && !parts[1].isEmpty() ? decode(parts[1]) : null
        };
    }
}
