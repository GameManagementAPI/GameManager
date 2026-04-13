package de.c4vxl.gamemanager.utils

import de.c4vxl.gamemanager.GameManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.*
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.RegisteredListener
import java.util.*

/**
 * A utility class for creating items with specific attributes
 */
class ItemBuilder(
    var material: Material,
    var name: Component? = null,
    var amount: Int = 1,
    var lore: List<Component> = mutableListOf(),
    var unbreakable: Boolean = false,
    var enchantments: MutableMap<Enchantment, Int> = mutableMapOf(),
    var itemMeta: ItemMeta? = null
) {
    interface ItemEventHandler<T : Event> { fun handle(event: T) }

    companion object : Listener {
        private val eventHandlers = mutableMapOf<Class<out Event>, MutableMap<String, ItemEventHandler<out Event>>>()
        private val registeredEvents: MutableSet<Class<out Event>> = mutableSetOf()

        // Maps of events to the items they were done with
        data class RegisteredEvent<T : Event>(
            val event: Class<T>,
            var handler: (T) -> List<ItemStack?>
        )
        private val supportedEvents = mutableListOf<RegisteredEvent<*>>()

        init {
            // Register event types
            registerEvent(InventoryClickEvent::class.java) { event -> listOf(event.currentItem, event.cursor) }
            registerEvent(PlayerInteractEvent::class.java) { event -> listOf(event.item) }
            registerEvent(PlayerDropItemEvent::class.java) { event -> listOf(event.itemDrop.itemStack) }
            registerEvent(PlayerItemBreakEvent::class.java) { event -> listOf(event.brokenItem) }
            registerEvent(PlayerItemDamageEvent::class.java) { event -> listOf(event.item) }
            registerEvent(PlayerItemConsumeEvent::class.java) { event -> listOf(event.item) }
            registerEvent(BlockPlaceEvent::class.java) { event -> listOf(event.itemInHand) }
        }

        /**
         * Unregisters an event from the possible events list
         * @param event The event to unregister
         */
        fun <T : Event> unregisterEvent(event: Class<T>): RegisteredEvent<*>? {
            val index = supportedEvents.indexOfFirst { it.event == event }
            if (index == -1)
                return null

            return supportedEvents.removeAt(index)
        }

        /**
         * Registers an event as usable with items
         * @param event The event to register
         * @param replace Set to true if an existing handler should be replaced
         */
        fun <T : Event> registerEvent(event: Class<T>, replace: Boolean = true, handler: (T) -> List<ItemStack?>): Boolean {
            // Try to unregister event
            if (supportedEvents.any { it.event == event }) {
                if (replace)
                    unregisterEvent(event)
                else
                    return false
            }

            // Register new event
            supportedEvents.add(RegisteredEvent(event, handler))

            return true
        }

        @EventHandler
        fun onEvent(event: Event) {
            val items: List<ItemStack?> = supportedEvents.find { it.event.isAssignableFrom(event::class.java) }
                ?.handler?.let {
                    @Suppress("UNCHECKED_CAST")
                    (it as (Event) -> List<ItemStack?>).invoke(event)
                } ?: run {
                    GameManager.logger.warning("Tried to hook into ${event.javaClass.name} using ItemBuilder#onEvent. This event is not supported!")
                    return
                }

            for (item in items) {
                if (item == null)
                    continue

                val meta = if (item.hasItemMeta()) item.itemMeta else continue

                val id = meta.persistentDataContainer.get(
                    NamespacedKey("gma", "itembuilder"),
                    PersistentDataType.STRING
                ) ?: continue

                eventHandlers[event::class.java]?.get(id)?.let {
                    @Suppress("UNCHECKED_CAST")
                    (it as ItemEventHandler<Event>).handle(event)
                }

                return
            }
        }

        /**
         * Creates an ItemBuilder instance from an existing item
         * Warning: calling .build will still return a completely new item, just with equal properties
         * @param itemStack The item
         */
        fun fromItemStack(itemStack: ItemStack): ItemBuilder {
            val meta = itemStack.takeIf { it.hasItemMeta() }?.itemMeta

            return ItemBuilder(
                itemStack.type,
                itemStack.displayName(),
                itemStack.amount,
                itemStack.lore() ?: emptyList(),
                meta?.isUnbreakable ?: false,
                meta?.enchants ?: mutableMapOf(),
                meta
            )
        }
    }

    /**
     * Holds a unique identifier for this item
     */
    val key: String = UUID.randomUUID().toString()

    /**
     * Registers an event listener for this exact item
     * @param eventClass The event to listen to
     * @param handler The code to be executed when event is triggered
     * @param priority The priority of the event
     */
    fun <T : Event> onEvent(eventClass: Class<T>, handler: ItemEventHandler<T>, priority: EventPriority = EventPriority.NORMAL): ItemBuilder {
        // Register handler
        val map = eventHandlers.getOrPut(eventClass) { mutableMapOf() }
        map[this.key] = handler

        // Register event listener
        if (eventClass !in registeredEvents) {
            // Get handler list
            val handlerList = try {
                val method = eventClass.getMethod("getHandlerList")
                method.invoke(null) as HandlerList
            } catch (e: Exception) {
                GameManager.logger.warning("Error registering event handler for item $key: $e")
                return this
            }

            // Create listener
            val listener = RegisteredListener(
                Companion,
                { _, event -> Companion.onEvent(event) },
                priority,
                GameManager.instance,
                false
            )

            // Register listener
            handlerList.register(listener)
            registeredEvents += eventClass
        }

        return this
    }

    /**
     * Builds the final item stack
     */
    fun build(): ItemStack {
        val itemStack = ItemStack(material, amount)
        val itemMeta = (itemMeta ?: itemStack.itemMeta) ?: return itemStack

        // Set name
        name?.let { itemMeta.itemName(it) }

        // Set unbreakable
        itemMeta.isUnbreakable = unbreakable

        // Set lore
        if (lore.isNotEmpty())
            itemMeta.lore(lore)

        // Store id
        itemMeta.persistentDataContainer.set(
            NamespacedKey("gma", "itembuilder"),
            PersistentDataType.STRING,
            this.key
        )

        // Set item meta
        itemStack.itemMeta = itemMeta

        // Set enchantments
        enchantments.forEach {
            itemStack.addUnsafeEnchantment(it.key, it.value)
        }

        return itemStack
    }
}