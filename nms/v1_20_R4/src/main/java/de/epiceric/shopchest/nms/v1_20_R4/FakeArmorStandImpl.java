package de.epiceric.shopchest.nms.v1_20_R4;

import de.epiceric.shopchest.nms.FakeArmorStand;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class FakeArmorStandImpl extends FakeEntityImpl<String> implements FakeArmorStand {

    private final static byte INVISIBLE_FLAG = 0b100000;
    private final static byte MARKER_FLAG = 0b10000;
    private final static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private final static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private final static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    private final static float MARKER_ARMOR_STAND_OFFSET = 1.975f;

    static {
        try {
            final Field dataSharedFlagsId = Entity.class.getDeclaredField("ao"); // DATA_SHARED_FLAGS_ID
            dataSharedFlagsId.setAccessible(true);
            DATA_SHARED_FLAGS_ID = forceCast(dataSharedFlagsId.get(null));
            final Field dataCustomNameField = Entity.class.getDeclaredField("aU"); // DATA_CUSTOM_NAME
            dataCustomNameField.setAccessible(true);
            DATA_CUSTOM_NAME = forceCast(dataCustomNameField.get(null));
            final Field dataCustomNameVisibleField = Entity.class.getDeclaredField("aV"); // DATA_CUSTOM_NAME_VISIBLE
            dataCustomNameVisibleField.setAccessible(true);
            DATA_CUSTOM_NAME_VISIBLE = forceCast(dataCustomNameVisibleField.get(null));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public FakeArmorStandImpl() {
        super();
    }

    @Override
    public void sendData(String name, Iterable<Player> receivers) {
        sendData(receivers, name);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    protected float getSpawnOffSet() {
        return MARKER_ARMOR_STAND_OFFSET;
    }

    @Override
    protected int getDataItemCount() {
        return 4;
    }
    @Override
    protected void addSpecificData(List<SynchedEntityData.DataValue<?>> packedItems, String name) {
        packedItems.add(SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, INVISIBLE_FLAG));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME, Optional.ofNullable(
                Component.Serializer.fromJson(
                        ComponentSerializer.toString(
                                TextComponent.fromLegacyText(name)
                        )
                )
        )));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME_VISIBLE, true));
        packedItems.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, MARKER_FLAG));
    }

    @Override
    public void setLocation(Location location, Iterable<Player> receivers) {
        ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        ArmorStand armorStand = (ArmorStand) getEntityType().create(world);

        // Set its position
        armorStand.setPos(location.getX(), location.getY() + MARKER_ARMOR_STAND_OFFSET, location.getZ());

        // TODO
        // Add any other necessary properties, e.g., custom name


        // Spawn the entity in the world
        world.addFreshEntity(armorStand);

        // Send the teleport packet to receivers
        ClientboundTeleportEntityPacket positionPacket = new ClientboundTeleportEntityPacket(armorStand);
        sendPacket(positionPacket, receivers);
    }
}