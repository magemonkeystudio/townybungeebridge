package me.travja.townybridge.listeners.towny;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.event.TownyTransactionEvent;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class BTownyTransactionEvent implements Listener {

/*    private static ArrayList<Transaction> trans = new ArrayList<>();

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

        if(player == null || amount == 0)
            return;

        TransactionType type = transaction.getType();

        Main.log.info("Transaction Event:{Amount: " + amount + ", Player: " + player + ", Type: " + type);

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

    private static <T, T2> boolean isOneNull(T o1, T2 o2) {
        return (o1 == null && o2 != null) || (o1 != null && o2 == null);
    }

    public static boolean isSameTransaction(Transaction trans1, Transaction trans2) {
        if (isOneNull(trans1, trans2)
                || trans1.getType() != trans2.getType()
                || trans1.getAmount() != trans2.getAmount()
                || isOneNull(trans1.getPlayer(), trans2.getPlayer() == null))
            return false;

        if ((trans1 != null && trans2 != null) && !trans1.getPlayer().equals(trans2.getPlayer()))
            return false;

        return true;
    }


    public static void received(int amount, String name, String typeStr) {
        TransactionType type = TransactionType.valueOf(typeStr.toUpperCase());

        trans.add(new Transaction(type, Bukkit.getPlayer(name), amount));

        if (type == TransactionType.SUBTRACT)
            TownyEconomyHandler.subtract(name, (double) amount, Bukkit.getWorlds().get(0));
        else if (type == TransactionType.ADD)
            TownyEconomyHandler.add(name, (double) amount, Bukkit.getWorlds().get(0));

    }*/
}
