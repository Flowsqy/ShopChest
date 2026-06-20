package de.epiceric.shopchest.nms.paper.v26_2;

import de.epiceric.shopchest.nms.FakeEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class FakeEntityImpl<T> implements FakeEntity {

    private final static EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private final static EntityDataAccessor<Boolean> DATA_SILENT;

    static {
        try {
            final Field dataNoGravityField = Entity.class.getDeclaredField(ObfuscatedFieldNames.DATA_NO_GRAVITY);
            dataNoGravityField.setAccessible(true);
            DATA_NO_GRAVITY = forceCast(dataNoGravityField.get(null));
            final Field dataSilentField = Entity.class.getDeclaredField(ObfuscatedFieldNames.DATA_SILENT);
            dataSilentField.setAccessible(true);
            DATA_SILENT = forceCast(dataSilentField.get(null));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected int entityId;
    private UUID worldId;

    public FakeEntityImpl() {
        entityId = 0;
    }

    @SuppressWarnings("unchecked")
    protected static <T> T forceCast(Object o) {
        return (T) o;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    protected void sendPacket(Packet<?> packet, Iterable<Player> receivers) {
        for (Player receiver : receivers) {
            ((CraftPlayer) receiver).getHandle().connection.send(packet);
        }
    }

    @Override
    public void spawn(UUID uuid, Location location, Iterable<Player> receivers) {
        final World world = Objects.requireNonNull(location.getWorld());
        if (entityId == 0 || !world.getUID().equals(worldId)) {
            worldId = world.getUID();
            entityId = ((CraftWorld) world).getHandle().getNextEntityId();
        }
        final ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                entityId,
                uuid,
                location.getX(),
                location.getY() + getSpawnOffSet(),
                location.getZ(),
                0f,
                0f,
                getEntityType(),
                0,
                Vec3.ZERO,
                0d);
        sendPacket(spawnPacket, receivers);
    }

    @Override
    public void remove(Iterable<Player> receivers) {
        final ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(entityId);
        sendPacket(removePacket, receivers);
    }

    protected void sendData(Iterable<Player> receivers, T data) {
        // Create packet
        final List<SynchedEntityData.DataValue<?>> packedItems = new LinkedList<>();
        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityId, packedItems);

        // Setup data
        packedItems.add(SynchedEntityData.DataValue.create(DATA_NO_GRAVITY, true));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_SILENT, true));
        addSpecificData(packedItems, data);

        // Send packet
        sendPacket(dataPacket, receivers);
    }

    protected abstract EntityType<?> getEntityType();

    protected float getSpawnOffSet() {
        return 0f;
    }

    protected abstract int getDataItemCount();

    protected abstract void addSpecificData(List<SynchedEntityData.DataValue<?>> packedItems, T data);

}
