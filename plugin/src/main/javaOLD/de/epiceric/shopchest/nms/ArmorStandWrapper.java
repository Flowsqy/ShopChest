package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class ArmorStandWrapper {

    private final UUID uuid = UUID.randomUUID();
    private final Platform platform;
    private final FakeArmorStand fakeArmorStand;

    private Location location;
    private String customName;

    public ArmorStandWrapper(ShopChest plugin, Location location, String customName) {
        this.location = location;
        this.customName = customName;
        this.platform = plugin.getPlatform();
        this.fakeArmorStand = platform.createFakeArmorStand();
    }

    public void setVisible(Player player, boolean visible) {
        if(visible){
            final PacketQueue packetQueue = platform.createPacketQueue();
            fakeArmorStand.create(packetQueue, uuid, location);
            final MetadataProperties mdp = platform.getMetadataProperties();
            final MetadataProperties.Entity entityMdp = mdp.entity();
            fakeArmorStand.metadata(packetQueue,
                    entityMdp.noGravity().set(true),
                    entityMdp.silent().set(true),
                    entityMdp.invisible().set(true),
                    // TODO Handle custom name properly
                    entityMdp.customName().set(new NMSComponent()),
                    entityMdp.customNameVisible().set(true),
                    mdp.armorStand().marker().set(true)
            );
            packetQueue.send(Collections.singletonList(player));
        }
        else if(fakeArmorStand.getEntityId() != -1){
            final PacketQueue packetQueue = platform.createPacketQueue();
            fakeArmorStand.remove(packetQueue);
            packetQueue.send(Collections.singletonList(player));
        }
    }

    public void setLocation(Location location) {
        this.location = location;
        final PacketQueue packetQueue = platform.createPacketQueue();
        fakeArmorStand.teleport(packetQueue, location.toVector());
        packetQueue.send(Objects.requireNonNull(location.getWorld()).getPlayers());
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        final PacketQueue packetQueue = platform.createPacketQueue();
        // TODO Handle custom name properly
        fakeArmorStand.metadata(packetQueue, platform.getMetadataProperties().entity().customName().set(new NMSComponent()));
        packetQueue.send(Objects.requireNonNull(location.getWorld()).getPlayers());
    }

    public void remove() {
        for (Player player : Objects.requireNonNull(location.getWorld()).getPlayers()) {
            setVisible(player, false);
        }
    }

    public int getEntityId() {
        return fakeArmorStand.getEntityId();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getCustomName() {
        return customName;
    }
}
