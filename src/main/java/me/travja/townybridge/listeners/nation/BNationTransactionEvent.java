package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationTransactionEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.UUID;

public class BNationTransactionEvent implements Listener {

    private static ArrayList<Transaction> trans = new ArrayList<>();

    public static int cached(Transaction tran) {
        for (int i = 0; i < trans.size(); i++) {
            Transaction tr = trans.get(i);
            if (isSameTransaction(tran, tr))
                return i;
        }

        return -1;
    }

    public static boolean isSameTransaction(Transaction trans1, Transaction trans2) {
        if (trans1.getType() != trans2.getType())
            return false;

        if (trans1.getAmount() != trans2.getAmount())
            return false;

        if (!trans1.getPlayer().equals(trans2.getPlayer()))
            return false;

        return true;
    }

    public static void received(UUID id, int amount, String name, String typeStr) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        TransactionType type = TransactionType.valueOf(typeStr);

        try {
            Nation nation = towny.getNation(id);
            Resident res = towny.getResident(name);
            try {

                if (type == TransactionType.WITHDRAW)
                    nation.withdrawFromBank(res, amount);
                else if (type == TransactionType.DEPOSIT)
                    res.getAccount().payTo(amount, nation, "Nation Deposit");

            } catch (TownyException e) {
                Main.log.info("Couldn't transfer funds.");
                e.printStackTrace();
            }
        } catch (NotRegisteredException e) {
            Main.log.info("Attempting to perform a transaction for a nation, but it wasn't registered.");
        }

    }

    @EventHandler
    public void transact(NationTransactionEvent event) {
        Nation nation = event.getNation();
        Transaction transaction = event.getTransaction();

        int index = cached(transaction);
        if (index != -1) {
            trans.remove(index);
            return;
        }

        double amount = transaction.getAmount();
        Player player = transaction.getPlayer();
        TransactionType type = transaction.getType();

        trans.add(transaction);
        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), String.valueOf(amount), player.getName(), type.getName());
    }

}
