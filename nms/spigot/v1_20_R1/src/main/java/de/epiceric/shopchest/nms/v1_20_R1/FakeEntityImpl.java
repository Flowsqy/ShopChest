package de.epiceric.shopchest.nms.v1_20_R1;

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
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FakeEntityImpl<T> implements FakeEntity {

    private final static AtomicInteger ENTITY_COUNTER;
    private final static EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private final static EntityDataAccessor<Boolean> DATA_SILENT;

    static {
        try {
            final Field entityCounterField = Entity.class.getDeclaredField("d"); // ENTITY_COUNTER
            entityCounterField.setAccessible(true);
            ENTITY_COUNTER = (AtomicInteger) entityCounterField.get(null);
            final Field dataNoGravityField = Entity.class.getDeclaredField("aX"); // DATA_NO_GRAVITY
            dataNoGravityField.setAccessible(true);
            DATA_NO_GRAVITY = forceCast(dataNoGravityField.get(null));
            final Field dataSilentField = Entity.class.getDeclaredField("aW"); // DATA_SILENT
            dataSilentField.setAccessible(true);
            DATA_SILENT = forceCast(dataSilentField.get(null));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected final int entityId;

    public FakeEntityImpl() {
        entityId = ENTITY_COUNTER.incrementAndGet();
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
                0d
        );
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
