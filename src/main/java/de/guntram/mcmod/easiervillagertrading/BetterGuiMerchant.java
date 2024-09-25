/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiervillagertrading;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.PossibleEffect;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * @author gbl
 */
public class BetterGuiMerchant extends MerchantScreen implements AutoTrade {

    public BetterGuiMerchant(MerchantMenu handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    public void postButtonClick() {
        super.postButtonClick();
        if (Screen.hasControlDown() != ConfigData.ctrlSwapped) {
            return;
        }
        this.slotClicked(null, 0, 0, ClickType.QUICK_MOVE);
        this.slotClicked(null, 1, 0, ClickType.QUICK_MOVE);

        ((AutoTrade) this).trade(this.shopItem);
    }

    @Override
    public void trade(int tradeIndex) {
        MerchantOffers trades = menu.getOffers();
        MerchantOffer recipe = trades.get(tradeIndex);
        int safeguard = 0;
        while (!recipe.isOutOfStock()
                // TODO how do we check this now? &&  client.player.getInventory().getCursorStack().isEmpty()
                && inputSlotsAreEmpty()
                && hasEnoughItemsInInventory(recipe)
                && canReceiveOutput(recipe.getResult())) {
            transact(recipe);
            if (hasShiftDown() == ConfigData.shiftSwapped || ++safeguard > 50) {
                break;
            }
        }
    }

    private boolean inputSlotsAreEmpty() {
        boolean result =
                menu.getSlot(0).getItem().isEmpty()
                        && menu.getSlot(1).getItem().isEmpty()
                        && menu.getSlot(2).getItem().isEmpty();
        return result;

    }

    private boolean hasEnoughItemsInInventory(MerchantOffer recipe) {
        if (!hasEnoughItemsInInventory(recipe.getCostA()))
            return false;
        if (!hasEnoughItemsInInventory(recipe.getCostB()))
            return false;
        return true;
    }

    private boolean hasEnoughItemsInInventory(ItemStack stack) {
        int remaining = stack.getCount();
        for (int i = menu.slots.size() - 36; i < menu.slots.size(); i++) {
            ItemStack invstack = menu.getSlot(i).getItem();
            if (invstack == null)
                continue;
            if (areItemStacksMergable(stack, invstack)) {
                //System.out.println("taking "+invstack.getCount()+" items from slot # "+i);
                remaining -= invstack.getCount();
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean canReceiveOutput(ItemStack stack) {
        int remaining = stack.getCount();
        for (int i = menu.slots.size() - 36; i < menu.slots.size(); i++) {
            ItemStack invstack = menu.getSlot(i).getItem();
            if (invstack == null || invstack.isEmpty()) {
                //System.out.println("can put result into empty slot "+i);
                return true;
            }
            if (areItemStacksMergable(stack, invstack)
                    && stack.getMaxStackSize() >= stack.getCount() + invstack.getCount()) {
                //System.out.println("Can merge "+(invstack.getMaxStackSize()-invstack.getCount())+" items with slot "+i);
                remaining -= (invstack.getMaxStackSize() - invstack.getCount());
            }
            if (remaining <= 0)
                return true;
        }
        return false;
    }

    private void transact(MerchantOffer recipe) {
        //System.out.println("fill input slots called");
        int putback0, putback1 = -1;
        putback0 = fillSlot(0, recipe.getCostA());
        putback1 = fillSlot(1, recipe.getCostB());

        getslot(2, recipe.getResult(), putback0, putback1);
        //System.out.println("putting back to slot "+putback0+" from 0, and to "+putback1+"from 1");
        if (putback0 != -1) {
            slotClick(0);
            slotClick(putback0);
        }
        if (putback1 != -1) {
            slotClick(1);
            slotClick(putback1);
        }
        // This is a serious hack. 
        // ScreenHandler checks:
        //    if (actionType == SlotActionType.SWAP && clickData >= 0 && clickData < 9) 
        // so this is a NOP on (a normal) server, but our mixin can watch for it and force an inventory resend.
        this.slotClicked(null, /* slot*/ 0, /* clickData*/ 99, ClickType.SWAP);
    }

    /**
     * @param slot  - the number of the (trading) slot that should receive items
     * @param stack - what the trading slot should receive
     * @return the number of the inventory slot into which these items should be put back
     * after the transaction. May be -1 if nothing needs to be put back.
     */
    private int fillSlot(int slot, ItemStack stack) {
        int remaining = stack.getCount();
        for (int i = menu.slots.size() - 36; i < menu.slots.size(); i++) {
            ItemStack invstack = menu.getSlot(i).getItem();
            if (invstack == null)
                continue;
            boolean needPutBack = false;
            if (areItemStacksMergable(stack, invstack)) {
                if (stack.getCount() + invstack.getCount() > stack.getMaxStackSize())
                    needPutBack = true;
                remaining -= invstack.getCount();
                // System.out.println("taking "+invstack.getCount()+" items from slot # "+i+", remaining is now "+remaining);
                slotClick(i);
                slotClick(slot);
            }
            if (needPutBack) {
                slotClick(i);
            }
            if (remaining <= 0)
                return remaining < 0 ? i : -1;
        }
        // We should not be able to arrive here, since hasEnoughItemsInInventory should have been
        // called before fillSlot. But if we do, something went wrong; in this case better do a bit less.
        return -1;
    }

    private boolean areItemStacksMergable(ItemStack a, ItemStack b) {
        if (a == null || b == null)
            return false;
        if (a.getItem() == b.getItem()
                && (!a.isDamageableItem() || a.getDamageValue() == b.getDamageValue())
                && isSameItemAndComponents(a, b))
            return true;
        return false;
    }
    
    //VanillaではFoodPropetiesの効果部分の同一判定でeffectSupplierのID比較しか行っていないので、マルチで別バージョンのクライアントを使用すると一致するはずが不一致になる。(例：フグ)
    //FoodProertiesの中身を見て確認
    public boolean isSameItemAndComponents(ItemStack item1, ItemStack item2) {
    	if (ItemStack.isSameItemSameComponents(item1, item2)) {
    		return true;
    	}
		//アイテムの同一性は確認済みなので、FoodPropertiesは片方だけ見ればよい
    	if (!ItemStack.isSameItem(item1, item2) || item1.getFoodProperties(null) == null
    			|| !item1.getDisplayName().getString().equals(item2.getDisplayName().getString())
    			|| !item1.getDescriptionId().equals(item2.getDescriptionId())) {
    		return false;
    	}
    	FoodProperties foodProp1 = item1.getFoodProperties(null);
    	FoodProperties foodProp2 = item2.getFoodProperties(null);
        if (foodProp1.nutrition() != foodProp2.nutrition() || foodProp1.saturation() != foodProp2.saturation()
        		|| foodProp1.canAlwaysEat() != foodProp2.canAlwaysEat() || foodProp1.eatSeconds() != foodProp2.eatSeconds()) {
        	return false;
        }
    	List<PossibleEffect> effects1 = foodProp1.effects();
    	List<PossibleEffect> effects2 = foodProp2.effects();
    	if (effects1.size() != effects2.size()) {
    		return false;
    	}
    	for  (PossibleEffect effect : effects1) {
    		if (effects2.contains(effect)) {
    			return false;
    		}
    	}
    	return true;
    }

    private void getslot(int slot, ItemStack stack, int... forbidden) {
        int remaining = stack.getCount();
        slotClick(slot);
        for (int i = menu.slots.size() - 36; i < menu.slots.size(); i++) {
            ItemStack invstack = menu.getSlot(i).getItem();
            if (invstack == null || invstack.isEmpty()) {
                continue;
            }
            if (areItemStacksMergable(stack, invstack)
                    && invstack.getCount() < invstack.getMaxStackSize()
            ) {
                // System.out.println("Can merge "+(invstack.getMaxStackSize()-invstack.getCount())+" items with slot "+i);
                remaining -= (invstack.getMaxStackSize() - invstack.getCount());
                slotClick(i);
            }
            if (remaining <= 0)
                return;
        }

        // When looking for an empty slot, don't take one that we want to put some input back to.
        for (int i = menu.slots.size() - 36; i < menu.slots.size(); i++) {
            boolean isForbidden = false;
            for (int f : forbidden) {
                if (i == f)
                    isForbidden = true;
            }
            if (isForbidden)
                continue;
            ItemStack invstack = menu.getSlot(i).getItem();
            if (invstack == null || invstack.isEmpty()) {
                slotClick(i);
                // System.out.println("putting result into empty slot "+i);
                return;
            }
        }
    }

    private void slotClick(int slot) {
        // System.out.println("Clicking slot "+slot);
        this.slotClicked(null, slot, 0, ClickType.PICKUP);
    }


}
