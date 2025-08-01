/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
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
package net.runelite.client.plugins.microbot.questhelper.bank.banktab;

import net.runelite.client.plugins.microbot.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.KeyringRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class QuestHelperBankTagService
{
	@Inject
	private QuestHelperPlugin plugin;

	@Inject
	private Client client;

	ArrayList<Integer> taggedItems;

	ArrayList<Integer> taggedItemsForBank;

	int lastTickUpdated = 0;

	private final String RECOMMENDED_TAB_NAME = "Recommended items";

	public ArrayList<Integer> itemsToTag()
	{
		if (client.getTickCount() <= lastTickUpdated)
		{
			return taggedItems;
		}

		lastTickUpdated = client.getTickCount();

		return getItemsFromTabs();
	}

	private ArrayList<Integer> getItemsFromTabs()
	{
		ArrayList<BankTabItems> sortedItems = getPluginBankTagItemsForSections(true);

		if (sortedItems == null)
		{
			return null;
		}

		taggedItemsForBank = new ArrayList<>();

		sortedItems.stream()
				.map(BankTabItems::getItems)
				.flatMap(Collection::stream)
				.map(BankTabItem::getItemIDs)
				.flatMap(Collection::stream)
				.filter(Objects::nonNull) // filter non-null just in case any Integer get in the list
				.filter(id -> !taggedItemsForBank.contains(id))
				.forEach(taggedItemsForBank::add);
		return taggedItemsForBank;
	}

	public ArrayList<BankTabItems> getPluginBankTagItemsForSections(boolean onlyGetMissingItems)
	{
		ArrayList<BankTabItems> newList = new ArrayList<>();

		List<PanelDetails> questSections = plugin.getSelectedQuest().getPanels();

		if (questSections == null || questSections.isEmpty())
		{
			return newList;
		}

		List<ItemRequirement> recommendedItems = plugin.getSelectedQuest().getItemRecommended();
		if (recommendedItems != null)
		{
			recommendedItems = recommendedItems.stream()
				.filter(Objects::nonNull)
				.filter(i -> (!onlyGetMissingItems || !i.checkWithAllContainers()) && i.shouldDisplayText(plugin.getClient()))
				.collect(Collectors.toList());
		}

		if (recommendedItems != null && !recommendedItems.isEmpty())
		{
			BankTabItems pluginItems = new BankTabItems(RECOMMENDED_TAB_NAME);
			// Here we specify getItems to avoid a double 'Recommended' title
			recommendedItems.forEach(item -> getItemsFromRequirement(pluginItems.getItems(), item, item));
			newList.add(pluginItems);
		}

		List<PanelDetails> shouldShowSections = questSections.stream()
			.filter(panelDetail -> panelDetail.getHideCondition() == null ||
				!panelDetail.getHideCondition().check(plugin.getClient()))
			.collect(Collectors.toList());

		for (PanelDetails questSection : shouldShowSections)
		{
			List<ItemRequirement> items = new ArrayList<>();
			if (questSection.getRequirements() != null)
			{
				items = questSection.getRequirements()
					.stream()
					.filter(ItemRequirement.class::isInstance)
					.map(ItemRequirement.class::cast)
					.filter(i -> (!onlyGetMissingItems
						|| !i.checkWithAllContainers())
						&& i.shouldDisplayText(plugin.getClient()))
					.collect(Collectors.toList());
			}
			List<ItemRequirement> recommendedItemsForSection = new ArrayList<>();
			if (questSection.getRecommended() != null)
			{
				recommendedItemsForSection = questSection.getRecommended()
					.stream()
					.filter(ItemRequirement.class::isInstance)
					.map(ItemRequirement.class::cast)
					.filter(i -> (!onlyGetMissingItems
						|| !i.checkWithAllContainers())
						&& i.shouldDisplayText(plugin.getClient()))
					.collect(Collectors.toList());
			}

			BankTabItems pluginItems = new BankTabItems(questSection.getHeader());
			items.forEach(item -> getItemsFromRequirement(pluginItems.getItems(), item, item));
			recommendedItemsForSection.forEach(item -> getItemsFromRequirement(pluginItems.getRecommendedItems(), item, item));
			// We don't add the recommended items as they're already used
			if (items.size() > 0)
			{
				newList.add(pluginItems);
			}
		}

		// If none of the sections have anything in it, create a generic require items section
		if (newList.size() == 0 || (newList.size() == 1 && newList.get(0).getName().equals(RECOMMENDED_TAB_NAME)))
		{
			BankTabItems allRequiredItems = new BankTabItems("Required items");
			List<ItemRequirement> allRequired = plugin.getSelectedQuest().getItemRequirements();
			List<ItemRequirement> items;
			if (allRequired != null && allRequired.size() > 0)
			{
				items = allRequired.stream()
					.filter(Objects::nonNull)
					.map(ItemRequirement.class::cast)
					.filter(i -> (!onlyGetMissingItems
				   || !i.check(plugin.getClient()))
				   && i.shouldDisplayText(plugin.getClient()))
					.collect(Collectors.toList());

				items.forEach(item -> getItemsFromRequirement(allRequiredItems.getItems(), item, item));
			}
			newList.add(allRequiredItems);
		}

		return newList;
	}

	private void getItemsFromRequirement(List<BankTabItem> pluginItems, ItemRequirement itemRequirement, ItemRequirement realItem)
	{
		if (itemRequirement instanceof ItemRequirements)
		{
			ItemRequirements itemRequirements = (ItemRequirements) itemRequirement;
			LogicType logicType = itemRequirements.getLogicType();
			ArrayList<ItemRequirement> requirements = itemRequirements.getItemRequirements();
			if (logicType == LogicType.AND)
			{
				requirements.forEach(req -> getItemsFromRequirement(pluginItems, req, req));
			}
			if (logicType == LogicType.OR)
			{
				List<ItemRequirement> itemsWhichPassReq = requirements.stream()
					.filter(r -> r.shouldDisplayText(plugin.getClient()))
					.collect(Collectors.toList());

				if (itemsWhichPassReq.isEmpty())
				{
					getItemsFromRequirement(pluginItems, requirements.get(0).named(itemRequirements.getName()), requirements.get(0));
				}
				else
				{
					ItemRequirement match = itemsWhichPassReq.stream()
						.filter(ItemRequirement::checkWithAllContainers)
						.findFirst()
						.orElse(itemsWhichPassReq.get(0).named(itemRequirements.getName()));

					getItemsFromRequirement(pluginItems, match, match);
				}
			}
		}
		else if (itemRequirement instanceof KeyringRequirement)
		{
			KeyringRequirement keyringRequirement = (KeyringRequirement) itemRequirement;
			KeyringRequirement fakeRequirement = new KeyringRequirement(keyringRequirement.getName(), plugin.getConfigManager(),
					keyringRequirement.getKeyringCollection());
			if (keyringRequirement.hasKeyOnKeyRing())
			{
				fakeRequirement.addAlternates(ItemID.FAVOUR_KEY_RING);
			}
			pluginItems.add(makeBankTabItem(fakeRequirement));

		}
		else
		{
			if (itemRequirement.getDisplayItemId() != null || !itemRequirement.getDisplayItemIds().contains(-1))
			{
				pluginItems.add(makeBankTabItem(realItem));
			}
		}
	}

	private BankTabItem makeBankTabItem(ItemRequirement item)
	{
		List<Integer> itemIds = item.getDisplayItemIds();
		Integer displayId = itemIds.stream()
				.filter(this::hasItemInBankOrPotionStorage)
				.findFirst()
				.orElse(item.getAllIds().stream()
						.filter(this::hasItemInBankOrPotionStorage)
						.findFirst()
						.orElse(item.getAllIds().get(0))
				);
		if (displayId == -1 && item.getDisplayItemId() != -1)
		{
			displayId = item.getDisplayItemId();
		}

		return new BankTabItem(item, displayId);
	}

	public boolean hasItemInBankOrPotionStorage(int itemID)
	{
		ItemRequirement tmpReq = new ItemRequirement("tmp", itemID);
		return tmpReq.checkWithAllContainers();
	}
}
