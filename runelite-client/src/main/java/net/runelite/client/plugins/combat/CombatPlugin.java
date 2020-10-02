package net.runelite.client.plugins.combat;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

import javax.inject.Inject;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

@PluginDescriptor(
	name = "Combat Helper",
	description = "Show combat statistics and infomation",
	tags = {"combat"}
)

@Slf4j
public class CombatPlugin extends Plugin
{
	@Inject
	private Notifier notifier;

	@Inject
	private Client client;

	@Inject
	private CombatConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	private final Set<NPC> targets = new HashSet<>();

	private List<String> targetNames = new ArrayList<>();

	private List<String> lootNames = new ArrayList<>();

	public Actor target = null;

	public boolean isCombatRunning = false;

	public boolean isPrayerRunning = false;

	public boolean isLooting = false;

	@Provides
	CombatConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatConfig.class);
	}

	private final HotkeyListener combatKeyListener = new HotkeyListener(() -> new Keybind(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK)) {
		@Override
		public void hotkeyPressed()
		{
			toggleCombat();
		}
	};

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals("combathelper"))
		{
			return;
		}

		targetNames = readTargetNames();
		lootNames = readLootNames();
		rebuildAllNpcs();
	}

	public List<String> readTargetNames(){
		final String configNpcs = config.getNpcToHighlight().toLowerCase();

		if (configNpcs.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configNpcs);
	}

	public List<String> readLootNames(){
	final java.lang.String configLoot = config.getLoot().toLowerCase();

	if (configLoot.isEmpty())
	{
		return Collections.emptyList();
	}

	return Text.fromCSV(configLoot);
}

	private void rebuildAllNpcs()
	{
		targets.clear();

		if (client.getGameState() != GameState.LOGGED_IN &&client.getGameState() != GameState.LOADING)
		{
			// NPCs are still in the client after logging out,
			// but we don't want to highlight those.
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			if(isMatch(npc)){
				targets.add(npc);
			}
		}
	}

	@Override
	protected void startUp() throws Exception {
		keyManager.registerKeyListener(combatKeyListener);
		targetNames = readTargetNames();
		lootNames = readLootNames();
		rebuildAllNpcs();
	}

	@Override
	protected void shutDown() throws Exception {
		keyManager.unregisterKeyListener(combatKeyListener);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick){
		Thread thread = new Thread(() -> loop());
		thread.start();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
 				targetNames.clear();
		}
	}

	private boolean switchEntries = false;

	private void loop(){
		if(!isCombatRunning) return;
		target = client.getLocalPlayer().getInteracting();
		boolean shouldAttack = isCombatRunning && target == null && getNextTarget() != null;

		if(isLooting)
		{
			LocalPoint local = client.getLocalPlayer().getLocalLocation();
			if(local.getSceneX() == lootEntry.getParam0() && local.getSceneY() == lootEntry.getParam1()){
				isLooting = false;
				lootEntry = null;
			}
			return;
		}

		if(shouldLoot){
			try {
				Thread.sleep(300);
				click();
				shouldLoot = false;
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		if(!shouldAttack) return;

		if(shouldAttack){
			try {
				Thread.sleep(300);
				switchEntries = true;
				click();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean shouldLoot = false;

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived)
	{
		if(!isCombatRunning) return;
		
		final Collection<ItemStack> items = npcLootReceived.getItems();

		for(ItemStack itemStack : items)
		{
			if(isMatch(itemStack))
			{
				lootStack(itemStack);
				shouldLoot = true;
				return;
			}
		}
	}

	private boolean isMatch(ItemStack item){
		log.info((itemManager.getItemComposition(item.getId()).getPrice() * item.getQuantity()) + "");
		if(itemManager.getItemComposition(item.getId()).getPrice() * item.getQuantity() > 1500 && item.getQuantity() > 1)
		{
			return true;
		}

		if(itemManager.getItemComposition(item.getId()).getPrice() * item.getQuantity() > 10000)
		{
			return true;
		}

		for(String name : lootNames)
		{
			if(WildcardMatcher.matches(itemManager.getItemComposition(item.getId()).getName(), name))
			{
				return true;
			}
		}
		return false;
	}

	private MenuEntry lootEntry;

	private void lootStack(ItemStack stack)
	{
		lootEntry = new MenuEntry();
		lootEntry.setTarget(itemManager.getItemComposition(stack.getId()).getName());
		lootEntry.setType(20);
		lootEntry.setOption("Take");
		lootEntry.setIdentifier(stack.getId());
		lootEntry.setParam0(stack.getLocation().getSceneX());
		lootEntry.setParam1(stack.getLocation().getSceneY());
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event){
		if(!isCombatRunning) return;
		if(lootEntry != null)
		{
			MenuEntry[] entries = new MenuEntry[1];
			entries[0] = lootEntry;
			client.setMenuEntries(entries);
			return;
		}



		if(!switchEntries) return;
		swapMenuEntry();
	}

	private void toggleCombat(){
		System.out.println("Combat Key Pressed!");
		isCombatRunning = !isCombatRunning;
		if(!isCombatRunning)
		{
			target = null;
		}
	}

	private void swapMenuEntry(){
		NPC newTarget = getNextTarget();
		if(newTarget == null) return;

		MenuEntry entry = new MenuEntry();
		entry.setTarget(newTarget.getName());
		entry.setType(10);
		entry.setOption("Attack");
		entry.setIdentifier(newTarget.getIndex());

		MenuEntry[] entries = new MenuEntry[1];
		entries[0] = entry;
		client.setMenuEntries(entries);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if(lootEntry != null)
		{
			isLooting = true;
			return;
		}

		if(switchEntries)
			switchEntries = false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		final NPC npc = npcSpawned.getNpc();
		if(isMatch(npc)){
			targets.add(npc);
		}
	}

	private boolean isMatch(NPC npc){
		final String npcName = npc.getName();

		if (npcName == null) {
			return false;
		}

		for (String name : targetNames) {
			if(name.contains("#")){
				if (WildcardMatcher.matches(getName(name), npcName)){
					if(npc.getCombatLevel() == getLevel(name)){
						return true;
					}
				}
			} else if (WildcardMatcher.matches(name, npcName)) {
				return true;
			}
		}

		return false;
	}

	private int getLevel(String target){
		String[] strings = target.split("#");
		if(strings.length == 2){
			return Integer.parseInt(strings[1]);
		}
		return -1;
	}

	private String getName(String target){
		String[] strings = target.split("#");
		if(strings.length == 2){
			return strings[0];
		}
		return target;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		final NPC npc = npcDespawned.getNpc();
		targets.remove(npc);
	}

	private void click(int x, int y){
		MouseEvent me = new MouseEvent(client.getCanvas(), // which
				MouseEvent.MOUSE_PRESSED,
				System.currentTimeMillis(),
				0,
				x, y,
				1,
				false);
		client.getCanvas().dispatchEvent(me);
	}

	private void click(){
		click(50, 50);
	}


	private NPC getNextTarget(){
		if(targets.isEmpty()) return null;

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		return  targets.stream().filter(e -> !e.isDead() && (e.getInteracting() == null || e.getInteracting() == client.getLocalPlayer()))
				.sorted(Comparator.comparingDouble(e -> e.getWorldLocation().distanceTo(playerLocation)))
				.findFirst()
				.get();
	}
}