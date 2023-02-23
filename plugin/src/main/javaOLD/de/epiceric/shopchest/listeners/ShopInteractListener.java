package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.GlobalConfig;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopInfoEvent;
import de.epiceric.shopchest.event.ShopOpenEvent;
import de.epiceric.shopchest.event.ShopRemoveEvent;
//import de.epiceric.shopchest.external.PlotSquaredOldShopFlag;
//import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.CreateClickType;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
//import fr.xephi.authme.api.v3.AuthMeApi;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
//import org.codemc.worldguardwrapper.WorldGuardWrapper;
//import org.codemc.worldguardwrapper.flag.IWrappedFlag;
//import org.codemc.worldguardwrapper.flag.WrappedState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ShopInteractListener implements Listener {

    // TODO Fix Admin shop selling (Idk why but it's broken)

    private ShopChest plugin;
    private Economy econ;
    private Database database;
    private ShopUtils shopUtils;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
        this.econ = plugin.getEconomy();
        this.database = plugin.getShopDatabase();
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.getHologramFormat().isDynamic()) return;

        Inventory chestInv = e.getInventory();

        if (!(chestInv.getHolder() instanceof Chest || chestInv.getHolder() instanceof ShulkerBox || chestInv.getHolder() instanceof Barrel || chestInv.getHolder() instanceof DoubleChest)) {
            return;
        }

        Location loc = null;
        if (chestInv.getHolder() instanceof Chest || chestInv.getHolder() instanceof ShulkerBox || chestInv.getHolder() instanceof Barrel) {
            loc = ((BlockState) chestInv.getHolder()).getLocation();
        } else if (chestInv.getHolder() instanceof DoubleChest) {
            loc = ((DoubleChest) chestInv.getHolder()).getLocation();
        }

        if (loc == null) return;
        final Shop shop = plugin.getShopUtils().getShop(loc);
        if (shop == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                shop.updateHologramText();
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractCreate(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!(ClickType.getPlayerClickType(p) instanceof CreateClickType))
            return;

        if (!ShopUtils.isShopMaterial(b.getType()))
            return;

        if (ClickType.getPlayerClickType(p).getClickType() != ClickType.EnumClickType.CREATE)
            return;

        if(!plugin.getHookManager().canInteract(p)){
            return;
        }

        if (e.useInteractedBlock() == Event.Result.DENY && !p.hasPermission(Permissions.CREATE_PROTECTED)) {
            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE_PROTECTED));
            plugin.getDebugLogger().debug(p.getName() + " is not allowed to create a shop on the selected chest");
        } else if (shopUtils.isShop(b.getLocation())) {
            p.sendMessage(LanguageUtils.getMessage(Message.CHEST_ALREADY_SHOP));
            plugin.getDebugLogger().debug("Chest is already a shop");
        } else if (!ItemUtils.isAir(b.getRelative(BlockFace.UP).getType())) {
            p.sendMessage(LanguageUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.getDebugLogger().debug("Chest is blocked");
        } else {
            CreateClickType clickType = (CreateClickType) ClickType.getPlayerClickType(p);
            ShopProduct product = clickType.getProduct();
            double buyPrice = clickType.getBuyPrice();
            double sellPrice = clickType.getSellPrice();
            ShopType shopType = clickType.getShopType();
    
            create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
        }

        e.setCancelled(true);
        ClickType.removePlayerClickType(p);
    }

    private Map<UUID, Set<Integer>> needsConfirmation = new HashMap<>();

    private void handleInteractEvent(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        boolean inverted = GlobalConfig.invertMouseButtons;

        if (Utils.getMajorVersion() >= 9 && e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        if (b == null || !ShopUtils.isShopMaterial(b.getType()))
            return;
        
        ClickType clickType = ClickType.getPlayerClickType(p);
        if (clickType != null) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            Shop shop = shopUtils.getShop(b.getLocation());
            switch (clickType.getClickType()) {
                case CREATE:
                case SELECT_ITEM:
                    break;
                default: 
                    if (shop == null) {
                        p.sendMessage(LanguageUtils.getMessage(Message.CHEST_NO_SHOP));
                        plugin.getDebugLogger().debug("Chest is not a shop");
                        return;
                    }
            }

            switch (clickType.getClickType()) {
                case INFO:
                    info(p, shop);
                    break;
                case REMOVE:
                    remove(p, shop);
                    break;
                case OPEN:
                    open(p, shop, true);
                    break;
                default: return;
            }

            e.setCancelled(true);
            ClickType.removePlayerClickType(p);
        } else {
            Shop shop = shopUtils.getShop(b.getLocation());

            if (shop == null)
                return;

            boolean confirmed = needsConfirmation.containsKey(p.getUniqueId()) && needsConfirmation.get(p.getUniqueId()).contains(shop.getID());
            
            if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p)) {
                return;
            }

            ItemStack infoItem = GlobalConfig.shopInfoItem;
            if (infoItem != null) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    ItemStack item = Utils.getItemInMainHand(p);

                    if (item == null || !(infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability())) {
                        item = Utils.getItemInOffHand(p);

                        if (item != null && infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability()) {
                            e.setCancelled(true);
                            info(p, shop);
                            return;
                        }
                    } else {
                        e.setCancelled(true);
                        info(p, shop);
                        return;
                    }
                }
            }

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && p.getUniqueId().equals(shop.getVendor().getUniqueId()) && shop.getShopType() != ShopType.ADMIN) {
                return;
            }

            if (p.getGameMode() == GameMode.CREATIVE) {
                e.setCancelled(true);
                p.sendMessage(LanguageUtils.getMessage(Message.USE_IN_CREATIVE));
                return;
            }

            if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.LEFT_CLICK_BLOCK && inverted)) {
                e.setCancelled(true);

                if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                    plugin.getDebugLogger().debug(p.getName() + " wants to buy");

                    if (shop.getBuyPrice() > 0) {
                        if (p.hasPermission(Permissions.BUY)) {
                            // TODO: Outsource shop use external permission
                            boolean externalPluginsAllowed = plugin.getHookManager().canUseShop(b, p, shop.getShopType() == ShopType.ADMIN);
                            
                            if (shop.getShopType() == ShopType.ADMIN) {
                                if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                    if (confirmed || !GlobalConfig.confirmShopping) {
                                        buy(p, shop, p.isSneaking());
                                        if (GlobalConfig.confirmShopping) {
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.remove(shop.getID());
                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        plugin.getDebugLogger().debug("Needs confirmation");
                                        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                        ids.add(shop.getID());
                                        needsConfirmation.put(p.getUniqueId(), ids);
                                    }
                                } else {
                                    plugin.getDebugLogger().debug(p.getName() + " doesn't have external plugin's permission");
                                    p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                }
                            } else {
                                if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                    Container c = (Container) b.getState();
                                    ItemStack itemStack = shop.getProduct().getItemStack();
                                    int amount = (p.isSneaking() ? itemStack.getMaxStackSize() : shop.getProduct().getAmount());
                                    
                                    // If shop has higher amounts than a stack, use the shop amount to allow players to use bulk discount
                                    if(shop.getProduct().getAmount()>itemStack.getMaxStackSize())
                                        amount = shop.getProduct().getAmount();
                                    
                                    if (Utils.getAmount(c.getInventory(), itemStack) >= amount) {
                                        if (confirmed || !GlobalConfig.confirmShopping) {
                                            buy(p, shop, p.isSneaking());
                                            if (GlobalConfig.confirmShopping) {
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.remove(shop.getID());
                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            plugin.getDebugLogger().debug("Needs confirmation");
                                            p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.add(shop.getID());
                                            needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        if (GlobalConfig.autoCalculateItemAmount && Utils.getAmount(c.getInventory(), itemStack) > 0) {
                                            if (confirmed || !GlobalConfig.confirmShopping) {
                                                buy(p, shop, p.isSneaking());
                                                if (GlobalConfig.confirmShopping) {
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.remove(shop.getID());
                                                    if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                    else needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                plugin.getDebugLogger().debug("Needs confirmation");
                                                p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.add(shop.getID());
                                                needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            p.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
                                            if (shop.getVendor().isOnline() && GlobalConfig.enableVendorMessages) {
                                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.VENDOR_OUT_OF_STOCK,
                                                        new Replacement(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                                new Replacement(Placeholder.ITEM_NAME, shop.getProduct().getLocalizedName())));
                                            } else if(!shop.getVendor().isOnline() && GlobalConfig.enableVendorBungeeMessages){
                                                String message = LanguageUtils.getMessage(Message.VENDOR_OUT_OF_STOCK,
                                                        new Replacement(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                        new Replacement(Placeholder.ITEM_NAME, shop.getProduct().getLocalizedName()));
                                                sendBungeeMessage(shop.getVendor().getName(), message);
                                            }
                                            plugin.getDebugLogger().debug("Shop is out of stock");
                                        }
                                    }
                                } else {
                                    plugin.getDebugLogger().debug(p.getName() + " doesn't have external plugin's permission");
                                    p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                }
                            }
                        } else {
                            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY));
                            plugin.getDebugLogger().debug(p.getName() + " is not permitted to buy");
                        }
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(Message.BUYING_DISABLED));
                        plugin.getDebugLogger().debug("Buying is disabled");
                    }
                }

            } else if ((e.getAction() == Action.LEFT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.RIGHT_CLICK_BLOCK && inverted)) {
                e.setCancelled(true);

                if ((shop.getShopType() == ShopType.ADMIN) || (!shop.getVendor().getUniqueId().equals(p.getUniqueId()))) {
                    plugin.getDebugLogger().debug(p.getName() + " wants to sell");

                    if (shop.getSellPrice() > 0) {
                        if (p.hasPermission(Permissions.SELL)) {
                            // TODO: Outsource shop use external permission
                            boolean externalPluginsAllowed = plugin.getHookManager().canUseShop(b, p, shop.getShopType() == ShopType.ADMIN);

                            ItemStack itemStack = shop.getProduct().getItemStack();

                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                boolean stack = p.isSneaking() && !Utils.hasAxeInHand(p);
                                int amount = stack ? itemStack.getMaxStackSize() : shop.getProduct().getAmount();

                                if (Utils.getAmount(p.getInventory(), itemStack) >= amount) {
                                    if (confirmed || !GlobalConfig.confirmShopping) {
                                        sell(p, shop, stack);
                                        if (GlobalConfig.confirmShopping) {
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.remove(shop.getID());
                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        plugin.getDebugLogger().debug("Needs confirmation");
                                        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                        ids.add(shop.getID());
                                        needsConfirmation.put(p.getUniqueId(), ids);
                                    }
                                } else {
                                    if (GlobalConfig.autoCalculateItemAmount && Utils.getAmount(p.getInventory(), itemStack) > 0) {
                                        if (confirmed || !GlobalConfig.confirmShopping) {
                                            sell(p, shop, stack);
                                            if (GlobalConfig.confirmShopping) {
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.remove(shop.getID());
                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            plugin.getDebugLogger().debug("Needs confirmation");
                                            p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.add(shop.getID());
                                            needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
                                        plugin.getDebugLogger().debug(p.getName() + " doesn't have enough items");
                                    }
                                }
                            } else {
                                plugin.getDebugLogger().debug(p.getName() + " doesn't have external plugin's permission");
                                p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_SELL_HERE));
                            }
                        } else {
                            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_SELL));
                            plugin.getDebugLogger().debug(p.getName() + " is not permitted to sell");
                        }
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(Message.SELLING_DISABLED));
                        plugin.getDebugLogger().debug("Selling is disabled");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(!plugin.getHookManager().canInteract(e.getPlayer())) {
            return;
        }
        handleInteractEvent(e);
    }

    /**
     * Create a new shop
     *
     * @param executor  Player, who executed the command, will receive the message and become the vendor of the shop
     * @param location  Where the shop will be located
     * @param product   Product of the Shop
     * @param buyPrice  Buy price
     * @param sellPrice Sell price
     * @param shopType  Type of the shop
     */
    private void create(final Player executor, final Location location, final ShopProduct product, final double buyPrice, final double sellPrice, final ShopType shopType) {
        plugin.getDebugLogger().debug(executor.getName() + " is creating new shop...");

        if (!executor.hasPermission(Permissions.CREATE)) {
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE));
            plugin.getDebugLogger().debug(executor.getName() + " is not permitted to create the shop");
            return;
        }

        double creationPrice = (shopType == ShopType.NORMAL) ? GlobalConfig.shopCreationPriceNormal : GlobalConfig.shopCreationPriceAdmin;
        Shop shop = new Shop(plugin, executor, product, location, buyPrice, sellPrice, shopType);


        final List<Block> shopBlocks = Utils.getChestLocations(shop).stream().map(Location::getBlock).toList();

        final boolean canCreateHook = plugin.getHookManager().canCreateShop(location.getBlock(), shopBlocks, executor);
        if(!canCreateHook && !executor.hasPermission(Permissions.CREATE_PROTECTED)) {
            plugin.getDebugLogger().debug("Create cancelled (Hook)");
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE_PROTECTED));
            return;
        }

        ShopCreateEvent event = new ShopCreateEvent(executor, shop, creationPrice);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.getDebugLogger().debug("Create event cancelled");
            return;
        }

        CompletableFuture.runAsync(() -> {

            if (creationPrice > 0) {
                EconomyResponse r = econ.withdrawPlayer(executor, location.getWorld().getName(), creationPrice);
                if (!r.transactionSuccess()) {
                    plugin.getDebugLogger().debug("Economy transaction failed: " + r.errorMessage);
                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                    return;
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {

                shop.create(true);

                plugin.getDebugLogger().debug("Shop created");
                shopUtils.addShop(shop, true);

                Message message = shopType == ShopType.ADMIN ? Message.ADMIN_SHOP_CREATED : Message.SHOP_CREATED;
                executor.sendMessage(LanguageUtils.getMessage(message, new Replacement(Placeholder.CREATION_PRICE, creationPrice)));
            });
        }, plugin.getShopCreationThreadPool());
    }

    /**
     * Remove a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be removed
     */
    private void remove(Player executor, Shop shop) {
        if (shop.getShopType() == ShopType.ADMIN && !executor.hasPermission(Permissions.REMOVE_ADMIN)) {
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_REMOVE_ADMIN));
            return;
        }

        if (shop.getShopType() == ShopType.NORMAL && !executor.getUniqueId().equals(shop.getVendor().getUniqueId())
                && !executor.hasPermission(Permissions.REMOVE_OTHER)) {
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_REMOVE_OTHERS));
            return;
        }

        plugin.getDebugLogger().debug(executor.getName() + " is removing " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopRemoveEvent event = new ShopRemoveEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.getDebugLogger().debug("Remove event cancelled (#" + shop.getID() + ")");
            return;
        }

        CompletableFuture.runAsync(() -> {

            double creationPrice = shop.getShopType() == ShopType.ADMIN ? GlobalConfig.shopCreationPriceAdmin : GlobalConfig.shopCreationPriceNormal;
            if (creationPrice > 0 && GlobalConfig.refundShopCreation && executor.getUniqueId().equals(shop.getVendor().getUniqueId())) {
                EconomyResponse r = econ.depositPlayer(executor, shop.getLocation().getWorld().getName(), creationPrice);
                if (!r.transactionSuccess()) {
                    plugin.getDebugLogger().debug("Economy transaction failed: " + r.errorMessage);
                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, r.errorMessage)));
                    executor.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED_REFUND,
                            new Replacement(Placeholder.CREATION_PRICE, 0)));
                } else {
                    executor.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED_REFUND,
                            new Replacement(Placeholder.CREATION_PRICE, creationPrice)));
                }
            } else {
                executor.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED));
            }

            Bukkit.getScheduler().runTask(plugin, () -> {

                shopUtils.removeShop(shop, true);
                plugin.getDebugLogger().debug("Removed shop (#" + shop.getID() + ")");
            });
        }, plugin.getShopCreationThreadPool());
    }

    /**
     * Open a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be opened
     * @param message Whether the player should receive the {@link Message#OPENED_SHOP} message
     */
    private void open(Player executor, Shop shop, boolean message) {
        if (!executor.getUniqueId().equals(shop.getVendor().getUniqueId()) && !executor.hasPermission(Permissions.OPEN_OTHER)) {
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_OPEN_OTHERS));
            return;
        }

        plugin.getDebugLogger().debug(executor.getName() + " is opening " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopOpenEvent event = new ShopOpenEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.getDebugLogger().debug("Open event cancelled (#" + shop.getID() + ")");
            return;
        }

        executor.openInventory(shop.getInventoryHolder().getInventory());
        plugin.getDebugLogger().debug("Opened shop (#" + shop.getID() + ")");
        if (message) executor.sendMessage(LanguageUtils.getMessage(Message.OPENED_SHOP,
                new Replacement(Placeholder.VENDOR, shop.getVendor().getName())));
    }

    /**
     *
     * @param executor Player, who executed the command and will retrieve the information
     * @param shop Shop from which the information will be retrieved
     */
    private void info(Player executor, Shop shop) {
        plugin.getDebugLogger().debug(executor.getName() + " is retrieving shop info (#" + shop.getID() + ")");
        ShopInfoEvent event = new ShopInfoEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.getDebugLogger().debug("Info event cancelled (#" + shop.getID() + ")");
            return;
        }

        Container c = (Container) shop.getLocation().getBlock().getState();
        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = Utils.getAmount(c.getInventory(), itemStack);
        int space = Utils.getFreeSpaceForItem(c.getInventory(), itemStack);

        String vendorName = (shop.getVendor().getName() == null ?
                shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());

        String vendorString = LanguageUtils.getMessage(Message.SHOP_INFO_VENDOR,
                new Replacement(Placeholder.VENDOR, vendorName));

        // Make JSON message with item preview
        final ShopProduct product = shop.getProduct();
        Consumer<Player> productMessage = plugin.getPlatform().getTextComponentHelper().getSendableItemInfo(
                LanguageUtils.getMessage(Message.SHOP_INFO_PRODUCT,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(product.getAmount()))),
                Placeholder.ITEM_NAME.toString(),
                product.getItemStack(),
                product.getLocalizedName()
        );

        String disabled = LanguageUtils.getMessage(Message.SHOP_INFO_DISABLED);

        String priceString = LanguageUtils.getMessage(Message.SHOP_INFO_PRICE,
                new Replacement(Placeholder.BUY_PRICE, (shop.getBuyPrice() > 0 ? String.valueOf(shop.getBuyPrice()) : disabled)),
                new Replacement(Placeholder.SELL_PRICE, (shop.getSellPrice() > 0 ? String.valueOf(shop.getSellPrice()) : disabled)));

        String shopType = LanguageUtils.getMessage(shop.getShopType() == ShopType.NORMAL ?
                Message.SHOP_INFO_NORMAL : Message.SHOP_INFO_ADMIN);

        String stock = LanguageUtils.getMessage(Message.SHOP_INFO_STOCK,
                new Replacement(Placeholder.STOCK, amount));

        String chestSpace = LanguageUtils.getMessage(Message.SHOP_INFO_CHEST_SPACE,
                new Replacement(Placeholder.CHEST_SPACE, space));

        executor.sendMessage(" ");
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(vendorString);
        productMessage.accept(executor);
        if (shop.getShopType() != ShopType.ADMIN && shop.getBuyPrice() > 0) executor.sendMessage(stock);
        if (shop.getShopType() != ShopType.ADMIN && shop.getSellPrice() > 0) executor.sendMessage(chestSpace);
        executor.sendMessage(priceString);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    /**
     * A player buys from a shop
     * @param executor Player, who executed the command and will buy the product
     * @param shop Shop, from which the player buys
     * @param stack Whether a whole stack should be bought
     */
    private void buy(Player executor, final Shop shop, boolean stack) {
        plugin.getDebugLogger().debug(executor.getName() + " is buying (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack && amount<=shop.getProduct().getAmount()) amount = itemStack.getMaxStackSize();

        String worldName = shop.getLocation().getWorld().getName();

        int finalAmount = amount;
        CompletableFuture.runAsync(() -> {

            double price = shop.getBuyPrice();
            if (stack) price = (price / shop.getProduct().getAmount()) * finalAmount;

            if (econ.getBalance(executor, worldName) >= price || GlobalConfig.autoCalculateItemAmount) {

                int amountForMoney = (int) (finalAmount / price * econ.getBalance(executor, worldName));

                if (amountForMoney == 0 && GlobalConfig.autoCalculateItemAmount) {
                    executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
                    return;
                }

                plugin.getDebugLogger().debug(executor.getName() + " has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

                double finalPrice = price;
                Bukkit.getScheduler().runTask(plugin, () -> {

                    Block b = shop.getLocation().getBlock();
                    Container c = (Container) b.getState();

                    int amountForChestItems = Utils.getAmount(c.getInventory(), itemStack);

                    if (amountForChestItems == 0 && shop.getShopType() != ShopType.ADMIN) {
                        executor.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
                        return;
                    }

                    ItemStack product = new ItemStack(itemStack);
                    if (stack) product.setAmount(finalAmount);

                    Inventory inventory = executor.getInventory();

                    int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

                    if (freeSpace == 0) {
                        executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
                        return;
                    }

                    CompletableFuture.runAsync(() -> {

                        int newAmount = finalAmount;

                        if (GlobalConfig.autoCalculateItemAmount) {
                            if (shop.getShopType() == ShopType.ADMIN)
                                newAmount = Math.min(amountForMoney, freeSpace);
                            else
                                newAmount = Math.min(Math.min(amountForMoney, amountForChestItems), freeSpace);
                        }

                        if (newAmount > finalAmount) newAmount = finalAmount;

                        ShopProduct newProduct = new ShopProduct(product, newAmount);
                        double newPrice = (finalPrice / finalAmount) * newAmount;
                        double tax = 0; //GlobalConfig.shopTaxes.getOrDefault(itemStack.getType().toString(), GlobalConfig.shopTaxes.get("default"));

                        if (freeSpace >= newAmount) {
                            plugin.getDebugLogger().debug(executor.getName() + " has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                            EconomyResponse r = econ.withdrawPlayer(executor, worldName, newPrice);

                            if (r.transactionSuccess()) {
                                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), worldName, newPrice * (100d - tax) / 100d) : null;

                                if (r2 != null) {
                                    if (r2.transactionSuccess()) {

                                        int finalNewAmount = newAmount;
                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, finalNewAmount, newPrice);
                                            Bukkit.getPluginManager().callEvent(event);

                                            if (event.isCancelled()) {
                                                econ.depositPlayer(executor, worldName, newPrice);
                                                econ.withdrawPlayer(shop.getVendor(), worldName, newPrice * (100d - tax) / 100d);
                                                plugin.getDebugLogger().debug("Buy event cancelled (#" + shop.getID() + ")");
                                                return;
                                            }

                                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

                                            addToInventory(inventory, newProduct);
                                            removeFromInventory(c.getInventory(), newProduct);
                                            executor.updateInventory();

                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    if (plugin.getHologramFormat().isDynamic()) {
                                                        shop.updateHologramText();
                                                    }
                                                }
                                            }.runTaskLater(plugin, 1L);

                                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                                            executor.sendMessage(LanguageUtils.getMessage(Message.BUY_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                                    new Replacement(Placeholder.VENDOR, vendorName)));

                                            plugin.getDebugLogger().debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                                            plugin.getLogger().info(String.format("%s bought %d of %s from %s", executor.getName(), finalNewAmount, newProduct.getItemStack().toString(), vendorName));

                                            if (shop.getVendor().isOnline() && GlobalConfig.enableVendorMessages) {
                                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_BOUGHT, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                                            } else if (!shop.getVendor().isOnline() && GlobalConfig.enableVendorBungeeMessages) {
                                                String message = LanguageUtils.getMessage(Message.SOMEONE_BOUGHT, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                                        new Replacement(Placeholder.PLAYER, executor.getName()));
                                                sendBungeeMessage(shop.getVendor().getName(), message);
                                            }
                                        });

                                    } else {
                                        CompletableFuture.runAsync(() -> {
                                            plugin.getDebugLogger().debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                                            executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
                                            econ.withdrawPlayer(shop.getVendor(), worldName, newPrice);
                                            econ.depositPlayer(executor, worldName, newPrice * (100d - tax) / 100d);
                                        }, plugin.getShopCreationThreadPool());
                                    }
                                } else {
                                    int finalNewAmount1 = newAmount;
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, finalNewAmount1, newPrice);
                                        Bukkit.getPluginManager().callEvent(event);

                                        if (event.isCancelled()) {
                                            econ.depositPlayer(executor, worldName, newPrice * (100d - tax) / 100d);
                                            plugin.getDebugLogger().debug("Buy event cancelled (#" + shop.getID() + ")");
                                            return;
                                        }

                                        database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

                                        addToInventory(inventory, newProduct);
                                        executor.updateInventory();

                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                if (plugin.getHologramFormat().isDynamic()) {
                                                    shop.updateHologramText();
                                                }
                                            }
                                        }.runTaskLater(plugin, 1L);

                                        executor.sendMessage(LanguageUtils.getMessage(Message.BUY_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount1)),
                                                new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice))));

                                        plugin.getDebugLogger().debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                                        plugin.getLogger().info(String.format("%s bought %d of %s from %s", executor.getName(), finalNewAmount1, newProduct.getItemStack().toString(), "ADMIN"));
                                    });
                                }
                            } else {
                                CompletableFuture.runAsync(() -> {
                                    plugin.getDebugLogger().debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                                    econ.depositPlayer(executor, worldName, newPrice);
                                }, plugin.getShopCreationThreadPool());
                            }
                        } else {
                            executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
                        }
                    }, plugin.getShopCreationThreadPool());
                });
            } else {
                executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
            }
        }, plugin.getShopCreationThreadPool());
    }

    /**
     * A player sells to a shop
     * @param executor Player, who executed the command and will sell the product
     * @param shop Shop, to which the player sells
     */
    private void sell(Player executor, final Shop shop, boolean stack) {
        plugin.getDebugLogger().debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack) amount = itemStack.getMaxStackSize();

        double price = shop.getSellPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        String worldName = shop.getLocation().getWorld().getName();

        int finalAmount = amount;
        double finalPrice = price;
        CompletableFuture.runAsync(() -> {

            if (shop.getShopType() == ShopType.ADMIN || econ.getBalance(shop.getVendor(), worldName) >= finalPrice || GlobalConfig.autoCalculateItemAmount) {
                int amountForMoney = 1;

                if (shop.getShopType() != ShopType.ADMIN) {
                    amountForMoney = (int) (finalAmount / finalPrice * econ.getBalance(shop.getVendor(), worldName));
                }

                plugin.getDebugLogger().debug("Vendor has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

                if (amountForMoney == 0 && GlobalConfig.autoCalculateItemAmount && shop.getShopType() != ShopType.ADMIN) {
                    executor.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
                    return;
                }

                int finalAmountForMoney = amountForMoney;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block block = shop.getLocation().getBlock();
                    Container chest = (Container) block.getState();

                    int amountForItemCount = Utils.getAmount(executor.getInventory(), itemStack);

                    if (amountForItemCount == 0) {
                        executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
                        return;
                    }

                    ItemStack product = new ItemStack(itemStack);
                    if (stack) product.setAmount(finalAmount);

                    Inventory inventory = chest.getInventory();

                    int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

                    if (freeSpace == 0 && shop.getShopType() != ShopType.ADMIN) {
                        executor.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
                        return;
                    }

                    int newAmount = finalAmount;

                    if (GlobalConfig.autoCalculateItemAmount) {
                        if (shop.getShopType() == ShopType.ADMIN)
                            newAmount = amountForItemCount;
                        else
                            newAmount = Math.min(Math.min(finalAmountForMoney, amountForItemCount), freeSpace);
                    }

                    if (newAmount > finalAmount) newAmount = finalAmount;

                    ShopProduct newProduct = new ShopProduct(product, newAmount);
                    double newPrice = (finalPrice / finalAmount) * newAmount;
                    double tax = 0; //plugin.getShopChestConfig().shopTaxes.getOrDefault(itemStack.getType().toString(), plugin.getShopChestConfig().shopTaxes.get("default"));

                    if (freeSpace >= newAmount || shop.getShopType() == ShopType.ADMIN) {
                        plugin.getDebugLogger().debug("Chest has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                        int finalNewAmount = newAmount;
                        CompletableFuture.runAsync(() -> {

                            EconomyResponse r = econ.depositPlayer(executor, worldName, newPrice * (100d - tax) / 100d);

                            if (r.transactionSuccess()) {
                                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.withdrawPlayer(shop.getVendor(), worldName, newPrice) : null;

                                if (r2 != null) {
                                    if (r2.transactionSuccess()) {
                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, finalNewAmount, newPrice);
                                            Bukkit.getPluginManager().callEvent(event);

                                            if (event.isCancelled()) {
                                                CompletableFuture.runAsync(() -> {
                                                    econ.withdrawPlayer(executor, worldName, newPrice * (100d - tax) / 100d);
                                                    econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                                                    plugin.getDebugLogger().debug("Sell event cancelled (#" + shop.getID() + ")");
                                                }, plugin.getShopCreationThreadPool());
                                                return;
                                            }

                                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

                                            addToInventory(inventory, newProduct);
                                            removeFromInventory(executor.getInventory(), newProduct);
                                            executor.updateInventory();

                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    if (plugin.getHologramFormat().isDynamic()) {
                                                        shop.updateHologramText();
                                                    }
                                                }
                                            }.runTaskLater(plugin, 1L);

                                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                                            executor.sendMessage(LanguageUtils.getMessage(Message.SELL_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                                    new Replacement(Placeholder.VENDOR, vendorName)));

                                            plugin.getDebugLogger().debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                                            plugin.getLogger().info(String.format("%s sold %d of %s from %s", executor.getName(), finalNewAmount, newProduct.getItemStack().toString(), vendorName));

                                            if (shop.getVendor().isOnline() && GlobalConfig.enableVendorMessages) {
                                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_SOLD, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                                            } else if (!shop.getVendor().isOnline() && GlobalConfig.enableVendorBungeeMessages) {
                                                String message = LanguageUtils.getMessage(Message.SOMEONE_SOLD, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                                        new Replacement(Placeholder.PLAYER, executor.getName()));
                                                sendBungeeMessage(shop.getVendor().getName(), message);
                                            }
                                        });

                                    } else {
                                        CompletableFuture.runAsync(() -> {
                                            plugin.getDebugLogger().debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                                            executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
                                            econ.withdrawPlayer(executor, worldName, newPrice * (100d - tax) / 100d);
                                            econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                                        }, plugin.getShopCreationThreadPool());
                                    }

                                } else {
                                    ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, finalNewAmount, newPrice);
                                    Bukkit.getPluginManager().callEvent(event);

                                    if (event.isCancelled()) {
                                        CompletableFuture.runAsync(() -> {
                                            econ.withdrawPlayer(executor, worldName, newPrice * (100d - tax) / 100d);
                                            plugin.getDebugLogger().debug("Sell event cancelled (#" + shop.getID() + ")");
                                        }, plugin.getShopCreationThreadPool());
                                        return;
                                    }

                                    database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

                                    removeFromInventory(executor.getInventory(), newProduct);
                                    executor.updateInventory();

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (plugin.getHologramFormat().isDynamic()) {
                                                shop.updateHologramText();
                                            }
                                        }
                                    }.runTaskLater(plugin, 1L);

                                    executor.sendMessage(LanguageUtils.getMessage(Message.SELL_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                                            new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice))));

                                    plugin.getDebugLogger().debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                                    plugin.getLogger().info(String.format("%s bought %d of %s from %s", executor.getName(), finalNewAmount, newProduct.getItemStack().toString(), "ADMIN"));
                                }

                            } else {
                                CompletableFuture.runAsync(() -> {
                                    plugin.getDebugLogger().debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                                    econ.withdrawPlayer(executor, worldName, newPrice);
                                }, plugin.getShopCreationThreadPool());
                            }
                        }, plugin.getShopCreationThreadPool());

                    } else {
                        executor.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
                    }
                });
            } else {
                executor.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
            }
        }, plugin.getShopCreationThreadPool());
    }

    /**
     * Adds items to an inventory
     * @param inventory The inventory, to which the items will be added
     * @param product Products to add
     * @return Whether all items were added to the inventory
     */
    private boolean addToInventory(Inventory inventory, ShopProduct product) {
        plugin.getDebugLogger().debug("Adding items to inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
        int added = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (added < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() != item.getMaxStackSize()) {
                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(item.getAmount() + 1);
                            inventory.setItem(slot, newItemStack);
                            added++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    ItemStack newItemStack = new ItemStack(itemStack);
                    newItemStack.setAmount(1);
                    inventory.setItem(slot, newItemStack);
                    added++;
                }
            }
        }

        return (added == amount);
    }

    /**
     * Removes items to from an inventory
     * @param inventory The inventory, from which the items will be removed
     * @param product Products to remove
     * @return Whether all items were removed from the inventory
     */
    private boolean removeFromInventory(Inventory inventory, ShopProduct product) {
        plugin.getDebugLogger().debug("Removing items from inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
        int removed = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (removed < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() > 0) {
                            int newAmount = item.getAmount() - 1;

                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(newAmount);

                            if (newAmount == 0)
                                inventory.setItem(slot, null);
                            else
                                inventory.setItem(slot, newItemStack);

                            removed++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    continue slotLoop;
                }

            }
        }

        return (removed == amount);
    }

    public void sendBungeeMessage(String player, String message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Message");
            out.writeUTF(player);
            out.writeUTF(message);

            if (!plugin.getServer().getOnlinePlayers().isEmpty()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player p = plugin.getServer().getOnlinePlayers().iterator().next();
                    p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                });
            }
        } catch (Exception e) {
            plugin.getDebugLogger().debug("Failed to send bungee message");
            plugin.getDebugLogger().debug(e);
            plugin.getLogger().warning("Failed to send BungeeCord message");
        }
    }
}
