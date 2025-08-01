/*
 * Copyright (c) 2020, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.recipefordisaster;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestVarbits;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.NpcCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemOnTileRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Client;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

import static net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicHelper.and;

public class RFDSkrachUglogwee extends BasicQuestHelper
{
	ItemRequirement rawJubbly, cookedJubbly, axeHighlighted, ironSpit, log, tinderbox, pickaxe, ogreBellows, ballOfWool, ogreBowAndArrows,
		ogreBow, ogreArrows, chompy, chompySpitted, ogreBellowsFilled, toad, toadReady, rock, cookedJubblyHighlighted;

	ItemRequirement feldipTeleport, lumbridgeTeleport, karamjaTeleport;

	Requirement inDiningRoom, jubblyNearby, jubblyCarcassNearby, rawJubblyOnFloor, hadBalloonToad, fireLit;

	DetailedQuestStep enterDiningRoom, inspectSkrach, talkToRantz, talkToRantzOnCoast, useAxeOnTree, useAxeOnTreeAgain, talkToRantzOnCoastAgain,
		useSpitOnChompy, lightFire, cookChompy, talkToRantzAfterReturn, getToad, getRock, useBellowOnToadInInv, dropBalloonToad, killJubbly, lootJubbly,
		fillUpBellows, cookJubbly, enterDiningRoomAgain, useJubblyOnSkrach, pickUpRawJubbly;

	//Zones
	Zone diningRoom;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();

		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep goInspectSkrach = new ConditionalStep(this, enterDiningRoom);
		goInspectSkrach.addStep(inDiningRoom, inspectSkrach);
		steps.put(0, goInspectSkrach);
		steps.put(10, talkToRantz);
		steps.put(20, talkToRantz);

		steps.put(30, talkToRantzOnCoast);
		steps.put(40, talkToRantzOnCoast);

		steps.put(50, useAxeOnTree);
		steps.put(60, useAxeOnTreeAgain);

		steps.put(70, talkToRantzOnCoastAgain);

		ConditionalStep getChildrenToKaramja = new ConditionalStep(this, useSpitOnChompy);
		getChildrenToKaramja.addStep(and(chompySpitted, fireLit), cookChompy);
		getChildrenToKaramja.addStep(chompySpitted, lightFire);

		steps.put(80, getChildrenToKaramja);

		steps.put(90, talkToRantzAfterReturn);
		steps.put(100, talkToRantzAfterReturn);

		ConditionalStep getJubbly = new ConditionalStep(this, fillUpBellows);
		getJubbly.addStep(rawJubbly.alsoCheckBank(questBank), cookJubbly);
		getJubbly.addStep(rawJubblyOnFloor, pickUpRawJubbly);
		getJubbly.addStep(jubblyCarcassNearby, lootJubbly);
		getJubbly.addStep(jubblyNearby, killJubbly);
		getJubbly.addStep(hadBalloonToad, dropBalloonToad);
		getJubbly.addStep(new Conditions(toad, rock, ogreBellowsFilled), useBellowOnToadInInv);
		getJubbly.addStep(new Conditions(toad, ogreBellowsFilled), getRock);
		getJubbly.addStep(ogreBellowsFilled, getToad);
		steps.put(110, getJubbly);
		steps.put(120, getJubbly);
		steps.put(130, getJubbly);
		steps.put(140, getJubbly);
		steps.put(150, getJubbly);

		ConditionalStep saveSkrach = new ConditionalStep(this, enterDiningRoomAgain);
		saveSkrach.addStep(inDiningRoom, useJubblyOnSkrach);
		steps.put(160, saveSkrach);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		rawJubbly = new ItemRequirement("Raw jubbly", ItemID._100_JUBBLY_MEAT_RAW).highlighted();
		cookedJubbly = new ItemRequirement("Cooked jubbly", ItemID._100_JUBBLY_MEAT_COOKED);
		cookedJubblyHighlighted = new ItemRequirement("Cooked jubbly", ItemID._100_JUBBLY_MEAT_COOKED).highlighted();
		axeHighlighted = new ItemRequirement("Any axe", ItemCollections.AXES).isNotConsumed().highlighted();
		ironSpit = new ItemRequirement("Iron spit", ItemID.SPIT_IRON).highlighted().isNotConsumed();
		log = new ItemRequirement("Any log to burn", ItemCollections.LOGS_FOR_FIRE);
		tinderbox = new ItemRequirement("Tinderbox", ItemID.TINDERBOX).isNotConsumed();
		pickaxe = new ItemRequirement("Any pickaxe", ItemCollections.PICKAXES).isNotConsumed();
		ogreBellows = new ItemRequirement("Ogre bellows", ItemID.EMPTY_OGRE_BELLOWS).isNotConsumed();
		ogreBellows.addAlternates(ItemID.FILLED_OGRE_BELLOW1, ItemID.FILLED_OGRE_BELLOW2, ItemID.FILLED_OGRE_BELLOW3);
		ogreBellowsFilled = new ItemRequirement("Ogre bellows", ItemID.FILLED_OGRE_BELLOW1).isNotConsumed();
		ogreBellowsFilled.addAlternates(ItemID.FILLED_OGRE_BELLOW2, ItemID.FILLED_OGRE_BELLOW3);
		ogreBellowsFilled.setHighlightInInventory(true);
		ballOfWool = new ItemRequirement("Balls of wool", ItemID.BALL_OF_WOOL);
		ogreBow = new ItemRequirement("Ogre bow", ItemID.OGRE_BOW).isNotConsumed();
		ogreBow.addAlternates(ItemID.ZOGRE_BOW);
		ogreArrows = new ItemRequirement("Ogre arrow", ItemID.OGRE_ARROW);
		ogreArrows.addAlternates(ItemID.ZOGRE_BRUTAL_BRONZE, ItemID.ZOGRE_BRUTAL_IRON, ItemID.ZOGRE_BRUTAL_STEEL, ItemID.ZOGRE_BRUTAL_BLACK,
			ItemID.ZOGRE_BRUTAL_MITHRIL, ItemID.ZOGRE_BRUTAL_ADAMANT, ItemID.ZOGRE_BRUTAL_RUNE);
		ogreBowAndArrows = new ItemRequirements("Ogre bow + ogre arrows", ogreBow, ogreArrows);

		chompy = new ItemRequirement("Raw chompy", ItemID.RAW_CHOMPY);
		chompySpitted = new ItemRequirement("Skewered chompy", ItemID.SPIT_SKEWERED_CHOMPY);
		toad = new ItemRequirement("Bloated toad", ItemID.BLOATED_TOAD);
		toad.setHighlightInInventory(true);
		toadReady = new ItemRequirement("Balloon toad", ItemID._100_JUBBLY_BALLOON_TOAD_BROWN);

		rock = new ItemRequirement("Rock", ItemID.SWAMPROCKS1);
		rock.setHighlightInInventory(true);


		feldipTeleport = new ItemRequirement("Feldip teleport. Fairy Ring (AKS), Gnome Glider", ItemID.TELEPORTSCROLL_FELDIP);
		feldipTeleport.addAlternates(ItemCollections.FAIRY_STAFF);

		lumbridgeTeleport = new ItemRequirement("Lumbridge teleport", ItemID.POH_TABLET_LUMBRIDGETELEPORT);
		karamjaTeleport = new ItemRequirement("Karamja teleport. Brimhaven Teleport tablet, Fairy Ring (CKR)", ItemID.NZONE_TELETAB_BRIMHAVEN);
		karamjaTeleport.addAlternates(ItemCollections.FAIRY_STAFF);
	}

	@Override
	protected void setupZones()
	{
		diningRoom = new Zone(new WorldPoint(1856, 5313, 0), new WorldPoint(1870, 5333, 0));
	}

	public void setupConditions()
	{
		inDiningRoom = new ZoneRequirement(diningRoom);
		hadBalloonToad = new Conditions(true, toadReady);
		jubblyNearby = new NpcCondition(NpcID._100_JUBBLY_BIRD);
		jubblyCarcassNearby = new NpcCondition(NpcID._100_JUBBLY_BIRD_DEAD);
		rawJubblyOnFloor = new ItemOnTileRequirement(rawJubbly);
		fireLit = new ObjectCondition(ObjectID.FIRE,
			new Zone(new WorldPoint(2755, 3076, 0),
				new WorldPoint(2768, 3087, 0)));
	}

	public void setupSteps()
	{
		enterDiningRoom = new ObjectStep(this, ObjectID.HUNDRED_LUMBRIDGE_DOUBLEDOORL, new WorldPoint(3213, 3221, 0), "Go inspect Skrach Uglogwee in Lumbridge Castle.");
		enterDiningRoom.addTeleport(lumbridgeTeleport);
		inspectSkrach = new ObjectStep(this, ObjectID.HUNDRED_OGRE_BASE, new WorldPoint(1864, 5329, 0), "Inspect Skrach Uglogwee.");
		inspectSkrach.addDialogSteps("Yes, I'm sure I can get some Jubbly Chompy.", "Oh Ok then, I guess I'll talk to Rantz.");
		inspectSkrach.addSubSteps(enterDiningRoom);

		talkToRantz = new NpcStep(this, NpcID.RANTZ, new WorldPoint(2630, 2984, 0), "Talk to Rantz in Feldip Hills.");
		talkToRantz.addDialogSteps("I'm trying to free Skrach, can you help?", "Ok, I'll do it.");
		talkToRantz.addTeleport(feldipTeleport);
		talkToRantzOnCoast = new NpcStep(this, NpcID._100_RANTZ, new WorldPoint(2649, 2964, 0), "Talk to Rantz on the east coast of Feldip Hills.");
		talkToRantzOnCoast.addDialogStep("Ok, here I am...I guess this is the watery place? What now?");
		useAxeOnTree = new ObjectStep(this, ObjectID._100_JUBBLY_MULTI_PUSH_TREE, new WorldPoint(2655, 2963, 0), "Use an axe on the old tree near Rantz.", axeHighlighted);
		useAxeOnTree.addIcon(ItemID.RUNE_AXE);
		useAxeOnTreeAgain = new ObjectStep(this, ObjectID._100_JUBBLY_MULTI_PUSH_TREE, new WorldPoint(2655, 2963, 0), "Use an axe on the old tree near Rantz again.", axeHighlighted);
		useAxeOnTreeAgain.addIcon(ItemID.RUNE_AXE);
		talkToRantzOnCoastAgain = new NpcStep(this, NpcID._100_RANTZ, new WorldPoint(2649, 2964, 0), "Talk to Rantz again on the east coast of Feldip Hills.");
		talkToRantzOnCoastAgain.addDialogStep("Ok, the boat's ready, now tell me how to get a Jubbly?");
		useSpitOnChompy = new DetailedQuestStep(this, "Use an iron spit on a chompy.", ironSpit.highlighted(), chompy.highlighted());
		lightFire = new DetailedQuestStep(this, new WorldPoint(2760, 3080, 0), "Light a fire on karamja's west coast. Afterwards, use your skewered chompy on it.",
			log.highlighted(), tinderbox.highlighted(), chompySpitted);
		lightFire.addIcon(ItemID.LOGS);
		lightFire.addTeleport(karamjaTeleport);
		cookChompy = new ObjectStep(this, ObjectID.FIRE, "Cook the skewered chompy on the fire.", chompySpitted.highlighted());
		cookChompy.addIcon(ItemID.SPIT_SKEWERED_CHOMPY);
		lightFire.addSubSteps(cookChompy);
		talkToRantzAfterReturn = new NpcStep(this, NpcID._100_RANTZ, new WorldPoint(2649, 2964, 0), "Travel back with Rantz's kids and talk to Rantz again.");
		talkToRantzAfterReturn.addDialogSteps("Yes please, I'll get a lift back with you.", "Ok, now tell me how to get Jubbly!");
		fillUpBellows = new ObjectStep(this, ObjectID.SWAMPBUBBLES, new WorldPoint(2601, 2967, 0), "Fill some ogre bellows on some swamp bubbles.", ogreBellows);
		getToad = new NpcStep(this, NpcID.TOAD, new WorldPoint(2601, 2967, 0), "Blow up a toad with the bellows.", ogreBellowsFilled);
		getToad.addSubSteps(fillUpBellows);
		getRock = new ObjectStep(this, ObjectID.SWAMP_ROCK1, new WorldPoint(2567, 2960, 0), "Mine a pile of rocks near the Feldip Hills Fairy Ring for a rock.", pickaxe);
		useBellowOnToadInInv = new DetailedQuestStep(this, "Use the bellows on your toad with a ball of wool in your inventory.", ogreBellowsFilled, toad, ballOfWool);
		dropBalloonToad = new DetailedQuestStep(this, new WorldPoint(2593, 2964, 0), "Drop the balloon toad near a swamp and wait for a Jubbly to arrive.", toadReady, ogreBowAndArrows);
		killJubbly = new NpcStep(this, NpcID._100_JUBBLY_BIRD, "Kill then pluck jubbly.", ogreBowAndArrows);
		pickUpRawJubbly = new ItemStep(this, "Pick up the raw jubbly.", rawJubbly);
		lootJubbly = new NpcStep(this, NpcID._100_JUBBLY_BIRD_DEAD, "Pluck the jubbly's carcass.");
		cookJubbly = new ObjectStep(this, ObjectID.MULTI_CHOMPYBIRD_SPITROAST_ENTITY, new WorldPoint(2631, 2990, 0), "Cook the raw jubbly on Rantz's spit.", rawJubbly);
		cookJubbly.addIcon(ItemID._100_JUBBLY_MEAT_RAW);

		enterDiningRoomAgain = new ObjectStep(this, ObjectID.HUNDRED_LUMBRIDGE_DOOR, new WorldPoint(3207, 3217, 0), "Go give the jubbly to Skrach Uglogwee to finish the quest.", cookedJubbly);
		enterDiningRoomAgain.addTeleport(lumbridgeTeleport);
		useJubblyOnSkrach = new ObjectStep(this, ObjectID.HUNDRED_OGRE_BASE, new WorldPoint(1864, 5329, 0), "Give the jubbly to Skrach Uglogwee to finish the quest.", cookedJubblyHighlighted);
		useJubblyOnSkrach.addIcon(ItemID._100_JUBBLY_MEAT_COOKED);
		useJubblyOnSkrach.addSubSteps(enterDiningRoomAgain);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(axeHighlighted, chompy, ironSpit, log, tinderbox, pickaxe, ogreBellows, ballOfWool, ogreBowAndArrows);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(lumbridgeTeleport.quantity(2), feldipTeleport, karamjaTeleport);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Jubbly (level 9)");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new SkillRequirement(Skill.COOKING, 41, true));
		req.add(new SkillRequirement(Skill.FIREMAKING, 20, true));
		req.add(new QuestRequirement(QuestHelperQuest.BIG_CHOMPY_BIRD_HUNTING, QuestState.FINISHED));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Arrays.asList(
				new ExperienceReward(Skill.WOODCUTTING, 1500),
				new ExperienceReward(Skill.COOKING, 1500),
				new ExperienceReward(Skill.CRAFTING, 1500),
				new ExperienceReward(Skill.RANGED, 1500));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("New method of travel between Karamja and Feldip Hills."),
				new UnlockReward("Increased access to the Culinaromancer's Chest."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Starting off", Collections.singletonList(inspectSkrach), Collections.singletonList(lumbridgeTeleport)));
		allSteps.add(new PanelDetails("Help Rantz", Arrays.asList(talkToRantz, talkToRantzOnCoast, useAxeOnTree, useAxeOnTreeAgain,
			talkToRantzOnCoastAgain, useSpitOnChompy, lightFire, talkToRantzAfterReturn),
			Arrays.asList(axeHighlighted, log, tinderbox, chompy, ironSpit, ogreBowAndArrows, pickaxe, ogreBellows, ballOfWool),
			Arrays.asList(feldipTeleport, karamjaTeleport, lumbridgeTeleport)));
		allSteps.add(new PanelDetails("Save Skrach", Arrays.asList(getToad, getRock, useBellowOnToadInInv,
			dropBalloonToad, killJubbly, lootJubbly, pickUpRawJubbly, cookJubbly, useJubblyOnSkrach),
			Arrays.asList(ogreBowAndArrows, pickaxe, ogreBellows, ballOfWool),
			Collections.singletonList(lumbridgeTeleport)));

		return allSteps;
	}

	@Override
	public QuestState getState(Client client)
	{
		int questState = client.getVarbitValue(QuestVarbits.QUEST_RECIPE_FOR_DISASTER_SKRACH_UGLOGWEE.getId());
		if (questState == 0)
		{
			return QuestState.NOT_STARTED;
		}

		if (questState < 170)
		{
			return QuestState.IN_PROGRESS;
		}

		return QuestState.FINISHED;
	}

	@Override
	public boolean isCompleted()
	{
		return (client.getVarbitValue(QuestVarbits.QUEST_RECIPE_FOR_DISASTER_SKRACH_UGLOGWEE.getId()) >= 170 || client.getVarbitValue(QuestVarbits.QUEST_RECIPE_FOR_DISASTER.getId()) < 3);
	}
}
