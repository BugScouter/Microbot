/*
 * Copyright (c) 2023, Zoinkwiz (https://www.github.com/Zoinkwiz)
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
package net.runelite.client.plugins.microbot.questhelper.helpers.miniquests.hisfaithfulservants;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.ComplexStateQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.ManualRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.npc.NpcInteractingRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.ChatMessageType;
import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BarrowsHelper extends ComplexStateQuestHelper
{
	// Required
	ItemRequirement combatGear, spade;

	// Recommended
	ItemRequirement barrowsTeleport, strangeOldLockpick, ghommalHilt2;

	Requirement inAhrim, inDharok, inVerac, inTorag, inKaril, inGuthan, inCrypt;

	QuestStep enterAhrim, killAhrim, leaveAhrim, enterDharok, killDharok, leaveDharok, enterKaril, killKaril, leaveKaril,
		enterVerac, killVerac, leaveVerac, enterTorag, killTorag, leaveTorag, enterGuthan, killGuthan, leaveGuthan;

	QuestStep searchAhrimSarc, searchDharokSarc, searchKarilSarc, searchVeracSarc, searchToragSarc, searchGuthanSarc;

	QuestStep enterAhrim2, enterDharok2, enterGuthan2, enterKaril2, enterTorag2, enterVerac2;
	QuestStep enterAhrimSarc, enterDharokSarc, enterKarilSarc, enterVeracSarc, enterToragSarc, enterGuthanSarc, enterSarc;

	DetailedQuestStep openChest, searchChest, killFinalBrother, leaveBarrows;

	Zone ahrimRoom, dharokRoom, veracRoom, toragRoom, karilRoom, guthanRoom, crypt;

	NpcInteractingRequirement dharokAttacking, ahrimAttacking, karilAttacking, guthanAttacking, toragAttacking, veracAttacking;

	Requirement brotherAttacking;
	Requirement killedDharok, killedVerac, killedAhrim, killedGuthan, killedTorag, killedKaril, killedAllSix;

	ManualRequirement isDharokTunnel, isVeracTunnel, isAhrimTunnel, isGuthanTunnel, isToragTunnel, isKarilTunnel;
	Requirement doneWithDharok, doneWithVerac, doneWithAhrim, doneWithGuthan, doneWithTorag, doneWithKaril, doneWithAll;

	ManualRequirement escapeCrypt;

	@Override
	public QuestStep loadStep()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();

		ConditionalStep killingDharok = new ConditionalStep(this, enterDharok);
		killingDharok.addStep(dharokAttacking, killDharok);
		killingDharok.addStep(inDharok, searchDharokSarc);

		ConditionalStep killingAhrim = new ConditionalStep(this, enterAhrim);
		killingAhrim.addStep(ahrimAttacking, killAhrim);
		killingAhrim.addStep(inAhrim, searchAhrimSarc);

		ConditionalStep killingKaril = new ConditionalStep(this, enterKaril);
		killingKaril.addStep(karilAttacking, killKaril);
		killingKaril.addStep(inKaril, searchKarilSarc);

		ConditionalStep killingGuthan = new ConditionalStep(this, enterGuthan);
		killingGuthan.addStep(guthanAttacking, killGuthan);
		killingGuthan.addStep(inGuthan, searchGuthanSarc);

		ConditionalStep killingTorag = new ConditionalStep(this, enterTorag);
		killingTorag.addStep(toragAttacking, killTorag);
		killingTorag.addStep(inTorag, searchToragSarc);

		ConditionalStep killingVerac = new ConditionalStep(this, enterVerac);
		killingVerac.addStep(veracAttacking, killVerac);
		killingVerac.addStep(inVerac, searchVeracSarc);

		// The Crypt
		ConditionalStep finishingTheRun = new ConditionalStep(this, enterDharok2);
		finishingTheRun.addStep(new Conditions(inCrypt, killedAllSix), searchChest);
		finishingTheRun.addStep(new Conditions(inCrypt, brotherAttacking), killFinalBrother);
		finishingTheRun.addStep(inCrypt, openChest);
		// Ahrim
		finishingTheRun.addStep(new Conditions(isAhrimTunnel, inAhrim), enterAhrimSarc);
		finishingTheRun.addStep(isAhrimTunnel, enterAhrim2);
		// Verac
		finishingTheRun.addStep(new Conditions(isVeracTunnel, inVerac), enterVeracSarc);
		finishingTheRun.addStep(isVeracTunnel, enterVerac2);
		// Guthan
		finishingTheRun.addStep(new Conditions(isGuthanTunnel, inGuthan), enterGuthanSarc);
		finishingTheRun.addStep(isGuthanTunnel, enterGuthan2);
		// Torag
		finishingTheRun.addStep(new Conditions(isToragTunnel, inTorag), enterToragSarc);
		finishingTheRun.addStep(isToragTunnel, enterTorag2);
		// Karil
		finishingTheRun.addStep(new Conditions(isKarilTunnel, inKaril), enterKarilSarc);
		finishingTheRun.addStep(isKarilTunnel, enterKaril2);
		// Dharok
		finishingTheRun.addStep(new Conditions(isDharokTunnel, inDharok), enterDharokSarc);

		ConditionalStep doingBarrows = new ConditionalStep(this, enterVerac);
		doingBarrows.addStep(new Conditions(escapeCrypt, inCrypt), leaveBarrows);
		doingBarrows.addStep(escapeCrypt, enterDharok);
		doingBarrows.addStep(doneWithAll, finishingTheRun);
		// Top condition is for catching 1 remains to point to tomb to raid
		doingBarrows.addStep(new Conditions(doneWithDharok, inDharok), leaveDharok);
		doingBarrows.addStep(new Conditions(doneWithAhrim, inAhrim), leaveAhrim);
		doingBarrows.addStep(new Conditions(doneWithKaril, inKaril), leaveKaril);
		doingBarrows.addStep(new Conditions(doneWithGuthan, inGuthan), leaveGuthan);
		doingBarrows.addStep(new Conditions(doneWithTorag, inTorag), leaveTorag);
		doingBarrows.addStep(new Conditions(doneWithVerac, inVerac), leaveVerac);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithDharok), killingDharok);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithAhrim), killingAhrim);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithKaril), killingKaril);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithGuthan), killingGuthan);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithTorag), killingTorag);
		doingBarrows.addStep(new Conditions(LogicType.NOR, doneWithVerac), killingVerac);

		return doingBarrows;
	}

	@Override
	protected void setupRequirements()
	{
		// Varp 1502 increases is KC. On KC increase, suggest leaving and entering
		combatGear = new ItemRequirement("Combat gear to kill all 6 Barrows Brothers", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());

		spade = new ItemRequirement("Spade", ItemID.SPADE);
		barrowsTeleport = new ItemRequirement("Barrows teleport", ItemID.TELETAB_BARROWS);
		strangeOldLockpick = new ItemRequirement("Strange old lockpick", ItemID.STRANGE_OLD_LOCKPICK);
		strangeOldLockpick.addAlternates(ItemID.STRANGE_OLD_LOCKPICK_FULL);
		ghommalHilt2 = new ItemRequirement("Ghommal's hilt 2 or higher to remove prayer drain in crypts", ItemID.CA_OFFHAND_MEDIUM);
		ghommalHilt2.addAlternates(ItemID.CA_OFFHAND_HARD, ItemID.CA_OFFHAND_ELITE, ItemID.CA_OFFHAND_MASTER, ItemID.CA_OFFHAND_GRANDMASTER,
			ItemID.INFERNAL_DEFENDER_GHOMMAL_5, ItemID.INFERNAL_DEFENDER_GHOMMAL_5_TROUVER, ItemID.INFERNAL_DEFENDER_GHOMMAL_6, ItemID.INFERNAL_DEFENDER_GHOMMAL_6_TROUVER);
	}

	@Override
	protected void setupZones()
	{
		ahrimRoom = new Zone(new WorldPoint(3549, 9693, 3), new WorldPoint(3562, 9705, 3));
		dharokRoom = new Zone(new WorldPoint(3547, 9709, 3), new WorldPoint(3560, 9719, 3));
		guthanRoom = new Zone(new WorldPoint(3533, 9698, 3), new WorldPoint(3548, 9711, 3));
		karilRoom = new Zone(new WorldPoint(3544, 9678, 3), new WorldPoint(3559, 9690, 3));
		toragRoom = new Zone(new WorldPoint(3563, 9682, 3), new WorldPoint(3577, 9694, 3));
		veracRoom = new Zone(new WorldPoint(3567, 9701, 3), new WorldPoint(3580, 9712, 3));
		crypt = new Zone(new WorldPoint(3520, 9660, 0), new WorldPoint(3597, 9733, 0));
	}

	public void setupConditions()
	{
		inAhrim = new ZoneRequirement(ahrimRoom);
		inDharok = new ZoneRequirement(dharokRoom);
		inGuthan = new ZoneRequirement(guthanRoom);
		inKaril = new ZoneRequirement(karilRoom);
		inTorag = new ZoneRequirement(toragRoom);
		inVerac = new ZoneRequirement(veracRoom);
		inCrypt = new ZoneRequirement(crypt);

		killedAhrim = new VarbitRequirement(VarbitID.BARROWS_KILLED_AHRIM, 1);
		killedDharok = new VarbitRequirement(VarbitID.BARROWS_KILLED_DHAROK, 1);
		killedGuthan = new VarbitRequirement(VarbitID.BARROWS_KILLED_GUTHAN, 1);
		killedKaril = new VarbitRequirement(VarbitID.BARROWS_KILLED_KARIL, 1);
		killedTorag = new VarbitRequirement(VarbitID.BARROWS_KILLED_TORAG, 1);
		killedVerac = new VarbitRequirement(VarbitID.BARROWS_KILLED_VERAC, 1);
		killedAllSix = new Conditions(killedAhrim, killedDharok, killedGuthan, killedKaril, killedTorag, killedVerac);

		dharokAttacking = new NpcInteractingRequirement(NpcID.BARROWS_DHAROK);
		ahrimAttacking = new NpcInteractingRequirement(NpcID.BARROWS_AHRIM);
		karilAttacking = new NpcInteractingRequirement(NpcID.BARROWS_KARIL);
		guthanAttacking = new NpcInteractingRequirement(NpcID.BARROWS_GUTHAN);
		toragAttacking = new NpcInteractingRequirement(NpcID.BARROWS_TORAG);
		veracAttacking = new NpcInteractingRequirement(NpcID.BARROWS_VERAC);
		brotherAttacking = new Conditions(LogicType.OR, dharokAttacking, ahrimAttacking, karilAttacking, guthanAttacking, toragAttacking, veracAttacking);

		isDharokTunnel = new ManualRequirement();
		isAhrimTunnel = new ManualRequirement();
		isKarilTunnel = new ManualRequirement();
		isGuthanTunnel = new ManualRequirement();
		isToragTunnel = new ManualRequirement();
		isVeracTunnel = new ManualRequirement();

		doneWithDharok = new Conditions(LogicType.OR, killedDharok, isDharokTunnel);
		doneWithAhrim = new Conditions(LogicType.OR, killedAhrim, isAhrimTunnel);
		doneWithKaril = new Conditions(LogicType.OR, killedKaril, isKarilTunnel);
		doneWithTorag = new Conditions(LogicType.OR, killedTorag, isToragTunnel);
		doneWithVerac = new Conditions(LogicType.OR, killedVerac, isVeracTunnel);
		doneWithGuthan = new Conditions(LogicType.OR, killedGuthan, isGuthanTunnel);
		doneWithAll = new Conditions(doneWithDharok, doneWithAhrim, doneWithKaril, doneWithTorag, doneWithVerac, doneWithGuthan);

		escapeCrypt = new ManualRequirement();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.BARROWS_KILLED_COUNT)
		{
			if (event.getValue() == 0)
			{
				isAhrimTunnel.setShouldPass(false);
				isDharokTunnel.setShouldPass(false);
				isVeracTunnel.setShouldPass(false);
				isGuthanTunnel.setShouldPass(false);
				isKarilTunnel.setShouldPass(false);
				isToragTunnel.setShouldPass(false);
				escapeCrypt.setShouldPass(false);
			}
		}
	}

	int lastKc = -1;

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (inCrypt.check(client))
		{
			int currentKc = client.getVarpValue(VarPlayerID.TOTAL_BARROWS_CHESTS);
			if (lastKc == -1)
			{
				lastKc = currentKc;
			}
			// Got chest
			else if (lastKc != currentKc)
			{
				lastKc = currentKc;
				escapeCrypt.setShouldPass(true);
				// set leave to true
			}

			List<WorldPoint> barrowsRoute = BarrowsRouteCalculator.startDelving(client);
			if (barrowsRoute != null)
			{
				searchChest.setLinePoints(barrowsRoute);
				openChest.setLinePoints(barrowsRoute);
			}
		}

		Widget textOption = client.getWidget(InterfaceID.Messagebox.TEXT);
		if (textOption == null)
		{
			return;
		}
		if (textOption.getText().equals("You've found a hidden tunnel, do you want to enter?"))
		{
			updateTunnel();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if(chatMessage.getType() != ChatMessageType.MESBOX)
		{
			return;
		}

		if (chatMessage.getMessage().equals("You've found a hidden tunnel, do you want to enter?"))
		{

			updateTunnel();
		}
	}

	private void updateTunnel()
	{
		if (inAhrim.check(client)) isAhrimTunnel.setShouldPass(true);
		if (inDharok.check(client)) isDharokTunnel.setShouldPass(true);
		if (inVerac.check(client)) isVeracTunnel.setShouldPass(true);
		if (inGuthan.check(client)) isGuthanTunnel.setShouldPass(true);
		if (inKaril.check(client)) isKarilTunnel.setShouldPass(true);
		if (inTorag.check(client)) isToragTunnel.setShouldPass(true);
	}

	public void setupSteps()
	{
		enterAhrim = new DigStep(this, new WorldPoint(3564, 3291, 0), "Dig on Ahrim's Barrow to enter it.", combatGear);
		enterDharok = new DigStep(this, new WorldPoint(3575, 3299, 0), "Dig on Dharok's Barrow to enter it.", combatGear);
		enterGuthan = new DigStep(this, new WorldPoint(3578, 3281, 0), "Dig on Guthan's Barrow to enter it.", combatGear);
		enterKaril = new DigStep(this, new WorldPoint(3567, 3274, 0), "Dig on Karil's Barrow to enter it.", combatGear);
		enterTorag = new DigStep(this, new WorldPoint(3553, 3281, 0), "Dig on Torag's Barrow to enter it.", combatGear);
		enterVerac = new DigStep(this, new WorldPoint(3556, 3297, 0), "Dig on Verac's Barrow to enter it.", combatGear);

		searchAhrimSarc = new ObjectStep(this, ObjectID.BARROW_AHRIM_SARCOPHAGUS, new WorldPoint(3555, 9699, 3),
			"Search Ahrim's Sarcophagus, ready to fight them. They use magic and are weak to ranged attacks.", combatGear);
		searchDharokSarc = new ObjectStep(this, ObjectID.BARROW_DHAROK_SARCOPHAGUS, new WorldPoint(3555, 9714, 3),
			"Search Dharok's Sarcophagus, ready to fight them. They use melee and are weak to magic attacks. Be careful as they hit VERY hard at low health.", combatGear);
		searchGuthanSarc = new ObjectStep(this, ObjectID.BARROW_GUTHAN_SARCOPHAGUS, new WorldPoint(3539, 9703, 3),
			"Search Guthan's Sarcophagus, ready to fight them. hey use melee and are weak to magic attacks.", combatGear);
		searchToragSarc = new ObjectStep(this, ObjectID.BARROW_TORAG_SARCOPHAGUS, new WorldPoint(3569, 9686, 3),
			"Search Ahrim's Sarcophagus, ready to fight them. hey use melee and are weak to magic attacks.", combatGear);
		searchKarilSarc = new ObjectStep(this, ObjectID.BARROW_KARIL_SARCOPHAGUS, new WorldPoint(3550, 9683, 3),
			"Search Karil's Sarcophagus, ready to fight them. They use ranged and are weak to melee attacks.", combatGear);
		searchVeracSarc = new ObjectStep(this, ObjectID.BARROW_VERAC_SARCOPHAGUS, new WorldPoint(3573, 9706, 3),
			"Search Ahrim's Sarcophagus, ready to fight them. hey use melee and are weak to magic attacks.", combatGear);

		killAhrim = new NpcStep(this, NpcID.BARROWS_AHRIM, "Defeat Ahrim.");
		((NpcStep) killAhrim).setMustBeFocusedOnPlayer(true);
		killDharok = new NpcStep(this, NpcID.BARROWS_DHAROK, "Defeat Dharok.");
		((NpcStep) killDharok).setMustBeFocusedOnPlayer(true);
		killGuthan = new NpcStep(this, NpcID.BARROWS_GUTHAN, "Defeat Guthan.");
		((NpcStep) killGuthan).setMustBeFocusedOnPlayer(true);
		killKaril = new NpcStep(this, NpcID.BARROWS_KARIL, "Defeat Karil.");
		((NpcStep) killKaril).setMustBeFocusedOnPlayer(true);
		killTorag = new NpcStep(this, NpcID.BARROWS_TORAG, "Defeat Torag.");
		((NpcStep) killTorag).setMustBeFocusedOnPlayer(true);
		killVerac = new NpcStep(this, NpcID.BARROWS_VERAC, "Defeat Verac.");
		((NpcStep) killVerac).setMustBeFocusedOnPlayer(true);

		leaveAhrim = new ObjectStep(this, ObjectID.BARROWS_STAIRS_AHRIM, new WorldPoint(3559, 9703, 3), "Leave the barrow.");
		leaveDharok = new ObjectStep(this, ObjectID.BARROWS_STAIRS_DHAROK, new WorldPoint(3558, 9718, 3), "Leave the barrow.");
		leaveGuthan = new ObjectStep(this, ObjectID.BARROWS_STAIRS_GUTHAN, new WorldPoint(3534, 9706, 3), "Leave the barrow.");
		leaveKaril = new ObjectStep(this, ObjectID.BARROWS_STAIRS_KARIL, new WorldPoint(3546, 9686, 3), "Leave the barrow.");
		leaveTorag = new ObjectStep(this, ObjectID.BARROWS_STAIRS_TORAG, new WorldPoint(3566, 9683, 3), "Leave the barrow.");
		leaveVerac = new ObjectStep(this, ObjectID.BARROWS_STAIRS_VERAC, new WorldPoint(3578, 9704, 3), "Leave the barrow.");

		killAhrim.addSubSteps(enterAhrim, searchAhrimSarc, leaveAhrim);
		killDharok.addSubSteps(enterDharok, searchDharokSarc, leaveDharok);
		killGuthan.addSubSteps(enterGuthan, searchGuthanSarc, leaveGuthan);
		killKaril.addSubSteps(enterKaril, searchKarilSarc, leaveKaril);
		killTorag.addSubSteps(enterTorag, searchToragSarc, leaveTorag);
		killVerac.addSubSteps(enterVerac, searchVeracSarc, leaveVerac);

		enterAhrim2 = new DigStep(this, new WorldPoint(3564, 3291, 0), "Dig on Ahrim's Barrow to enter it.", combatGear);
		enterDharok2 = new DigStep(this, new WorldPoint(3575, 3299, 0), "Dig on Dharok's Barrow to enter it.", combatGear);
		enterGuthan2 = new DigStep(this, new WorldPoint(3578, 3281, 0), "Dig on Guthan's Barrow to enter it.", combatGear);
		enterKaril2 = new DigStep(this, new WorldPoint(3567, 3274, 0), "Dig on Karil's Barrow to enter it.", combatGear);
		enterTorag2 = new DigStep(this, new WorldPoint(3553, 3281, 0), "Dig on Torag's Barrow to enter it.", combatGear);
		enterVerac2 = new DigStep(this, new WorldPoint(3556, 3297, 0), "Dig on Verac's Barrow to enter it.", combatGear);

		enterAhrimSarc = new ObjectStep(this, ObjectID.BARROW_AHRIM_SARCOPHAGUS, new WorldPoint(3555, 9699, 3),
			"Search Ahrim's Sarcophagus to enter it.", combatGear);
		enterAhrimSarc.addDialogStep("Yeah I'm fearless!");
		enterDharokSarc = new ObjectStep(this, ObjectID.BARROW_DHAROK_SARCOPHAGUS, new WorldPoint(3555, 9714, 3),
			"Search Dharok's Sarcophagusto enter it.", combatGear);
		enterDharokSarc.addDialogStep("Yeah I'm fearless!");
		enterGuthanSarc = new ObjectStep(this, ObjectID.BARROW_GUTHAN_SARCOPHAGUS, new WorldPoint(3539, 9703, 3),
			"Search Guthan's Sarcophagus to enter it.", combatGear);
		enterGuthanSarc.addDialogStep("Yeah I'm fearless!");
		enterToragSarc = new ObjectStep(this, ObjectID.BARROW_TORAG_SARCOPHAGUS, new WorldPoint(3569, 9686, 3),
			"Search Torag's Sarcophagus to enter it.", combatGear);
		enterToragSarc.addDialogStep("Yeah I'm fearless!");
		enterKarilSarc = new ObjectStep(this, ObjectID.BARROW_KARIL_SARCOPHAGUS, new WorldPoint(3550, 9683, 3),
			"Search Karil's Sarcophagus to enter it.", combatGear);
		enterKarilSarc.addDialogStep("Yeah I'm fearless!");
		enterVeracSarc = new ObjectStep(this, ObjectID.BARROW_VERAC_SARCOPHAGUS, new WorldPoint(3573, 9706, 3),
			"Search Verac's Sarcophagus to enter it.", combatGear);
		enterVeracSarc.addDialogStep("Yeah I'm fearless!");

		enterSarc = new DetailedQuestStep(this, "Enter the sarcophagus that was empty to enter the crypt.");
		enterSarc.addSubSteps(enterAhrimSarc, enterDharokSarc, enterGuthanSarc, enterToragSarc, enterKarilSarc, enterVeracSarc,
			enterVerac2, enterDharok2, enterAhrim2, enterKaril2, enterTorag2, enterGuthan2);

		openChest = new ObjectStep(this, ObjectID.BARROWS_STONE_CHEST_CLOSED, new WorldPoint(3551, 9695, 0),
			"Open the chest in the middle of the crypt. DO NOT SEARCH IT until you've killed the 6th brother.");

		killFinalBrother = new NpcStep(this, NpcID.BARROWS_AHRIM, "Kill the final brother.");
		((NpcStep) killFinalBrother).addAlternateNpcs(NpcID.BARROWS_DHAROK, NpcID.BARROWS_KARIL, NpcID.BARROWS_GUTHAN,
			NpcID.BARROWS_TORAG, NpcID.BARROWS_VERAC);
		((NpcStep) killFinalBrother).setMustBeFocusedOnPlayer(true);

		searchChest = new ObjectStep(this, ObjectID.BARROWS_STONE_CHEST_CLOSED, new WorldPoint(3551, 9695, 0),
			"Search the chest in the middle of the crypt for the strange icon.");
		((ObjectStep) searchChest).addAlternateObjects(ObjectID.BARROWS_STONE_CHEST_OPEN);

		leaveBarrows = new ObjectStep(this, ObjectID.BARROWS_STAIRS_AHRIM, "Escape back to the surface.", true);
		((ObjectStep) leaveBarrows).addAlternateObjects(ObjectID.BARROWS_STAIRS_DHAROK, ObjectID.BARROWS_STAIRS_GUTHAN,
			ObjectID.BARROWS_STAIRS_KARIL, ObjectID.BARROWS_STAIRS_TORAG, ObjectID.BARROWS_STAIRS_VERAC, ObjectID.BARROWS_LADDER_G);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(combatGear, spade);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(barrowsTeleport, strangeOldLockpick, ghommalHilt2);
	}


	@Override
	public List<String> getCombatRequirements()
	{
		return Arrays.asList(
			"Ahrim the Blighted (level 98)",
			"Dharok the Wretched (level 115)",
			"Guthan the Infested (level 115)",
			"Karil the Tainted (level 98)",
			"Torag the Corrupted (level 115)",
			"Verac the Defiled (level 115)"
		);
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.PRIEST_IN_PERIL, QuestState.FINISHED));
		return req;
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("Chance at getting barrows equipment", ItemID.BARROWS_MAP, 1));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Barrows Run",
			Arrays.asList(killDharok, killAhrim, killKaril, killGuthan, killTorag, killVerac,
				enterSarc, openChest, killFinalBrother, searchChest, leaveBarrows),
			Arrays.asList(combatGear, spade), Arrays.asList(barrowsTeleport, strangeOldLockpick, ghommalHilt2)));

		return allSteps;
	}
}
