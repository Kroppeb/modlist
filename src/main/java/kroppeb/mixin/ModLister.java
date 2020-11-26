package kroppeb.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ModLister {
	@Shadow
	@Final
	private ClientConnection connection;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ClientBrandRetriever;getClientModName()Ljava/lang/String;"), method = "onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	private void getClientModName(GameJoinS2CPacket packet, CallbackInfo ci) {
		Collection<ModContainer> allMods = FabricLoader.getInstance().getAllMods();
		PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
		// protocol version
		packetByteBuf.writeVarInt(0);
		packetByteBuf.writeVarInt(allMods.size());
		for (ModContainer allMod : allMods) {
			ModMetadata metadata = allMod.getMetadata();
			packetByteBuf.writeString(metadata.getId());
			packetByteBuf.writeString(metadata.getVersion().getFriendlyString());
		}
		
		connection.send(new CustomPayloadC2SPacket(
				new Identifier("fabric-modlister", "modlist"),
				packetByteBuf
		));
	}
}