package de.epiceric.shopchest.nms.v1_21_R2;

import de.epiceric.shopchest.nms.FakeArmorStand;
import de.epiceric.shopchest.nms.FakeItem;
import de.epiceric.shopchest.nms.Platform;

public class PlatformImpl implements Platform {

    @Override
    public FakeArmorStand createFakeArmorStand() {
        return new FakeArmorStandImpl();
    }

    @Override
    public FakeItem createFakeItem() {
        return new FakeItemImpl();
    }

}
