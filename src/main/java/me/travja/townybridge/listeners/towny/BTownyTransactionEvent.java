package me.travja.townybridge.listeners.towny;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.event.TownyTransactionEvent;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class BTownyTransactionEvent implements Listener {

    private static ArrayList<Transaction> trans = new ArrayList<>();

    @EventHandler
    public void transact(TownyTransactionEvent event) {
        Transaction transaction = event.getTransaction();

        int index = cached(transaction);
        if (index != -1) {
            trans.remove(index);
            return;
        }

        int amount = transaction.getAmount();
        Player player = transaction.getPlayer();
        TransactionType type = transaction.getType();

        trans.add(transaction);
        BungeeUtil.sendMessage(event.getEventName(), String.valueOf(amount), player.getName(), type.getName());
    }

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


    public static void received(int amount, String name, String typeStr) {
        TransactionType type = TransactionType.valueOf(typeStr);

        trans.add(new Transaction(type, Bukkit.getPlayer(name), amount));

        if (type == TransactionType.SUBTRACT)
            TownyEconomyHandler.subtract(name, (double) amount, Bukkit.getWorlds().get(0));
        else if (type == TransactionType.ADD)
            TownyEconomyHandler.add(name, (double) amount, Bukkit.getWorlds().get(0));

    }
}
