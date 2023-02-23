package de.epiceric.shopchest.action;

import java.util.*;

public class ActionManager {

    private final Map<UUID, PendingAction> playerActions;

    public ActionManager() {
        playerActions = new HashMap<>();
    }

    /**
     * Submit a {@link PendingAction} for a {@link org.bukkit.entity.Player}
     *
     * @param playerId the {@link org.bukkit.entity.Player}'s {@link UUID}
     * @param action   The {@link PendingAction} that he will do
     * @return An {@link Optional} {@link PendingAction} which is :
     * - empty if the action was correctly submitted
     * - filled by the current action that has not been executed (mean that the submitted action is rejected)
     */
    public Optional<PendingAction> submitAction(UUID playerId, PendingAction action) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(action);
        final PendingAction registeredAction = playerActions.compute(
                playerId,
                (k, v) -> (v == null || v.hasExpired() ? action : v)
        );
        return registeredAction == action ? Optional.empty() : Optional.of(registeredAction);
    }

    /**
     * Get the current action of a {@link org.bukkit.entity.Player}
     *
     * @param uuid {@link org.bukkit.entity.Player}'s {@link UUID}
     * @return A {@link PendingAction} which the player should do when interacting
     */
    public PendingAction getAction(UUID uuid) {
        final PendingAction action = playerActions.computeIfPresent(uuid, (k, v) -> v.hasExpired() ? null : v);
        return action == null ? new InteractShopPendingAction(-1) : action;
    }

}
