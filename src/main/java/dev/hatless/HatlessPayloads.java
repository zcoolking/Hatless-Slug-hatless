package dev.hatless;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public final class HatlessPayloads {

    private HatlessPayloads() {}

    /** Client -> Server: "these are my head settings". */
    public record Update(boolean hidden, float size, float width) implements CustomPayload {
        public static final CustomPayload.Id<Update> ID =
                new CustomPayload.Id<>(Identifier.of(HatlessMod.MOD_ID, "update"));
        public static final PacketCodec<ByteBuf, Update> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOLEAN, Update::hidden,
                PacketCodecs.FLOAT, Update::size,
                PacketCodecs.FLOAT, Update::width,
                Update::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Server -> Client: settings of another player (present=false means "player left / has no mod"). */
    public record Sync(UUID player, boolean present, boolean hidden, float size, float width) implements CustomPayload {
        public static final CustomPayload.Id<Sync> ID =
                new CustomPayload.Id<>(Identifier.of(HatlessMod.MOD_ID, "sync"));
        public static final PacketCodec<ByteBuf, Sync> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, Sync::player,
                PacketCodecs.BOOLEAN, Sync::present,
                PacketCodecs.BOOLEAN, Sync::hidden,
                PacketCodecs.FLOAT, Sync::size,
                PacketCodecs.FLOAT, Sync::width,
                Sync::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
