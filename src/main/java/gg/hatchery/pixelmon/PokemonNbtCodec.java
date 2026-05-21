package gg.hatchery.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * (De)serializes a Pixelmon {@link Pokemon} to/from a base64 string for stable storage.
 */
public final class PokemonNbtCodec {

    private PokemonNbtCodec() {}

    public static String encode(Pokemon p) {
        if (p == null) return "";
        try {
            CompoundTag tag = new CompoundTag();
            writePokemonNbt(p, tag);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                writeTag(tag, out);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Throwable e) {
            return null;
        }
    }

    public static Pokemon decode(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(s);
            CompoundTag tag;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                tag = readTag(in);
            }
            return createPokemon(tag);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Pair serializer: encodes both parents as a delimited string. */
    public static String encodePair(Pokemon a, Pokemon b) {
        String left = encode(a);
        String right = encode(b);
        if (left == null || right == null) return null;
        return left + "|" + right;
    }

    public static Pokemon[] decodePair(String pairStr) {
        if (pairStr == null || !pairStr.contains("|")) return new Pokemon[]{null, null};
        String[] parts = pairStr.split("\\|", 2);
        return new Pokemon[]{ decode(parts[0]), decode(parts[1]) };
    }

    private static void writePokemonNbt(Pokemon pokemon, CompoundTag tag) throws ReflectiveOperationException {
        try {
            pokemon.getClass().getMethod("writeToNBT", CompoundTag.class).invoke(pokemon, tag);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        Method method = pokemon.getClass().getMethod("writeToNBT", CompoundTag.class, providerClass());
        method.invoke(pokemon, tag, registryProvider());
    }

    private static Pokemon createPokemon(CompoundTag tag) throws ReflectiveOperationException {
        try {
            return (Pokemon) PokemonFactory.class.getMethod("create", CompoundTag.class).invoke(null, tag);
        } catch (NoSuchMethodException ignored) {
        }
        Method method = PokemonFactory.class.getMethod("create", CompoundTag.class, providerClass());
        return (Pokemon) method.invoke(null, tag, registryProvider());
    }

    private static Class<?> providerClass() throws ClassNotFoundException {
        return Class.forName("net.minecraft.core.HolderLookup$Provider");
    }

    private static void writeTag(CompoundTag tag, DataOutput out) throws ReflectiveOperationException {
        try {
            NbtIo.class.getMethod("write", CompoundTag.class, DataOutput.class).invoke(null, tag, out);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        NbtIo.class.getMethod("m_128941_", CompoundTag.class, DataOutput.class).invoke(null, tag, out);
    }

    private static CompoundTag readTag(DataInput in) throws ReflectiveOperationException {
        try {
            return (CompoundTag) NbtIo.class.getMethod("read", DataInput.class).invoke(null, in);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            Method method = NbtIo.class.getMethod("read", DataInput.class, NbtAccounter.class);
            return (CompoundTag) method.invoke(null, in, nbtAccounter());
        } catch (NoSuchMethodException ignored) {
        }
        Method method = NbtIo.class.getMethod("m_128934_", DataInput.class, NbtAccounter.class);
        return (CompoundTag) method.invoke(null, in, nbtAccounter());
    }

    private static Object nbtAccounter() throws ReflectiveOperationException {
        for (String method : new String[]{"unlimitedHeap", "m_301669_"}) {
            try {
                return NbtAccounter.class.getMethod(method).invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("No NbtAccounter factory found");
    }

    private static Object registryProvider() throws ReflectiveOperationException {
        Object craftServer = Bukkit.getServer();
        Object minecraftServer = craftServer.getClass().getMethod("getServer").invoke(craftServer);
        try {
            return minecraftServer.getClass().getMethod("registryAccess").invoke(minecraftServer);
        } catch (NoSuchMethodException ignored) {
            return minecraftServer.getClass().getMethod("m_206579_").invoke(minecraftServer);
        }
    }
}
