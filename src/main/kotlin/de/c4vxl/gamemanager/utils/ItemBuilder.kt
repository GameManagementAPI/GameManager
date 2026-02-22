package de.c4vxl.gamemanager.utils

import de.c4vxl.gamemanager.GameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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
    var lore: MutableList<TextComponent> = mutableListOf(),
    var unbreakable: Boolean = false,
    var enchantments: MutableMap<Enchantment, Int> = mutableMapOf(),
    var itemMeta: ItemMeta? = null
) {
    interface ItemEventHandler<T : Event> { fun handle(event: T) }

    companion object : Listener {
        private val eventHandlers = mutableMapOf<Class<out Event>, MutableMap<String, ItemEventHandler<out Event>>>()
        private val registeredEvents: MutableSet<Class<out Event>> = mutableSetOf()

        @EventHandler
        fun onEvent(event: Event) {
            val item: ItemStack = when (event) {
                is InventoryClickEvent    -> event.currentItem
                is PlayerInteractEvent    -> event.item
                is PlayerDropItemEvent    -> event.itemDrop.itemStack
                is PlayerItemBreakEvent   -> event.brokenItem
                is PlayerItemDamageEvent  -> event.item
                is PlayerItemConsumeEvent -> event.item
                is BlockPlaceEvent        -> event.itemInHand
                else -> null
            } ?: return

            val meta = if (item.hasItemMeta()) item.itemMeta
                       else return

            val id = meta.persistentDataContainer.get(
                NamespacedKey("gma", "itembuilder"),
                PersistentDataType.STRING
            ) ?: return

            eventHandlers[event::class.java]?.get(id)?.let {
                @Suppress("UNCHECKED_CAST")
                (it as ItemEventHandler<Event>).handle(event)
            }
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
     */
    fun <T : Event> onEvent(eventClass: Class<T>, handler: ItemEventHandler<T>): ItemBuilder {
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
                EventPriority.NORMAL,
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

        // Set enchantments
        if (enchantments.isNotEmpty())
            itemStack.addUnsafeEnchantments(enchantments)

        // Store id
        itemMeta.persistentDataContainer.set(
            NamespacedKey("gma", "itembuilder"),
            PersistentDataType.STRING,
            this.key
        )

        // Set item meta
        itemStack.itemMeta = itemMeta

        return itemStack
    }
}