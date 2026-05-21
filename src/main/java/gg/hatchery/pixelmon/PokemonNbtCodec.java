package gg.hatchery.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Base64;

/**
 * (De)serializes a Pixelmon {@link Pokemon} to/from a base64 string for stable storage.
 */
public final class PokemonNbtCodec {

    private PokemonNbtCodec() {}

    public static String encode(Pokemon p) {
        try {
            CompoundTag tag = new CompoundTag();
            p.writeToNBT(tag);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                NbtIo.m_128941_(tag, out);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static Pokemon decode(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(s);
            CompoundTag tag;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                tag = NbtIo.m_128934_(in, NbtAccounter.m_301669_());
            }
            return PokemonFactory.create(tag);
        } catch (Exception e) {
            return null;
        }
    }

    /** Pair serializer: encodes both parents as a delimited string. */
    public static String encodePair(Pokemon a, Pokemon b) {
        return encode(a) + "|" + encode(b);
    }

    public static Pokemon[] decodePair(String pairStr) {
        if (pairStr == null || !pairStr.contains("|")) return new Pokemon[]{null, null};
        String[] parts = pairStr.split("\\|", 2);
        return new Pokemon[]{ decode(parts[0]), decode(parts[1]) };
    }
}
