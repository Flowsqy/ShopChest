package de.epiceric.shopchest.external;

import de.epiceric.shopchest.config.GlobalConfig;
import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.external.worldguard.WGLoader;
import de.epiceric.shopchest.hook.HookManager;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ExternalManager {

    private final HookManager hookManager;
    private final DebugLogger logger;
    private final List<ExternalLoader> loaders;

    public ExternalManager(HookManager hookManager, DebugLogger logger) {
        this.hookManager = hookManager;
        this.logger = logger;
        loaders = new LinkedList<>();
    }

    /**
     * Setup external loaders from configuration options
     */
    public void setup() {
        // Add WorldGuard
        if (GlobalConfig.enableWorldGuardIntegration) {
            final ExternalLoadData loadData = new ExternalLoadData();
            if (GlobalConfig.wgAllowCreateShopDefault) {
                loadData.setFlag(ExternalLoadData.Flags.CREATE);
            }
            if (GlobalConfig.wgAllowUseShopDefault) {
                loadData.setFlag(ExternalLoadData.Flags.USE);
            }
            if (GlobalConfig.wgAllowUseAdminShopDefault) {
                loadData.setFlag(ExternalLoadData.Flags.USE_ADMIN);
            }
            loaders.add(new WGLoader(hookManager, loadData));
        }
    }

    /**
     * Execute a phase of the {@link ExternalLoader} lifecycle
     *
     * @param phase The phase to execute
     */
    private void executePhase(Consumer<ExternalLoader> phase) {
        for (ExternalLoader loader : loaders) {
            if (loader.isStopped()) {
                continue;
            }
            try {
                phase.accept(loader);
            } catch (Exception e) {
                logger.debug("Can not load '" + loader.getName() + "' integration :");
                logger.debug(e.getMessage());
                loader.setStopped(true);
            }
        }
    }

    /**
     * Check if the external loader can load the plugin support
     */
    public void check() {
        executePhase(ExternalLoader::check);
    }

    /**
     * Execute the 'load' phase of every {@link ExternalLoader}
     */
    public void load() {
        executePhase(ExternalLoader::load);


        // TODO EXTERNAL : Register WorldGuard Flags

        /*
        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            WorldGuardShopFlag.register(this);
        }*/
    }

    /**
     * Execute the 'enable' phase of every {@link ExternalLoader}
     */
    public void enable() {
        executePhase(ExternalLoader::enable);

        executePhase(loader -> logger.debug("Successfully loaded '" + loader.getName() + "' integration"));

        // TODO EXTERNAL : Load Integrations

        /*
        Plugin townyPlugin = Bukkit.getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin instanceof Towny) {
            towny = (Towny) townyPlugin;
        }

        Plugin authMePlugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
        if (authMePlugin instanceof AuthMe) {
            authMe = (AuthMe) authMePlugin;
        }

        Plugin uSkyBlockPlugin = Bukkit.getServer().getPluginManager().getPlugin("uSkyBlock");
        if (uSkyBlockPlugin instanceof uSkyBlockAPI) {
            uSkyBlock = (uSkyBlockAPI) uSkyBlockPlugin;
        }

        Plugin aSkyBlockPlugin = Bukkit.getServer().getPluginManager().getPlugin("ASkyBlock");
        if (aSkyBlockPlugin instanceof ASkyBlock) {
            aSkyBlock = (ASkyBlock) aSkyBlockPlugin;
        }

        Plugin islandWorldPlugin = Bukkit.getServer().getPluginManager().getPlugin("IslandWorld");
        if (islandWorldPlugin instanceof IslandWorld) {
            islandWorld = (IslandWorld) islandWorldPlugin;
        }

        Plugin griefPreventionPlugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (griefPreventionPlugin instanceof GriefPrevention) {
            griefPrevention = (GriefPrevention) griefPreventionPlugin;
        }

        Plugin areaShopPlugin = Bukkit.getServer().getPluginManager().getPlugin("AreaShop");
        if (areaShopPlugin instanceof AreaShop) {
            areaShop = (AreaShop) areaShopPlugin;
        }

        Plugin bentoBoxPlugin = getServer().getPluginManager().getPlugin("BentoBox");
        if (bentoBoxPlugin instanceof BentoBox) {
            bentoBox = (BentoBox) bentoBoxPlugin;
        }
        */


        // TODO EXTERNAL : Register flags and
        /*
        if (hasWorldGuard()) {
            WorldGuardWrapper.getInstance().registerEvents(this);
        }

        if (hasPlotSquared()) {
            try {
                Class.forName("com.plotsquared.core.PlotSquared");
                PlotSquaredShopFlag.register(this);
            } catch (ClassNotFoundException ex) {
                PlotSquaredOldShopFlag.register(this);
            }
        }

        if (hasBentoBox()) {
            BentoBoxShopFlag.register(this);
        }*/

        // TODO EXTERNAL : Register Listeners 1

        /*
        if (hasWorldGuard()) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);

            if (hasAreaShop()) {
                getServer().getPluginManager().registerEvents(new AreaShopListener(this), this);
            }
        }

        if (hasBentoBox()) {
            getServer().getPluginManager().registerEvents(new BentoBoxListener(this), this);
        }*/

        // TODO : EXTERNAL : Register Listeners 2
        /*
        if (hasASkyBlock())
            getServer().getPluginManager().registerEvents(new ASkyBlockListener(this), this);
        if (hasGriefPrevention())
            getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
        if (hasIslandWorld())
            getServer().getPluginManager().registerEvents(new IslandWorldListener(this), this);
        if (hasPlotSquared()) {
            PlotSquaredListener psListener = new PlotSquaredListener(this);
            getServer().getPluginManager().registerEvents(psListener, this);
            PlotSquared.get().getEventDispatcher().registerListener(psListener);
        }
        if (hasTowny())
            getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        if (hasUSkyBlock())
            getServer().getPluginManager().registerEvents(new USkyBlockListener(this), this);
        if (hasWorldGuard())
            getServer().getPluginManager().registerEvents(new de.epiceric.shopchest.external.listeners.WorldGuardListener(this), this);
        if (hasBentoBox())
            getServer().getPluginManager().registerEvents(new de.epiceric.shopchest.external.listeners.BentoBoxListener(this), this);
        */
    }


    // TODO EXTERNAL Plugins supports

    /*
    public boolean hasAreaShop() {
        return Config.enableAreaShopIntegration && areaShop != null && areaShop.isEnabled();
    }


    public boolean hasGriefPrevention() {
        return Config.enableGriefPreventionIntegration && griefPrevention != null && griefPrevention.isEnabled();
    }


    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
    }


    public boolean hasIslandWorld() {
        return Config.enableIslandWorldIntegration && islandWorld != null && islandWorld.isEnabled();
    }

    public boolean hasASkyBlock() {
        return Config.enableASkyblockIntegration && aSkyBlock != null && aSkyBlock.isEnabled();
    }

    public boolean hasUSkyBlock() {
        return Config.enableUSkyblockIntegration && uSkyBlock != null && uSkyBlock.isEnabled();
    }

    public uSkyBlockAPI getUSkyBlock() {
        return uSkyBlock;
    }

    public boolean hasPlotSquared() {
        if (!Config.enablePlotsquaredIntegration) {
            return false;
        }

        if (Utils.getMajorVersion() < 13) {
            // Supported PlotSquared versions don't support versions below 1.13
            return false;
        }
        Plugin p = getServer().getPluginManager().getPlugin("PlotSquared");
        return p != null && p.isEnabled();
    }


    public boolean hasAuthMe() {
        return Config.enableAuthMeIntegration && authMe != null && authMe.isEnabled();
    }

    public boolean hasTowny() {
        return Config.enableTownyIntegration && towny != null && towny.isEnabled();
    }

    public boolean hasWorldGuard() {
        return Config.enableWorldGuardIntegration && worldGuard != null && worldGuard.isEnabled();
    }

    public boolean hasBentoBox() {
        return Config.enableBentoBoxIntegration && bentoBox != null && bentoBox.isEnabled();
    }
    */


    // TODO Hook this

    // TODO EXTERNAL : Check USE
    /*
    if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
        try {
            Class.forName("com.plotsquared.core.PlotSquared");
            com.plotsquared.core.location.Location plotLocation =
                    com.plotsquared.core.location.Location.at(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
            com.plotsquared.core.plot.Plot plot = plotLocation.getOwnedPlot();
            externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.USE_SHOP, p);
        } catch (ClassNotFoundException ex) {
            com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                    new com.github.intellectualsites.plotsquared.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
            com.github.intellectualsites.plotsquared.plot.object.Plot plot = plotLocation.getOwnedPlot();
            externalPluginsAllowed = PlotSquaredOldShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredOldShopFlag.USE_SHOP, p);
        }
    }

    if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
        String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
        WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
        Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
        if (!flag.isPresent()) plugin.getDebugLogger().debug("WorldGuard flag '" + flagName + "' is not present!");
        WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
        externalPluginsAllowed = state == WrappedState.ALLOW;
    }*/

    // TODO EXTERNAL : Check USE
    /*
    if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
        try {
            Class.forName("com.plotsquared.core.PlotSquared");
            com.plotsquared.core.location.Location plotLocation =
                    com.plotsquared.core.location.Location.at(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
            com.plotsquared.core.plot.Plot plot = plotLocation.getOwnedPlot();
            externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.USE_SHOP, p);
        } catch (ClassNotFoundException ex) {
            com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                    new com.github.intellectualsites.plotsquared.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
            com.github.intellectualsites.plotsquared.plot.object.Plot plot = plotLocation.getOwnedPlot();
            externalPluginsAllowed = PlotSquaredOldShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredOldShopFlag.USE_SHOP, p);
        }
    }

    if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
        String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
        WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
        Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
        if (!flag.isPresent()) plugin.getDebugLogger().debug("WorldGuard flag '" + flagName + "' is not present!");
        WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
        externalPluginsAllowed = state == WrappedState.ALLOW;
    }*/

    // TODO EXTERNAL : Check AUTH
        /*
        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(p))
            return;*/

    // TODO EXTERNAL : Check AUTH
    //if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(e.getPlayer())) return;

}
