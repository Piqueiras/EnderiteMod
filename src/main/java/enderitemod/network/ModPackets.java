package enderitemod.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                DoubleJumpPayload.TYPE,
                DoubleJumpPayload.STREAM_CODEC,
                DoubleJumpPayload::handle
        );
    }
}
